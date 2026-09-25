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

package de.serosystems.lib1090.decoding.diffbaroalt;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAlt.Status;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiffBaroAltTest {

    private static final Interval EXACTLY_ZERO = Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 0);

    private static Interval upTo(double lower, double upper) {
        return Interval.of(Bound.MORE_THAN, lower, Bound.AT_MOST, upper);
    }

    private static Interval above(double lower) {
        return Interval.of(Bound.MORE_THAN, lower, Bound.NONE, Double.NaN);
    }

    private static void assertDifference(Interval expected, double value, DiffBaroAlt actual) {
        assertEquals(Status.AVAILABLE, actual.getStatus(), actual.toString());
        assertEquals(expected, actual.getDifference(), actual.toString());
        assertEquals(value, actual.getValue(), actual.toString());
    }

    private static void assertNothing(Status expected, DiffBaroAlt actual) {
        assertEquals(expected, actual.getStatus(), actual.toString());
        assertFalse(actual.hasDifference());
        assertNull(actual.getDifference());
        assertNull(actual.getValue());
    }

    /** ED-102B TABLE 2-27 as revised by Change 1, row by row. */
    @Test
    void testVersion3() {
        assertNothing(Status.UNKNOWN, DiffBaroAltV3.of(false, 0));
        assertNothing(Status.UNKNOWN, DiffBaroAltV3.of(false, 0b000_0000_0000 | 0b011 << 8));
        assertNothing(Status.INVALID, DiffBaroAltV3.of(false, 1));
        assertNothing(Status.INVALID, DiffBaroAltV3.of(false, 0b100_0000_0000));

        assertDifference(EXACTLY_ZERO, 0, DiffBaroAltV3.of(false, 2));
        assertDifference(upTo(0, 12.5), 6.25, DiffBaroAltV3.of(false, 3));
        assertDifference(upTo(12.5, 25), 18.75, DiffBaroAltV3.of(false, 4));
        assertDifference(upTo(3112.5, 3125), 3118.75, DiffBaroAltV3.of(false, 0b000_1111_1100));
        assertDifference(upTo(3137.5, 3150), 3143.75, DiffBaroAltV3.of(false, 0b000_1111_1110));
        assertDifference(upTo(3150, 3250), 3200, DiffBaroAltV3.of(false, 0b000_1111_1111));

        // the N/A range 001 0000 0000 - 001 1111 1101 starts with a code the first row claims as
        // "Invalid / Unknown": ME 47-48 are NIC supplement D there, whatever their value
        assertNothing(Status.UNKNOWN, DiffBaroAltV3.of(false, 0b001_0000_0000));
        assertNothing(Status.INVALID, DiffBaroAltV3.of(false, 0b001_0000_0001));
        assertNothing(Status.INVALID, DiffBaroAltV3.of(false, 0b001_1111_1101));
        assertDifference(upTo(3250, 3350), 3300, DiffBaroAltV3.of(false, 0b001_1111_1110));
        assertDifference(upTo(3350, 3450), 3400, DiffBaroAltV3.of(false, 0b001_1111_1111));
        assertDifference(upTo(4350, 4450), 4400, DiffBaroAltV3.of(false, 0b110_1111_1111));
        assertDifference(upTo(4450, 4550), 4500, DiffBaroAltV3.of(false, 0b111_1111_1110));
        // "≥ 4550" as a midpoint, "> 4550" as an interval
        assertDifference(above(4550), 4550, DiffBaroAltV3.of(false, 0b111_1111_1111));
    }

    /** The defined version 3 codes, in order of their magnitude, tile [0, ∞) without gaps. */
    @Test
    void testVersion3IsContiguous() {
        List<Interval> intervals = new ArrayList<>();
        for (int code = 2; code <= 0xFF; code++)
            intervals.add(DiffBaroAltV3.of(false, code).getDifference());
        for (int top = 1; top <= 7; top++) {
            intervals.add(DiffBaroAltV3.of(false, top << 8 | 0xFE).getDifference());
            intervals.add(DiffBaroAltV3.of(false, top << 8 | 0xFF).getDifference());
        }

        for (int i = 1; i < intervals.size(); i++)
            assertEquals(intervals.get(i - 1).getUpper(), intervals.get(i).getLower(), intervals.get(i).toString());
    }

    /** The sign mirrors the interval, and the value with it, without making a negative zero of 0. */
    @Test
    void testSign() {
        assertDifference(Interval.of(Bound.AT_LEAST, -12.5, Bound.BELOW, 0), -6.25, DiffBaroAltV3.of(true, 3));
        assertDifference(Interval.of(Bound.NONE, Double.NaN, Bound.BELOW, -4550), -4550,
                DiffBaroAltV3.of(true, 0x7FF));
        assertDifference(EXACTLY_ZERO, 0, DiffBaroAltV3.of(true, 2));
        assertEquals("[-12.5, 0.0)", DiffBaroAltV3.of(true, 3).getDifference().toString());
    }

    /** ED-102B TABLE 2-186: each code stands for the two version 3 codes sharing its bits. */
    @Test
    void testADSRVersion3() {
        assertNothing(Status.UNKNOWN, ADSRDiffBaroAltV3.of(false, 0));
        assertNothing(Status.INVALID, ADSRDiffBaroAltV3.of(false, 0b10_0000_0000));
        // as in version 3, the N/A range starting at 00 1000 0000 overlaps "0x x000 0000"
        assertNothing(Status.UNKNOWN, ADSRDiffBaroAltV3.of(false, 0b00_1000_0000));
        assertNothing(Status.INVALID, ADSRDiffBaroAltV3.of(false, 0b00_1000_0001));

        // printed "0 < DFBA ≤ 12.5", but its version 3 codes include exactly 0
        assertDifference(Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 12.5), 0, ADSRDiffBaroAltV3.of(false, 1));
        assertDifference(upTo(12.5, 37.5), 25, ADSRDiffBaroAltV3.of(false, 2));
        assertDifference(upTo(3112.5, 3137.5), 3125, ADSRDiffBaroAltV3.of(false, 0b00_0111_1110));
        assertDifference(upTo(3137.5, 3250), 3193.75, ADSRDiffBaroAltV3.of(false, 0b00_0111_1111));
        assertDifference(upTo(3250, 3450), 3350, ADSRDiffBaroAltV3.of(false, 0b00_1111_1111));
        assertDifference(upTo(4250, 4450), 4350, ADSRDiffBaroAltV3.of(false, 0b11_0111_1111));
        assertDifference(above(4450), 4450, ADSRDiffBaroAltV3.of(false, 0b11_1111_1111));
    }

    /** The 7-bit field: (L - 1) * 25 ft rounded, and everything above 3137.5 ft for all ones. */
    @Test
    void testVersion0To2() {
        assertNothing(Status.UNKNOWN, DiffBaroAltV0V2.of(false, 0));
        assertDifference(Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 12.5), 0, DiffBaroAltV0V2.of(false, 1));
        assertDifference(upTo(12.5, 37.5), 25, DiffBaroAltV0V2.of(false, 2));
        assertDifference(upTo(537.5, 562.5), 550, DiffBaroAltV0V2.of(false, 23));
        assertDifference(upTo(3112.5, 3137.5), 3125, DiffBaroAltV0V2.of(false, 126));
        assertDifference(above(3137.5), 3137.5, DiffBaroAltV0V2.of(false, 127));
    }

    private static boolean contains(Interval outer, Interval inner) {
        return outer.getGuaranteedLowerBound() <= inner.getGuaranteedLowerBound()
                && inner.getGuaranteedUpperBound() <= outer.getGuaranteedUpperBound();
    }

    /**
     * What makes the codings compatible: whatever a version 3 code reports, reading only the bits the
     * coarser codings keep reports an interval that contains it.
     */
    @Test
    void testCoarserCodingsContainVersion3() {
        for (int code = 0; code <= 0x7FF; code++) {
            DiffBaroAlt v3 = DiffBaroAltV3.of(false, code);
            if (!v3.hasDifference()) continue;

            DiffBaroAlt adsr = ADSRDiffBaroAltV3.of(false, code >>> 1);
            DiffBaroAlt legacy = DiffBaroAltV0V2.of(false, (code >>> 1) & 0x7F);
            assertTrue(contains(adsr.getDifference(), v3.getDifference()), v3 + " in " + adsr);
            assertTrue(contains(legacy.getDifference(), v3.getDifference()), v3 + " in " + legacy);
        }
    }

    @Test
    void testCodesMustFit() {
        assertThrows(IllegalArgumentException.class, () -> DiffBaroAltV3.of(false, 0x800));
        assertThrows(IllegalArgumentException.class, () -> ADSRDiffBaroAltV3.of(false, 0x400));
        assertThrows(IllegalArgumentException.class, () -> DiffBaroAltV0V2.of(false, 0x80));
        assertThrows(IllegalArgumentException.class, () -> DiffBaroAltV0V2.of(false, -1));
    }
}
