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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainmentRadiusTest {

    /**
     * Every constant is named after the bound and the value it carries, so the name is checkable
     * against the fields. This catches the one mistake the type exists to prevent: a value
     * transcribed onto the wrong constant.
     */
    @Test
    void testNamesMatchTheirValues() {
        for (ContainmentRadius radius : ContainmentRadius.values()) {
            if (radius == ContainmentRadius.UNKNOWN)
                continue;

            String name = radius.name();
            ContainmentRadius.Bound expectedBound = name.startsWith("BELOW_")
                    ? ContainmentRadius.Bound.UPPER
                    : ContainmentRadius.Bound.LOWER;
            double expectedMeters = Double.parseDouble(
                    name.replaceFirst("^(BELOW|AT_LEAST)_", "").replace('_', '.'));

            assertEquals(expectedBound, radius.getBound(), name);
            assertEquals(expectedMeters, radius.getMeters(), name);
        }
    }

    /**
     * The same value appears on both sides for four of the tabulated radii, which is why the bound
     * cannot be left out of the type.
     */
    @Test
    void testSameValueOccursAsBothBounds() {
        assertEquals(ContainmentRadius.BELOW_25.getMeters(), ContainmentRadius.AT_LEAST_25.getMeters());
        assertEquals(ContainmentRadius.BELOW_185_2.getMeters(), ContainmentRadius.AT_LEAST_185_2.getMeters());
        assertEquals(ContainmentRadius.BELOW_1111_2.getMeters(), ContainmentRadius.AT_LEAST_1111_2.getMeters());
        assertEquals(ContainmentRadius.BELOW_37040.getMeters(), ContainmentRadius.AT_LEAST_37040.getMeters());
    }

    @Test
    void testUnknownReportsNothing() {
        assertTrue(ContainmentRadius.UNKNOWN.isUnknown());
        assertEquals(ContainmentRadius.Bound.NONE, ContainmentRadius.UNKNOWN.getBound());
        assertTrue(Double.isNaN(ContainmentRadius.UNKNOWN.getMeters()));
        assertTrue(Double.isNaN(ContainmentRadius.UNKNOWN.getGuaranteedUpperBound()));
    }

    @Test
    void testGuaranteedUpperBound() {
        assertEquals(185.2, ContainmentRadius.BELOW_185_2.getGuaranteedUpperBound());
        assertEquals(Double.POSITIVE_INFINITY, ContainmentRadius.AT_LEAST_185_2.getGuaranteedUpperBound());
        assertTrue(Double.isNaN(ContainmentRadius.UNKNOWN.getGuaranteedUpperBound()));
    }

    /**
     * The point of the NaN and infinity encoding: "is the position known to be better than this?"
     * answers false wherever the answer is not known, so a caller needs no special cases.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 5000;

        assertTrue(ContainmentRadius.BELOW_185_2.getGuaranteedUpperBound() < limit);
        assertFalse(ContainmentRadius.AT_LEAST_185_2.getGuaranteedUpperBound() < limit);
        assertFalse(ContainmentRadius.UNKNOWN.getGuaranteedUpperBound() < limit);
        assertFalse(ContainmentRadius.BELOW_37040.getGuaranteedUpperBound() < limit);
    }

    @Test
    void testWorseOfPrefersTheLargerValue() {
        assertEquals(ContainmentRadius.BELOW_185_2,
                ContainmentRadius.worseOf(ContainmentRadius.BELOW_75, ContainmentRadius.BELOW_185_2));
        assertEquals(ContainmentRadius.BELOW_185_2,
                ContainmentRadius.worseOf(ContainmentRadius.BELOW_185_2, ContainmentRadius.BELOW_75));
    }

    /** At the same value a lower bound rules nothing out above it, so it is the poorer report. */
    @Test
    void testWorseOfPrefersALowerBoundAtTheSameValue() {
        assertEquals(ContainmentRadius.AT_LEAST_185_2,
                ContainmentRadius.worseOf(ContainmentRadius.BELOW_185_2, ContainmentRadius.AT_LEAST_185_2));
        assertEquals(ContainmentRadius.AT_LEAST_185_2,
                ContainmentRadius.worseOf(ContainmentRadius.AT_LEAST_185_2, ContainmentRadius.BELOW_185_2));
    }

    @Test
    void testWorseOfTreatsUnknownAsTheWorst() {
        for (ContainmentRadius radius : ContainmentRadius.values()) {
            assertEquals(ContainmentRadius.UNKNOWN,
                    ContainmentRadius.worseOf(radius, ContainmentRadius.UNKNOWN), radius.name());
            assertEquals(ContainmentRadius.UNKNOWN,
                    ContainmentRadius.worseOf(ContainmentRadius.UNKNOWN, radius), radius.name());
        }
    }

    /** Order of arguments never changes the outcome, so a fold over a set is well-defined. */
    @Test
    void testWorseOfIsSymmetric() {
        for (ContainmentRadius first : ContainmentRadius.values())
            for (ContainmentRadius second : ContainmentRadius.values())
                assertEquals(ContainmentRadius.worseOf(first, second),
                        ContainmentRadius.worseOf(second, first),
                        first.name() + " / " + second.name());
    }

    @Test
    void testNoConstantIsUnknownButForUnknownItself() {
        for (ContainmentRadius radius : ContainmentRadius.values())
            assertEquals(radius == ContainmentRadius.UNKNOWN, radius.isUnknown(), radius.name());
    }

    /**
     * The whole of ED-102B §2.2.3.2.7.5.4.8 TABLE 2-82, which is also the switch this replaced in
     * HVAVelocityMsg, so a transcription slip in either direction shows up here.
     */
    @Test
    void testTheTabulatedPositionIntegrityCategories() {
        double[] upperBounds = {37040, 18520, 14816, 7408, 3704, 1852, 1111.2, 926, 555.6, 370.4,
                185.2, 75, 25, 7.5};

        for (int pic = 1; pic <= 14; pic++) {
            ContainmentRadius radius = ContainmentRadius.forPIC((byte) pic);

            assertEquals(ContainmentRadius.Bound.UPPER, radius.getBound(), "PIC " + pic);
            assertEquals(upperBounds[pic - 1], radius.getMeters(), "PIC " + pic);
        }
    }

    /**
     * The two categories at the ends of the table report nothing: PIC 0 because the radius is
     * unknown, PIC 15 because the standard reserves it. They are one answer here — the encoded
     * category is what tells them apart.
     */
    @Test
    void testTheCategoriesAtBothEndsReportNothing() {
        assertEquals(ContainmentRadius.UNKNOWN, ContainmentRadius.forPIC((byte) 0));
        assertEquals(ContainmentRadius.UNKNOWN, ContainmentRadius.forPIC((byte) 15));
    }

    /** The category is four bits, so anything else is a caller's mistake rather than a reading. */
    @Test
    void testPositionIntegrityCategoriesOutsideTheFieldAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ContainmentRadius.forPIC((byte) 16));
        assertThrows(IllegalArgumentException.class, () -> ContainmentRadius.forPIC((byte) -1));
    }

    /** A higher category is a tighter bound, the table's own ordering. */
    @Test
    void testHigherPositionIntegrityCategoriesBoundTighter() {
        for (byte pic = 2; pic <= 14; pic++)
            assertTrue(ContainmentRadius.forPIC(pic).getGuaranteedUpperBound()
                            < ContainmentRadius.forPIC((byte) (pic - 1)).getGuaranteedUpperBound(),
                    "PIC " + pic);
    }
}
