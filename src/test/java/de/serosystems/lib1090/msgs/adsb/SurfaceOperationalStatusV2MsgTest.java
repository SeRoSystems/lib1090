package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV1MsgTest.surfaceOpStatus;
import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusV2MsgTest {

	static String surfaceOpStatusV2(int cc, int om) {
		return surfaceOpStatus(cc, om, 0x40, 0x00);
	}

	private static SurfaceOperationalStatusV2Msg withCapabilityClassCode(int cc) throws BadFormatException, UnspecifiedFormatError {
		return new SurfaceOperationalStatusV2Msg(surfaceOpStatusV2(cc, 0));
	}

	private static SurfaceOperationalStatusV2Msg withOperationalModeCode(int om) throws BadFormatException, UnspecifiedFormatError {
		return new SurfaceOperationalStatusV2Msg(surfaceOpStatusV2(0, om));
	}

	@Test
	void testRejectVersion1() {
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0, 0, 0x20, 0)));
	}

	@Test
	void testDecodeCommonFields() throws BadFormatException, UnspecifiedFormatError {
		// version 2, NACp 10; SIL 2, SIL supplement
		SurfaceOperationalStatusV2Msg opstat = new SurfaceOperationalStatusV2Msg(surfaceOpStatus(0, 0, 0x4A, 0x22));

		assertEquals(2, opstat.getVersion());
		assertFalse(opstat.hasNICSupplementA());
		assertEquals(10, opstat.getNACp());
		assertEquals(2, opstat.getSIL());
		assertTrue(opstat.hasSILSupplement());
	}

	@Test
	void testCapabilityClassCode() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV2Msg es1090In = withCapabilityClassCode(0x100);
		assertTrue(es1090In.has1090ESIn());
		assertFalse(es1090In.hasLowTxPower());
		assertFalse(es1090In.hasUATIn());

		SurfaceOperationalStatusV2Msg b2Low = withCapabilityClassCode(0x020);
		assertFalse(b2Low.has1090ESIn());
		assertTrue(b2Low.hasLowTxPower());
		assertFalse(b2Low.hasUATIn());

		SurfaceOperationalStatusV2Msg uatIn = withCapabilityClassCode(0x010);
		assertTrue(uatIn.hasUATIn());
		assertEquals(0, uatIn.getNACv());
		assertFalse(uatIn.getNICSupplementC());

		SurfaceOperationalStatusV2Msg nacv = withCapabilityClassCode(0x00A);
		assertFalse(nacv.hasUATIn());
		assertEquals(5, nacv.getNACv());
		assertFalse(nacv.getNICSupplementC());

		SurfaceOperationalStatusV2Msg nicSupplementC = withCapabilityClassCode(0x001);
		assertFalse(nicSupplementC.hasUATIn());
		assertEquals(0, nicSupplementC.getNACv());
		assertTrue(nicSupplementC.getNICSupplementC());
	}

	@Test
	void testCapabilityClassCodePOABitIsIgnored() throws BadFormatException, UnspecifiedFormatError {
		// ME bit 11 carries POA in version 1 only
		assertFalse(withCapabilityClassCode(0x200).hasPositionOffsetApplied());
	}

	@Test
	void testOperationalModeCode() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV2Msg singleAntenna = withOperationalModeCode(0x0400);
		assertTrue(singleAntenna.hasSingleAntenna());
		assertEquals(0, singleAntenna.getSystemDesignAssurance());
		assertEquals(0, singleAntenna.getGPSAntennaOffset());

		SurfaceOperationalStatusV2Msg sda = withOperationalModeCode(0x0300);
		assertFalse(sda.hasSingleAntenna());
		assertEquals(3, sda.getSystemDesignAssurance());
		assertEquals(0, sda.getGPSAntennaOffset());

		SurfaceOperationalStatusV2Msg gpsAntennaOffset = withOperationalModeCode(0x005A);
		assertFalse(gpsAntennaOffset.hasSingleAntenna());
		assertEquals(0, gpsAntennaOffset.getSystemDesignAssurance());
		assertEquals(0x5A, gpsAntennaOffset.getGPSAntennaOffset());
		assertEquals(0xFF, withOperationalModeCode(0x00FF).getGPSAntennaOffset() & 0xFF);
	}

	@Test
	void testLateralAxisGPSAntennaOffset() throws BadFormatException, UnspecifiedFormatError {
		assertNull(withOperationalModeCode(0x00).getLateralAxisGPSAntennaOffset());
		assertEquals(2, withOperationalModeCode(0x20).getLateralAxisGPSAntennaOffset());
		assertEquals(4, withOperationalModeCode(0x40).getLateralAxisGPSAntennaOffset());
		assertEquals(6, withOperationalModeCode(0x60).getLateralAxisGPSAntennaOffset());
		assertEquals(0, withOperationalModeCode(0x80).getLateralAxisGPSAntennaOffset());
		assertEquals(-2, withOperationalModeCode(0xA0).getLateralAxisGPSAntennaOffset());
		assertEquals(-4, withOperationalModeCode(0xC0).getLateralAxisGPSAntennaOffset());
		assertEquals(-6, withOperationalModeCode(0xFF).getLateralAxisGPSAntennaOffset());

		// independent of the longitudinal offset
		assertEquals(4, withOperationalModeCode(0x5A).getLateralAxisGPSAntennaOffset());
	}

	@Test
	void testLongitudinalAxisGPSAntennaOffset() throws BadFormatException, UnspecifiedFormatError {
		assertNull(withOperationalModeCode(0x00).getLongitudinalAxisGPSAntennaOffset());
		assertEquals(0, withOperationalModeCode(0x01).getLongitudinalAxisGPSAntennaOffset());
		assertEquals(2, withOperationalModeCode(0x02).getLongitudinalAxisGPSAntennaOffset());
		assertEquals(50, withOperationalModeCode(0x1A).getLongitudinalAxisGPSAntennaOffset());
		assertEquals(60, withOperationalModeCode(0x1F).getLongitudinalAxisGPSAntennaOffset());

		// independent of the lateral offset
		assertNull(withOperationalModeCode(0xE0).getLongitudinalAxisGPSAntennaOffset());
		assertEquals(50, withOperationalModeCode(0x5A).getLongitudinalAxisGPSAntennaOffset());
	}

	@Test
	void testHasPositionOffsetApplied() throws BadFormatException, UnspecifiedFormatError {
		assertTrue(withOperationalModeCode(0x01).hasPositionOffsetApplied());
		// POA only depends on the longitudinal offset
		assertTrue(withOperationalModeCode(0xE1).hasPositionOffsetApplied());
		assertFalse(withOperationalModeCode(0x00).hasPositionOffsetApplied());
		assertFalse(withOperationalModeCode(0x02).hasPositionOffsetApplied());
	}
}
