package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdentifyReplyTest {

    @Test
    void testSquawk7000() throws BadFormatException, UnspecifiedFormatError {
        IdentifyReply shortReply = new IdentifyReply("28280a805dcd47");
        Assertions.assertEquals("7000", shortReply.getIdentity(), "Squawk should be 7000");

        CommBIdentifyReply longReply = new CommBIdentifyReply("a8200a8002010000000000946c23");
        Assertions.assertEquals("7000", longReply.getIdentity(), "Squawk should be 7000");
    }
}