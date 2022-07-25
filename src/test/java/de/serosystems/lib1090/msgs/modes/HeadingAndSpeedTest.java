package de.serosystems.lib1090.msgs.modes;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeadingAndSpeedTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b11111111, (byte) 0b10111010, (byte) 0b10100001, (byte) 0b00011110, (byte) 0b00100000,
                (byte) 0b00000100, (byte) 0b01110010
        };

        for (byte b : msg)
            System.out.print(StringUtils.leftPad(Integer.toBinaryString(b & 0xFF), 8, '0') + " ");

    }

    @Test
    public void magneticHeading(){

        boolean status = HeadingAndSpeed.extractMagneticHeadingStatus(msg);
        boolean sign = HeadingAndSpeed.extractMagneticHeadingSign(msg);
        short value = HeadingAndSpeed.extractMagneticHeadingValue(msg);
        Float magneticHeading = HeadingAndSpeed.computeMagneticHeading(true, true, value);

        Assert.assertTrue(status);
        Assert.assertTrue(sign);
        Assert.assertEquals(1019, value);
        Assert.assertNotNull(magneticHeading);
        Assert.assertEquals(-0.87, magneticHeading, 0.1);

    }

    @Test
    public void indicatedAirspeed(){

        boolean status = HeadingAndSpeed.extractIndicatedAirspeedStatus(msg);
        short value = HeadingAndSpeed.extractIndicatedAirspeedValue(msg);
        Short indicatedAirspeed = HeadingAndSpeed.computeIndicatedAirspeed(true, value);

        Assert.assertTrue(status);
        Assert.assertEquals(336, value);
        Assert.assertNotNull(indicatedAirspeed);
        Assert.assertEquals(336, indicatedAirspeed, 0.1);

    }

    @Test
    public void matchNumber(){

        boolean status = HeadingAndSpeed.extractMatchNumberStatus(msg);
        short value = HeadingAndSpeed.extractMatchNumberValue(msg);
        Float matchNumber = HeadingAndSpeed.computeMatchNumber(true, value);

        Assert.assertTrue(status);
        Assert.assertEquals(120, value);
        Assert.assertNotNull(matchNumber);
        Assert.assertEquals(0.48, matchNumber, 0.1);

    }

    @Test
    public void barometricAltitudeRate(){

        boolean status = HeadingAndSpeed.extractBarometricAltitudeRateStatus(msg);
        boolean sign = HeadingAndSpeed.extractBarometricAltitudeRateSign(msg);
        short value = HeadingAndSpeed.extractBarometricAltitudeRateValue(msg);
        Integer barometricAltitudeRate = HeadingAndSpeed.computeBarometricAltitude(true, false, value);

        Assert.assertTrue(status);
        Assert.assertFalse(sign);
        Assert.assertEquals(0, value);
        Assert.assertNotNull(barometricAltitudeRate);
        Assert.assertEquals(0, barometricAltitudeRate, 0.1);

    }

    @Test
    public void inertialVerticalRate(){

        boolean status = HeadingAndSpeed.extractInertialVerticalRateStatus(msg);
        boolean sign = HeadingAndSpeed.extractInertialVerticalRateSign(msg);
        short value = HeadingAndSpeed.extractInertialVerticalRateValue(msg);
        Integer inertialVerticalRate = HeadingAndSpeed.computeInertialVerticalRate(true, false, value);

        Assert.assertTrue(status);
        Assert.assertFalse(sign);
        Assert.assertEquals(114, value);
        Assert.assertNotNull(inertialVerticalRate);
        Assert.assertEquals(3648, inertialVerticalRate, 0.1);

    }

}
