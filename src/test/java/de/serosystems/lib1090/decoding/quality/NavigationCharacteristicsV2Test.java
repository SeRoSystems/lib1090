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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NavigationCharacteristicsV2Test {

    /** Builds the supplements the airborne rows read: A and B. */
    private static NICSupplements airborneSupplements(NICSupplement a, NICSupplement b) {
        return NICSupplements.none().withA(a).withB(b);
    }

    /** Builds the supplements the surface rows read: A and C. */
    private static NICSupplements surfaceSupplements(NICSupplement a, NICSupplement c) {
        return NICSupplements.none().withA(a).withC(c);
    }

    private static final Set<Integer> SURFACE_TYPE_CODES = new HashSet<>(Arrays.asList(0, 5, 6, 7, 8));
    private static final Set<Integer> AIRBORNE_TYPE_CODES =
            new HashSet<>(Arrays.asList(0, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22));

    /** Dispatches to the entry point the type code belongs to; type code 0 is shared. */
    private static NavigationCharacteristicsV2 lookup(int formatTypeCode, NICSupplement first,
                                                      NICSupplement second) {
        return formatTypeCode >= 5 && formatTypeCode <= 8
                ? NavigationCharacteristicsV2.forSurfaceFormatTypeCode((byte) formatTypeCode, surfaceSupplements(first, second))
                : NavigationCharacteristicsV2.forAirborneFormatTypeCode((byte) formatTypeCode, airborneSupplements(first, second));
    }

    /** What a supplement could turn out to be: itself if known, either value if not. */
    private static List<NICSupplement> completions(NICSupplement nicSupplement) {
        return nicSupplement == NICSupplement.UNKNOWN
                ? Arrays.asList(NICSupplement.SET, NICSupplement.CLEAR)
                : Arrays.asList(nicSupplement);
    }

    private static NavigationCharacteristicsV2 worseOf(NavigationCharacteristicsV2 first,
                                                       NavigationCharacteristicsV2 second) {
        if (first == null)
            return second;
        if (first.getNICEncoded() != second.getNICEncoded())
            return first.getNICEncoded() < second.getNICEncoded() ? first : second;

        ContainmentRadius worse =
                ContainmentRadius.worseOf(first.getContainmentRadius(), second.getContainmentRadius());
        return worse == first.getContainmentRadius() ? first : second;
    }

    /**
     * The whole rule set in one assertion: whatever is not known, the answer is exactly the poorest
     * answer any completion of that knowledge could give. A rule that guessed a supplement, or that
     * reported better than the worst case, fails here without needing a case of its own.
     */
    @Test
    void testPartialKnowledgeIsTheWorstOfItsCompletions() {
        for (int formatTypeCode : union(SURFACE_TYPE_CODES, AIRBORNE_TYPE_CODES)) {
            for (NICSupplement first : NICSupplement.values()) {
                for (NICSupplement second : NICSupplement.values()) {
                    NavigationCharacteristicsV2 worst = null;
                    for (NICSupplement completedFirst : completions(first))
                        for (NICSupplement completedSecond : completions(second))
                            worst = worseOf(worst, lookup(formatTypeCode, completedFirst, completedSecond));

                    assertSame(worst, lookup(formatTypeCode, first, second),
                            "type code " + formatTypeCode + ", " + first + " / " + second);
                }
            }
        }
    }

    /**
     * Supplement B decides airborne type codes 11 and 16 on its own: the table never pairs a set B
     * with a clear A, so a receiver that has heard no operational status message still decodes them
     * exactly.
     */
    @Test
    void testSupplementAIsNotConsultedWhereSupplementBDecides() {
        for (byte formatTypeCode : new byte[]{11, 16}) {
            for (NICSupplement nicSupplementB : NICSupplement.values()) {
                NavigationCharacteristicsV2 expected = NavigationCharacteristicsV2
                        .forAirborneFormatTypeCode(formatTypeCode, airborneSupplements(NICSupplement.UNKNOWN, nicSupplementB));

                for (NICSupplement nicSupplementA : NICSupplement.values())
                    assertSame(expected, NavigationCharacteristicsV2
                                    .forAirborneFormatTypeCode(formatTypeCode, airborneSupplements(nicSupplementA, nicSupplementB)),
                            "type code " + formatTypeCode + ", B " + nicSupplementB);
            }
        }
    }

    /** Supplement C does not reach surface type codes 5, 6 and 7. */
    @Test
    void testSupplementCIsNotConsultedBelowTypeCode8() {
        for (byte formatTypeCode : new byte[]{0, 5, 6, 7}) {
            for (NICSupplement nicSupplementA : NICSupplement.values()) {
                NavigationCharacteristicsV2 expected = NavigationCharacteristicsV2
                        .forSurfaceFormatTypeCode(formatTypeCode, surfaceSupplements(nicSupplementA, NICSupplement.UNKNOWN));

                for (NICSupplement nicSupplementC : NICSupplement.values())
                    assertSame(expected, NavigationCharacteristicsV2
                                    .forSurfaceFormatTypeCode(formatTypeCode, surfaceSupplements(nicSupplementA, nicSupplementC)),
                            "type code " + formatTypeCode + ", A " + nicSupplementA);
            }
        }
    }

    /**
     * Type code 13 is the exception: a set supplement B leaves two rows, and there a clear A is the
     * better of them, so an unknown A reports the larger radius rather than the smaller.
     */
    @Test
    void testTypeCode13IsWhereSupplementAStillMatters() {
        assertEquals(ContainmentRadius.BELOW_555_6, airborne(13, NICSupplement.CLEAR, NICSupplement.SET));
        assertEquals(ContainmentRadius.BELOW_1111_2, airborne(13, NICSupplement.SET, NICSupplement.SET));
        assertEquals(ContainmentRadius.BELOW_1111_2, airborne(13, NICSupplement.UNKNOWN, NICSupplement.SET));

        // a clear B leaves one row whatever A says, which is what makes B authoritative
        for (NICSupplement nicSupplementA : NICSupplement.values())
            assertEquals(ContainmentRadius.BELOW_926, airborne(13, nicSupplementA, NICSupplement.CLEAR),
                    "A " + nicSupplementA);
    }

    @Test
    void testEntryPointsRejectTheOtherSubtypesTypeCodes() {
        for (byte formatTypeCode = 0; formatTypeCode < 32; formatTypeCode++) {
            byte code = formatTypeCode;

            if (!AIRBORNE_TYPE_CODES.contains((int) code))
                assertThrows(IllegalArgumentException.class, () -> NavigationCharacteristicsV2
                        .forAirborneFormatTypeCode(code, airborneSupplements(NICSupplement.UNKNOWN, NICSupplement.UNKNOWN)));

            if (!SURFACE_TYPE_CODES.contains((int) code))
                assertThrows(IllegalArgumentException.class, () -> NavigationCharacteristicsV2
                        .forSurfaceFormatTypeCode(code, surfaceSupplements(NICSupplement.UNKNOWN, NICSupplement.UNKNOWN)));
        }
    }

    /**
     * Every constant naming a supplement combination is the row that combination selects, so a value
     * transcribed onto the wrong row shows up here.
     */
    @Test
    void testNamesSelectTheirOwnRow() {
        for (NavigationCharacteristicsV2 row : NavigationCharacteristicsV2.values()) {
            String name = row.name();
            String[] parts = name.split("_");
            byte formatTypeCode = Byte.parseByte(parts[2]);

            assertEquals(formatTypeCode, row.getFormatTypeCode(), name);

            if (parts.length == 3)
                continue;   // the type code alone settles this row

            NICSupplement first = NICSupplement.valueOf(parts[4]);
            NICSupplement second = parts.length > 5 ? NICSupplement.valueOf(parts[6]) : NICSupplement.UNKNOWN;

            assertSame(row, lookup(formatTypeCode, first, second), name);
        }
    }

    @Test
    void testTableCoversExactlyThePositionTypeCodes() {
        Set<Integer> covered = Arrays.stream(NavigationCharacteristicsV2.values())
                .map(row -> (int) row.getFormatTypeCode())
                .collect(Collectors.toSet());

        assertEquals(union(SURFACE_TYPE_CODES, AIRBORNE_TYPE_CODES), covered);
    }

    /**
     * Every NIC sits on its own containment radius bucket. NIC 6 is the bucket version 2 subdivides —
     * three ways airborne, two ways surface — and NIC 0 reports no radius at all.
     */
    @Test
    void testEachNICPairsWithItsOwnRadius() {
        for (NavigationCharacteristicsV2 row : NavigationCharacteristicsV2.values()) {
            ContainmentRadius expected;
            switch (row.getNICEncoded()) {
                case 11: expected = ContainmentRadius.BELOW_7_5; break;
                case 10: expected = ContainmentRadius.BELOW_25; break;
                case 9: expected = ContainmentRadius.BELOW_75; break;
                case 8: expected = ContainmentRadius.BELOW_185_2; break;
                case 7: expected = ContainmentRadius.BELOW_370_4; break;
                case 5: expected = ContainmentRadius.BELOW_1852; break;
                case 4: expected = ContainmentRadius.BELOW_3704; break;
                case 3: expected = ContainmentRadius.BELOW_7408; break;
                case 2: expected = ContainmentRadius.BELOW_14816; break;
                case 1: expected = ContainmentRadius.BELOW_37040; break;
                default: continue;   // NIC 6 is subdivided, NIC 0 reports no radius
            }

            assertEquals(expected, row.getContainmentRadius(), row.name());
        }
    }

    @Test
    void testSpotChecksAgainstTheStandardTable() {
        assertEquals(ContainmentRadius.BELOW_370_4, surface(8, NICSupplement.SET, NICSupplement.SET));
        assertEquals(ContainmentRadius.BELOW_555_6, surface(8, NICSupplement.SET, NICSupplement.CLEAR));
        assertEquals(ContainmentRadius.BELOW_1111_2, surface(8, NICSupplement.CLEAR, NICSupplement.SET));
        assertEquals(ContainmentRadius.AT_LEAST_1111_2, surface(8, NICSupplement.CLEAR, NICSupplement.CLEAR));

        assertEquals((byte) 7, NavigationCharacteristicsV2
                .forSurfaceFormatTypeCode((byte) 8, surfaceSupplements(NICSupplement.SET, NICSupplement.SET)).getNICEncoded());
        assertEquals((byte) 0, NavigationCharacteristicsV2
                .forSurfaceFormatTypeCode((byte) 8, surfaceSupplements(NICSupplement.CLEAR, NICSupplement.CLEAR)).getNICEncoded());

        assertEquals((byte) 9, NavigationCharacteristicsV2
                .forAirborneFormatTypeCode((byte) 11, airborneSupplements(NICSupplement.UNKNOWN, NICSupplement.SET)).getNICEncoded());
        assertEquals((byte) 3, NavigationCharacteristicsV2
                .forAirborneFormatTypeCode((byte) 16, airborneSupplements(NICSupplement.UNKNOWN, NICSupplement.SET)).getNICEncoded());

        assertEquals(ContainmentRadius.AT_LEAST_37040, airborne(18, NICSupplement.UNKNOWN, NICSupplement.UNKNOWN));
        assertEquals(ContainmentRadius.AT_LEAST_25, airborne(22, NICSupplement.UNKNOWN, NICSupplement.UNKNOWN));
        assertEquals(ContainmentRadius.UNKNOWN, airborne(0, NICSupplement.UNKNOWN, NICSupplement.UNKNOWN));
    }

    private static ContainmentRadius airborne(int formatTypeCode, NICSupplement a, NICSupplement b) {
        return NavigationCharacteristicsV2
                .forAirborneFormatTypeCode((byte) formatTypeCode, airborneSupplements(a, b)).getContainmentRadius();
    }

    private static ContainmentRadius surface(int formatTypeCode, NICSupplement a, NICSupplement c) {
        return NavigationCharacteristicsV2
                .forSurfaceFormatTypeCode((byte) formatTypeCode, surfaceSupplements(a, c)).getContainmentRadius();
    }

    private static Set<Integer> union(Set<Integer> first, Set<Integer> second) {
        Set<Integer> all = new HashSet<>(first);
        all.addAll(second);
        return all;
    }
}
