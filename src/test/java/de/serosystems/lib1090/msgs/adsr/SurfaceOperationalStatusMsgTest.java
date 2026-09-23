package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusMsgTest {

	/**
	 * Builds a DF18/CF6 surface operational status message (parity not valid, not tested here)
	 */
	static String surfaceOpStatus(int cc, int om, int me41to48, int me49to56) {
		return String.format("964D0131F9%03X0%04X%02X%02X000000", cc, om, me41to48, me49to56);
	}

	@Test
	void testV1CapabilityClassCode() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV1Msg poa = new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x200, 0, 0x20, 0));
		assertTrue(poa.hasPositionOffsetApplied());
		assertFalse(poa.has1090ESIn());

		assertTrue(new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x100, 0, 0x20, 0)).has1090ESIn());
		assertTrue(new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x020, 0, 0x20, 0)).hasLowTxPower());
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x400, 0, 0x20, 0)));
	}

	@Test
	@SuppressWarnings("deprecation")
	void testV1Version2FieldsAreNotDecoded() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV1Msg opstat = new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x01F, 0x07FF, 0x20, 0xC1));

		assertFalse(opstat.hasUATIn());
		assertEquals(0, opstat.getNACv());
		assertFalse(opstat.getNICSupplementC());
		assertFalse(opstat.getNICSupplementB());
		assertFalse(opstat.hasSingleAntenna());
		assertEquals(0, opstat.getSystemDesignAssurance());
		assertEquals(0, opstat.getGPSAntennaOffset());
		assertEquals(-1, opstat.getGeometricVerticalAccuracy());
		assertTrue(opstat.getIMF());
	}

	@Test
	void testV2Fields() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV2Msg nacv = new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0x00A, 0, 0x40, 0));
		assertEquals(5, nacv.getNACv());
		assertFalse(nacv.hasUATIn());
		assertFalse(nacv.getNICSupplementC());

		assertTrue(new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0x010, 0, 0x40, 0)).hasUATIn());
		assertTrue(new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0x001, 0, 0x40, 0)).getNICSupplementC());

		SurfaceOperationalStatusV2Msg offset = new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0, 0x075A, 0x40, 0));
		assertTrue(offset.hasSingleAntenna());
		assertEquals(3, offset.getSystemDesignAssurance());
		assertEquals(4, offset.getLateralAxisGPSAntennaOffset());
		assertEquals(50, offset.getLongitudinalAxisGPSAntennaOffset());
		assertFalse(offset.hasPositionOffsetApplied());

		assertTrue(new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0, 0x0001, 0x40, 0)).hasPositionOffsetApplied());
	}
}
