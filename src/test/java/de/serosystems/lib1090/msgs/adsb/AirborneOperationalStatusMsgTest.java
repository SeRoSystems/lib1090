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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared tests for the capability class code and operational mode code fields, which are encoded
 * identically (same bit offsets, same reserved-bit validation) across ADS-B versions 1 and 2 for
 * airborne operational status messages. hasOperationalTCAS()'s polarity differs between versions
 * and is intentionally NOT covered here; it stays in the version-specific subclasses.
 */
abstract class AirborneOperationalStatusMsgTest {

	protected abstract byte[] baseMessage();

	protected abstract AirborneOperationalStatusV1V2Msg create(byte[] msg) throws Exception;

	protected AirborneOperationalStatusV1V2Msg withCapabilityClassCode(int capabilityClassCode) throws Exception {
		byte[] msg = baseMessage();
		msg[5] = (byte) (capabilityClassCode >>> 8);
		msg[6] = (byte) capabilityClassCode;
		return create(msg);
	}

	protected AirborneOperationalStatusV1V2Msg withOperationalModeCode(int operationalModeCode) throws Exception {
		byte[] msg = baseMessage();
		msg[7] = (byte) (operationalModeCode >>> 8);
		msg[8] = (byte) operationalModeCode;
		return create(msg);
	}

	@Test
	void testSubtypeCode() throws Exception {
		assertEquals(0, create(baseMessage()).getSubtypeCode());
	}

	@Test
	void testCapabilityClassCodeFlags() throws Exception {
		AirborneOperationalStatusV1V2Msg es1090In = withCapabilityClassCode(0x1000);
		assertTrue(es1090In.has1090ESIn());
		assertFalse(es1090In.hasAirReferencedVelocity());
		assertFalse(es1090In.hasTargetStateReport());

		AirborneOperationalStatusV1V2Msg airReferencedVelocity = withCapabilityClassCode(0x0200);
		assertFalse(airReferencedVelocity.has1090ESIn());
		assertTrue(airReferencedVelocity.hasAirReferencedVelocity());
		assertFalse(airReferencedVelocity.hasTargetStateReport());

		AirborneOperationalStatusV1V2Msg targetStateReport = withCapabilityClassCode(0x0100);
		assertFalse(targetStateReport.has1090ESIn());
		assertFalse(targetStateReport.hasAirReferencedVelocity());
		assertTrue(targetStateReport.hasTargetStateReport());
	}

	@Test
	void testTargetChangeReportCapability() throws Exception {
		assertEquals(0, withCapabilityClassCode(0x00).getTargetChangeReportCapabilityEncoded());
		assertEquals(1, withCapabilityClassCode(0x40).getTargetChangeReportCapabilityEncoded());
		assertEquals(2, withCapabilityClassCode(0x80).getTargetChangeReportCapabilityEncoded());
		assertEquals(3, withCapabilityClassCode(0xC0).getTargetChangeReportCapabilityEncoded());
	}

	@Test
	void testCapabilityClassCodeReservedBits() throws Exception {
		assertThrows(BadFormatException.class, () -> withCapabilityClassCode(0x8000));
		assertThrows(BadFormatException.class, () -> withCapabilityClassCode(0x4000));

		withCapabilityClassCode(0x0000);
	}

	@Test
	void testOperationalModeCodeFlags() throws Exception {
		AirborneOperationalStatusV1V2Msg tcasResolutionAdvisory = withOperationalModeCode(0x2000);
		assertTrue(tcasResolutionAdvisory.hasTCASResolutionAdvisory());
		assertFalse(tcasResolutionAdvisory.hasActiveIDENTSwitch());
		assertFalse(tcasResolutionAdvisory.hasReceivingATCServices());

		AirborneOperationalStatusV1V2Msg activeIdentSwitch = withOperationalModeCode(0x1000);
		assertFalse(activeIdentSwitch.hasTCASResolutionAdvisory());
		assertTrue(activeIdentSwitch.hasActiveIDENTSwitch());
		assertFalse(activeIdentSwitch.hasReceivingATCServices());

		AirborneOperationalStatusV1V2Msg receivingAtcServices = withOperationalModeCode(0x0800);
		assertFalse(receivingAtcServices.hasTCASResolutionAdvisory());
		assertFalse(receivingAtcServices.hasActiveIDENTSwitch());
		assertTrue(receivingAtcServices.hasReceivingATCServices());
	}

	@Test
	void testOperationalModeCodeReservedBits() throws Exception {
		assertThrows(BadFormatException.class, () -> withOperationalModeCode(0x8000));
		assertThrows(BadFormatException.class, () -> withOperationalModeCode(0x4000));

		withOperationalModeCode(0x0000);
	}
}
