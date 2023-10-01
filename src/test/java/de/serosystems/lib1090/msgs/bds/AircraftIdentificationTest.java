package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.Identification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AircraftIdentificationTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00100000, (byte) 0b00101100, (byte) 0b11000011, (byte) 0b01110001, (byte) 0b11000011,
                (byte) 0b00011101, (byte) 0b11100000
        };

    }

    @Test
    public void bdsCode() {

        short bds = AircraftIdentification.extractBdsCode(msg);
        assertEquals(20, bds);

    }


    @Test
    public void aircraftIdentification() {

        byte[] identityByteArray = Identification.decodeAircraftIdentification(msg);
        char[] identityCharArray = Identification.mapChar(identityByteArray);

        assertEquals("KLM1017 ", String.valueOf(identityCharArray));

    }
    
}
