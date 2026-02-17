package de.serosystems.lib1090.msgs.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentificationMsgTest {
	@Test
	public void testCallsignEZY85MH() throws Exception {
		IdentificationMsg msg = new IdentificationMsg("8D406B902015A678D4D220AA4BDA");
		assertEquals("EZY85MH ", new String(msg.getIdentity()));
	}

	@Test
	public void testCallsignKLM1023() throws Exception {
		IdentificationMsg msg = new IdentificationMsg("8D4840D6202CC371C32CE0576098");
		assertEquals("KLM1023 ", new String(msg.getIdentity()));
	}

	@Test
	public void testCategoryEZY85MH() throws Exception {
		IdentificationMsg msg = new IdentificationMsg("8D406B902015A678D4D220AA4BDA");
		assertEquals(0, msg.getEmitterCategory());
	}

	@Test
	public void testCategoryKLM1023() throws Exception {
		IdentificationMsg msg = new IdentificationMsg("8D4840D6202CC371C32CE0576098");
		assertEquals(0, msg.getEmitterCategory());
	}

	@Test
	public void testTypeCodeIdentification() throws Exception {
		IdentificationMsg msg = new IdentificationMsg("8D406B902015A678D4D220AA4BDA");
		assertEquals(4, msg.getFormatTypeCode());
	}
}