package de.serosystems.lib1090.msgs.modes;

import org.apache.commons.lang3.StringUtils;
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

        for (byte b : msg)
            System.out.print(StringUtils.leftPad(Integer.toBinaryString(b & 0xFF), 8, '0') + " ");

    }

    @Test
    public void bdsCode() {

        int bds = AircraftIdentification.extractBdsCode(msg);
        Assert.assertEquals(20, bds);

    }


    @Test
    public void aircraftIdentification() {

        byte[] identityByteArray = AircraftIdentification.extractAircraftIdentification(msg);
        char[] identityCharArray = AircraftIdentification.mapChar(identityByteArray);

        Assert.assertEquals("KLM1017 ", String.valueOf(identityCharArray));

    }


}
