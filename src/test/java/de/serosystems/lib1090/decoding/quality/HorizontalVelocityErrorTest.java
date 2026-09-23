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

package de.serosystems.lib1090.decoding.quality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorizontalVelocityErrorTest {

    @Test
    void testTheTabulatedCategories() {
        assertSame(HorizontalVelocityError.BELOW_10, HorizontalVelocityError.forNACv((byte) 1));
        assertSame(HorizontalVelocityError.BELOW_3, HorizontalVelocityError.forNACv((byte) 2));
        assertSame(HorizontalVelocityError.BELOW_1, HorizontalVelocityError.forNACv((byte) 3));
        assertSame(HorizontalVelocityError.BELOW_0_3, HorizontalVelocityError.forNACv((byte) 4));

        assertEquals(10.0, HorizontalVelocityError.BELOW_10.getGuaranteedUpperBound());
        assertEquals(3.0, HorizontalVelocityError.BELOW_3.getGuaranteedUpperBound());
        assertEquals(1.0, HorizontalVelocityError.BELOW_1.getGuaranteedUpperBound());
        assertEquals(0.3, HorizontalVelocityError.BELOW_0_3.getGuaranteedUpperBound());
    }

    /**
     * Category 0 reads "unknown or 10 m/s or more" in ED-102B — one category for two different facts
     * a receiver cannot separate, so nothing is guaranteed. The categories the standard reserves are
     * reported the same way.
     */
    @Test
    void testCategoryZeroAndTheReservedOnesGuaranteeNothing() {
        for (byte nacV : new byte[]{0, 5, 6, 7})
            assertSame(HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10,
                    HorizontalVelocityError.forNACv(nacV), "NACv " + nacV);

        assertFalse(HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10.isKnown());
        assertTrue(Double.isNaN(HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10.getGuaranteedUpperBound()));
    }

    /**
     * The point of reporting {@code NaN} where nothing is guaranteed: "is this velocity known to be
     * better than the limit?" answers false without the caller special-casing anything — and so does
     * the reverse question, which is right, since the category may only mean the transmitter does not
     * know.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 5;

        assertTrue(HorizontalVelocityError.BELOW_3.getGuaranteedUpperBound() < limit);
        assertFalse(HorizontalVelocityError.BELOW_10.getGuaranteedUpperBound() < limit);
        assertFalse(HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10.getGuaranteedUpperBound() < limit);
        assertFalse(HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10.getGuaranteedUpperBound() > limit);
    }

    @Test
    void testEveryCategoryButZeroGuaranteesABound() {
        for (HorizontalVelocityError error : HorizontalVelocityError.values())
            assertEquals(error != HorizontalVelocityError.UNKNOWN_OR_AT_LEAST_10, error.isKnown(),
                    error.name());
    }

    /** The field is three bits, so anything else is a caller's mistake rather than a reading. */
    @Test
    void testValuesOutsideTheFieldAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> HorizontalVelocityError.forNACv((byte) 8));
        assertThrows(IllegalArgumentException.class, () -> HorizontalVelocityError.forNACv((byte) -1));
    }

    /** Each constant is named after what it guarantees, so a transcription slip shows up here. */
    @Test
    void testNamesMatchTheirBound() {
        for (HorizontalVelocityError error : HorizontalVelocityError.values()) {
            if (!error.isKnown())
                continue;

            double expected = Double.parseDouble(error.name().substring("BELOW_".length()).replace('_', '.'));
            assertEquals(expected, error.getGuaranteedUpperBound(), error.name());
        }
    }
}
