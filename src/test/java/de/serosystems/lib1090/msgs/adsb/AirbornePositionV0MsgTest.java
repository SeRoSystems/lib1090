package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionV0MsgTest {

	@Test
	public void testAltitude39000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), null);
		assertEquals(39000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg325() throws Exception {
		// Negative altitude from jet1090 test vectors
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d484fde5803b647ecec4fcdd74f"), null);
		assertEquals(-325, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg300() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4845575803c647bcec2a980abc"), null);
		assertEquals(-300, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg275() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3424d25803d64c18ee03351f89"), null);
		assertEquals(-275, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeZero() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4401e458058645a8ea90496290"), null);
		assertEquals(0, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude25() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d346355580596459cea86756acc"), null);
		assertEquals(25, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude50() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3463555805a64584ea756d352e"), null);
		assertEquals(50, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude100() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3463555805c2d9f6f0f3f1b6c3"), null);
		assertEquals(100, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude1000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d346355580b064116e70a269f97"), null);
		assertEquals(1000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude5000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d343386581f06318ad4fecab734"), null);
		assertEquals(5000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude37025() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D06A15358BF17FF7D4A84B47B95"), null);
		assertEquals(37025, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude9550() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d45ac2d583561285c4fa686fcdc"), null);
		assertEquals(9550, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude37000_pair1() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4d224f58bf07c2d41a9a353d70"), null);
		assertEquals(37000, msg.getAltitude().intValue());
	}

	@Test
	public void testOddFlagEvenFrame() throws Exception {
		// 8D40058B58C901375147EFD09357: odd_flag=0 (even)
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), null);
		assertFalse(msg.getCPREncodedPosition().isOddFormat());
	}

	@Test
	public void testOddFlagOddFrame() throws Exception {
		// 8D40058B58C904A87F402D3B8C59: odd_flag=1 (odd)
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C904A87F402D3B8C59"), null);
		assertTrue(msg.getCPREncodedPosition().isOddFormat());
	}

	@Test
	public void testTypeCode11() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), null);
		assertEquals(11, msg.getFormatTypeCode());
	}

	@Test
	public void testTypeCode18() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d45cab390c39509496ca9a32912"), null);
		assertEquals(18, msg.getFormatTypeCode());
	}
}