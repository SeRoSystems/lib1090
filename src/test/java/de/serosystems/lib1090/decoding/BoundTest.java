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

package de.serosystems.lib1090.decoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundTest {

    /** Every bound but {@link Bound#NONE} is exactly one of upper and lower; none says both or neither. */
    @Test
    void testEveryBoundHasOneSide() {
        for (Bound bound : Bound.values()) {
            if (bound == Bound.NONE) {
                assertFalse(bound.isUpper());
                assertFalse(bound.isLower());
            } else {
                assertTrue(bound.isUpper() ^ bound.isLower(), bound.name());
            }
        }
    }

    @Test
    void testSidesAndInclusivity() {
        assertTrue(Bound.BELOW.isUpper());
        assertFalse(Bound.BELOW.isInclusive());

        assertTrue(Bound.AT_MOST.isUpper());
        assertTrue(Bound.AT_MOST.isInclusive());

        assertTrue(Bound.AT_LEAST.isLower());
        assertTrue(Bound.AT_LEAST.isInclusive());

        assertTrue(Bound.MORE_THAN.isLower());
        assertFalse(Bound.MORE_THAN.isInclusive());

        assertFalse(Bound.NONE.isInclusive());
        assertEquals(5, Bound.values().length);
    }
}
