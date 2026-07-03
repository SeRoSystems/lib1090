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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceOperationalStatusV2MsgTest {

	@Test
	public void testCapabilityClassCodeVersion2Fields() throws Exception {
		SurfaceOperationalStatusV2Msg uatIn = statusWithCapabilityClassCode(0x10);
		assertTrue(uatIn.hasUATIn());
		assertEquals(0, uatIn.getNACv());
		assertFalse(uatIn.getNICSupplementC());

		SurfaceOperationalStatusV2Msg nacv = statusWithCapabilityClassCode(0x0A);
		assertFalse(nacv.hasUATIn());
		assertEquals(5, nacv.getNACv());
		assertFalse(nacv.getNICSupplementC());

		SurfaceOperationalStatusV2Msg nicSupplementC = statusWithCapabilityClassCode(0x01);
		assertFalse(nicSupplementC.hasUATIn());
		assertEquals(0, nicSupplementC.getNACv());
		assertTrue(nicSupplementC.getNICSupplementC());
	}

	@Test
	public void testOperationalModeCodeVersion2Fields() throws Exception {
		SurfaceOperationalStatusV2Msg singleAntenna = statusWithOperationalModeCode(0x0400);
		assertTrue(singleAntenna.hasSingleAntenna());
		assertEquals(0, singleAntenna.getSystemDesignAssurance());
		assertEquals(0, singleAntenna.getGPSAntennaOffset());

		SurfaceOperationalStatusV2Msg systemDesignAssurance = statusWithOperationalModeCode(0x0300);
		assertFalse(systemDesignAssurance.hasSingleAntenna());
		assertEquals(3, systemDesignAssurance.getSystemDesignAssurance());
		assertEquals(0, systemDesignAssurance.getGPSAntennaOffset());

		SurfaceOperationalStatusV2Msg gpsAntennaOffset = statusWithOperationalModeCode(0x005A);
		assertFalse(gpsAntennaOffset.hasSingleAntenna());
		assertEquals(0, gpsAntennaOffset.getSystemDesignAssurance());
		assertEquals(0x5A, gpsAntennaOffset.getGPSAntennaOffset());
	}

	@Test
	public void testVersion2PositionOffsetApplied() throws Exception {
		assertTrue(statusWithOperationalModeCode(0x0001).hasPositionOffsetApplied());
		assertFalse(statusWithOperationalModeCode(0x0002).hasPositionOffsetApplied());
	}

	@Test
	public void testSILSupplement() throws Exception {
		assertFalse(defaultStatus().hasSILSupplement());

		byte[] msg = version2Message();
		msg[10] |= 0x02;
		assertTrue(new SurfaceOperationalStatusV2Msg(msg).hasSILSupplement());
	}

	private static SurfaceOperationalStatusV2Msg statusWithCapabilityClassCode(int capabilityClassCode) throws Exception {
		byte[] msg = version2Message();
		msg[5] = (byte) (capabilityClassCode >>> 4);
		msg[6] = (byte) ((msg[6] & 0x0F) | ((capabilityClassCode & 0x0F) << 4));
		return new SurfaceOperationalStatusV2Msg(msg);
	}

	private static SurfaceOperationalStatusV2Msg statusWithOperationalModeCode(int operationalModeCode) throws Exception {
		byte[] msg = version2Message();
		msg[7] = (byte) (operationalModeCode >>> 8);
		msg[8] = (byte) operationalModeCode;
		return new SurfaceOperationalStatusV2Msg(msg);
	}

	private static SurfaceOperationalStatusV2Msg defaultStatus() throws Exception {
		return new SurfaceOperationalStatusV2Msg(version2Message());
	}

	private static byte[] version2Message() {
		return Tools.hexStringToByteArray("8D000000F9000000004000000000");
	}
}
