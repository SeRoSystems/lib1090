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

import de.serosystems.lib1090.msgs.adsb.EmergencyOrPriorityStatusV3Msg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyOrPriorityStatusTest {

    /**
     * ED-102B TABLE 2-100 and TABLE 2-101: code 126 is (0.840, 0.850], and code 127, any EDR above 0.850, can be
     * told apart from it.
     */
    @Test
    void edrCodesAreIntervals() {
        assertNull(EmergencyOrPriorityStatus.edr(0));
        assertEquals(Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 0.002), EmergencyOrPriorityStatus.edr(1));
        assertInterval(0.018, 0.020, EmergencyOrPriorityStatus.edr(10));
        assertInterval(0.020, 0.025, EmergencyOrPriorityStatus.edr(11));
        assertInterval(0.345, 0.350, EmergencyOrPriorityStatus.edr(76));
        assertInterval(0.350, 0.360, EmergencyOrPriorityStatus.edr(77));
        assertInterval(0.840, 0.850, EmergencyOrPriorityStatus.edr(126));
        assertEquals(Interval.of(Bound.MORE_THAN, 0.850, Bound.NONE, Double.NaN), EmergencyOrPriorityStatus.edr(127));
    }

    /**
     * Encoding an EDR as TABLE 2-100 says, rounding up, and decoding the code gives an interval containing it.
     */
    @Test
    void decodedEdrIntervalContainsTheEncodedEdr() {
        for (double edr = 0.0005; edr < 1; edr += 0.00317) {
            Interval decoded = EmergencyOrPriorityStatus.edr(encodeEdr(edr));
            assertTrue(decoded.getGuaranteedLowerBound() <= edr, edr + " in " + decoded);
            assertTrue(edr <= decoded.getGuaranteedUpperBound() + 1e-12, edr + " in " + decoded);
        }
    }

    private static int encodeEdr(double edr) {
        if (edr <= 0.002) return 1;
        if (edr <= 0.020) return (int) Math.ceil(edr / 0.002 - 1e-9);
        if (edr <= 0.350) return (int) Math.ceil(10 + (edr - 0.020) / 0.005 - 1e-9);
        if (edr <= 0.850) return (int) Math.ceil(76 + (edr - 0.350) / 0.010 - 1e-9);
        return 127;
    }

    /**
     * ED-102B TABLE 2-102: code n is (-7.5 (n + 1), -7.5 n], code 0 ending at 0 rather than -0.
     */
    @Test
    void peakEdrOffsetCodesAreIntervals() {
        Interval zero = EmergencyOrPriorityStatus.peakEdrOffset(0);
        assertEquals(-7.5, zero.getLower());
        assertEquals(0, Double.compare(0.0, zero.getUpper()), "0, not -0");
        assertInterval(-60, -52.5, EmergencyOrPriorityStatus.peakEdrOffset(7));
    }

    /**
     * ED-102B TABLE 2-103: code 1 is below 0.00001, code n below that [0.00001 (n - 1), 0.00001 n), and code 4095
     * 0.04094 or more.
     */
    @Test
    void waterVaporCodesAreIntervals() {
        assertNull(EmergencyOrPriorityStatus.waterVapor(0));
        assertEquals(Interval.of(Bound.AT_LEAST, 0, Bound.BELOW, 1e-5), EmergencyOrPriorityStatus.waterVapor(1));
        assertEquals(Interval.of(Bound.AT_LEAST, 0.04093, Bound.BELOW, 0.04094),
                EmergencyOrPriorityStatus.waterVapor(4094));
        assertEquals(Interval.of(Bound.AT_LEAST, 0.04094, Bound.NONE, Double.NaN),
                EmergencyOrPriorityStatus.waterVapor(4095));
    }

    /**
     * The issue's example messages: mean and peak EDR at code 126, then at code 127.
     */
    @Test
    void messagesTellCode126FromCode127() throws Exception {
        EmergencyOrPriorityStatusV3Msg at126 = new EmergencyOrPriorityStatusV3Msg("8D4B1A2CE100007EFC0000BE85AE");
        EmergencyOrPriorityStatusV3Msg at127 = new EmergencyOrPriorityStatusV3Msg("8D4B1A2CE100007FFE00008ED07F");
        assertInterval(0.840, 0.850, at126.getMeanEdr());
        assertInterval(0.840, 0.850, at126.getPeakEdr());
        assertEquals(Bound.NONE, at127.getMeanEdr().getUpperBound());
        assertEquals(Bound.NONE, at127.getPeakEdr().getUpperBound());
    }

    /**
     * An upper-inclusive interval (lower, upper], compared with a tolerance for the decimal steps.
     */
    private static void assertInterval(double lower, double upper, Interval actual) {
        assertEquals(Bound.MORE_THAN, actual.getLowerBound(), String.valueOf(actual));
        assertEquals(lower, actual.getLower(), 1e-12, String.valueOf(actual));
        assertEquals(Bound.AT_MOST, actual.getUpperBound(), String.valueOf(actual));
        assertEquals(upper, actual.getUpper(), 1e-12, String.valueOf(actual));
    }

}
