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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NavigationCharacteristicsV3Test {

    private static final Set<Integer> SURFACE_TYPE_CODES = new HashSet<>(Arrays.asList(0, 5, 6, 7, 8));
    private static final Set<Integer> AIRBORNE_TYPE_CODES =
            new HashSet<>(Arrays.asList(0, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22));

    /** The type codes version 2 and version 3 both define, and must define identically. */
    private static final Set<Integer> SHARED_WITH_V2 =
            new HashSet<>(Arrays.asList(0, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18));

    private static List<NICSupplement> completions(NICSupplement nicSupplement) {
        return nicSupplement == NICSupplement.UNKNOWN
                ? Arrays.asList(NICSupplement.SET, NICSupplement.CLEAR)
                : Arrays.asList(nicSupplement);
    }

    private static List<NICSupplementD> completions(NICSupplementD nicSupplementD) {
        return nicSupplementD == NICSupplementD.UNKNOWN
                ? Arrays.asList(NICSupplementD.ZERO, NICSupplementD.ONE, NICSupplementD.TWO,
                NICSupplementD.THREE)
                : Arrays.asList(nicSupplementD);
    }

    private static NavigationCharacteristicsV3 worseOf(NavigationCharacteristicsV3 first,
                                                       NavigationCharacteristicsV3 second) {
        if (first == null)
            return second;
        if (first.getNIC() != second.getNIC())
            return first.getNIC() < second.getNIC() ? first : second;

        ContainmentRadius worse =
                ContainmentRadius.worseOf(first.getContainmentRadius(), second.getContainmentRadius());
        return worse == first.getContainmentRadius() ? first : second;
    }

    /**
     * The airborne rule set in one assertion: whatever is not known, the answer is exactly the poorest
     * answer any completion of that knowledge could give.
     */
    @Test
    void testPartialAirborneKnowledgeIsTheWorstOfItsCompletions() {
        for (int formatTypeCode : AIRBORNE_TYPE_CODES) {
            byte ftc = (byte) formatTypeCode;

            for (NICSupplement a : NICSupplement.values())
                for (NICSupplement b : NICSupplement.values())
                    for (NICSupplementD d : NICSupplementD.values()) {
                        NavigationCharacteristicsV3 worst = null;
                        for (NICSupplement completedA : completions(a))
                            for (NICSupplement completedB : completions(b))
                                for (NICSupplementD completedD : completions(d))
                                    worst = worseOf(worst, NavigationCharacteristicsV3
                                            .forAirborneFormatTypeCode(ftc, completedA, completedB, completedD));

                        assertSame(worst,
                                NavigationCharacteristicsV3.forAirborneFormatTypeCode(ftc, a, b, d),
                                "type code " + formatTypeCode + ", " + a + " / " + b + " / " + d);
                    }
        }
    }

    @Test
    void testPartialSurfaceKnowledgeIsTheWorstOfItsCompletions() {
        for (int formatTypeCode : SURFACE_TYPE_CODES) {
            byte ftc = (byte) formatTypeCode;

            for (NICSupplement a : NICSupplement.values())
                for (NICSupplement c : NICSupplement.values()) {
                    NavigationCharacteristicsV3 worst = null;
                    for (NICSupplement completedA : completions(a))
                        for (NICSupplement completedC : completions(c))
                            worst = worseOf(worst, NavigationCharacteristicsV3
                                    .forSurfaceFormatTypeCode(ftc, completedA, completedC));

                    assertSame(worst, NavigationCharacteristicsV3.forSurfaceFormatTypeCode(ftc, a, c),
                            "type code " + formatTypeCode + ", " + a + " / " + c);
                }
        }
    }

    /**
     * Version 3's rows for type codes 0 to 18 restate version 2's, because the two standards state
     * them separately and happen to agree. This is what keeps the restatement honest: every supplement
     * combination of every shared type code must give the same NIC and radius in both.
     */
    @Test
    void testAgreesWithVersion2WhereTheTablesShareRows() {
        for (int formatTypeCode : SHARED_WITH_V2) {
            byte ftc = (byte) formatTypeCode;

            for (NICSupplement first : NICSupplement.values())
                for (NICSupplement second : NICSupplement.values()) {
                    String where = "type code " + formatTypeCode + ", " + first + " / " + second;

                    if (SURFACE_TYPE_CODES.contains(formatTypeCode)) {
                        NavigationCharacteristicsV2 v2 =
                                NavigationCharacteristicsV2.forSurfaceFormatTypeCode(ftc, first, second);
                        NavigationCharacteristicsV3 v3 =
                                NavigationCharacteristicsV3.forSurfaceFormatTypeCode(ftc, first, second);

                        assertEquals(v2.getNIC(), v3.getNIC(), where);
                        assertEquals(v2.getContainmentRadius(), v3.getContainmentRadius(), where);
                    }

                    if (AIRBORNE_TYPE_CODES.contains(formatTypeCode)) {
                        NavigationCharacteristicsV2 v2 =
                                NavigationCharacteristicsV2.forAirborneFormatTypeCode(ftc, first, second);
                        NavigationCharacteristicsV3 v3 = NavigationCharacteristicsV3
                                .forAirborneFormatTypeCode(ftc, first, second, NICSupplementD.UNKNOWN);

                        assertEquals(v2.getNIC(), v3.getNIC(), where);
                        assertEquals(v2.getContainmentRadius(), v3.getContainmentRadius(), where);
                    }
                }
        }
    }

    /**
     * The three type codes the versions genuinely differ on: version 2 reads them from TABLE N-24,
     * version 3 grades them by supplement D.
     */
    @Test
    void testDiffersFromVersion2WhereSupplementDApplies() {
        assertEquals((byte) 11, v2Airborne(20).getNIC());
        assertEquals((byte) 8, airborne(20, NICSupplementD.UNKNOWN).getNIC());

        assertEquals((byte) 10, v2Airborne(21).getNIC());
        assertEquals((byte) 7, airborne(21, NICSupplementD.UNKNOWN).getNIC());

        assertEquals(ContainmentRadius.AT_LEAST_25, v2Airborne(22).getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_3704,
                airborne(22, NICSupplementD.UNKNOWN).getContainmentRadius());
    }

    /**
     * Supplement D grades monotonically, unlike the single-bit supplements: every step up reports a
     * smaller radius and a higher NIC. That is why an unknown D can take the {@code D = 0} row.
     */
    @Test
    void testSupplementDIsMonotone() {
        for (byte formatTypeCode : new byte[]{20, 22}) {
            NICSupplementD[] ascending = {NICSupplementD.ZERO, NICSupplementD.ONE, NICSupplementD.TWO,
                    NICSupplementD.THREE};

            for (int step = 1; step < ascending.length; step++) {
                NavigationCharacteristicsV3 lower = airborne(formatTypeCode, ascending[step - 1]);
                NavigationCharacteristicsV3 higher = airborne(formatTypeCode, ascending[step]);

                assertEquals(lower, worseOf(lower, higher), "type code " + formatTypeCode + " at " + step);
            }

            assertSame(airborne(formatTypeCode, NICSupplementD.ZERO),
                    airborne(formatTypeCode, NICSupplementD.UNKNOWN), "type code " + formatTypeCode);
        }
    }

    /** Supplements A and B do not reach the type codes supplement D grades. */
    @Test
    void testSupplementsAAndBAreNotConsultedWhereSupplementDApplies() {
        for (byte formatTypeCode : new byte[]{20, 21, 22}) {
            for (NICSupplementD d : NICSupplementD.values()) {
                NavigationCharacteristicsV3 expected = NavigationCharacteristicsV3.forAirborneFormatTypeCode(
                        formatTypeCode, NICSupplement.UNKNOWN, NICSupplement.UNKNOWN, d);

                for (NICSupplement a : NICSupplement.values())
                    for (NICSupplement b : NICSupplement.values())
                        assertSame(expected, NavigationCharacteristicsV3
                                        .forAirborneFormatTypeCode(formatTypeCode, a, b, d),
                                "type code " + formatTypeCode + ", D " + d);
            }
        }
    }

    /** Type code 21 defines only {@code D = 0}, so nothing there depends on the supplement. */
    @Test
    void testTypeCode21IgnoresSupplementD() {
        for (NICSupplementD d : NICSupplementD.values())
            assertSame(NavigationCharacteristicsV3.TYPE_CODE_21, airborne(21, d), "D " + d);
    }

    @Test
    void testEntryPointsRejectTheOtherSubtypesTypeCodes() {
        for (byte formatTypeCode = 0; formatTypeCode < 32; formatTypeCode++) {
            byte code = formatTypeCode;

            if (!AIRBORNE_TYPE_CODES.contains((int) code))
                assertThrows(IllegalArgumentException.class,
                        () -> NavigationCharacteristicsV3.forAirborneFormatTypeCode(code,
                                NICSupplement.UNKNOWN, NICSupplement.UNKNOWN, NICSupplementD.UNKNOWN));

            if (!SURFACE_TYPE_CODES.contains((int) code))
                assertThrows(IllegalArgumentException.class,
                        () -> NavigationCharacteristicsV3.forSurfaceFormatTypeCode(code,
                                NICSupplement.UNKNOWN, NICSupplement.UNKNOWN));
        }
    }

    @Test
    void testTableCoversExactlyThePositionTypeCodes() {
        Set<Integer> covered = Arrays.stream(NavigationCharacteristicsV3.values())
                .map(row -> (int) row.getFormatTypeCode())
                .collect(Collectors.toSet());

        Set<Integer> expected = new HashSet<>(SURFACE_TYPE_CODES);
        expected.addAll(AIRBORNE_TYPE_CODES);

        assertEquals(expected, covered);
    }

    /**
     * Every NIC sits on its own containment radius bucket. NIC 6 is the bucket the standard
     * subdivides, and NIC 0 reports no radius at all.
     */
    @Test
    void testEachNICPairsWithItsOwnRadius() {
        for (NavigationCharacteristicsV3 row : NavigationCharacteristicsV3.values()) {
            ContainmentRadius expected;
            switch (row.getNIC()) {
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
        assertEquals(ContainmentRadius.BELOW_7_5, airborne(20, NICSupplementD.THREE).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_25, airborne(20, NICSupplementD.TWO).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_75, airborne(20, NICSupplementD.ONE).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_185_2, airborne(20, NICSupplementD.ZERO).getContainmentRadius());

        assertEquals(ContainmentRadius.BELOW_370_4, airborne(21, NICSupplementD.ZERO).getContainmentRadius());

        assertEquals(ContainmentRadius.BELOW_1111_2, airborne(22, NICSupplementD.THREE).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_1852, airborne(22, NICSupplementD.TWO).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_3704, airborne(22, NICSupplementD.ONE).getContainmentRadius());

        // the row the old decoder could not express: -1 stood for this and for "nothing reported"
        assertEquals(ContainmentRadius.AT_LEAST_3704, airborne(22, NICSupplementD.ZERO).getContainmentRadius());
        assertEquals((byte) 0, airborne(22, NICSupplementD.ZERO).getNIC());
        assertNotEquals(ContainmentRadius.UNKNOWN, airborne(22, NICSupplementD.ZERO).getContainmentRadius());
    }

    private static NavigationCharacteristicsV3 airborne(int formatTypeCode, NICSupplementD nicSupplementD) {
        return NavigationCharacteristicsV3.forAirborneFormatTypeCode((byte) formatTypeCode,
                NICSupplement.UNKNOWN, NICSupplement.UNKNOWN, nicSupplementD);
    }

    private static NavigationCharacteristicsV2 v2Airborne(int formatTypeCode) {
        return NavigationCharacteristicsV2.forAirborneFormatTypeCode((byte) formatTypeCode,
                NICSupplement.UNKNOWN, NICSupplement.UNKNOWN);
    }
}
