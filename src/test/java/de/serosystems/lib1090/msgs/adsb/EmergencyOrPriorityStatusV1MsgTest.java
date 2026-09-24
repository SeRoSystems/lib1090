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

import de.serosystems.lib1090.decoding.emergency.EmergencyStateV1V2;
import de.serosystems.lib1090.decoding.emergency.EmergencyStateV3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EmergencyOrPriorityStatusV1MsgTest {

    /** Downed aircraft in version 1, reported as a general emergency, ED-102B §N.3.3.4 TABLE N-19. */
    @Test
    public void testDownedAircraft() throws Exception {
        EmergencyOrPriorityStatusV1Msg msg =
                new EmergencyOrPriorityStatusV1Msg(EmergencyOrPriorityStatusV0MsgTest.EMERGENCY_STATE_6);
        assertEquals(6, msg.getEmergencyStateEncoded());
        assertSame(EmergencyStateV1V2.DOWNED_AIRCRAFT, msg.getEmergencyState());
        assertEquals("Downed Aircraft", msg.getEmergencyState().getText());
        assertSame(EmergencyStateV3.GENERAL_EMERGENCY, msg.getReportedEmergencyState());
    }
}
