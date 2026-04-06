package de.serosystems.lib1090.msgs.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyOrPriorityStatusMsgTest {

	@Test
	public void testEmergencyState() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(0, msg.getEmergencyStateCode());
		assertEquals("no emergency", msg.getEmergencyStateText());
	}

	@Test
	public void testTypeCode28() throws Exception {
		EmergencyOrPriorityStatusMsg msg = new EmergencyOrPriorityStatusMsg("8DA2C1B6E112B600000000760759");
		assertEquals(28, msg.getFormatTypeCode());
	}
}
