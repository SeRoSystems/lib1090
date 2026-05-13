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

public class ModeACodeV1MsgTest {

	public static final String MODE_A_CODE_V1 = "8d89653ebf95b00000000059abfb";

	@Test
	void testSubtype() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals(7, msg.getSubtype());
	}

	@Test
	void testSquawkCode() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals("6513", msg.getIdentity());
	}

	@Test
	void testTypeCode23() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals(23, msg.getFormatTypeCode());
	}
}
