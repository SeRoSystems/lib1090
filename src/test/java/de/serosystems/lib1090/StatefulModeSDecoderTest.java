package de.serosystems.lib1090;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.adsb.OperationalStatusMsgTest;
import de.serosystems.lib1090.msgs.adsb.SurfacePositionV2Msg;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsg;
import de.serosystems.lib1090.msgs.adsb.TargetStateAndStatusMsgTest;
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

	// surface position with type code 8, i.e. NIC depends on NIC supplements A and C
	private static final String SURFACE_POSITION_TC8 = "8D4D013140000000000000000000";

	private static String surfaceOpStatus(int cc, int version) {
		return String.format("8D4D0131F9%03X00000%02X00000000", cc, version << 5);
	}

	@Test
	public void surfaceOpStatusV2_shouldPassNICSupplementCToSurfacePosition() throws UnspecifiedFormatError, BadFormatException {
		decoder.decode(surfaceOpStatus(0x001, 2), 0L);

		final ModeSDownlinkMsg reply = decoder.decode(SURFACE_POSITION_TC8, 0L);
		assertEquals(ModeSDownlinkMsg.subtype.ADSB_SURFACE_POSITION_V2, reply.getType());
		assertTrue(((SurfacePositionV2Msg) reply).hasNICSupplementC());
		assertEquals(6, ((SurfacePositionV2Msg) reply).getNIC());
	}

	@Test
	public void surfaceOpStatusV2UATIn_shouldNotSetNICSupplementC() throws UnspecifiedFormatError, BadFormatException {
		// UAT IN and NACv set, but not NIC supplement C
		decoder.decode(surfaceOpStatus(0x01E, 2), 0L);

		final ModeSDownlinkMsg reply = decoder.decode(SURFACE_POSITION_TC8, 0L);
		assertEquals(ModeSDownlinkMsg.subtype.ADSB_SURFACE_POSITION_V2, reply.getType());
		assertFalse(((SurfacePositionV2Msg) reply).hasNICSupplementC());
		assertEquals(0, ((SurfacePositionV2Msg) reply).getNIC());
	}

}
