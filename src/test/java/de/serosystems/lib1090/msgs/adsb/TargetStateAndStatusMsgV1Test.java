package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TargetStateAndStatusMsgV1Test {

	public static final String TSS_V1 = withSubtype(TargetStateAndStatusMsgV2Test.TSS_WITHOUT_HEADING, 0);
	public static final String A_OPSTAT_V1 = withAddress("8D000000F8000200492900000000", "89653e");

	@Test
	public void testDecodeTssV1() throws UnspecifiedFormatError, BadFormatException {
		final TargetStateAndStatusMsgV1 tss = new TargetStateAndStatusMsgV1(Tools.hexStringToByteArray(TSS_V1));

		assertEquals("89653e", tss.getAddress().getHexAddress());
		assertEquals(17, tss.getDownlinkFormat());
		assertEquals(29, tss.getFormatTypeCode());

		assertEquals(0, tss.getVerticalDataAvailableAndSourceIndicator());
		assertTrue(tss.hasSelectedAltitudeInfo());
		assertEquals(144, tss.getSelectedAltitudeRaw());
		assertEquals(13400, tss.getSelectedAltitude());
		assertTrue(tss.hasSelectedHeadingInfo());
		assertNotNull(tss.getSelectedHeading());
		assertEquals(9, tss.getNACp());
		assertTrue(tss.getBarometricAltitudeIntegrityCode());
		assertEquals(3, tss.getSIL());
		assertTrue(tss.hasOperationalTCAS());
		assertTrue(tss.hasActiveTCASResolutionAdvisory());
		assertEquals(4, tss.getEmergencyPriorityStatus());
	}

	private static String withSubtype(String message, int subtype) {
		byte[] raw = Tools.hexStringToByteArray(message);
		raw[4] = (byte) ((raw[4] & ~0x06) | ((subtype & 0x3) << 1));
		return withParity(raw);
	}

	private static String withAddress(String message, String address) {
		byte[] raw = Tools.hexStringToByteArray(message);
		byte[] icao = Tools.hexStringToByteArray(address);
		System.arraycopy(icao, 0, raw, 1, icao.length);
		return withParity(raw);
	}

	private static String withParity(byte[] raw) {
		byte[] parity = ModeSDownlinkMsg.calcParity(Arrays.copyOf(raw, raw.length - 3));
		System.arraycopy(parity, 0, raw, raw.length - 3, parity.length);
		return Tools.toHexString(raw);
	}
}
