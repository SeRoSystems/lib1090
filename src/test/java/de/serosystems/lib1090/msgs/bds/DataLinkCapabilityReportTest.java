package de.serosystems.lib1090.msgs.bds;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataLinkCapabilityReportTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00010000, (byte) 0b11000000, (byte) 0b00000011, (byte) 0b010110011, (byte) 0b11111101,
                (byte) 0b01110010, (byte) 0b01100000
        };

    }

    @Test
    public void bdsCode() {
        Assert.assertEquals(10, DataLinkCapabilityReport.extractBdsCode(msg));
    }

    @Test
    public void continuationFlag() {
        Assert.assertTrue(DataLinkCapabilityReport.extractContinuationFlag(msg));
    }

    @Test
    public void tcasOperationalCoordinationMessage() {
        Assert.assertTrue(DataLinkCapabilityReport.extractTcasOperationalCoordinationMessage(msg));
    }

    @Test
    public void tcasExtendedVersionNumber() {
        Assert.assertEquals(0, DataLinkCapabilityReport.extractTcasExtendedVersionNumber(msg));
    }

    @Test
    public void overlayCommandCapability() {
        Assert.assertFalse(DataLinkCapabilityReport.extractOverlayCommandCapability(msg));
    }

    @Test
    public void tcasInterfaceOperational() {
        Assert.assertFalse(DataLinkCapabilityReport.extractTcasInterfaceOperational(msg));
    }

    @Test
    public void modeSSubNetworkVersionNumber() {
        Assert.assertEquals(1, DataLinkCapabilityReport.extractModeSSubNetworkVersionNumber(msg));
    }

    @Test
    public void transponderEnhancedProtocolIndicator() {
        Assert.assertTrue(DataLinkCapabilityReport.extractTransponderEnhancedProtocolIndicator(msg));
    }

    @Test
    public void modeSSpecificServicesCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractModeSSpecificServicesCapability(msg));
    }

    @Test
    public void uelmAverageThroughputCapability() {
        Assert.assertEquals(3, DataLinkCapabilityReport.extractUelmAverageThroughputCapability(msg));
    }

    @Test
    public void delmThroughputCapability() {
        Assert.assertEquals(3, DataLinkCapabilityReport.extractDelmThroughputCapability(msg));
    }

    @Test
    public void aircraftIdentificationCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractAircraftIdentificationCapability(msg));
    }

    @Test
    public void squitterCapabilitySubfield() {
        Assert.assertTrue(DataLinkCapabilityReport.extractSquitterCapabilitySubfield(msg));
    }

    @Test
    public void surveillanceIdentifierCode() {
        Assert.assertTrue(DataLinkCapabilityReport.extractSurveillanceIdentifierCode(msg));
    }

    @Test
    public void commonUsageGICB() {
        Assert.assertTrue(DataLinkCapabilityReport.extractCommonUsageGICB(msg));
    }

    @Test
    public void tcasHybridSurveillanceCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractTcasHybridSurveillanceCapability(msg));
    }

    @Test
    public void tcasRataCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractTcasRataCapability(msg));
    }

    @Test
    public void tcasVersionNumber() {
        Assert.assertEquals(1, DataLinkCapabilityReport.extractTcasVersionNumber(msg));
    }

    @Test
    public void basicDataFlashCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractBasicDataFlashCapability(msg));
    }

    @Test
    public void phaseOverlayExtendedSquitterCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractPhaseOverlayExtendedSquitterCapability(msg));
    }

    @Test
    public void phaseOverlayModeSCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractPhaseOverlayModeSCapability(msg));
    }

    @Test
    public void enhancedSurveillanceCapability() {
        Assert.assertTrue(DataLinkCapabilityReport.extractEnhancedSurveillanceCapability(msg));
    }

    @Test
    public void activeTransponderSideIndicator() {
        Assert.assertEquals(1, DataLinkCapabilityReport.extractActiveTransponderSideIndicator(msg));
    }

    @Test
    public void extractChangeFlag() {
        Assert.assertTrue(DataLinkCapabilityReport.extractChangeFlag(msg));
    }

}
