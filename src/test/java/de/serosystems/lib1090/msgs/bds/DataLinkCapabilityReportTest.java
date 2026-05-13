/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        assertEquals(2, DataLinkCapabilityReport.extractTcasVersionNumber(msg));
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

    @Test
    void tcacsVersion0() {
       byte[] message =  new byte[]{
               (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
               (byte) 0b11111100,
               (byte) 0xff, (byte) 0xff
       };
        assertEquals(0, DataLinkCapabilityReport.extractTcasVersionNumber(message));
    }

    @Test
    void tcacsVersion1() {
        byte[] message =  new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111110,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(1, DataLinkCapabilityReport.extractTcasVersionNumber(message));
    }

    @Test
    void tcacsVersion2() {
        byte[] message =  new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111101,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(2, DataLinkCapabilityReport.extractTcasVersionNumber(message));
    }

    @Test
    void tcacsVersion3() {
        byte[] message =  new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111111,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(3, DataLinkCapabilityReport.extractTcasVersionNumber(message));
    }
}
