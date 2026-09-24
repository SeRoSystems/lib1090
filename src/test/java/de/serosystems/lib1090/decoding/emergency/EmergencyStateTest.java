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

package de.serosystems.lib1090.decoding.emergency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmergencyStateTest {

    /**
     * Every value the three-bit field can carry names a constant, and the one it names carries that
     * value — which is what lets {@code forEncoded} index the constants by declaration order. Anything
     * else is a caller's mistake.
     */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte encoded = 0; encoded <= 7; encoded++) {
            assertEquals(encoded, EmergencyStateV0.forEncoded(encoded).getEncoded(), "v0 " + encoded);
            assertEquals(encoded, EmergencyStateV1V2.forEncoded(encoded).getEncoded(), "v1/v2 " + encoded);
            assertEquals(encoded, EmergencyStateV3.forEncoded(encoded).getEncoded(), "v3 " + encoded);
        }

        assertThrows(IllegalArgumentException.class, () -> EmergencyStateV0.forEncoded((byte) 8));
        assertThrows(IllegalArgumentException.class, () -> EmergencyStateV1V2.forEncoded((byte) -1));
        assertThrows(IllegalArgumentException.class, () -> EmergencyStateV3.forEncoded((byte) 8));
    }

    /** ED-102B §N.2.3.4 TABLE N-7. */
    @Test
    void testVersion0Mapping() {
        EmergencyStateV3[] reported = {
                EmergencyStateV3.NO_REPORTED_EMERGENCY,
                EmergencyStateV3.GENERAL_EMERGENCY,
                EmergencyStateV3.GENERAL_EMERGENCY,
                EmergencyStateV3.MINIMUM_FUEL,
                EmergencyStateV3.NO_COMMUNICATIONS,
                EmergencyStateV3.UNLAWFUL_INTERFERENCE,
                EmergencyStateV3.NO_REPORTED_EMERGENCY,
                EmergencyStateV3.NO_REPORTED_EMERGENCY,
        };

        for (byte encoded = 0; encoded <= 7; encoded++)
            assertSame(reported[encoded], EmergencyStateV0.forEncoded(encoded).getReported(), "v0 " + encoded);
    }

    /** ED-102B §N.3.3.4 TABLE N-19, which this library applies to version 2 as well. */
    @Test
    void testVersion1And2Mapping() {
        EmergencyStateV3[] reported = {
                EmergencyStateV3.NO_REPORTED_EMERGENCY,
                EmergencyStateV3.GENERAL_EMERGENCY,
                EmergencyStateV3.GENERAL_EMERGENCY,
                EmergencyStateV3.MINIMUM_FUEL,
                EmergencyStateV3.NO_COMMUNICATIONS,
                EmergencyStateV3.UNLAWFUL_INTERFERENCE,
                EmergencyStateV3.GENERAL_EMERGENCY,
                EmergencyStateV3.NO_REPORTED_EMERGENCY,
        };

        for (byte encoded = 0; encoded <= 7; encoded++)
            assertSame(reported[encoded], EmergencyStateV1V2.forEncoded(encoded).getReported(), "v1/v2 " + encoded);
    }

    @Test
    void testVersion3ReportsItself() {
        for (EmergencyStateV3 state : EmergencyStateV3.values())
            assertSame(state, state.getReported());
    }

    /** The value whose meaning differs in every version, and is why version 0 and 1 need separate tables. */
    @Test
    void testCode6() {
        byte six = 6;

        assertEquals("Reserved", EmergencyStateV0.forEncoded(six).getText());
        assertSame(EmergencyStateV3.NO_REPORTED_EMERGENCY, EmergencyStateV0.forEncoded(six).getReported());

        assertEquals("Downed Aircraft", EmergencyStateV1V2.forEncoded(six).getText());
        assertSame(EmergencyStateV3.GENERAL_EMERGENCY, EmergencyStateV1V2.forEncoded(six).getReported());

        assertEquals("Aircraft in Distress - Automatic Activation", EmergencyStateV3.forEncoded(six).getText());
    }
}
