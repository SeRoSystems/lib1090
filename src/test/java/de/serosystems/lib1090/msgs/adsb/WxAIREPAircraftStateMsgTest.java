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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WxAIREPAircraftStateMsgTest {

    /**
     * A Wx AIREP aircraft state message (TYPE Code 26, subtype 0) with the given wingspan code in ME 48-55, as
     * ADS-B (DF=17) or ADS-R (DF=18, CF=6).
     */
    private static String message(int wingspanCode, boolean adsr) {
        long me = 26L << 51 | (long) wingspanCode << 1;
        return (adsr ? "96" : "8D") + "4840D6" + String.format("%014X", me) + "000000";
    }

    private static Interval adsb(int code) throws Exception {
        return new WxAIREPAircraftStateMsg(message(code, false)).getWingspan();
    }

    private static Interval adsr(int code) throws Exception {
        return new de.serosystems.lib1090.msgs.adsr.WxAIREPAircraftStateMsg(message(code, true)).getWingspan();
    }

    /**
     * ED-102B §2.2.3.2.7.6.3.5: code 1 is any wingspan below 6 ft, code 255 any of 387.018 ft or more, and each
     * code in between the interval from its lower end up to the next code's.
     */
    @Test
    void wingspanCodesAreIntervals() throws Exception {
        assertNull(new WxAIREPAircraftStateMsg(message(0, false)).getWingspan());
        for (boolean isADSR : new boolean[]{false, true}) {
            Interval one = isADSR ? adsr(1) : adsb(1);
            assertEquals(Interval.of(Bound.AT_LEAST, 0, Bound.BELOW, 6), one);

            Interval two = isADSR ? adsr(2) : adsb(2);
            assertEquals(Bound.AT_LEAST, two.getLowerBound());
            assertEquals(6, two.getLower(), 1e-9);
            assertEquals(Bound.BELOW, two.getUpperBound());
            assertEquals(7.002, two.getUpper(), 1e-9);

            Interval top = isADSR ? adsr(255) : adsb(255);
            assertEquals(Bound.AT_LEAST, top.getLowerBound());
            assertEquals(387.018, top.getLower(), 1e-9);
            assertEquals(Bound.NONE, top.getUpperBound());
        }
    }

    /**
     * Encoding a wingspan with the standard's formula and decoding the code gives an interval containing it.
     */
    @Test
    void decodedIntervalContainsTheEncodedWingspan() throws Exception {
        for (double wingspan = 0.5; wingspan < 500; wingspan += 0.73) {
            Interval decoded = adsb(encode(wingspan));
            assertTrue(decoded.getLower() <= wingspan, wingspan + " in " + decoded);
            assertTrue(decoded.getGuaranteedUpperBound() > wingspan, wingspan + " in " + decoded);
        }
    }

    /**
     * ED-102B §2.2.3.2.7.6.3.5 (1) to (3).
     */
    private static int encode(double wingspan) {
        int n = (int) Math.floor(2 + (Math.sqrt(wingspan * 0.008 + 0.952) - 1) / 0.004);
        return Math.max(1, Math.min(255, n));
    }

}
