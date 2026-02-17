package de.serosystems.lib1090.msgs.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VelocityOverGroundMsgTest {

	@Test
	public void testGroundSpeed_485020() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D485020994409940838175B284F");
		assertTrue(msg.hasVelocityInfo());
		assertEquals(159.0, msg.getGroundSpeed(), 1.0);
	}

	@Test
	public void testTrackAngle_485020() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D485020994409940838175B284F");
		assertEquals(182.88, msg.getTrueTrackAngle(), 0.01);
	}

	@Test
	public void testVerticalRate_485020() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D485020994409940838175B284F");
		assertTrue(msg.hasVerticalRateInfo());
		assertEquals(-832, msg.getVerticalRate().intValue());
	}

	@Test
	public void testGeoMinusBaro_485020() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D485020994409940838175B284F");
		assertTrue(msg.hasGeoMinusBaroInfo());
		assertEquals(550, msg.getGeoMinusBaro().intValue());
	}

	@Test
	public void testGroundSpeed_45AC2D() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d45ac2d9904d910613f94ba81b5");
		assertEquals(252.0, msg.getGroundSpeed(), 1.0);
	}

	@Test
	public void testHeading_45AC2D() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d45ac2d9904d910613f94ba81b5");
		assertEquals(301.04, msg.getTrueTrackAngle(), 0.01);
	}

	@Test
	public void testVerticalRatePositive_45AC2D() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d45ac2d9904d910613f94ba81b5");
		assertEquals(4992, msg.getVerticalRate().intValue());
	}

	@Test
	public void testGeoMinusBaroNegative_45AC2D() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d45ac2d9904d910613f94ba81b5");
		assertEquals(-475, msg.getGeoMinusBaro().intValue());
	}

	@Test
	public void testSmallPositiveVerticalRate_451E8B() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D451E8B99019699C00B0A81F36E");
		assertTrue(msg.hasVerticalRateInfo());
		assertEquals(64, msg.getVerticalRate().intValue());
	}

	@Test
	public void testVerticalRate64_3461cf_a() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d3461cf9908388930080f948ea1");
		assertEquals(64, msg.getVerticalRate().intValue());
		assertEquals(350, msg.getGeoMinusBaro().intValue());
	}

	@Test
	public void testVerticalRate128_3461cf_b() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d3461cf9908558e100c1071eb67");
		assertEquals(128, msg.getVerticalRate().intValue());
		assertEquals(375, msg.getGeoMinusBaro().intValue());
	}

	@Test
	public void testVerticalRate960_3461cf_c() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d3461cf99085a8f10400f80e6ac");
		assertEquals(960, msg.getVerticalRate().intValue());
	}

	@Test
	public void testVerticalRateNeg64_394c0f() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8d394c0f990c4932780838866883");
		assertEquals(-64, msg.getVerticalRate().intValue());
		assertEquals(1375, msg.getGeoMinusBaro().intValue());
	}

	@Test
	public void testSpeedType() throws Exception {
		VelocityOverGroundMsg msg = new VelocityOverGroundMsg("8D485020994409940838175B284F");
		assertFalse(msg.isSupersonic(), "Ground speed messages should not be supersonic");
	}
}