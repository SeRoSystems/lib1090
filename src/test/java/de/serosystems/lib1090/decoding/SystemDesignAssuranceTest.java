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

import de.serosystems.lib1090.decoding.SystemDesignAssurance.DesignAssuranceLevel;
import de.serosystems.lib1090.decoding.SystemDesignAssurance.SupportedFailureCondition;
import de.serosystems.lib1090.decoding.SystemDesignAssurance.UndetectedFaultProbability;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemDesignAssuranceTest {

    /** The three columns of TABLE 2-58, spot-checked row by row. */
    @Test
    void testTheTabulatedRows() {
        assertSame(SupportedFailureCondition.MINOR,
                SystemDesignAssurance.forSDA((byte) 1).getSupportedFailureCondition());
        assertSame(UndetectedFaultProbability.AT_MOST_1E_MINUS_3,
                SystemDesignAssurance.forSDA((byte) 1).getUndetectedFaultProbability());
        assertSame(DesignAssuranceLevel.LEVEL_D,
                SystemDesignAssurance.forSDA((byte) 1).getDesignAssuranceLevel());

        assertSame(SupportedFailureCondition.MAJOR,
                SystemDesignAssurance.forSDA((byte) 2).getSupportedFailureCondition());
        assertSame(UndetectedFaultProbability.AT_MOST_1E_MINUS_5,
                SystemDesignAssurance.forSDA((byte) 2).getUndetectedFaultProbability());
        assertSame(DesignAssuranceLevel.LEVEL_C,
                SystemDesignAssurance.forSDA((byte) 2).getDesignAssuranceLevel());

        assertSame(SupportedFailureCondition.HAZARDOUS,
                SystemDesignAssurance.forSDA((byte) 3).getSupportedFailureCondition());
        assertSame(UndetectedFaultProbability.AT_MOST_1E_MINUS_7,
                SystemDesignAssurance.forSDA((byte) 3).getUndetectedFaultProbability());
        assertSame(DesignAssuranceLevel.LEVEL_B,
                SystemDesignAssurance.forSDA((byte) 3).getDesignAssuranceLevel());
    }

    /**
     * Row 0 reads "Unknown / No safety effect", "&gt; 10^-3 per flight hour or unknown" and "N/A" —
     * an installation that says nothing looks exactly like one assessed as harmless, so no column may
     * claim anything.
     */
    @Test
    void testTheLowestRowClaimsNothing() {
        SystemDesignAssurance nothing = SystemDesignAssurance.forSDA((byte) 0);

        assertSame(SupportedFailureCondition.UNKNOWN_OR_NO_SAFETY_EFFECT,
                nothing.getSupportedFailureCondition());
        assertSame(DesignAssuranceLevel.NOT_APPLICABLE, nothing.getDesignAssuranceLevel());
        assertFalse(nothing.getUndetectedFaultProbability().isKnown());
        assertTrue(Double.isNaN(nothing.getUndetectedFaultProbability().getGuaranteedUpperBound()));
    }

    /**
     * The point of reporting {@code NaN} where nothing is guaranteed: "is this equipment known to
     * fault less often than the limit?" answers false without the caller special-casing anything —
     * and so does the reverse question, the row possibly meaning nothing was reported.
     */
    @Test
    void testNothingIsKnownToBeBetterThanALimitUnlessItIs() {
        double limit = 1e-4;

        assertTrue(UndetectedFaultProbability.AT_MOST_1E_MINUS_5.getGuaranteedUpperBound() < limit);
        assertFalse(UndetectedFaultProbability.AT_MOST_1E_MINUS_3.getGuaranteedUpperBound() < limit);
        assertFalse(UndetectedFaultProbability.UNKNOWN_OR_ABOVE_1E_MINUS_3.getGuaranteedUpperBound() < limit);
        assertFalse(UndetectedFaultProbability.UNKNOWN_OR_ABOVE_1E_MINUS_3.getGuaranteedUpperBound() > limit);
    }

    /** Every value the two-bit field can carry names a row; anything else is a caller's mistake. */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte sda = 0; sda <= 3; sda++)
            assertNotEquals(null, SystemDesignAssurance.forSDA(sda), "SDA " + sda);

        assertThrows(IllegalArgumentException.class, () -> SystemDesignAssurance.forSDA((byte) 4));
        assertThrows(IllegalArgumentException.class, () -> SystemDesignAssurance.forSDA((byte) -1));
    }

    /**
     * The columns advance together — a higher value supports a worse failure condition, guarantees a
     * smaller fault probability and names a more rigorous assurance level — so a row transcribed from
     * the wrong line of the table shows up as a column out of step.
     */
    @Test
    void testTheColumnsAdvanceTogether() {
        SystemDesignAssurance[] rows = {
                SystemDesignAssurance.forSDA((byte) 0), SystemDesignAssurance.forSDA((byte) 1),
                SystemDesignAssurance.forSDA((byte) 2), SystemDesignAssurance.forSDA((byte) 3)};

        for (int sda = 0; sda < rows.length; sda++) {
            assertEquals(sda, rows[sda].getSupportedFailureCondition().ordinal(), "failure condition " + sda);
            assertEquals(sda, rows[sda].getUndetectedFaultProbability().ordinal(), "probability " + sda);
            assertEquals(sda, rows[sda].getDesignAssuranceLevel().ordinal(), "assurance level " + sda);
        }

        for (byte sda = 2; sda <= 3; sda++)
            assertTrue(rows[sda].getUndetectedFaultProbability().getGuaranteedUpperBound()
                            < rows[sda - 1].getUndetectedFaultProbability().getGuaranteedUpperBound(),
                    "SDA " + sda);
    }

    /**
     * The fault probability tabulates the same four figures as the source integrity level and means
     * something else by them: how often the equipment transmits something false without noticing,
     * against how often a position falls outside its containment radius. Separate types, so that
     * neither can be read for the other.
     */
    @Test
    void testTheFaultProbabilityIsNotTheSourceIntegrityLevel() {
        assertNotEquals(SourceIntegrityLevel.AT_MOST_1E_MINUS_5,
                (Object) UndetectedFaultProbability.AT_MOST_1E_MINUS_5);

        assertEquals(SourceIntegrityLevel.AT_MOST_1E_MINUS_5.getGuaranteedUpperBound(),
                UndetectedFaultProbability.AT_MOST_1E_MINUS_5.getGuaranteedUpperBound());
    }

    /** Each constant is named after what it guarantees, so a transcription slip shows up here. */
    @Test
    void testNamesMatchTheirBound() {
        for (UndetectedFaultProbability probability : UndetectedFaultProbability.values()) {
            if (!probability.isKnown())
                continue;

            double expected = Double.parseDouble(
                    probability.name().substring("AT_MOST_".length()).replace("E_MINUS_", "E-"));
            assertEquals(expected, probability.getGuaranteedUpperBound(), probability.name());
        }
    }
}
