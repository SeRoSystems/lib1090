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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NavigationCharacteristicsV0Test {

    /** The type codes that carry a position, and therefore the type codes the table must cover. */
    private static final Set<Integer> POSITION_TYPE_CODES =
            new HashSet<>(Arrays.asList(0, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22));

    @Test
    void testNamesMatchTheirTypeCodes() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values()) {
            String name = characteristics.name();
            byte expected = Byte.parseByte(name.substring("TYPE_CODE_".length()));

            assertEquals(expected, characteristics.getFormatTypeCode(), name);
        }
    }

    @Test
    void testTableCoversExactlyThePositionTypeCodes() {
        Set<Integer> covered = Arrays.stream(NavigationCharacteristicsV0.values())
                .map(characteristics -> (int) characteristics.getFormatTypeCode())
                .collect(Collectors.toSet());

        assertEquals(POSITION_TYPE_CODES, covered);
    }

    @Test
    void testLookupFindsEveryRow() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values())
            assertEquals(characteristics,
                    NavigationCharacteristicsV0.forFormatTypeCode(characteristics.getFormatTypeCode()));
    }

    /**
     * A type code that carries no position has no row, and asking for one is a caller's mistake
     * rather than a position of unknown quality.
     */
    @Test
    void testLookupRejectsTypeCodesWithoutPosition() {
        for (byte formatTypeCode = 0; formatTypeCode < 32; formatTypeCode++) {
            if (POSITION_TYPE_CODES.contains((int) formatTypeCode))
                continue;

            byte rejected = formatTypeCode;
            assertThrows(IllegalArgumentException.class,
                    () -> NavigationCharacteristicsV0.forFormatTypeCode(rejected));
        }
    }

    /**
     * NIC and NACp agree in every row of the merged table. This is a coincidence of how ED-102B
     * tabulates version 0 and not an identity — the two describe integrity and accuracy — so the
     * assertion guards the transcription, and must not be read as license to compute one from the
     * other.
     */
    @Test
    void testNICAndNACpCoincideInEveryRow() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values())
            assertEquals(characteristics.getNACpEncoded(), characteristics.getNICEncoded(), characteristics.name());
    }

    /**
     * NUCp is not a key into this table: type codes 8 and 12 report the same NUCp but differ in
     * everything else, which is why nothing may be derived from a NUCp value.
     */
    @Test
    void testNUCpDoesNotIdentifyARow() {
        NavigationCharacteristicsV0 surface = NavigationCharacteristicsV0.TYPE_CODE_8;
        NavigationCharacteristicsV0 airborne = NavigationCharacteristicsV0.TYPE_CODE_12;

        assertEquals(surface.getNUCpEncoded(), airborne.getNUCpEncoded());
        assertEquals((byte) 0, surface.getNACpEncoded());
        assertEquals((byte) 7, airborne.getNACpEncoded());
    }

    @Test
    void testOnlyTypeCode22HasNoNUCp() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values()) {
            if (characteristics == NavigationCharacteristicsV0.TYPE_CODE_22)
                assertNull(characteristics.getNUCpEncoded(), characteristics.name());
            else
                assertNotNull(characteristics.getNUCpEncoded(), characteristics.name());
        }
    }

    /**
     * The three rows the old {@code -1} containment radius collapsed into one value: no position at
     * all, and the two type codes that bound the radius from below.
     */
    @Test
    void testRowsWithoutAGuaranteedContainmentRadius() {
        assertEquals(ContainmentRadius.UNKNOWN,
                NavigationCharacteristicsV0.TYPE_CODE_0.getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_185_2,
                NavigationCharacteristicsV0.TYPE_CODE_8.getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_37040,
                NavigationCharacteristicsV0.TYPE_CODE_18.getContainmentRadius());
        assertEquals(ContainmentRadius.AT_LEAST_25,
                NavigationCharacteristicsV0.TYPE_CODE_22.getContainmentRadius());
    }

    /**
     * A type code reports SIL 2 exactly when it guarantees a containment radius at all. The two
     * columns are tabulated independently, and the surface type code 8 row is where they most
     * recently disagreed.
     */
    @Test
    void testSILAgreesWithTheContainmentRadiusBound() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values()) {
            boolean guaranteesRadius =
                    characteristics.getContainmentRadius().getBound() == ContainmentRadius.Bound.UPPER;

            assertEquals(guaranteesRadius ? (byte) 2 : (byte) 0, characteristics.getSILEncoded(),
                    characteristics.name());
            assertEquals(guaranteesRadius
                            ? SourceIntegrityLevel.AT_MOST_1E_MINUS_5
                            : SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3,
                    characteristics.getSourceIntegrityLevel(), characteristics.name());
        }
    }

    /** The two SIL accessors are one column read two ways, so they may never disagree. */
    @Test
    void testTheSILCodeAndWhatItGuaranteesAgree() {
        for (NavigationCharacteristicsV0 characteristics : NavigationCharacteristicsV0.values())
            assertEquals(SourceIntegrityLevel.forSIL(characteristics.getSILEncoded()),
                    characteristics.getSourceIntegrityLevel(), characteristics.name());
    }

    @Test
    void testSpotChecksAgainstTheStandardTable() {
        NavigationCharacteristicsV0 typeCode13 = NavigationCharacteristicsV0.forFormatTypeCode((byte) 13);

        assertEquals((byte) 5, typeCode13.getNUCpEncoded());
        assertEquals((byte) 6, typeCode13.getNACpEncoded());
        assertEquals((byte) 6, typeCode13.getNICEncoded());
        assertEquals(SourceIntegrityLevel.AT_MOST_1E_MINUS_5, typeCode13.getSourceIntegrityLevel());
        assertEquals(ContainmentRadius.BELOW_926, typeCode13.getContainmentRadius());

        NavigationCharacteristicsV0 typeCode16 = NavigationCharacteristicsV0.forFormatTypeCode((byte) 16);

        assertEquals((byte) 2, typeCode16.getNUCpEncoded());
        assertEquals((byte) 1, typeCode16.getNACpEncoded());
        assertEquals(ContainmentRadius.BELOW_18520, typeCode16.getContainmentRadius());
    }

    /**
     * The surface type codes used to have their own format-type-code-to-EPU switch, which the NACp
     * column plus {@link EstimatedPositionUncertainty} now replace. The answers must be the ones that
     * switch gave, except for type code 8, where it reported an infinite upper bound and the category
     * it maps to turns out to mean "unknown accuracy" as much as "poor accuracy".
     */
    @Test
    void testTheSurfaceTypeCodesStillGiveTheirTabulatedAccuracy() {
        assertEquals(EstimatedPositionUncertainty.BELOW_3, epuForTypeCode(5));
        assertEquals(EstimatedPositionUncertainty.BELOW_10, epuForTypeCode(6));
        assertEquals(EstimatedPositionUncertainty.BELOW_92_6, epuForTypeCode(7));

        assertEquals(EstimatedPositionUncertainty.UNKNOWN_OR_AT_LEAST_18520, epuForTypeCode(8));
        assertFalse(epuForTypeCode(8).isKnown());
    }

    private static EstimatedPositionUncertainty epuForTypeCode(int formatTypeCode) {
        return EstimatedPositionUncertainty.forNACp(
                NavigationCharacteristicsV0.forFormatTypeCode((byte) formatTypeCode).getNACpEncoded());
    }
}
