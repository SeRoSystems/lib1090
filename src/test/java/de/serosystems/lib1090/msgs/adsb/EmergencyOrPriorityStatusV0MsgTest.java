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

import de.serosystems.lib1090.decoding.emergency.EmergencyStateV0;
import de.serosystems.lib1090.decoding.emergency.EmergencyStateV3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EmergencyOrPriorityStatusV0MsgTest {

    // ME: 11100 => type code 28, 001 => subtype 1, 110 => emergency state 6
    static final String EMERGENCY_STATE_6 = "8DA2C1B6E1C00000000000000000";

    @Test
    public void testEmergencyState() throws Exception {
        EmergencyOrPriorityStatusV0Msg msg = new EmergencyOrPriorityStatusV0Msg("8DA2C1B6E112B600000000760759");
        assertEquals(0, msg.getEmergencyStateEncoded());
        assertSame(EmergencyStateV0.NO_EMERGENCY, msg.getEmergencyState());
        assertEquals("No Emergency", msg.getEmergencyState().getText());
        assertSame(EmergencyStateV3.NO_REPORTED_EMERGENCY, msg.getReportedEmergencyState());
    }

    /** Reserved in version 0, so nothing is reported, ED-102B §N.2.3.4 TABLE N-7. */
    @Test
    public void testReservedEmergencyState() throws Exception {
        EmergencyOrPriorityStatusV0Msg msg = new EmergencyOrPriorityStatusV0Msg(EMERGENCY_STATE_6);
        assertEquals(6, msg.getEmergencyStateEncoded());
        assertSame(EmergencyStateV0.RESERVED_6, msg.getEmergencyState());
        assertSame(EmergencyStateV3.NO_REPORTED_EMERGENCY, msg.getReportedEmergencyState());
    }

    @Test
    public void testTypeCode28() throws Exception {
        EmergencyOrPriorityStatusV0Msg msg = new EmergencyOrPriorityStatusV0Msg("8DA2C1B6E112B600000000760759");
        assertEquals(28, msg.getFormatTypeCode());
    }
}
