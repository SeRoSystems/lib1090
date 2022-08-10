package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.msgs.adsb.IdentificationMsg;
import de.serosystems.lib1090.msgs.bds.AircraftIdentification;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AircraftIdentificationTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00100000, (byte) 0b00101100, (byte) 0b11000011, (byte) 0b01110001, (byte) 0b11000011,
                (byte) 0b00011101, (byte) 0b11100000
        };

    }

    @Test
    public void bdsCode() {

        short bds = AircraftIdentification.extractBdsCode(msg);
        Assert.assertEquals(20, bds);

    }


    @Test
    public void aircraftIdentification() {

        byte[] identityByteArray = IdentificationMsg.decodeAircraftIdentification(msg);
        char[] identityCharArray = IdentificationMsg.mapChar(identityByteArray);

        Assert.assertEquals("KLM1017 ", String.valueOf(identityCharArray));

    }
    
}
