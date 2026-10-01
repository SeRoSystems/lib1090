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

import de.serosystems.lib1090.msgs.acas.ACASType;
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
    public void ocmTransmitCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isOCMTransmitCapability());
    }

    @Test
    public void acasType() {
        assertEquals(0, new DataLinkCapabilityReport(msg).getACASTypeEncoded());
        assertEquals(ACASType.TCAS_OR_OTHER, new DataLinkCapabilityReport(msg).getACASType());
    }

    /**
     * MB bits 11-14, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3: 0001 is ACAS Xa, 0010 to 1111 are reserved
     * for ACAS III.
     */
    @Test
    public void acasType_acasXaAndReserved() {
        byte[] acasXa = {0x10, 0x04, 0, 0, 0, 0, 0}; // MB bit 14
        assertEquals(1, new DataLinkCapabilityReport(acasXa).getACASTypeEncoded());
        assertEquals(ACASType.ACAS_XA, new DataLinkCapabilityReport(acasXa).getACASType());

        byte[] reserved = {0x10, 0x3C, 0, 0, 0, 0, 0}; // MB bits 11-14 = 1111
        assertEquals(15, new DataLinkCapabilityReport(reserved).getACASTypeEncoded());
        assertEquals(ACASType.ACAS_III, new DataLinkCapabilityReport(reserved).getACASType());
    }

    @Test
    public void overlayCommandCapability() {
        assertFalse(new DataLinkCapabilityReport(msg).isOverlayCommandCapability());
    }

    @Test
    public void acasOperating() {
        assertFalse(new DataLinkCapabilityReport(msg).isACASOperating());
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
    public void acasHybridSurveillanceCapability() {
        assertTrue(new DataLinkCapabilityReport(msg).isACASHybridSurveillanceCapability());
    }

    @Test
    public void acasGeneratingRAs() {
        assertTrue(new DataLinkCapabilityReport(msg).isACASGeneratingRAs());
    }

    @Test
    public void acasVersionNumber() {
        assertEquals(2, new DataLinkCapabilityReport(msg).getACASVersionNumber());
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
    public void activeTransponderSideIndicator() {
        assertEquals(1, new DataLinkCapabilityReport(msg).getActiveTransponderSideIndicator());
    }

    @Test
    public void changeFlag() {
        assertTrue(new DataLinkCapabilityReport(msg).isChangeFlag());
    }

    @Test
    void acasVersion0() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111100,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(0, new DataLinkCapabilityReport(message).getACASVersionNumber());
    }

    @Test
    void acasVersion1() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111110,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(1, new DataLinkCapabilityReport(message).getACASVersionNumber());
    }

    @Test
    void acasVersion2() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111101,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(2, new DataLinkCapabilityReport(message).getACASVersionNumber());
    }

    @Test
    void acasVersion3() {
        byte[] message = new byte[]{
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0b11111111,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(3, new DataLinkCapabilityReport(message).getACASVersionNumber());
    }
}
