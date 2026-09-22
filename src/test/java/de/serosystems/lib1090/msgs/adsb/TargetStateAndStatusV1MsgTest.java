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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TargetStateAndStatusV1MsgTest {

    // A Target State and Status message, Subtype 0, composed by single bits
    public static final String TSS_V1 = withParity(Tools.hexStringToByteArray(
            // 10001 => DF 17
            //   101 => FF (first field, not needed here)
            "8d" +

                    // ICAO 24 bit address
                    "89653e" +

                    //      11101 => type code 29
                    //         00 => subtype 0
                    //         01 => vertical data available and source indicator
                    //          1 => target altitude type
                    //          0 => reserved
                    //         00 => target altitude capability
                    //         10 => vertical mode indicator
                    // 0010010000 => target altitude
                    //         01 => horizontal data available and source indicator
                    //  010000000 => target heading / track angle
                    "e8c448280" +

                    //        0 => target heading / track indicator
                    //       10 => horizontal mode indicator
                    //     1001 => NACp
                    //        1 => NICbaro
                    "53" +

                    //    11 => SIL
                    // 00000 => reserved
                    //     0 => capability: not TCAS
                    "c0" +

                    //   1 => TCAS resolution advisory active
                    // 100 => emergency / priority status
                    "c" +

                    // parity, overwritten below
                    "000000"));

    public static final String A_OPSTAT_V1 = withAddress("8D000000F8000200492900000000", "89653e");

    @Test
    public void testDecodeTssV1() throws UnspecifiedFormatError, BadFormatException {
        final TargetStateAndStatusV1Msg tss = new TargetStateAndStatusV1Msg(Tools.hexStringToByteArray(TSS_V1));

        assertEquals("89653e", tss.getAddress().getHexAddress());
        assertEquals(17, tss.getDownlinkFormat());
        assertEquals(29, tss.getFormatTypeCode());

        assertEquals(1, tss.getVerticalDataAvailableAndSourceIndicator());
        assertTrue(tss.hasTargetAltitudeCapability());
        assertTrue(tss.hasSelectedAltitude());
        assertEquals(144, tss.getSelectedAltitudeEncoded());
        assertEquals(13400, tss.getSelectedAltitude());
        assertTrue(tss.hasSelectedHeading());
        assertEquals(128, tss.getSelectedHeadingEncoded());
        assertEquals(90.f, tss.getSelectedHeading());
        assertEquals(9, tss.getNACpEncoded());
        assertTrue(tss.getBarometricAltitudeIntegrityCode());
        assertEquals(3, tss.getSILEncoded());
        assertTrue(tss.hasOperationalTCAS());
        assertTrue(tss.hasActiveTCASResolutionAdvisory());
        assertEquals(4, tss.getEmergencyPriorityStatus());
    }

    @Test
    public void testTargetAltitudeCapability() throws UnspecifiedFormatError, BadFormatException {
        for (int capability : new int[]{0, 3}) {
            final TargetStateAndStatusV1Msg tss = decode(withTargetAltitudeCapability(TSS_V1, capability));

            assertTrue(tss.hasTargetAltitudeCapability(), "capability " + capability);
            assertTrue(tss.hasSelectedAltitude(), "capability " + capability);
            assertEquals(13400, tss.getSelectedAltitude(), "capability " + capability);
        }

        for (int capability : new int[]{1, 2}) {
            final TargetStateAndStatusV1Msg tss = decode(withTargetAltitudeCapability(TSS_V1, capability));

            assertFalse(tss.hasTargetAltitudeCapability(), "capability " + capability);
            assertFalse(tss.hasSelectedAltitude(), "capability " + capability);
            assertNull(tss.getSelectedAltitude(), "capability " + capability);
            assertEquals(144, tss.getSelectedAltitudeEncoded(), "capability " + capability);
        }
    }

    @Test
    public void testSelectedAltitudeOutOfRange() throws UnspecifiedFormatError, BadFormatException {
        final TargetStateAndStatusV1Msg inRange = decode(withTargetAltitude(TSS_V1, 1010));

        assertTrue(inRange.hasSelectedAltitude());
        assertEquals(100000, inRange.getSelectedAltitude());

        for (int altitude : new int[]{1011, 1023}) {
            final TargetStateAndStatusV1Msg tss = decode(withTargetAltitude(TSS_V1, altitude));

            assertTrue(tss.hasTargetAltitudeCapability(), "altitude " + altitude);
            assertFalse(tss.hasSelectedAltitude(), "altitude " + altitude);
            assertNull(tss.getSelectedAltitude(), "altitude " + altitude);
            assertEquals(altitude, tss.getSelectedAltitudeEncoded(), "altitude " + altitude);
        }
    }

    private static TargetStateAndStatusV1Msg decode(String message)
            throws UnspecifiedFormatError, BadFormatException {
        return new TargetStateAndStatusV1Msg(Tools.hexStringToByteArray(message));
    }

    private static String withTargetAltitudeCapability(String message, int capability) {
        byte[] raw = Tools.hexStringToByteArray(message);
        // ME bits 12-13
        raw[5] = (byte) ((raw[5] & ~0x18) | ((capability & 0x3) << 3));
        return withParity(raw);
    }

    private static String withTargetAltitude(String message, int altitude) {
        byte[] raw = Tools.hexStringToByteArray(message);
        // ME bits 16-25
        raw[5] = (byte) ((raw[5] & ~0x01) | ((altitude >> 9) & 0x1));
        raw[6] = (byte) ((altitude >> 1) & 0xFF);
        raw[7] = (byte) ((raw[7] & ~0x80) | ((altitude & 0x1) << 7));
        return withParity(raw);
    }

    private static String withAddress(String message, String address) {
        byte[] raw = Tools.hexStringToByteArray(message);
        byte[] icao = Tools.hexStringToByteArray(address);
        System.arraycopy(icao, 0, raw, 1, icao.length);
        return withParity(raw);
    }

    private static String withParity(byte[] raw) {
        byte[] parity = ModeSDownlinkMsg.calcParity(Arrays.copyOf(raw, raw.length - 3));
        System.arraycopy(parity, 0, raw, raw.length - 3, parity.length);
        return Tools.toHexString(raw);
    }
}
