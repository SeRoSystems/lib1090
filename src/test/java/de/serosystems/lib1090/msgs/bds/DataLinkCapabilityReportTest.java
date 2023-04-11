package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataLinkCapabilityReportTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00010000, (byte) 0b11000000, (byte) 0b00000011, (byte) 0b010110011, (byte) 0b11111101,
                (byte) 0b01110010, (byte) 0b01100000
        };

    }

    @Test
    public void bdsCode() {
        assertEquals(10, DataLinkCapabilityReport.extractBdsCode(msg));
    }

    @Test
    public void continuationFlag() {
        assertTrue(DataLinkCapabilityReport.extractContinuationFlag(msg));
    }

    @Test
    public void tcasOperationalCoordinationMessage() {
        assertTrue(DataLinkCapabilityReport.extractTcasOperationalCoordinationMessage(msg));
    }

    @Test
    public void tcasExtendedVersionNumber() {
        assertEquals(0, DataLinkCapabilityReport.extractTcasExtendedVersionNumber(msg));
    }

    @Test
    public void overlayCommandCapability() {
        assertFalse(DataLinkCapabilityReport.extractOverlayCommandCapability(msg));
    }

    @Test
    public void tcasInterfaceOperational() {
        assertFalse(DataLinkCapabilityReport.extractTcasInterfaceOperational(msg));
    }

    @Test
    public void modeSSubNetworkVersionNumber() {
        assertEquals(1, DataLinkCapabilityReport.extractModeSSubNetworkVersionNumber(msg));
    }

    @Test
    public void transponderEnhancedProtocolIndicator() {
        assertTrue(DataLinkCapabilityReport.extractTransponderEnhancedProtocolIndicator(msg));
    }

    @Test
    public void modeSSpecificServicesCapability() {
        assertTrue(DataLinkCapabilityReport.extractModeSSpecificServicesCapability(msg));
    }

    @Test
    public void uelmAverageThroughputCapability() {
        assertEquals(3, DataLinkCapabilityReport.extractUelmAverageThroughputCapability(msg));
    }

    @Test
    public void delmThroughputCapability() {
        assertEquals(3, DataLinkCapabilityReport.extractDelmThroughputCapability(msg));
    }

    @Test
    public void aircraftIdentificationCapability() {
        assertTrue(DataLinkCapabilityReport.extractAircraftIdentificationCapability(msg));
    }

    @Test
    public void squitterCapabilitySubfield() {
        assertTrue(DataLinkCapabilityReport.extractSquitterCapabilitySubfield(msg));
    }

    @Test
    public void surveillanceIdentifierCode() {
        assertTrue(DataLinkCapabilityReport.extractSurveillanceIdentifierCode(msg));
    }

    @Test
    public void commonUsageGICB() {
        assertTrue(DataLinkCapabilityReport.extractCommonUsageGICB(msg));
    }

    @Test
    public void tcasHybridSurveillanceCapability() {
        assertTrue(DataLinkCapabilityReport.extractTcasHybridSurveillanceCapability(msg));
    }

    @Test
    public void tcasRataCapability() {
        assertTrue(DataLinkCapabilityReport.extractTcasRataCapability(msg));
    }

    @Test
    public void tcasVersionNumber() {
        assertEquals(1, DataLinkCapabilityReport.extractTcasVersionNumber(msg));
    }

    @Test
    public void basicDataFlashCapability() {
        assertTrue(DataLinkCapabilityReport.extractBasicDataFlashCapability(msg));
    }

    @Test
    public void phaseOverlayExtendedSquitterCapability() {
        assertTrue(DataLinkCapabilityReport.extractPhaseOverlayExtendedSquitterCapability(msg));
    }

    @Test
    public void phaseOverlayModeSCapability() {
        assertTrue(DataLinkCapabilityReport.extractPhaseOverlayModeSCapability(msg));
    }

    @Test
    public void enhancedSurveillanceCapability() {
        assertTrue(DataLinkCapabilityReport.extractEnhancedSurveillanceCapability(msg));
    }

    @Test
    public void activeTransponderSideIndicator() {
        assertEquals(1, DataLinkCapabilityReport.extractActiveTransponderSideIndicator(msg));
    }

    @Test
    public void extractChangeFlag() {
        assertTrue(DataLinkCapabilityReport.extractChangeFlag(msg));
    }

}
