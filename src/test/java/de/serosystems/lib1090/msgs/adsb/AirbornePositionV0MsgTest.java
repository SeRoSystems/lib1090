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
import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.msgs.SingleAntennaMsg;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionV0MsgTest {

	@Test
	public void testAltitude39000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), Instant.EPOCH);
		assertEquals(39000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg325() throws Exception {
		// Negative altitude from jet1090 test vectors
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d484fde5803b647ecec4fcdd74f"), Instant.EPOCH);
		assertEquals(-325, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg300() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4845575803c647bcec2a980abc"), Instant.EPOCH);
		assertEquals(-300, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeNeg275() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3424d25803d64c18ee03351f89"), Instant.EPOCH);
		assertEquals(-275, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitudeZero() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4401e458058645a8ea90496290"), Instant.EPOCH);
		assertEquals(0, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude25() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d346355580596459cea86756acc"), Instant.EPOCH);
		assertEquals(25, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude50() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3463555805a64584ea756d352e"), Instant.EPOCH);
		assertEquals(50, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude100() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d3463555805c2d9f6f0f3f1b6c3"), Instant.EPOCH);
		assertEquals(100, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude1000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d346355580b064116e70a269f97"), Instant.EPOCH);
		assertEquals(1000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude5000() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d343386581f06318ad4fecab734"), Instant.EPOCH);
		assertEquals(5000, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude37025() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D06A15358BF17FF7D4A84B47B95"), Instant.EPOCH);
		assertEquals(37025, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude9550() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d45ac2d583561285c4fa686fcdc"), Instant.EPOCH);
		assertEquals(9550, msg.getAltitude().intValue());
	}

	@Test
	public void testAltitude37000_pair1() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d4d224f58bf07c2d41a9a353d70"), Instant.EPOCH);
		assertEquals(37000, msg.getAltitude().intValue());
	}

	@Test
	public void testOddFlagEvenFrame() throws Exception {
		// 8D40058B58C901375147EFD09357: odd_flag=0 (even)
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), Instant.EPOCH);
		assertFalse(msg.getCPREncodedPosition().isOddFormat());
	}

	@Test
	public void testOddFlagOddFrame() throws Exception {
		// 8D40058B58C904A87F402D3B8C59: odd_flag=1 (odd)
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C904A87F402D3B8C59"), Instant.EPOCH);
		assertTrue(msg.getCPREncodedPosition().isOddFormat());
	}

	@Test
	public void testTypeCode11() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), Instant.EPOCH);
		assertEquals(11, msg.getFormatTypeCode());
		assertEquals(Position.AltitudeType.BAROMETRIC_ALTITUDE, msg.getAltitudeType());
	}

	@Test
	public void testTypeCode18() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8d45cab390c39509496ca9a32912"), Instant.EPOCH);
		assertEquals(18, msg.getFormatTypeCode());
	}

	@Test
	public void testSingleAntennaFlagIsExposed() throws Exception {
		AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), Instant.EPOCH);

		assertTrue(msg instanceof SingleAntennaMsg);
		assertFalse(msg.hasSingleAntenna());
		assertTrue(msg.toString().contains("singleAntennaFlag="));
	}
}
