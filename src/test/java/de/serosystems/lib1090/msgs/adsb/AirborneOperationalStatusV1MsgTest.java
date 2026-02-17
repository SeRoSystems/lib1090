package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirborneOperationalStatusV1MsgTest {

	@Test
	public void testCapabilityCodeWithHighByteBits() throws Exception {
		// Bypass CRC checks
		// 14 bytes: DF(1) + ICAO(3) + ME(7) + CRC(3)
		byte[] msg = Tools.hexStringToByteArray("8D000000F8008000002900000000");
		AirborneOperationalStatusV1Msg status = new AirborneOperationalStatusV1Msg(msg);
		assertEquals(1, status.getVersion());
	}

	@Test
	public void testOperationalModeCodeWithHighByte() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F8000000802900000000");
		AirborneOperationalStatusV1Msg status = new AirborneOperationalStatusV1Msg(msg);
		assertEquals(1, status.getVersion());
	}

	@Test
	public void testValidVersion1Message() throws Exception {
		// ME = F8 00 02 00 49 29 00
		byte[] msg = Tools.hexStringToByteArray("8D000000F8000200492900000000");
		AirborneOperationalStatusV1Msg status = new AirborneOperationalStatusV1Msg(msg);
		assertEquals(1, status.getVersion());
		assertEquals(9, status.getNACp());
	}
}