package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyOrPriorityStatusMsgTest {

	@Test
	void testSquawk() throws BadFormatException, UnspecifiedFormatError {
		EmergencyOrPriorityStatusMsg reply1 = new EmergencyOrPriorityStatusMsg("8c3d1a68e10a8000000000ad61a4");
		EmergencyOrPriorityStatusMsg reply2 = new EmergencyOrPriorityStatusMsg("8d3d1a68e10a8000000000f510dc");
		EmergencyOrPriorityStatusMsg reply3 = new EmergencyOrPriorityStatusMsg("8f3d1a68e10a800000000045f22c");

		assertEquals(reply1.getIdentity(), "7000", "Squawk should be 7000");
		assertEquals(reply2.getIdentity(), "7000", "Squawk should be 7000");
		assertEquals(reply3.getIdentity(), "7000", "Squawk should be 7000");

	}

	@Test
	public void testEmergencyState() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(0, msg.getEmergencyStateCode());
		assertEquals("no emergency", msg.getEmergencyStateText());
	}

	@Test
	public void testSquawkCode() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals("6513", msg.getIdentity());
	}

	@Test
	public void testTypeCode28() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(28, msg.getFormatTypeCode());
	}
}