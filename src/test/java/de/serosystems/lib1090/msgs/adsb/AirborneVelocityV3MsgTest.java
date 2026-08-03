/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AirborneVelocityV3MsgTest extends VelocityOverGroundMsgTest {

    @Override
    protected VelocityOverGroundMsg create(String hex) throws Exception {
        return new AirborneVelocityV3Msg(hex);
    }

    @Override
    public void testGeoMinusBaro_485020() throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg("8D485020994409940838175B284F");
        assertFalse(msg.hasDiffBaroAlt());
        assertNull(msg.getDiffBaroAlt());
    }

    @Override
    public void testGeoMinusBaroNegative_45AC2D() throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg("8d45ac2d9904d910613f94ba81b5");
        assertFalse(msg.hasDiffBaroAlt());
        assertNull(msg.getDiffBaroAlt());
    }

    @Test
    void testNICSupplementDAvailableWhenDiffBaroAltEncodedIsZero() throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(messageWithNICSupplementD(0));

        assertTrue(msg.hasNICSupplementD());
        assertFalse(msg.hasDiffBaroAlt());
        assertNull(msg.getDiffBaroAlt());
        assertNull(msg.getDiffBaroAltMidpoint());
    }

    @Test
    void testNICSupplementDValues() throws Exception {
        for (byte nicD = 0; nicD <= 3; nicD++) {
            AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(messageWithNICSupplementD(nicD));
            assertTrue(msg.hasNICSupplementD());
            assertEquals(nicD, msg.getNICSupplementD());
        }
    }

    @Test
    void testNICSupplementDUnavailableWhenDiffBaroAltEncodedIsNonZero() throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(messageWithExtended(0b00000000010, false));
        assertFalse(msg.hasNICSupplementD());
    }

    @Test
    void testExtendedDiffBaroAltUnavailable() throws Exception {
        // 0xx00000000, 00000000001, 00100000000, 01000000000, 01100000000: diffBaroAltEncoded == 0
        // -- NIC supplement D is present instead
        assertUnavailable(messageWithNICSupplementD(0));
        assertUnavailable(messageWithExtended(0b00000000001, false));
        assertUnavailable(messageWithExtended(0b00100000000, false));
        assertUnavailable(messageWithExtended(0b01000000000, false));
        assertUnavailable(messageWithExtended(0b01100000000, false));

        // 00111111101, 01011111101, 01111111101: compat zone (diffBaroAltEncoded == 0x7E) with one
        // of the top-3 (reserved) bits set
        assertUnavailable(messageWithExtended(0b00111111101, false));
        assertUnavailable(messageWithExtended(0b01011111101, false));
        assertUnavailable(messageWithExtended(0b01111111101, false));
    }

    @Test
    void testExtendedDiffBaroAltCompatZoneLowEnd() throws Exception {
        assertAvailable(0b00000000010, 0., 0.); // smallest available value: clamped to 0/0
        assertAvailable(0b00000000011, 12.5, 6.25);
        assertAvailable(0b00000000100, 25., 18.75);
        assertAvailable(0b00000000101, 37.5, 31.25);
        assertAvailable(0b00000000110, 50., 43.75);
    }

    @Test
    void testExtendedDiffBaroAltCompatZoneHighEnd() throws Exception {
        assertAvailable(0b00011111100, 3125., 3118.75);
        assertAvailable(0b00011111101, 3137.5, 3131.25);
    }

    @Test
    void testExtendedDiffBaroAltCompatToExtendedTransition() throws Exception {
        // diffBaroAltEncoded == 0x7F from here on -- extended zone
        assertAvailable(0b00011111110, 3150., 3143.75);
        assertAvailable(0b00011111111, 3250., 3200.);
    }

    @Test
    void testExtendedDiffBaroAltExtendedZoneWithReservedTopBitsSet() throws Exception {
        // top-3 bits nonzero, but legitimate here since diffBaroAltEncoded == 0x7F
        assertAvailable(0b00111111110, 3350., 3300.);
        assertAvailable(0b00111111111, 3450., 3400.);
        assertAvailable(0b01011111110, 3550., 3500.);
        assertAvailable(0b01011111111, 3650., 3600.);
        assertAvailable(0b01111111110, 3750., 3700.);
        assertAvailable(0b01111111111, 3850., 3800.);
        assertAvailable(0b10011111110, 3950., 3900.);
        assertAvailable(0b10011111111, 4050., 4000.);
        assertAvailable(0b10111111110, 4150., 4100.);
        assertAvailable(0b10111111111, 4250., 4200.);
        assertAvailable(0b11011111110, 4350., 4300.);
        assertAvailable(0b11011111111, 4450., 4400.);
    }

    @Test
    void testExtendedDiffBaroAltSaturated() throws Exception {
        AirborneVelocityV3Msg notSaturated = new AirborneVelocityV3Msg(messageWithExtended(0b11111111110, false));
        assertTrue(notSaturated.hasDiffBaroAlt());
        assertFalse(notSaturated.isDiffBaroAltSaturated());
        assertEquals(4550., notSaturated.getDiffBaroAlt());
        assertEquals(4500., notSaturated.getDiffBaroAltMidpoint());

        AirborneVelocityV3Msg saturated = new AirborneVelocityV3Msg(messageWithExtended(0b11111111111, false));
        assertTrue(saturated.hasDiffBaroAlt());
        assertTrue(saturated.isDiffBaroAltSaturated());
        assertEquals(4550., saturated.getDiffBaroAltMidpoint());
    }

    @Test
    void testExtendedDiffBaroAltNegativeSign() throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(messageWithExtended(0b00000000011, true));
        assertTrue(msg.hasDiffBaroAlt());
        assertEquals(-12.5, msg.getDiffBaroAlt());
        assertEquals(-6.25, msg.getDiffBaroAltMidpoint());
    }

    private void assertUnavailable(String hex) throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(hex);
        assertFalse(msg.hasDiffBaroAlt());
        assertNull(msg.getDiffBaroAlt());
        assertNull(msg.getDiffBaroAltMidpoint());
    }

    private void assertAvailable(int encodedValue, double expectedUpper, double expectedMidpoint) throws Exception {
        AirborneVelocityV3Msg msg = new AirborneVelocityV3Msg(messageWithExtended(encodedValue, false));
        assertTrue(msg.hasDiffBaroAlt());
        assertEquals(expectedUpper, msg.getDiffBaroAlt());
        assertEquals(expectedMidpoint, msg.getDiffBaroAltMidpoint());
    }

    /**
     * Builds a velocity-over-ground message whose extended Difference from Barometric Altitude
     * encoding (ME bits 9, 10, 47-48 and 50-56) equals the given 11-bit value: bit 0 is ME bit 9,
     * bits 1-7 are the raw diffBaroAltEncoded field, bits 8-9 are ME bits 47-48, and bit 10 is ME
     * bit 10.
     */
    private static String messageWithExtended(int encodedValue, boolean negative) {
        boolean me9 = (encodedValue & 0x1) != 0;
        int diffBaroAltEncoded = (encodedValue >>> 1) & 0x7F;
        int me4748 = (encodedValue >>> 8) & 0x3;
        boolean me10 = (encodedValue & 0x400) != 0;
        return buildMessage(me9, me10, me4748, negative, diffBaroAltEncoded);
    }

    /**
     * Builds a velocity-over-ground message with diffBaroAltEncoded == 0 (Difference from
     * Barometric Altitude unavailable), so that ME bits 47-48 are interpreted as NIC supplement D.
     */
    private static String messageWithNICSupplementD(int nicSupplementD) {
        return buildMessage(false, false, nicSupplementD, false, 0);
    }

    private static String buildMessage(boolean me9, boolean me10, int me4748, boolean negative, int diffBaroAltEncoded) {
        byte[] raw = new byte[14];
        raw[0] = (byte) 0x8D; // DF17
        raw[1] = 0x48;
        raw[2] = 0x50;
        raw[3] = 0x20; // arbitrary ICAO address

        // ME byte 0 (ME bits 1-8): typecode 19 (10011), subtype 1 (001)
        raw[4] = (byte) 0x99;

        // ME byte 1 (ME bits 9-16): ME bit 9, ME bit 10, rest (NACv, velocity-to-east sign/high bits) 0
        raw[5] = (byte) ((me9 ? 0x80 : 0) | (me10 ? 0x40 : 0));

        // ME bytes 2-4 (ME bits 17-40): remaining velocity-to-east/north and vertical source, all 0
        raw[6] = 0;
        raw[7] = 0;
        raw[8] = 0;

        // ME byte 5 (ME bits 41-48): vertical rate (0) followed by ME bits 47-48
        raw[9] = (byte) (me4748 & 0x3);

        // ME byte 6 (ME bits 49-56): sign bit followed by the 7-bit diffBaroAltEncoded field
        raw[10] = (byte) ((negative ? 0x80 : 0) | (diffBaroAltEncoded & 0x7F));

        byte[] parity = ModeSDownlinkMsg.calcParity(Arrays.copyOf(raw, raw.length - 3));
        System.arraycopy(parity, 0, raw, raw.length - 3, parity.length);

        return Tools.toHexString(raw);
    }
}
