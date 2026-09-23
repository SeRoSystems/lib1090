package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusV1MsgTest {

	/**
	 * Builds a DF17 surface operational status message (parity not valid, not tested here)
	 *
	 * @param cc capability class code, ME bits 9-20
	 * @param om operational mode code, ME bits 25-40
	 * @param me41to48 ME bits 41-48 (version, NIC supplement A, NACp)
	 * @param me49to56 ME bits 49-56 (reserved, SIL, TRK/HDG, HRD, SIL supplement, reserved)
	 */
	static String surfaceOpStatus(int cc, int om, int me41to48, int me49to56) {
		return String.format("8D4D0131F9%03X0%04X%02X%02X000000", cc, om, me41to48, me49to56);
	}

	static String surfaceOpStatusV1(int cc, int om) {
		return surfaceOpStatus(cc, om, 0x20, 0x00);
	}

	@Test
	void testDecodeCommonFields() throws BadFormatException, UnspecifiedFormatError {
		// version 1, NIC supplement A, NACp 9; SIL 3, TRK/HDG, HRD
		SurfaceOperationalStatusV1Msg opstat = new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0, 0, 0x39, 0x3C));

		assertEquals(1, opstat.getSubtypeCode());
		assertEquals(1, opstat.getVersion());
		assertTrue(opstat.hasNICSupplementA());
		assertEquals(9, opstat.getNACp());
		assertEquals(3, opstat.getSIL());
		assertTrue(opstat.hasTrackHeadingInfo());
		assertTrue(opstat.getHorizontalReferenceDirection());
	}

	@Test
	void testCapabilityClassCode() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV1Msg poa = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0x200, 0));
		assertTrue(poa.hasPositionOffsetApplied());
		assertFalse(poa.has1090ESIn());
		assertFalse(poa.hasLowTxPower());

		SurfaceOperationalStatusV1Msg cdti = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0x100, 0));
		assertFalse(cdti.hasPositionOffsetApplied());
		assertTrue(cdti.has1090ESIn());
		assertFalse(cdti.hasLowTxPower());

		SurfaceOperationalStatusV1Msg b2Low = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0x020, 0));
		assertFalse(b2Low.hasPositionOffsetApplied());
		assertFalse(b2Low.has1090ESIn());
		assertTrue(b2Low.hasLowTxPower());
	}

	@Test
	void testRejectUnknownCapabilityClassCodeFormat() {
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0x800, 0)));
		assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0x400, 0)));
	}

	@Test
	void testOperationalModeCode() throws BadFormatException, UnspecifiedFormatError {
		SurfaceOperationalStatusV1Msg tcas = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0, 0x2000));
		assertTrue(tcas.hasTCASResolutionAdvisory());
		assertFalse(tcas.hasActiveIDENTSwitch());
		assertFalse(tcas.hasReceivingATCServices());

		SurfaceOperationalStatusV1Msg ident = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0, 0x1000));
		assertFalse(ident.hasTCASResolutionAdvisory());
		assertTrue(ident.hasActiveIDENTSwitch());
		assertFalse(ident.hasReceivingATCServices());

		SurfaceOperationalStatusV1Msg atc = new SurfaceOperationalStatusV1Msg(surfaceOpStatusV1(0, 0x0800));
		assertFalse(atc.hasTCASResolutionAdvisory());
		assertFalse(atc.hasActiveIDENTSwitch());
		assertTrue(atc.hasReceivingATCServices());
	}

	@Test
	@SuppressWarnings("deprecation")
	void testVersion2FieldsAreNotDecoded() throws BadFormatException, UnspecifiedFormatError {
		// all bits set where version 2 defines fields that version 1 does not have, incl. GVA bits
		SurfaceOperationalStatusV1Msg opstat = new SurfaceOperationalStatusV1Msg(surfaceOpStatus(0x01F, 0x07FF, 0x20, 0xC0));

		assertFalse(opstat.hasUATIn());
		assertEquals(0, opstat.getNACv());
		assertFalse(opstat.getNICSupplementC());
		assertFalse(opstat.hasSingleAntenna());
		assertEquals(0, opstat.getSystemDesignAssurance());
		assertEquals(0, opstat.getGPSAntennaOffset());
		assertEquals(-1, opstat.getGeometricVerticalAccuracy());
	}
}
