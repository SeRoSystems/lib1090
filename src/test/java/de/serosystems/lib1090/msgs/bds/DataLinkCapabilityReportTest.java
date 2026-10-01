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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
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
    public void continuationFlag() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isContinuationFlag());
    }

    @Test
    public void ocmTransmitCapability() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isOCMTransmitCapability());
    }

    @Test
    public void acasType() throws BadFormatException {
        assertEquals(0, DataLinkCapabilityReport.decode(msg).getACASTypeEncoded());
        assertEquals(ACASType.TCAS_OR_OTHER, DataLinkCapabilityReport.decode(msg).getACASType());
    }

    /**
     * MB bits 11-14, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3: 0001 is ACAS Xa, 0010 to 1111 are reserved
     * for ACAS III.
     */
    @Test
    public void acasType_acasXaAndReserved() throws BadFormatException {
        byte[] acasXa = {0x10, 0x04, 0, 0, 0, 0, 0}; // MB bit 14
        assertEquals(1, DataLinkCapabilityReport.decode(acasXa).getACASTypeEncoded());
        assertEquals(ACASType.ACAS_XA, DataLinkCapabilityReport.decode(acasXa).getACASType());

        byte[] reserved = {0x10, 0x3C, 0, 0, 0, 0, 0}; // MB bits 11-14 = 1111
        assertEquals(15, DataLinkCapabilityReport.decode(reserved).getACASTypeEncoded());
        assertEquals(ACASType.ACAS_III, DataLinkCapabilityReport.decode(reserved).getACASType());
    }

    @Test
    public void overlayCommandCapability() throws BadFormatException {
        assertFalse(DataLinkCapabilityReport.decode(msg).isOverlayCommandCapability());
    }

    @Test
    public void acasOperating() throws BadFormatException {
        assertFalse(DataLinkCapabilityReport.decode(msg).isACASOperating());
    }

    @Test
    public void modeSSubNetworkVersionNumber() throws BadFormatException {
        assertEquals(1, DataLinkCapabilityReport.decode(msg).getModeSSubNetworkVersionNumber());
    }

    @Test
    public void transponderEnhancedProtocolIndicator() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isTransponderEnhancedProtocolIndicator());
    }

    @Test
    public void modeSSpecificServicesCapability() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isModeSSpecificServicesCapability());
    }

    @Test
    public void uelmAverageThroughputCapability() throws BadFormatException {
        assertEquals(3, DataLinkCapabilityReport.decode(msg).getUelmAverageThroughputCapability());
    }

    @Test
    public void delmThroughputCapability() throws BadFormatException {
        assertEquals(3, DataLinkCapabilityReport.decode(msg).getDelmThroughputCapability());
    }

    @Test
    public void aircraftIdentificationCapability() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isAircraftIdentificationCapability());
    }

    @Test
    public void squitterCapabilitySubfield() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isSquitterCapabilitySubfield());
    }

    @Test
    public void surveillanceIdentifierCode() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isSurveillanceIdentifierCode());
    }

    @Test
    public void commonUsageGICB() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isCommonUsageGicb());
    }

    @Test
    public void acasHybridSurveillanceCapability() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isACASHybridSurveillanceCapability());
    }

    @Test
    public void acasGeneratingRAs() throws BadFormatException {
        assertTrue(DataLinkCapabilityReport.decode(msg).isACASGeneratingRAs());
    }

    @Test
    public void acasVersionNumber() throws BadFormatException {
        assertEquals(2, DataLinkCapabilityReport.decode(msg).getACASVersionNumber());
    }

    /**
     * The test register with subnetwork version 6, the version of DO-181F and ED-73F, so that MB bits 41-56 have the
     * ED-73F layout.
     */
    private static DataLinkCapabilityReportV6 version6() throws BadFormatException {
        byte[] v6 = msg.clone();
        v6[2] = 0b00001101; // MB bits 17-23 = 6, bit 24 as before
        return (DataLinkCapabilityReportV6) DataLinkCapabilityReport.decode(v6);
    }

    @Test
    public void basicDataFlashCapability() throws BadFormatException {
        assertTrue(version6().isBasicDataFlashCapability());
    }

    @Test
    public void phaseOverlayExtendedSquitterCapability() throws BadFormatException {
        assertTrue(version6().isPhaseOverlayExtendedSquitterCapability());
    }

    @Test
    public void phaseOverlayModeSCapability() throws BadFormatException {
        assertTrue(version6().isPhaseOverlayModeSCapability());
    }

    @Test
    public void activeTransponderSideIndicator() throws BadFormatException {
        assertEquals(1, version6().getActiveTransponderSideIndicator());
    }

    @Test
    public void changeFlag() throws BadFormatException {
        assertTrue(version6().isChangeFlag());
    }

    /**
     * decode() picks the layout by the subnetwork version number: version 6 and up DO-181F/ED-73F, below it the DTE
     * sub-address array of ICAO Doc 9871 First and Second Edition. The constructors reject the other layout with a
     * BadFormatException.
     */
    @Test
    public void decode_picksTheLayoutByVersion() throws BadFormatException {
        assertInstanceOf(DataLinkCapabilityReportV6.class, version6());
        assertEquals(6, version6().getModeSSubNetworkVersionNumber());
        assertInstanceOf(DataLinkCapabilityReportV0V5.class, DataLinkCapabilityReport.decode(msg)); // version 1

        assertThrows(BadFormatException.class, () -> new DataLinkCapabilityReportV6(msg));
        byte[] v6 = msg.clone();
        v6[2] = 0b00001101;
        assertThrows(BadFormatException.class, () -> new DataLinkCapabilityReportV0V5(v6));
    }

    /**
     * Below subnetwork version 6, MB bits 41-56 are the DTE sub-address array, MSB first.
     */
    @Test
    public void belowVersion6_isTheDTESubAddressArray() throws BadFormatException {
        DataLinkCapabilityReportV0V5 report = (DataLinkCapabilityReportV0V5) DataLinkCapabilityReport.decode(msg);

        // MB bits 41-56 = 0111 0010 0110 0000: sub-addresses 1, 2, 3, 6, 9 and 10
        assertEquals(0x7260, report.getDTESubAddressSupportEncoded());
        assertArrayEquals(new boolean[]{false, true, true, true, false, false, true, false, false, true, true, false,
                false, false, false, false}, report.getDTESubAddressSupport());

        // the repro of #187: DTE sub-addresses 1 and 5 used to read as basic dataflash capability
        DataLinkCapabilityReport repro = DataLinkCapabilityReport.decode(Tools.hexStringToByteArray("10000000004400"));
        assertInstanceOf(DataLinkCapabilityReportV0V5.class, repro);
        assertTrue(((DataLinkCapabilityReportV0V5) repro).getDTESubAddressSupport()[1]);
        assertTrue(((DataLinkCapabilityReportV0V5) repro).getDTESubAddressSupport()[5]);
    }

    @Test
    void acasVersion0() throws BadFormatException {
        byte[] message = new byte[]{
                (byte) 0x10, (byte) 0xff, (byte) 0xff, (byte) 0xff, // BDS 1,0
                (byte) 0b11111100,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(0, DataLinkCapabilityReport.decode(message).getACASVersionNumber());
    }

    @Test
    void acasVersion1() throws BadFormatException {
        byte[] message = new byte[]{
                (byte) 0x10, (byte) 0xff, (byte) 0xff, (byte) 0xff, // BDS 1,0
                (byte) 0b11111110,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(1, DataLinkCapabilityReport.decode(message).getACASVersionNumber());
    }

    @Test
    void acasVersion2() throws BadFormatException {
        byte[] message = new byte[]{
                (byte) 0x10, (byte) 0xff, (byte) 0xff, (byte) 0xff, // BDS 1,0
                (byte) 0b11111101,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(2, DataLinkCapabilityReport.decode(message).getACASVersionNumber());
    }

    @Test
    void acasVersion3() throws BadFormatException {
        byte[] message = new byte[]{
                (byte) 0x10, (byte) 0xff, (byte) 0xff, (byte) 0xff, // BDS 1,0
                (byte) 0b11111111,
                (byte) 0xff, (byte) 0xff
        };
        assertEquals(3, DataLinkCapabilityReport.decode(message).getACASVersionNumber());
    }
}
