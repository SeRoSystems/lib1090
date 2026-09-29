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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HVAMsgTest {

    /**
     * An HVA message (TYPE Code 25) of the given subtype, ME bits 6-7, with the given bits in ME bits 8-56.
     */
    private static String message(int subtype, long fields) {
        long me = 25L << 51 | (long) subtype << 49 | fields;
        return "8D4B1A2C" + String.format("%014X", me) + "000000";
    }

    /**
     * ED-102B TABLE 2-82: PIC 1 to 14 report a containment radius, PIC 0 ("RC unknown") and the reserved PIC 15
     * do not.
     */
    @Test
    void hasPICOnlyWhereAContainmentRadiusIsReported() throws Exception {
        for (int pic = 0; pic < 16; pic++) {
            HVAVelocityMsg velocity = new HVAVelocityMsg(message(1, (long) pic << 35)); // PIC in ME 18-21
            assertEquals(pic >= 1 && pic <= 14, velocity.hasPIC(), "PIC " + pic);
            assertEquals(!velocity.hasPIC(), velocity.getContainmentRadius().isUnknown(), "PIC " + pic);
        }
        // the issue's example, PIC 15
        assertFalse(new HVAVelocityMsg("8D4B1A2CCA007800000000048ACF").hasPIC());
    }

    /**
     * Latitude and longitude coded as ZERO are what, together with a PIC and NACp of ZERO, marks the position as
     * unavailable; any other coding is not.
     */
    @Test
    void isPositionAllZeros() throws Exception {
        assertTrue(new HVAPositionMsg(message(0, 0)).isPositionAllZeros());
        assertFalse(new HVAPositionMsg(message(0, 1L << 19)).isPositionAllZeros()); // latitude code 1
        assertFalse(new HVAPositionMsg(message(0, 1)).isPositionAllZeros()); // longitude code 1
        assertFalse(new HVAPositionMsg(message(0, 1L << 36)).isPositionAllZeros()); // southern latitude sign only
    }

}
