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

public class TrackAndTurnTest {

    private static final byte[] MSG = {
            (byte) 0b10000001, (byte) 0b10010101, (byte) 0b00010101, (byte) 0b00110110, (byte) 0b11100000,
            (byte) 0b00100100, (byte) 0b11010100
    };

    /** A message with {@code value} written into ME bits {@code from} to {@code to}, all other bits clear. */
    private static TrackAndTurn with(int from, int to, int value) {
        byte[] message = new byte[7];
        for (int bit = from; bit <= to; bit++) {
            if (((value >>> (to - bit)) & 1) != 0)
                message[(bit - 1) / 8] |= (byte) (0x80 >>> ((bit - 1) % 8));
        }
        return new TrackAndTurn(message);
    }

    /** A signed field: status bit, sign bit and a value of {@code valueBits} bits, starting at ME bit {@code status}. */
    private static TrackAndTurn withSigned(int status, int valueBits, boolean sign, int value) {
        return with(status, status + 1 + valueBits, 1 << (valueBits + 1) | (sign ? 1 << valueBits : 0) | value);
    }

    @Test
    public void decodesEveryField() {
        TrackAndTurn tt = new TrackAndTurn(MSG);

        assertTrue(tt.hasRollAngle());
        assertFalse(tt.getRollAngleSign());
        assertEquals(12, tt.getRollAngleEncoded());
        // 12 * 45/256, which integer arithmetic used to truncate to 2
        assertEquals(2.109375f, tt.getRollAngle());

        assertTrue(tt.hasTrueTrackAngle());
        assertFalse(tt.getTrueTrackAngleSign());
        assertEquals(650, tt.getTrueTrackAngleEncoded());
        // 650 * 90/512, which integer arithmetic used to truncate to 114
        assertEquals(114.2578125f, tt.getTrueTrackAngle());

        assertTrue(tt.hasGroundSpeed());
        assertEquals(219, tt.getGroundSpeedEncoded());
        assertEquals(438, tt.getGroundSpeed());

        assertTrue(tt.hasTrackAngleRate());
        assertFalse(tt.getTrackAngleRateSign());
        assertEquals(4, tt.getTrackAngleRateEncoded());
        // 4 * 8/256, which integer arithmetic used to truncate to 0
        assertEquals(0.125f, tt.getTrackAngleRate());

        assertTrue(tt.hasTrueAirspeed());
        assertEquals(212, tt.getTrueAirspeedEncoded());
        assertEquals(424, tt.getTrueAirspeed());
    }

    /** Sign and value together are a 10-bit two's complement number; a set sign means left wing down. */
    @Test
    public void rollAngleSign() {
        TrackAndTurn leftWingDown = withSigned(1, 9, true, 12);
        assertTrue(leftWingDown.getRollAngleSign());
        assertEquals(12, leftWingDown.getRollAngleEncoded());
        assertEquals((12 - 512) * 45f / 256, leftWingDown.getRollAngle());

        assertEquals(-90f, withSigned(1, 9, true, 0).getRollAngle());
        assertEquals(-45f / 256, withSigned(1, 9, true, 511).getRollAngle());
        assertEquals(0f, withSigned(1, 9, false, 0).getRollAngle());
        assertEquals(511 * 45f / 256, withSigned(1, 9, false, 511).getRollAngle());
    }

    /** An 11-bit two's complement number; a set sign means west of north. */
    @Test
    public void trueTrackAngleSign() {
        TrackAndTurn west = withSigned(12, 10, true, 768);
        assertTrue(west.getTrueTrackAngleSign());
        assertEquals(768, west.getTrueTrackAngleEncoded());
        assertEquals(-45f, west.getTrueTrackAngle());

        assertEquals(-180f, withSigned(12, 10, true, 0).getTrueTrackAngle());
        assertEquals(-90f / 512, withSigned(12, 10, true, 1023).getTrueTrackAngle());
        assertEquals(0f, withSigned(12, 10, false, 0).getTrueTrackAngle());
        assertEquals(1023 * 90f / 512, withSigned(12, 10, false, 1023).getTrueTrackAngle());
    }

    /** A 10-bit two's complement number; a set sign means a decreasing track angle. */
    @Test
    public void trackAngleRateSign() {
        TrackAndTurn left = withSigned(35, 9, true, 480);
        assertTrue(left.getTrackAngleRateSign());
        assertEquals(480, left.getTrackAngleRateEncoded());
        assertEquals(-1f, left.getTrackAngleRate());

        assertEquals(-16f, withSigned(35, 9, true, 0).getTrackAngleRate());
        assertEquals(-1f / 32, withSigned(35, 9, true, 511).getTrackAngleRate());
        assertEquals(0f, withSigned(35, 9, false, 0).getTrackAngleRate());
        assertEquals(511f / 32, withSigned(35, 9, false, 511).getTrackAngleRate());
    }

    @Test
    public void speeds() {
        assertEquals(2046, with(24, 34, 0x7FF).getGroundSpeed());
        assertEquals(2046, with(46, 56, 0x7FF).getTrueAirspeed());
    }

    /** A clear status bit makes the interpreting accessor answer null; the encoded value is still readable. */
    @Test
    public void unavailable() {
        TrackAndTurn roll = with(2, 11, 12);
        assertFalse(roll.hasRollAngle());
        assertEquals(12, roll.getRollAngleEncoded());
        assertNull(roll.getRollAngle());

        TrackAndTurn track = with(13, 23, 650);
        assertFalse(track.hasTrueTrackAngle());
        assertEquals(650, track.getTrueTrackAngleEncoded());
        assertNull(track.getTrueTrackAngle());

        TrackAndTurn groundSpeed = with(25, 34, 219);
        assertFalse(groundSpeed.hasGroundSpeed());
        assertEquals(219, groundSpeed.getGroundSpeedEncoded());
        assertNull(groundSpeed.getGroundSpeed());

        TrackAndTurn rate = with(36, 45, 4);
        assertFalse(rate.hasTrackAngleRate());
        assertEquals(4, rate.getTrackAngleRateEncoded());
        assertNull(rate.getTrackAngleRate());

        TrackAndTurn airspeed = with(47, 56, 212);
        assertFalse(airspeed.hasTrueAirspeed());
        assertEquals(212, airspeed.getTrueAirspeedEncoded());
        assertNull(airspeed.getTrueAirspeed());
    }
}
