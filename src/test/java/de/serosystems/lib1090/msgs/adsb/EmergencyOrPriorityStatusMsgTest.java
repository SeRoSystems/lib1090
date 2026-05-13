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

class EmergencyOrPriorityStatusMsgTest {

	@Test
	public void testEmergencyState() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(0, msg.getEmergencyStateCode());
		assertEquals("no emergency", msg.getEmergencyStateText());
	}

	@Test
	public void testTypeCode28() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(28, msg.getFormatTypeCode());
	}
}
