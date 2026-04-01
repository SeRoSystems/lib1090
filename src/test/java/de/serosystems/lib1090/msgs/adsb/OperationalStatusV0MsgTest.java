package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalStatusV0MsgTest {

	@Test
	public void testValidEnrouteCapabilities() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F8300000000000000000");
		OperationalStatusV0Msg status = new OperationalStatusV0Msg(msg);
		assertFalse(status.hasOperationalTCAS());
		assertTrue(status.hasOperationalCDTI());
	}

	@Test
	public void testInvalidEnrouteCapabilitiesHighBits() throws Exception {
		byte[] msg = Tools.hexStringToByteArray("8D000000F8800000000000000000");
		assertThrows(BadFormatException.class, () -> new OperationalStatusV0Msg(msg));
	}
}
