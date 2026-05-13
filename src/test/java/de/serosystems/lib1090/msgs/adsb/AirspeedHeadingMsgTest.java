/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirspeedHeadingMsgTest {

	@Test
	public void testHeadingPrecision() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertTrue(msg.hasHeadingStatusFlag());
		assertEquals(243.984375, msg.getHeading(), 0.001);
	}

	@Test
	public void testAirspeed() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertTrue(msg.hasAirspeedInfo());
		assertEquals(375, msg.getAirspeed().intValue());
	}

	@Test
	public void testTrueAirspeed() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertTrue(msg.isTrueAirspeed());
	}

	@Test
	public void testNACvRawAndAccuracyBound() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertEquals(0, msg.getNACv());
		assertEquals(-1.0f, msg.getAccuracyBound());
	}

	@Test
	public void testImplementsAirborneVelocityMessage() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertInstanceOf(AirborneVelocityMessage.class, msg);
	}

	@Test
	public void testVerticalRate() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertTrue(msg.hasVerticalRateInfo());
		assertEquals(-2304, msg.getVerticalRate().intValue());
	}

	@Test
	public void testGeoMinusBaroUnavailable() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertFalse(msg.hasGeoMinusBaroInfo(), "geo-minus-baro should not be available when raw field is 0");
	}

	@Test
	public void testGeoMinusBaroReturnsNullWhenUnavailable() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertNull(msg.getGeoMinusBaro(), "getGeoMinusBaro() should return null when unavailable");
	}

	@Test
	public void testIcaoExtraction() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertEquals("a05f21", msg.getAddress().getHexAddress());
	}

	@Test
	public void testSubtype3NotSupersonic() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg("8DA05F219B06B6AF189400CBC33F");
		assertFalse(msg.isSupersonic());
	}

	@Test
	public void testNoHeadingAvailable() throws Exception {
		AirspeedHeadingMsg msg = new AirspeedHeadingMsg(Tools.hexStringToByteArray("8d4400cd9b0000b4f87000e71a10"));
		assertFalse(msg.hasHeadingStatusFlag());
		assertNull(msg.getHeading());
	}
}
