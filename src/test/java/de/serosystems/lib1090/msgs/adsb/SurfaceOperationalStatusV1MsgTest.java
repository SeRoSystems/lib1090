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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceOperationalStatusV1MsgTest {

	@Test
	public void testValidVersion1Message() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000000002000000000");
		SurfaceOperationalStatusV1Msg status = new SurfaceOperationalStatusV1Msg(msg);
		assertEquals(1, status.getSubtypeCode());
		assertEquals(1, status.getVersion());
	}

	@Test
	public void testOperationalModeCodeWithHighByte() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000080002000000000");
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(msg));
	}

	@Test
	public void testCapabilityClassCodeFlags() throws Exception {
		SurfaceOperationalStatusV1Msg positionOffsetApplied = statusWithCapabilityClassCode(0x200);
		assertTrue(positionOffsetApplied.hasPositionOffsetApplied());
		assertFalse(positionOffsetApplied.has1090ESIn());
		assertFalse(positionOffsetApplied.hasLowTxPower());

		SurfaceOperationalStatusV1Msg esIn = statusWithCapabilityClassCode(0x100);
		assertFalse(esIn.hasPositionOffsetApplied());
		assertTrue(esIn.has1090ESIn());
		assertFalse(esIn.hasLowTxPower());

		SurfaceOperationalStatusV1Msg lowTxPower = statusWithCapabilityClassCode(0x20);
		assertFalse(lowTxPower.hasPositionOffsetApplied());
		assertFalse(lowTxPower.has1090ESIn());
		assertTrue(lowTxPower.hasLowTxPower());
	}

	@Test
	public void testCapabilityClassCodeReservedBits() throws Exception {
		assertThrows(BadFormatException.class, () -> statusWithCapabilityClassCode(0x800));
		assertThrows(BadFormatException.class, () -> statusWithCapabilityClassCode(0x400));

		statusWithCapabilityClassCode(0x200);
	}

	@Test
	public void testOperationalModeCodeFlags() throws Exception {
		SurfaceOperationalStatusV1Msg tcasResolutionAdvisory = statusWithOperationalModeCode(0x2000);
		assertTrue(tcasResolutionAdvisory.hasTCASResolutionAdvisory());
		assertFalse(tcasResolutionAdvisory.hasActiveIDENTSwitch());
		assertFalse(tcasResolutionAdvisory.hasReceivingATCServices());

		SurfaceOperationalStatusV1Msg activeIdentSwitch = statusWithOperationalModeCode(0x1000);
		assertFalse(activeIdentSwitch.hasTCASResolutionAdvisory());
		assertTrue(activeIdentSwitch.hasActiveIDENTSwitch());
		assertFalse(activeIdentSwitch.hasReceivingATCServices());

		SurfaceOperationalStatusV1Msg receivingAtcServices = statusWithOperationalModeCode(0x0800);
		assertFalse(receivingAtcServices.hasTCASResolutionAdvisory());
		assertFalse(receivingAtcServices.hasActiveIDENTSwitch());
		assertTrue(receivingAtcServices.hasReceivingATCServices());
	}

	@Test
	public void testOperationalModeCodeReservedBits() throws Exception {
		assertThrows(BadFormatException.class, () -> statusWithOperationalModeCode(0x8000));
		assertThrows(BadFormatException.class, () -> statusWithOperationalModeCode(0x4000));

		statusWithOperationalModeCode(0x2000);
	}

	private static SurfaceOperationalStatusV1Msg statusWithCapabilityClassCode(int capabilityClassCode) throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000000002000000000");
		msg[5] = (byte) (capabilityClassCode >>> 4);
		msg[6] = (byte) ((msg[6] & 0x0F) | ((capabilityClassCode & 0x0F) << 4));
		return new SurfaceOperationalStatusV1Msg(msg);
	}

	private static SurfaceOperationalStatusV1Msg statusWithOperationalModeCode(int operationalModeCode) throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F9000000002000000000");
		msg[7] = (byte) (operationalModeCode >>> 8);
		msg[8] = (byte) operationalModeCode;
		return new SurfaceOperationalStatusV1Msg(msg);
	}
}
