package de.serosystems.lib1090.msgs.bds;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TrackAndTurnTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b10000001, (byte) 0b10010101, (byte) 0b00010101, (byte) 0b00110110, (byte) 0b11100000,
                (byte) 0b00100100, (byte) 0b11010100
        };

    }

    @Test
    public void rollAngle(){

        boolean status = TrackAndTurn.extractRollAngleStatus(msg);
        boolean sign = TrackAndTurn.extractRollAngleSign(msg);
        short value = TrackAndTurn.extractRollAngleValue(msg);
        Float rollAngle = TrackAndTurn.computeRollAngle(true, true, value);

        Assert.assertTrue(status);
        Assert.assertFalse(sign);
        Assert.assertEquals(12, value);
        Assert.assertNotNull(rollAngle);
        Assert.assertEquals(-87.9, rollAngle, 0.1);

    }

    @Test
    public void trueTrackAngle() {

        boolean status = TrackAndTurn.extractTrueTrackAngleStatus(msg);
        boolean sign = TrackAndTurn.extractTrueTrackAngleSign(msg);
        short value = TrackAndTurn.extractTrueTrackAngleValue(msg);
        Float trueTrackAngle = TrackAndTurn.computeTrueTrackAngle(true, true, value);

        Assert.assertTrue(status);
        Assert.assertFalse(sign);
        Assert.assertEquals(650, value);
        Assert.assertNotNull(trueTrackAngle);
        Assert.assertEquals(-65.7, trueTrackAngle, 0.1);

    }

    @Test
    public void groundSpeed() {

        boolean status = TrackAndTurn.extractGroundSpeedStatus(msg);
        short value = TrackAndTurn.extractGroundSpeedValue(msg);
        Integer groundSpeed = TrackAndTurn.computeGroundSpeed(true, value);

        Assert.assertTrue(status);
        Assert.assertEquals(219, value);
        Assert.assertNotNull(groundSpeed);
        Assert.assertEquals(438, groundSpeed.intValue());

    }

    @Test
    public void trackAngleRate() {

        boolean status = TrackAndTurn.extractTrackAngleRateStatus(msg);
        boolean sign = TrackAndTurn.extractTrackAngleRateSign(msg);
        short value = TrackAndTurn.extractTrackAngleRateValue(msg);
        Float trackAngleRate = TrackAndTurn.computeTrackAngleRate(true, true, value);

        Assert.assertTrue(status);
        Assert.assertFalse(sign);
        Assert.assertEquals(4, value);
        Assert.assertNotNull(trackAngleRate);
        Assert.assertEquals(-15.8, trackAngleRate, 0.1);

    }

    @Test
    public void trueAirSpeed() {

        boolean status = TrackAndTurn.extractTrueAirSpeedStatus(msg);
        short value = TrackAndTurn.extractTrueAirSpeedValue(msg);
        Integer trueAirspeed = TrackAndTurn.computeTrueAirSpeed(true, value);

        Assert.assertTrue(status);
        Assert.assertEquals(212, value);
        Assert.assertNotNull(trueAirspeed);
        Assert.assertEquals(424, trueAirspeed.intValue());

    }

}
