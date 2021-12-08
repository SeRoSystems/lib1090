package org.opensky.libadsb.msgs;

import org.junit.Before;
import org.junit.Test;
import org.opensky.libadsb.StatefulModeSDecoder;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.adsb.TargetStateAndStatusMsg;
import org.opensky.libadsb.msgs.modes.ModeSReply;

import static org.junit.Assert.*;


/**
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class ModeSDecoderTest {

	private StatefulModeSDecoder decoder;

	@Before
	public void setUp() {
		decoder = new StatefulModeSDecoder();
	}

	@Test
	public void tssV0Me11Set_shouldNotDecode() throws UnspecifiedFormatError, BadFormatException {
		// decoder assumes ADS-B v0 and should not decode TSS
		final ModeSReply reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSReply.subtype.EXTENDED_SQUITTER, reply.getType());
		assertNotEquals(ModeSReply.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());
	}

	@Test
	public void tssV2ME11Set_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		// tell decoder that the aircraft uses ADS-B v2
		decoder.decode(OperationalStatusMsgTest.A_OPSTAT_V2, 0L);

		// decode message with ME bit 11 set
		final ModeSReply reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSReply.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusMsg tss = (TargetStateAndStatusMsg) reply;

		assertFalse(tss.hasSILSupplement());
		assertFalse(tss.isFMSSelectedAltitude());
		assertEquals((907 - 1) * 32, tss.getSelectedAltitude().intValue());
	}

	@Test
	public void tssV0Me11NotSet_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		final ModeSReply reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITHOUT_HEADING, 0L);

		assertEquals(ModeSReply.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusMsg tss = (TargetStateAndStatusMsg) reply;

		assertFalse(tss.hasSelectedHeadingInfo());
	}

}
