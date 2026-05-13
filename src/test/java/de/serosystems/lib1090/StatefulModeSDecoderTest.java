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

package de.serosystems.lib1090;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.adsb.ModeACodeV1Msg;
import de.serosystems.lib1090.msgs.adsb.ModeACodeV1MsgTest;
import de.serosystems.lib1090.msgs.adsb.OperationalStatusMsgTest;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusV1MsgTest;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusV2Msg;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusV2MsgTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class StatefulModeSDecoderTest {

	private StatefulModeSDecoder decoder;

	@BeforeEach
	public void setUp() {
		decoder = new StatefulModeSDecoder();
	}

	@Test
	public void tssV0Me11Set_shouldNotDecode() throws UnspecifiedFormatError, BadFormatException {
		// decoder assumes ADS-B v0 and should not decode TSS
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV2MsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.EXTENDED_SQUITTER, reply.getType());
		assertNotEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());
	}

	@Test
	public void tssV2ME11Set_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		// tell decoder that the aircraft uses ADS-B v2
		decoder.decode(OperationalStatusMsgTest.A_OPSTAT_V2, 0L);

		// decode message with ME bit 11 set
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV2MsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusV2Msg tss = (TargetStateAndStatusV2Msg) reply;

		assertFalse(tss.hasSILSupplement());
		assertFalse(tss.isFMSSelectedAltitude());
		assertEquals((907 - 1) * 32, tss.getSelectedAltitude().intValue());
	}

	@Test
	public void tssV1_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		decoder.decode(TargetStateAndStatusV1MsgTest.A_OPSTAT_V1, 0L);

		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV1MsgTest.TSS_V1, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		assertTrue(reply instanceof TargetStateAndStatusV1Msg);

		TargetStateAndStatusV1Msg tss = (TargetStateAndStatusV1Msg) reply;

		assertEquals(9, tss.getNACp());
	}

	@Test
	public void modeACodeV1_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		decoder.decode(TargetStateAndStatusV1MsgTest.A_OPSTAT_V1, 0L);

		final ModeSDownlinkMsg reply = decoder.decode(ModeACodeV1MsgTest.MODE_A_CODE_V1, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_MODE_A_CODE_V1, reply.getType());
		assertTrue(reply instanceof ModeACodeV1Msg);
		assertEquals("6513", ((ModeACodeV1Msg) reply).getIdentity());
	}

}
