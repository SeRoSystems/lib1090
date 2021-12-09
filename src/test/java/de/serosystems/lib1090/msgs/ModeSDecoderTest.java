package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsg;
import org.junit.Before;
import org.junit.Test;

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
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.EXTENDED_SQUITTER, reply.getType());
		assertNotEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());
	}

	@Test
	public void tssV2ME11Set_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		// tell decoder that the aircraft uses ADS-B v2
		decoder.decode(OperationalStatusMsgTest.A_OPSTAT_V2, 0L);

		// decode message with ME bit 11 set
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusMsg tss = (TargetStateAndStatusMsg) reply;

		assertFalse(tss.hasSILSupplement());
		assertFalse(tss.isFMSSelectedAltitude());
		assertEquals((907 - 1) * 32, tss.getSelectedAltitude().intValue());
	}

	@Test
	public void tssV0Me11NotSet_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgTest.TSS_WITHOUT_HEADING, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusMsg tss = (TargetStateAndStatusMsg) reply;

		assertFalse(tss.hasSelectedHeadingInfo());
	}

}
