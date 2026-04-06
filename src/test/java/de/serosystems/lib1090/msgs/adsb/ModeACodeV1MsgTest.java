package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModeACodeV1MsgTest {

	public static final String MODE_A_CODE_V1 = "8d89653ebf95b00000000059abfb";

	@Test
	void testSubtype() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals(7, msg.getSubtype());
	}

	@Test
	void testSquawkCode() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals("6513", msg.getIdentity());
	}

	@Test
	void testTypeCode23() throws BadFormatException, UnspecifiedFormatError {
		ModeACodeV1Msg msg = new ModeACodeV1Msg(MODE_A_CODE_V1);
		assertEquals(23, msg.getFormatTypeCode());
	}
}
