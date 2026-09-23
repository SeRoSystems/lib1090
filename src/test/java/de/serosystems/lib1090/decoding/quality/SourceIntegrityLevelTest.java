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

class SourceIntegrityLevelTest {

    @Test
    void testTheTabulatedLevels() {
        assertSame(SourceIntegrityLevel.AT_MOST_1E_MINUS_3, SourceIntegrityLevel.forSIL((byte) 1));
        assertSame(SourceIntegrityLevel.AT_MOST_1E_MINUS_5, SourceIntegrityLevel.forSIL((byte) 2));
        assertSame(SourceIntegrityLevel.AT_MOST_1E_MINUS_7, SourceIntegrityLevel.forSIL((byte) 3));

        assertEquals(1e-3, SourceIntegrityLevel.AT_MOST_1E_MINUS_3.getGuaranteedUpperBound());
        assertEquals(1e-5, SourceIntegrityLevel.AT_MOST_1E_MINUS_5.getGuaranteedUpperBound());
        assertEquals(1e-7, SourceIntegrityLevel.AT_MOST_1E_MINUS_7.getGuaranteedUpperBound());
    }

    /**
     * Level 0 reads "unknown or more than 10^-3" in ED-102B — one level for two different facts a
     * receiver cannot separate, so nothing is guaranteed.
     */
    @Test
    void testLevelZeroGuaranteesNothing() {
        assertSame(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3, SourceIntegrityLevel.forSIL((byte) 0));

        assertFalse(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3.isKnown());
        assertTrue(Double.isNaN(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3.getGuaranteedUpperBound()));
    }

    /**
     * The point of reporting {@code NaN} where nothing is guaranteed: "is this position known to be
     * more trustworthy than the limit?" answers false without the caller special-casing anything — and
     * so does the reverse question, which is right, since the level may only mean the transmitter does
     * not know.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 1e-4;

        assertTrue(SourceIntegrityLevel.AT_MOST_1E_MINUS_5.getGuaranteedUpperBound() < limit);
        assertFalse(SourceIntegrityLevel.AT_MOST_1E_MINUS_3.getGuaranteedUpperBound() < limit);
        assertFalse(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3.getGuaranteedUpperBound() < limit);
        assertFalse(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3.getGuaranteedUpperBound() > limit);
    }

    /** Every value the two-bit field can carry names a level; anything else is a caller's mistake. */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte sil = 0; sil <= 3; sil++)
            assertNotEquals(null, SourceIntegrityLevel.forSIL(sil), "SIL " + sil);

        assertThrows(IllegalArgumentException.class, () -> SourceIntegrityLevel.forSIL((byte) 4));
        assertThrows(IllegalArgumentException.class, () -> SourceIntegrityLevel.forSIL((byte) -1));
    }

    /** Each constant is named after what it guarantees, so a transcription slip shows up here. */
    @Test
    void testNamesMatchTheirBound() {
        for (SourceIntegrityLevel level : SourceIntegrityLevel.values()) {
            if (!level.isKnown())
                continue;

            double expected = Double.parseDouble(
                    level.name().substring("AT_MOST_".length()).replace("E_MINUS_", "E-"));
            assertEquals(expected, level.getGuaranteedUpperBound(), level.name());
        }
    }

    /** A higher level is a stronger guarantee, which the table's ordering should reflect. */
    @Test
    void testHigherLevelsGuaranteeMore() {
        for (byte sil = 2; sil <= 3; sil++)
            assertTrue(SourceIntegrityLevel.forSIL(sil).getGuaranteedUpperBound()
                            < SourceIntegrityLevel.forSIL((byte) (sil - 1)).getGuaranteedUpperBound(),
                    "SIL " + sil);
    }
}
