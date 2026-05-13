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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurfaceOperationalStatusV1MsgTest {

	@Test
	public void testValidVersion1Message() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000000002000000000");
		SurfaceOperationalStatusV1Msg status = new SurfaceOperationalStatusV1Msg(msg);
		assertEquals(1, status.getVersion());
	}

	@Test
	public void testOperationalModeCodeWithHighByte() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000080002000000000");
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(msg));
	}
}
