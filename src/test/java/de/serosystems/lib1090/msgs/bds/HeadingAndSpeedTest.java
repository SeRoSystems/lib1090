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

package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeadingAndSpeedTest {

    private static final byte[] MSG = {
            (byte) 0b11111111, (byte) 0b10111010, (byte) 0b10100001, (byte) 0b00011110, (byte) 0b00100000,
            (byte) 0b00000100, (byte) 0b01110010
    };

    /** A message with {@code value} written into ME bits {@code from} to {@code to}, all other bits clear. */
    private static HeadingAndSpeed with(int from, int to, int value) {
        byte[] message = new byte[7];
        for (int bit = from; bit <= to; bit++) {
            if (((value >>> (to - bit)) & 1) != 0)
                message[(bit - 1) / 8] |= (byte) (0x80 >>> ((bit - 1) % 8));
        }
        return new HeadingAndSpeed(message);
    }

    /** A signed field: status bit, sign bit and a value of {@code valueBits} bits, starting at ME bit {@code status}. */
    private static HeadingAndSpeed withSigned(int status, int valueBits, boolean sign, int value) {
        return with(status, status + 1 + valueBits, 1 << (valueBits + 1) | (sign ? 1 << valueBits : 0) | value);
    }

    @Test
    public void decodesEveryField() {
        HeadingAndSpeed hs = new HeadingAndSpeed(MSG);

        assertTrue(hs.hasMagneticHeading());
        assertTrue(hs.getMagneticHeadingSign());
        assertEquals(1019, hs.getMagneticHeadingEncoded());
        assertEquals((1019 - 1024) * 90f / 512, hs.getMagneticHeading());

        assertTrue(hs.hasIndicatedAirspeed());
        assertEquals(336, hs.getIndicatedAirspeedEncoded());
        assertEquals((short) 336, hs.getIndicatedAirspeed());

        assertTrue(hs.hasMachNumber());
        assertEquals(120, hs.getMachNumberEncoded());
        assertEquals(0.48f, hs.getMachNumber(), 1e-6f);

        assertTrue(hs.hasBarometricAltitudeRate());
        assertFalse(hs.getBarometricAltitudeRateSign());
        assertEquals(0, hs.getBarometricAltitudeRateEncoded());
        assertEquals(0, hs.getBarometricAltitudeRate());

        assertTrue(hs.hasInertialVerticalRate());
        assertFalse(hs.getInertialVerticalRateSign());
        assertEquals(114, hs.getInertialVerticalRateEncoded());
        assertEquals(3648, hs.getInertialVerticalRate());
    }

    /** An 11-bit two's complement number; a set sign means west of north. */
    @Test
    public void magneticHeadingSign() {
        // 650 * 90/512, which integer arithmetic used to truncate to 114
        assertEquals(114.2578125f, withSigned(1, 10, false, 650).getMagneticHeading());

        HeadingAndSpeed west = withSigned(1, 10, true, 768);
        assertTrue(west.getMagneticHeadingSign());
        assertEquals(768, west.getMagneticHeadingEncoded());
        assertEquals(-45f, west.getMagneticHeading());

        assertEquals(-180f, withSigned(1, 10, true, 0).getMagneticHeading());
        assertEquals(-90f / 512, withSigned(1, 10, true, 1023).getMagneticHeading());
        assertEquals(0f, withSigned(1, 10, false, 0).getMagneticHeading());
        assertEquals(1023 * 90f / 512, withSigned(1, 10, false, 1023).getMagneticHeading());
    }

    /** A 10-bit two's complement number; a set sign means below, i.e. descending. */
    @Test
    public void barometricAltitudeRateSign() {
        HeadingAndSpeed descending = withSigned(35, 9, true, 480);
        assertTrue(descending.getBarometricAltitudeRateSign());
        assertEquals(480, descending.getBarometricAltitudeRateEncoded());
        assertEquals(-1024, descending.getBarometricAltitudeRate());

        assertEquals(-16384, withSigned(35, 9, true, 0).getBarometricAltitudeRate());
        assertEquals(-32, withSigned(35, 9, true, 511).getBarometricAltitudeRate());
        assertEquals(16352, withSigned(35, 9, false, 511).getBarometricAltitudeRate());
    }

    /** A 10-bit two's complement number; a set sign means below, i.e. descending. */
    @Test
    public void inertialVerticalRateSign() {
        HeadingAndSpeed descending = withSigned(46, 9, true, 480);
        assertTrue(descending.getInertialVerticalRateSign());
        assertEquals(480, descending.getInertialVerticalRateEncoded());
        assertEquals(-1024, descending.getInertialVerticalRate());

        assertEquals(-16384, withSigned(46, 9, true, 0).getInertialVerticalRate());
        assertEquals(-32, withSigned(46, 9, true, 511).getInertialVerticalRate());
        assertEquals(16352, withSigned(46, 9, false, 511).getInertialVerticalRate());
    }

    @Test
    public void speeds() {
        assertEquals((short) 1023, with(13, 23, 0x7FF).getIndicatedAirspeed());
        assertEquals(4.092f, with(24, 34, 0x7FF).getMachNumber(), 1e-6f);
    }

    /** A clear status bit makes the interpreting accessor answer null; the encoded value is still readable. */
    @Test
    public void unavailable() {
        HeadingAndSpeed heading = with(2, 12, 650);
        assertFalse(heading.hasMagneticHeading());
        assertEquals(650, heading.getMagneticHeadingEncoded());
        assertNull(heading.getMagneticHeading());

        HeadingAndSpeed indicatedAirspeed = with(14, 23, 336);
        assertFalse(indicatedAirspeed.hasIndicatedAirspeed());
        assertEquals(336, indicatedAirspeed.getIndicatedAirspeedEncoded());
        assertNull(indicatedAirspeed.getIndicatedAirspeed());

        HeadingAndSpeed mach = with(25, 34, 120);
        assertFalse(mach.hasMachNumber());
        assertEquals(120, mach.getMachNumberEncoded());
        assertNull(mach.getMachNumber());

        HeadingAndSpeed baro = with(36, 45, 114);
        assertFalse(baro.hasBarometricAltitudeRate());
        assertEquals(114, baro.getBarometricAltitudeRateEncoded());
        assertNull(baro.getBarometricAltitudeRate());

        HeadingAndSpeed inertial = with(47, 56, 114);
        assertFalse(inertial.hasInertialVerticalRate());
        assertEquals(114, inertial.getInertialVerticalRateEncoded());
        assertNull(inertial.getInertialVerticalRate());
    }
}
