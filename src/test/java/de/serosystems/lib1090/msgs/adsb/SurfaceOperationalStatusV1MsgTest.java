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
