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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmergencyOrPriorityStatusV2MsgTest {

	@Test
	void testSquawk() throws BadFormatException, UnspecifiedFormatError {
		EmergencyOrPriorityStatusV2Msg reply1 = new EmergencyOrPriorityStatusV2Msg("8c3d1a68e10a8000000000ad61a4");
		EmergencyOrPriorityStatusV2Msg reply2 = new EmergencyOrPriorityStatusV2Msg("8d3d1a68e10a8000000000f510dc");
		EmergencyOrPriorityStatusV2Msg reply3 = new EmergencyOrPriorityStatusV2Msg("8f3d1a68e10a800000000045f22c");

		assertEquals("7000", reply1.getIdentity(), "Squawk should be 7000");
		assertEquals("7000", reply2.getIdentity(), "Squawk should be 7000");
		assertEquals("7000", reply3.getIdentity(), "Squawk should be 7000");
	}

	@Test
	public void testSquawkCode() throws Exception {
		EmergencyOrPriorityStatusV2Msg msg = new EmergencyOrPriorityStatusV2Msg("8DA2C1B6E112B600000000760759");
		assertEquals("6513", msg.getIdentity());
	}
}
