package de.serosystems.lib1090;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.adsb.OperationalStatusMsgTest;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsgV1;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsgV1Test;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsgV2;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsgV2Test;
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
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgV2Test.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.EXTENDED_SQUITTER, reply.getType());
		assertNotEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());
	}

	@Test
	public void tssV2ME11Set_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		// tell decoder that the aircraft uses ADS-B v2
		decoder.decode(OperationalStatusMsgTest.A_OPSTAT_V2, 0L);

		// decode message with ME bit 11 set
		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgV2Test.TSS_WITH_ME11_BIT_SET, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		TargetStateAndStatusMsgV2 tss = (TargetStateAndStatusMsgV2) reply;

		assertFalse(tss.hasSILSupplement());
		assertFalse(tss.isFMSSelectedAltitude());
		assertEquals((907 - 1) * 32, tss.getSelectedAltitude().intValue());
	}

	@Test
	public void tssV1_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
		decoder.decode(TargetStateAndStatusMsgV1Test.A_OPSTAT_V1, 0L);

		final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusMsgV1Test.TSS_V1, 0L);

		assertEquals(ModeSDownlinkMsg.subtype.ADSB_TARGET_STATE_AND_STATUS, reply.getType());

		assertTrue(reply instanceof TargetStateAndStatusMsgV1);

		TargetStateAndStatusMsgV1 tss = (TargetStateAndStatusMsgV1) reply;

		assertEquals(9, tss.getNACp());
	}

}
