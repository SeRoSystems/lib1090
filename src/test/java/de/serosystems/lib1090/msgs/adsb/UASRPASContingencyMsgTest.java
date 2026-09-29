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

import de.serosystems.lib1090.decoding.ContingencyPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UASRPASContingencyMsgTest {

    /**
     * ED-102B TABLE 2-104: contingency plan 0 declares the whole message invalid or empty, so no TCP is reported,
     * as in the issue's example.
     */
    @Test
    void plan0_isInvalid() throws Exception {
        UASRPASContingencyMsg msg = new UASRPASContingencyMsg("8D4B1A2CE4085C8001C000EC0669");
        assertEquals(ContingencyPlan.INVALID_OR_NO_DATA, msg.getContingencyPlan());
        assertFalse(msg.isValid());
        assertNull(msg.getTcpAltitude());
        assertNull(msg.getTcpLatitude());
        assertNull(msg.getTcpLongitude());
    }

    /**
     * The same message with contingency plan 1 reports its TCP; ED-102B TABLE 2-105: ME bit 13 tells the next TCP
     * from the current one.
     */
    @Test
    void validPlanReportsTheCurrentOrNextTCP() throws Exception {
        UASRPASContingencyMsg next = new UASRPASContingencyMsg("8D4B1A2CE4185C8001C000000000");
        assertEquals(ContingencyPlan.RETURN_TO_TAKEOFF_LOCATION, next.getContingencyPlan());
        assertTrue(next.isValid());
        assertTrue(next.isNextTCP());
        assertEquals(10000, next.getTcpAltitude().intValue());
        assertEquals(45.0, next.getTcpLatitude(), 1e-3);
        assertEquals(-45.0, next.getTcpLongitude(), 1e-3);

        assertFalse(new UASRPASContingencyMsg("8D4B1A2CE4105C8001C000000000").isNextTCP());
    }

    /**
     * Every 4-bit code has its plan, in the order of TABLE 2-104.
     */
    @Test
    void contingencyPlanCodes() {
        for (int code = 0; code < 16; code++)
            assertEquals(code, ContingencyPlan.forEncoded(code).getEncoded());
        assertEquals(ContingencyPlan.OPERATOR_DEFINED_PLAN_A, ContingencyPlan.forEncoded(6));
        assertEquals(ContingencyPlan.OPERATOR_DEFINED_PLAN_I, ContingencyPlan.forEncoded(14));
        assertEquals(ContingencyPlan.LOST_LINK_RESTORED, ContingencyPlan.forEncoded(15));
        assertThrows(IllegalArgumentException.class, () -> ContingencyPlan.forEncoded(16));
    }

}
