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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalStatusV0MsgTest {

	@Test
	public void testValidEnrouteCapabilities() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F8300000000000000000");
		OperationalStatusV0Msg status = new OperationalStatusV0Msg(msg);
		assertFalse(status.hasOperationalTCAS());
		assertTrue(status.hasOperationalCDTI());
	}

	@Test
	public void testInvalidEnrouteCapabilitiesHighBits() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F8800000000000000000");
		assertThrows(BadFormatException.class, () -> new OperationalStatusV0Msg(msg));
	}
}
