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

package de.serosystems.lib1090.decoding.movement;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTest {

    private static Interval v0v1(int encoded) {
        return MovementV0V1.forEncoded((byte) encoded).getGroundSpeed();
    }

    private static Interval v2v3(int encoded) {
        return MovementV2V3.forEncoded((byte) encoded).getGroundSpeed();
    }

    private static void assertInterval(Bound lowerBound, double lower, Bound upperBound, double upper,
                                       Interval speed) {
        assertEquals(lowerBound, speed.getLowerBound(), speed.toString());
        assertEquals(lower, speed.getLower(), speed.toString());
        assertEquals(upperBound, speed.getUpperBound(), speed.toString());
        assertEquals(upper, speed.getUpper(), speed.toString());
    }

    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte encoded = 0; encoded >= 0; encoded++) {
            assertEquals(encoded, MovementV0V1.forEncoded(encoded).getEncoded());
            assertEquals(encoded, MovementV2V3.forEncoded(encoded).getEncoded());
            assertSame(MovementV0V1.forEncoded(encoded), MovementV0V1.forEncoded(encoded));
        }

        assertThrows(IllegalArgumentException.class, () -> MovementV0V1.forEncoded((byte) -1));
        assertThrows(IllegalArgumentException.class, () -> MovementV2V3.forEncoded((byte) 128));
    }

    /** No information (0) and the three reserved codes report no ground speed; all others do. */
    @Test
    void testCodesWithoutGroundSpeed() {
        for (int encoded : new int[]{0, 125, 126, 127}) {
            assertFalse(MovementV0V1.forEncoded((byte) encoded).hasGroundSpeed(), "v0/v1 " + encoded);
            assertFalse(MovementV2V3.forEncoded((byte) encoded).hasGroundSpeed(), "v2/v3 " + encoded);
            assertNull(v0v1(encoded));
            assertNull(v2v3(encoded));
        }
        for (int encoded = 1; encoded <= 124; encoded++) {
            assertTrue(MovementV0V1.forEncoded((byte) encoded).hasGroundSpeed(), "v0/v1 " + encoded);
            assertTrue(MovementV2V3.forEncoded((byte) encoded).hasGroundSpeed(), "v2/v3 " + encoded);
        }
    }

    /** The intervals tile the speed range without gaps: each code ends where the next begins. */
    @Test
    void testIntervalsAreContiguous() {
        for (int encoded = 1; encoded < 124; encoded++) {
            assertEquals(v0v1(encoded).getUpper(), v0v1(encoded + 1).getLower(), "v0/v1 " + encoded);
            assertEquals(v2v3(encoded).getUpper(), v2v3(encoded + 1).getLower(), "v2/v3 " + encoded);
        }
    }

    /** Versions 0 and 1 include the lower end and exclude the upper. */
    @Test
    void testVersion0And1() {
        assertInterval(Bound.AT_LEAST, 0, Bound.BELOW, 0.125, v0v1(1));
        assertInterval(Bound.AT_LEAST, 0.125, Bound.BELOW, 0.25, v0v1(2));
        assertInterval(Bound.AT_LEAST, 0.875, Bound.BELOW, 1, v0v1(8));
        assertInterval(Bound.AT_LEAST, 1, Bound.BELOW, 1.25, v0v1(9));
        assertInterval(Bound.AT_LEAST, 2, Bound.BELOW, 2.5, v0v1(13));
        assertInterval(Bound.AT_LEAST, 15, Bound.BELOW, 16, v0v1(39));
        assertInterval(Bound.AT_LEAST, 70, Bound.BELOW, 72, v0v1(94));
        assertInterval(Bound.AT_LEAST, 170, Bound.BELOW, 175, v0v1(123));
        assertInterval(Bound.AT_LEAST, 175, Bound.NONE, Double.NaN, v0v1(124));
    }

    /**
     * Versions 2 and 3 exclude the lower end and include the upper, so 2 kt is code 12 here and code
     * 13 in versions 0 and 1. "Aircraft Stopped" is exactly 0, and codes 3 to 8 step by 7/48 kt.
     */
    @Test
    void testVersion2And3() {
        assertInterval(Bound.AT_LEAST, 0, Bound.AT_MOST, 0, v2v3(1));
        assertInterval(Bound.MORE_THAN, 0, Bound.AT_MOST, 0.125, v2v3(2));
        assertInterval(Bound.MORE_THAN, 0.125, Bound.AT_MOST, 0.125 + 7.0 / 48, v2v3(3));
        assertInterval(Bound.MORE_THAN, 0.125 + 35.0 / 48, Bound.AT_MOST, 1, v2v3(8));
        assertInterval(Bound.MORE_THAN, 1.75, Bound.AT_MOST, 2, v2v3(12));
        assertInterval(Bound.MORE_THAN, 2, Bound.AT_MOST, 2.5, v2v3(13));
        assertInterval(Bound.MORE_THAN, 15, Bound.AT_MOST, 16, v2v3(39));
        assertInterval(Bound.MORE_THAN, 170, Bound.AT_MOST, 175, v2v3(123));
        assertInterval(Bound.MORE_THAN, 175, Bound.NONE, Double.NaN, v2v3(124));
    }

    @Test
    void testWidthAndGuaranteedUpperBound() {
        assertEquals(0.5, v0v1(13).getWidth());
        assertEquals(2.5, v2v3(13).getGuaranteedUpperBound());
        assertEquals(0, v2v3(1).getWidth());
        assertEquals(Double.POSITIVE_INFINITY, v0v1(124).getWidth());
        assertEquals(Double.POSITIVE_INFINITY, v2v3(124).getGuaranteedUpperBound());
    }

    @Test
    void testToString() {
        assertEquals("[2.0, 2.5)", v0v1(13).toString());
        assertEquals("(2.0, 2.5]", v2v3(13).toString());
        assertEquals("[175.0, ∞)", v0v1(124).toString());
        assertEquals("(175.0, ∞)", v2v3(124).toString());
    }
}
