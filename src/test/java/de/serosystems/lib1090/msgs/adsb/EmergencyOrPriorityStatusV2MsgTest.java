package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmergencyOrPriorityStatusV2MsgTest {

	@Test
	void testSquawk() throws BadFormatException, UnspecifiedFormatError {
		EmergencyOrPriorityStatusV2Msg reply1 = new EmergencyOrPriorityStatusV2Msg("8c3d1a68e10a8000000000ad61a4");
		EmergencyOrPriorityStatusV2Msg reply2 = new EmergencyOrPriorityStatusV2Msg("8d3d1a68e10a8000000000f510dc");
		EmergencyOrPriorityStatusV2Msg reply3 = new EmergencyOrPriorityStatusV2Msg("8f3d1a68e10a800000000045f22c");

		assertEquals("7000", reply1.getIdentity(), "Squawk should be 7000");
		assertEquals("7000", reply2.getIdentity(), "Squawk should be 7000");
		assertEquals("7000", reply3.getIdentity(), "Squawk should be 7000");
	}

	@Test
	public void testSquawkCode() throws Exception {
		EmergencyOrPriorityStatusV2Msg msg = new EmergencyOrPriorityStatusV2Msg("8DA2C1B6E112B600000000760759");
		assertEquals("6513", msg.getIdentity());
	}
}
