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

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstimatedPositionUncertaintyTest {

    /** TABLE 2-68, row by row. */
    @Test
    void testTheTabulatedCategories() {
        double[] upperBounds = {18520, 7408, 3704, 1852, 926, 555.6, 185.2, 92.6, 30, 10, 3};

        for (int nacP = 1; nacP <= 11; nacP++) {
            EstimatedPositionUncertainty uncertainty =
                    EstimatedPositionUncertainty.forNACp((byte) nacP);

            assertTrue(uncertainty.isKnown(), "NACp " + nacP);
            assertEquals(upperBounds[nacP - 1], uncertainty.getGuaranteedUpperBound(), "NACp " + nacP);
        }
    }

    /**
     * Category 0 is 18.52 km or more and the table annotates it "Unknown accuracy" — one category for
     * two different facts a receiver cannot separate, so nothing is guaranteed. It stays a category of
     * its own all the same: a defined value meaning "poor or unreported" is not the same as a value
     * the standard has not defined.
     */
    @Test
    void testCategoryZeroGuaranteesNothingButIsNotReserved() {
        EstimatedPositionUncertainty unknown = EstimatedPositionUncertainty.forNACp((byte) 0);

        assertSame(EstimatedPositionUncertainty.UNKNOWN_OR_AT_LEAST_18520, unknown);
        assertFalse(unknown.isKnown());
        assertTrue(Double.isNaN(unknown.getGuaranteedUpperBound()));
        assertNotSame(EstimatedPositionUncertainty.RESERVED, unknown);
    }

    /** The four reserved values share one constant, the encoded category still telling them apart. */
    @Test
    void testTheReservedCategoriesReportNothing() {
        for (byte nacP = 12; nacP <= 15; nacP++) {
            EstimatedPositionUncertainty reserved = EstimatedPositionUncertainty.forNACp(nacP);

            assertSame(EstimatedPositionUncertainty.RESERVED, reserved, "NACp " + nacP);
            assertFalse(reserved.isKnown(), "NACp " + nacP);
            assertTrue(Double.isNaN(reserved.getGuaranteedUpperBound()), "NACp " + nacP);
        }
    }

    /**
     * The point of the encoding: "is this position known to be more accurate than the limit?" answers
     * false for both degenerate categories without the caller special-casing either.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 100;

        assertTrue(EstimatedPositionUncertainty.BELOW_92_6.getGuaranteedUpperBound() < limit);
        assertFalse(EstimatedPositionUncertainty.BELOW_185_2.getGuaranteedUpperBound() < limit);
        assertFalse(EstimatedPositionUncertainty.UNKNOWN_OR_AT_LEAST_18520.getGuaranteedUpperBound() < limit);
        assertFalse(EstimatedPositionUncertainty.RESERVED.getGuaranteedUpperBound() < limit);

        // and neither can be shown to be worse than the limit either, both possibly meaning "unknown"
        assertFalse(EstimatedPositionUncertainty.UNKNOWN_OR_AT_LEAST_18520.getGuaranteedUpperBound() > limit);
        assertFalse(EstimatedPositionUncertainty.RESERVED.getGuaranteedUpperBound() > limit);
    }

    /** Every value the four-bit field can carry names a category; anything else is a caller's mistake. */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        Set<EstimatedPositionUncertainty> seen = EnumSet.noneOf(EstimatedPositionUncertainty.class);

        for (byte nacP = 0; nacP <= 15; nacP++)
            seen.add(EstimatedPositionUncertainty.forNACp(nacP));

        assertEquals(EnumSet.allOf(EstimatedPositionUncertainty.class), seen,
                "every constant is reachable from some category, and every category names one");

        assertThrows(IllegalArgumentException.class, () -> EstimatedPositionUncertainty.forNACp((byte) 16));
        assertThrows(IllegalArgumentException.class, () -> EstimatedPositionUncertainty.forNACp((byte) -1));
    }

    /** A higher category is a tighter bound, the table's own ordering. */
    @Test
    void testHigherCategoriesBoundTighter() {
        for (byte nacP = 2; nacP <= 11; nacP++)
            assertTrue(EstimatedPositionUncertainty.forNACp(nacP).getGuaranteedUpperBound()
                            < EstimatedPositionUncertainty.forNACp((byte) (nacP - 1)).getGuaranteedUpperBound(),
                    "NACp " + nacP);
    }

    /** Each constant is named after the bound it guarantees, so a transcription slip shows up here. */
    @Test
    void testNamesMatchTheirBound() {
        for (EstimatedPositionUncertainty uncertainty : EstimatedPositionUncertainty.values()) {
            if (!uncertainty.isKnown())
                continue;

            double expected = Double.parseDouble(
                    uncertainty.name().substring("BELOW_".length()).replace('_', '.'));

            assertEquals(expected, uncertainty.getGuaranteedUpperBound(), uncertainty.name());
        }
    }
}
