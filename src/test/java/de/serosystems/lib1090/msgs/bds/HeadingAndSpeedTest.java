package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class HeadingAndSpeedTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b11111111, (byte) 0b10111010, (byte) 0b10100001, (byte) 0b00011110, (byte) 0b00100000,
                (byte) 0b00000100, (byte) 0b01110010
        };

    }

    @Test
    public void magneticHeading(){

        boolean status = HeadingAndSpeed.extractMagneticHeadingStatus(msg);
        boolean sign = HeadingAndSpeed.extractMagneticHeadingSign(msg);
        short value = HeadingAndSpeed.extractMagneticHeadingValue(msg);
        Float magneticHeading = HeadingAndSpeed.computeMagneticHeading(true, true, value);

        assertTrue(status);
        assertTrue(sign);
        assertEquals(1019, value);
        assertNotNull(magneticHeading);
        assertEquals(-0.87, magneticHeading, 0.1);

    }

    @Test
    public void indicatedAirspeed(){

        boolean status = HeadingAndSpeed.extractIndicatedAirspeedStatus(msg);
        short value = HeadingAndSpeed.extractIndicatedAirspeedValue(msg);
        Short indicatedAirspeed = HeadingAndSpeed.computeIndicatedAirspeed(true, value);

        assertTrue(status);
        assertEquals(336, value);
        assertNotNull(indicatedAirspeed);
        assertEquals(336, indicatedAirspeed, 0.1);

    }

    @Test
    public void matchNumber(){

        boolean status = HeadingAndSpeed.extractMatchNumberStatus(msg);
        short value = HeadingAndSpeed.extractMatchNumberValue(msg);
        Float matchNumber = HeadingAndSpeed.computeMatchNumber(true, value);

        assertTrue(status);
        assertEquals(120, value);
        assertNotNull(matchNumber);
        assertEquals(0.48, matchNumber, 0.1);

    }

    @Test
    public void barometricAltitudeRate(){

        boolean status = HeadingAndSpeed.extractBarometricAltitudeRateStatus(msg);
        boolean sign = HeadingAndSpeed.extractBarometricAltitudeRateSign(msg);
        short value = HeadingAndSpeed.extractBarometricAltitudeRateValue(msg);
        Integer barometricAltitudeRate = HeadingAndSpeed.computeBarometricAltitude(true, false, value);

        assertTrue(status);
        assertFalse(sign);
        assertEquals(0, value);
        assertNotNull(barometricAltitudeRate);
        assertEquals(0, barometricAltitudeRate, 0.1);

    }

    @Test
    public void inertialVerticalRate(){

        boolean status = HeadingAndSpeed.extractInertialVerticalRateStatus(msg);
        boolean sign = HeadingAndSpeed.extractInertialVerticalRateSign(msg);
        short value = HeadingAndSpeed.extractInertialVerticalRateValue(msg);
        Integer inertialVerticalRate = HeadingAndSpeed.computeInertialVerticalRate(true, false, value);

        assertTrue(status);
        assertFalse(sign);
        assertEquals(114, value);
        assertNotNull(inertialVerticalRate);
        assertEquals(3648, inertialVerticalRate, 0.1);

    }

}
