package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class SelectedVerticalIntentionTest {

    private static byte[] msg;

    @BeforeAll
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

        assertTrue(status);
        assertEquals(188, value);
        assertEquals(3008, selectedAltitude.intValue());
    }

    @Test
    public void fmsSelectedAltitude() {
        boolean status = SelectedVerticalIntention.extractFmsSelectedAltitudeStatus(msg);
        int value = SelectedVerticalIntention.extractFmsSelectedAltitudeValue(msg);
        Integer fmsSelectedAltitude = SelectedVerticalIntention.computeSelectedAltitude(status, value);

        assertTrue(status);
        assertEquals(188, value);
        assertEquals(3008, fmsSelectedAltitude.intValue());
    }

    @Test
    public void barometricPressureSetting() {
        boolean status = SelectedVerticalIntention.extractBarometricPressureSettingStatus(msg);
        float value = SelectedVerticalIntention.extractBarometricPressureSettingValue(msg);
        Float barometricPressureSetting = SelectedVerticalIntention.computeBarometricPressureSetting(status, value);

        assertTrue(status);
        assertEquals(2200, value ,0.0);
        assertEquals(1020, barometricPressureSetting, 0.0);
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

        assertFalse(status);
        assertFalse(value1);
        assertFalse(value2);
        assertFalse(value3);
        assertNull(vnav);
        assertNull(altHold);
        assertNull(approach);
    }

    @Test
    public void targetAltSource() {
        boolean status = SelectedVerticalIntention.extractTargetAltSourceStatus(msg);
        short value = SelectedVerticalIntention.extractTargetAltSourceValue(msg);
        Short targetAltSource = SelectedVerticalIntention.computeTargetAltSource(status, value);

        assertFalse(status);
        assertEquals(0, value);
        assertNull(targetAltSource);
    }

}
