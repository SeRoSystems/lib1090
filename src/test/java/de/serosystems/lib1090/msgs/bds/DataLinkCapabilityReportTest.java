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
    public void continuationFlag() {
        assertTrue(new DataLinkCapabilityReport(msg).isContinuationFlag());
    }

    @Test
    public void tcasOperationalCoordinationMessage() {
        assertTrue(new DataLinkCapabilityReport(msg).isTcasOperationalCoordinationMessage());
    }

    @Test
    public void tcasExtendedVersionNumber() {
        assertEquals(0, new DataLinkCapabilityReport(msg).getTcasExtendedVersionNumber());
    }

    @Test
    public void overlayCommandCapability() {
        assertFalse(new DataLinkCapabilityReport(msg).isOverlayCommandCapability());
    }

    @Test
    public void tcasInterfaceOperational() {
        assertFalse(new DataLinkCapabilityReport(msg).isTcasInterfaceOperational());
    }

    @Test
    public void modeSSubNetworkVersionNumber() {
        assertEquals(1, new DataLinkCapabilityReport(msg).getModeSSubNetworkVersionNumber());
    }

    @Test
    public void transponderEnhancedProtocolIndicator() {
        assertTrue(new DataLinkCapabilityReport(msg).isTransponderEnhancedProtocolIndicator());
    }

    @Test
    public void modeSSpecificServicesCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isModeSSpecificServicesCapability());
    }

    @Test
    public void uelmAverageThroughputCapability() {
        assertEquals(3, new DataLinkCapabilityReport(msg).getUelmAverageThroughputCapability());
    }

    @Test
    public void delmThroughputCapability() {
        assertEquals(3, new DataLinkCapabilityReport(msg).getDelmThroughputCapability());
    }

    @Test
    public void aircraftIdentificationCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isAircraftIdentificationCapability());
    }

    @Test
    public void squitterCapabilitySubfield() {
        assertTrue(new DataLinkCapabilityReport(msg).isSquitterCapabilitySubfield());
    }

    @Test
    public void surveillanceIdentifierCode() {
        assertTrue(new DataLinkCapabilityReport(msg).isSurveillanceIdentifierCode());
    }

    @Test
    public void commonUsageGICB() {
        assertTrue(new DataLinkCapabilityReport(msg).isCommonUsageGicb());
    }

    @Test
    public void tcasHybridSurveillanceCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isTcasHybridSurveillanceCapability());
    }

    @Test
    public void tcasRataCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isTcasRataCapability());
    }

    @Test
    public void tcasVersionNumber() {
        assertEquals(2, new DataLinkCapabilityReport(msg).getTcasVersionNumber());
    }

    @Test
    public void basicDataFlashCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isBasicDataFlashCapability());
    }

    @Test
    public void phaseOverlayExtendedSquitterCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isPhaseOverlayExtendedSquitterCapability());
    }

    @Test
    public void phaseOverlayModeSCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isPhaseOverlayModeSCapability());
    }

    @Test
    public void enhancedSurveillanceCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isEnhancedSurveillanceCapability());
    }

    @Test
    public void activeTransponderSideIndicator() {
        assertEquals(1, new DataLinkCapabilityReport(msg).getActiveTransponderSideIndicator());
    }

    @Test
    public void changeFlag() {
        assertTrue(new DataLinkCapabilityReport(msg).isChangeFlag());
    }

    @Test
    void tcacsVersion0() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111100,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(0, new DataLinkCapabilityReport(message).getTcasVersionNumber());
    }

    @Test
    void tcacsVersion1() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111110,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(1, new DataLinkCapabilityReport(message).getTcasVersionNumber());
    }

    @Test
    void tcacsVersion2() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111101,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(2, new DataLinkCapabilityReport(message).getTcasVersionNumber());
    }

    @Test
    void tcacsVersion3() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111111,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(3, new DataLinkCapabilityReport(message).getTcasVersionNumber());
    }
}
