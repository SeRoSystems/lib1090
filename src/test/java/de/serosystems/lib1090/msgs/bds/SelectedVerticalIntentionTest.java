package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.msgs.bds.SelectedVerticalIntention;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SelectedVerticalIntentionTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b10000101, (byte) 0b11100100, (byte) 0b00101111, (byte) 0b00110001, (byte) 0b00110000,
                (byte) 0b00000000, (byte) 0b00000000
        };

    }

    @Test
    public void mcpFcuSelectedAltitude() {
        boolean status = SelectedVerticalIntention.extractMcpFcuSelectedAltitudeStatus(msg);
        int value = SelectedVerticalIntention.extractMcpFcuSelectedAltitudeValue(msg);
        Integer selectedAltitude = SelectedVerticalIntention.computeSelectedAltitude(status, value);

        Assert.assertTrue(status);
        Assert.assertEquals(188, value);
        Assert.assertEquals(3008, selectedAltitude.intValue());
    }

    @Test
    public void fmsSelectedAltitude() {
        boolean status = SelectedVerticalIntention.extractFmsSelectedAltitudeStatus(msg);
        int value = SelectedVerticalIntention.extractFmsSelectedAltitudeValue(msg);
        Integer fmsSelectedAltitude = SelectedVerticalIntention.computeSelectedAltitude(status, value);

        Assert.assertTrue(status);
        Assert.assertEquals(188, value);
        Assert.assertEquals(3008, fmsSelectedAltitude.intValue());
    }

    @Test
    public void barometricPressureSetting() {
        boolean status = SelectedVerticalIntention.extractBarometricPressureSettingStatus(msg);
        float value = SelectedVerticalIntention.extractBarometricPressureSettingValue(msg);
        Float barometricPressureSetting = SelectedVerticalIntention.computeBarometricPressureSetting(status, value);

        Assert.assertTrue(status);
        Assert.assertEquals(2200, value ,0.0);
        Assert.assertEquals(1020, barometricPressureSetting, 0.0);
    }

    @Test
    public void others() {
        boolean status = SelectedVerticalIntention.extractOtherStatus(msg);
        boolean value1 = SelectedVerticalIntention.extractVnavValue(msg);
        boolean value2 = SelectedVerticalIntention.extractAltHoldValue(msg);
        boolean value3 = SelectedVerticalIntention.extractApproachValue(msg);
        Boolean vnav = SelectedVerticalIntention.computeOthers(status, value1);
        Boolean altHold = SelectedVerticalIntention.computeOthers(status, value2);
        Boolean approach = SelectedVerticalIntention.computeOthers(status, value3);

        Assert.assertFalse(status);
        Assert.assertFalse(value1);
        Assert.assertFalse(value2);
        Assert.assertFalse(value3);
        Assert.assertNull(vnav);
        Assert.assertNull(altHold);
        Assert.assertNull(approach);
    }

    @Test
    public void targetAltSource() {
        boolean status = SelectedVerticalIntention.extractTargetAltSourceStatus(msg);
        short value = SelectedVerticalIntention.extractTargetAltSourceValue(msg);
        Short targetAltSource = SelectedVerticalIntention.computeTargetAltSource(status, value);

        Assert.assertFalse(status);
        Assert.assertEquals(0, value);
        Assert.assertNull(targetAltSource);
    }

}
