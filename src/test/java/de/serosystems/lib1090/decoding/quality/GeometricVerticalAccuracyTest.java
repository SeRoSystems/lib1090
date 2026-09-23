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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeometricVerticalAccuracyTest {

    @Test
    void testTheTabulatedCategories() {
        assertSame(GeometricVerticalAccuracy.AT_MOST_150, GeometricVerticalAccuracy.forGVA((byte) 1));
        assertSame(GeometricVerticalAccuracy.AT_MOST_45, GeometricVerticalAccuracy.forGVA((byte) 2));
        assertSame(GeometricVerticalAccuracy.AT_MOST_10, GeometricVerticalAccuracy.forGVA((byte) 3));

        assertEquals(150.0, GeometricVerticalAccuracy.AT_MOST_150.getGuaranteedUpperBound());
        assertEquals(45.0, GeometricVerticalAccuracy.AT_MOST_45.getGuaranteedUpperBound());
        assertEquals(10.0, GeometricVerticalAccuracy.AT_MOST_10.getGuaranteedUpperBound());
    }

    /**
     * Category 3 is the most accurate the table defines, and the switch this type replaced reported it
     * as {@code -1} — the same answer it gave for knowing nothing at all. The best category was
     * indistinguishable from the worst.
     */
    @Test
    void testTheMostAccurateCategoryIsNotMistakenForNoInformation() {
        GeometricVerticalAccuracy best = GeometricVerticalAccuracy.forGVA((byte) 3);

        assertTrue(best.isKnown());
        assertEquals(10.0, best.getGuaranteedUpperBound());
        assertNotEquals(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150, best);
    }

    /**
     * Category 0 reads "unknown or more than 150 m" in ED-102B — one category for two different facts
     * a receiver cannot separate, so nothing is guaranteed.
     */
    @Test
    void testCategoryZeroGuaranteesNothing() {
        assertSame(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150, GeometricVerticalAccuracy.forGVA((byte) 0));

        assertFalse(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150.isKnown());
        assertTrue(Double.isNaN(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150.getGuaranteedUpperBound()));
    }

    /**
     * The point of reporting {@code NaN} where nothing is guaranteed: "is this accuracy known to be
     * better than the limit?" answers false without the caller special-casing anything — and so does
     * the reverse question, which is right, since the category may only mean the transmitter does not
     * know.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 50;

        assertTrue(GeometricVerticalAccuracy.AT_MOST_45.getGuaranteedUpperBound() < limit);
        assertFalse(GeometricVerticalAccuracy.AT_MOST_150.getGuaranteedUpperBound() < limit);
        assertFalse(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150.getGuaranteedUpperBound() < limit);
        assertFalse(GeometricVerticalAccuracy.UNKNOWN_OR_ABOVE_150.getGuaranteedUpperBound() > limit);
    }

    /** Every value the two-bit field can carry names a category; anything else is a caller's mistake. */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte gva = 0; gva <= 3; gva++)
            assertNotEquals(null, GeometricVerticalAccuracy.forGVA(gva), "GVA " + gva);

        assertThrows(IllegalArgumentException.class, () -> GeometricVerticalAccuracy.forGVA((byte) 4));
        assertThrows(IllegalArgumentException.class, () -> GeometricVerticalAccuracy.forGVA((byte) -1));
    }

    /** Each constant is named after what it guarantees, so a transcription slip shows up here. */
    @Test
    void testNamesMatchTheirBound() {
        for (GeometricVerticalAccuracy accuracy : GeometricVerticalAccuracy.values()) {
            if (!accuracy.isKnown())
                continue;

            double expected = Double.parseDouble(accuracy.name().substring("AT_MOST_".length()));
            assertEquals(expected, accuracy.getGuaranteedUpperBound(), accuracy.name());
        }
    }
}
