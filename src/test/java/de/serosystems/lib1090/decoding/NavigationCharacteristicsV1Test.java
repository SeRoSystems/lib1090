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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationCharacteristicsV1Test {

    /** Builds the supplements version 1 reads: A alone. */
    private static NICSupplements supplements(NICSupplement nicSupplementA) {
        return NICSupplements.none().withA(nicSupplementA);
    }

    /** The type codes that carry a position, and therefore the type codes the table must cover. */
    private static final Set<Integer> POSITION_TYPE_CODES =
            new HashSet<>(Arrays.asList(0, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22));

    private static NavigationCharacteristicsV1 lookup(byte formatTypeCode, NICSupplement nicSupplement) {
        return NavigationCharacteristicsV1.forFormatTypeCode(formatTypeCode, supplements(nicSupplement));
    }

    @Test
    void testNamesMatchTheirTypeCodeAndSupplement() {
        for (NavigationCharacteristicsV1 row : NavigationCharacteristicsV1.values()) {
            String name = row.name();
            String[] parts = name.substring("TYPE_CODE_".length()).split("_SUPPLEMENT_");

            assertEquals(Byte.parseByte(parts[0]), row.getFormatTypeCode(), name);

            if (parts.length == 2) {
                NICSupplement supplement = parts[1].equals("SET") ? NICSupplement.SET : NICSupplement.CLEAR;
                assertSame(row, NavigationCharacteristicsV1.forFormatTypeCode(row.getFormatTypeCode(), supplements(supplement)),
                        name);
            }
        }
    }

    @Test
    void testTableCoversExactlyThePositionTypeCodes() {
        Set<Integer> covered = Arrays.stream(NavigationCharacteristicsV1.values())
                .map(row -> (int) row.getFormatTypeCode())
                .collect(Collectors.toSet());

        assertEquals(POSITION_TYPE_CODES, covered);
    }

    @Test
    void testLookupRejectsTypeCodesWithoutPosition() {
        for (byte formatTypeCode = 0; formatTypeCode < 32; formatTypeCode++) {
            if (POSITION_TYPE_CODES.contains((int) formatTypeCode))
                continue;

            byte rejected = formatTypeCode;
            for (NICSupplement supplement : NICSupplement.values())
                assertThrows(IllegalArgumentException.class,
                        () -> NavigationCharacteristicsV1.forFormatTypeCode(rejected, supplements(supplement)));
        }
    }

    /**
     * A type code the supplement does not touch answers the same row whatever is known, because the
     * supplement describes the aircraft and such an aircraft still sends these type codes.
     */
    @Test
    void testTypeCodesIndependentOfTheSupplement() {
        for (byte formatTypeCode : new byte[]{0, 5, 6, 8, 9, 10, 12, 14, 15, 17, 18, 20, 21, 22}) {
            NavigationCharacteristicsV1 unknown = lookup(formatTypeCode, NICSupplement.UNKNOWN);

            assertSame(unknown, lookup(formatTypeCode, NICSupplement.SET));
            assertSame(unknown, lookup(formatTypeCode, NICSupplement.CLEAR));
        }
    }

    @Test
    void testTypeCodesRefinedByTheSupplement() {
        for (byte formatTypeCode : new byte[]{7, 11, 13, 16})
            assertNotEquals(lookup(formatTypeCode, NICSupplement.SET), lookup(formatTypeCode, NICSupplement.CLEAR),
                    "type code " + formatTypeCode);
    }

    /**
     * The supplement's polarity is not constant: a clear supplement is the poorer report at type
     * codes 7, 11 and 16, but the better one at 13. No constant assumption is conservative, which is
     * why an unknown supplement needs its own lookup rather than a default value.
     */
    @Test
    void testTheSupplementHasNoConstantPolarity() {
        for (byte formatTypeCode : new byte[]{7, 11, 16})
            assertSame(lookup(formatTypeCode, NICSupplement.CLEAR), lookup(formatTypeCode, NICSupplement.UNKNOWN),
                    "clear is the worst case at type code " + formatTypeCode);

        assertSame(NavigationCharacteristicsV1.TYPE_CODE_13_SUPPLEMENT_SET,
                lookup((byte) 13, NICSupplement.UNKNOWN));
        assertEquals(ContainmentRadius.BELOW_1111_2,
                lookup((byte) 13, NICSupplement.UNKNOWN).getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_926,
                lookup((byte) 13, NICSupplement.CLEAR).getContainmentRadius());
    }

    /**
     * An unknown supplement never reports better than the truth, whichever value the transmitter
     * actually meant.
     */
    @Test
    void testUnknownIsNeverBetterThanEitherKnownValue() {
        for (int formatTypeCode : POSITION_TYPE_CODES) {
            byte ftc = (byte) formatTypeCode;
            NavigationCharacteristicsV1 unknown = lookup(ftc, NICSupplement.UNKNOWN);

            for (NICSupplement supplement : new NICSupplement[]{NICSupplement.SET, NICSupplement.CLEAR}) {
                NavigationCharacteristicsV1 known = lookup(ftc, supplement);
                String where = "type code " + formatTypeCode + ", supplement " + supplement;

                assertTrue(unknown.getNICEncoded() <= known.getNICEncoded(), where);
                assertEquals(ContainmentRadius.worseOf(unknown.getContainmentRadius(),
                        known.getContainmentRadius()), unknown.getContainmentRadius(), where);
            }
        }
    }

    /**
     * Picking the worst row by NIC and picking it by containment radius agree everywhere, which is
     * what lets one row answer both questions. Should a future table break this, the worst row stops
     * being well-defined and the lookup needs revisiting rather than silently mixing two rows.
     */
    @Test
    void testWorstByNICAgreesWithWorstByContainmentRadius() {
        for (int formatTypeCode : POSITION_TYPE_CODES) {
            List<NavigationCharacteristicsV1> rows = new ArrayList<>();
            for (NavigationCharacteristicsV1 row : NavigationCharacteristicsV1.values())
                if (row.getFormatTypeCode() == formatTypeCode)
                    rows.add(row);

            byte worstNIC = rows.stream().map(NavigationCharacteristicsV1::getNICEncoded)
                    .min(Byte::compare).orElseThrow(AssertionError::new);
            ContainmentRadius worstRadius = rows.stream().map(NavigationCharacteristicsV1::getContainmentRadius)
                    .reduce(ContainmentRadius::worseOf).orElseThrow(AssertionError::new);

            NavigationCharacteristicsV1 worst = lookup((byte) formatTypeCode, NICSupplement.UNKNOWN);

            assertEquals(worstNIC, worst.getNICEncoded(), "type code " + formatTypeCode);
            assertEquals(worstRadius, worst.getContainmentRadius(), "type code " + formatTypeCode);
        }
    }

    /**
     * Every NIC sits on its own containment radius bucket, so a row pairing them inconsistently shows
     * up here. NIC 6 is the one bucket version 1 subdivides, and NIC 0 reports no radius at all.
     */
    @Test
    void testEachNICPairsWithItsOwnRadius() {
        for (NavigationCharacteristicsV1 row : NavigationCharacteristicsV1.values()) {
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

        assertEquals(ContainmentRadius.BELOW_926,
                NavigationCharacteristicsV1.TYPE_CODE_13_SUPPLEMENT_CLEAR.getContainmentRadius());
        assertEquals(ContainmentRadius.BELOW_1111_2,
                NavigationCharacteristicsV1.TYPE_CODE_13_SUPPLEMENT_SET.getContainmentRadius());
    }

    @Test
    void testSpotChecksAgainstTheStandardTable() {
        assertEquals((byte) 9, lookup((byte) 7, NICSupplement.SET).getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_75,
                lookup((byte) 7, NICSupplement.SET).getContainmentRadius());

        assertEquals((byte) 8, lookup((byte) 11, NICSupplement.CLEAR).getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_185_2,
                lookup((byte) 11, NICSupplement.CLEAR).getContainmentRadius());

        assertEquals((byte) 2, lookup((byte) 16, NICSupplement.CLEAR).getNICEncoded());
        assertEquals((byte) 3, lookup((byte) 16, NICSupplement.SET).getNICEncoded());

        assertEquals(ContainmentRadius.AT_LEAST_185_2,
                lookup((byte) 8, NICSupplement.UNKNOWN).getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_37040,
                lookup((byte) 18, NICSupplement.UNKNOWN).getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_25,
                lookup((byte) 22, NICSupplement.UNKNOWN).getContainmentRadius());
        assertEquals(ContainmentRadius.UNKNOWN,
                lookup((byte) 0, NICSupplement.UNKNOWN).getContainmentRadius());
    }
}
