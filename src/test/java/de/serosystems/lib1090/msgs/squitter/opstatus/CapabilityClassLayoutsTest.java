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

package de.serosystems.lib1090.msgs.squitter.opstatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every capability class layout, checked subfield by subfield against the tables of om_cc.md.
 * Generated from that document: for each subfield the test sets exactly its ME bits and asserts
 * the accessor sees them, which catches an accessor wired to the wrong bit or missing from a
 * layout. Reserved ranges and selector bits are skipped.
 */
class CapabilityClassLayoutsTest {

    /** ads-b v0 airborne capability class 0,0 */
    @Test
    public void airborneCapabilityClassCodeV0() {
        assertEquals(9, new AirborneCapabilityClassCodeV0(0).getFirstMEBit());
        assertEquals(12, new AirborneCapabilityClassCodeV0(0).getLastMEBit());
        assertEquals(0, new AirborneCapabilityClassCodeV0(0).getFormatSelector());

        assertFalse(new AirborneCapabilityClassCodeV0(0x2).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new AirborneCapabilityClassCodeV0(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new AirborneCapabilityClassCodeV0(0x1).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new AirborneCapabilityClassCodeV0(0).has1090ESIn(), "has1090ESIn ME 12");

        assertThrows(IllegalArgumentException.class, () -> new AirborneCapabilityClassCodeV0(0x8));
    }

    /** ads-r v1 airborne capability class 0,0 */
    @Test
    public void aDSRAirborneCapabilityClassCodeV1() {
        assertEquals(9, new ADSRAirborneCapabilityClassCodeV1(0).getFirstMEBit());
        assertEquals(24, new ADSRAirborneCapabilityClassCodeV1(0).getLastMEBit());
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV1(0).getFormatSelector());

        assertFalse(new ADSRAirborneCapabilityClassCodeV1(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new ADSRAirborneCapabilityClassCodeV1(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new ADSRAirborneCapabilityClassCodeV1(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new ADSRAirborneCapabilityClassCodeV1(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new ADSRAirborneCapabilityClassCodeV1(0x200).supportsARVReport(), "supportsARVReport ME 15");
        assertFalse(new ADSRAirborneCapabilityClassCodeV1(0).supportsARVReport(), "supportsARVReport ME 15");
        assertTrue(new ADSRAirborneCapabilityClassCodeV1(0x100).supportsTSReport(), "supportsTSReport ME 16");
        assertFalse(new ADSRAirborneCapabilityClassCodeV1(0).supportsTSReport(), "supportsTSReport ME 16");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV1(0xC0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV1(0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertTrue(new ADSRAirborneCapabilityClassCodeV1(0x10).hasNICSupplementB(), "hasNICSupplementB ME 20");
        assertFalse(new ADSRAirborneCapabilityClassCodeV1(0).hasNICSupplementB(), "hasNICSupplementB ME 20");

        assertThrows(IllegalArgumentException.class, () -> new ADSRAirborneCapabilityClassCodeV1(0x8000));
        assertThrows(IllegalArgumentException.class, () -> new ADSRAirborneCapabilityClassCodeV1(0x800));
    }

    /** ads-b v1 airborne capability class 0,0 */
    @Test
    public void airborneCapabilityClassCodeV1() {
        assertEquals(9, new AirborneCapabilityClassCodeV1(0).getFirstMEBit());
        assertEquals(24, new AirborneCapabilityClassCodeV1(0).getLastMEBit());
        assertEquals(0, new AirborneCapabilityClassCodeV1(0).getFormatSelector());

        assertFalse(new AirborneCapabilityClassCodeV1(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new AirborneCapabilityClassCodeV1(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11 (inverted)");
        assertTrue(new AirborneCapabilityClassCodeV1(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new AirborneCapabilityClassCodeV1(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new AirborneCapabilityClassCodeV1(0x200).supportsARVReport(), "supportsARVReport ME 15");
        assertFalse(new AirborneCapabilityClassCodeV1(0).supportsARVReport(), "supportsARVReport ME 15");
        assertTrue(new AirborneCapabilityClassCodeV1(0x100).supportsTSReport(), "supportsTSReport ME 16");
        assertFalse(new AirborneCapabilityClassCodeV1(0).supportsTSReport(), "supportsTSReport ME 16");
        assertEquals(3, new AirborneCapabilityClassCodeV1(0xC0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertEquals(0, new AirborneCapabilityClassCodeV1(0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");

        assertThrows(IllegalArgumentException.class, () -> new AirborneCapabilityClassCodeV1(0x8000));
        assertThrows(IllegalArgumentException.class, () -> new AirborneCapabilityClassCodeV1(0x800));
    }

    /** ads-b v1 surface capability class 0,0 */
    @Test
    public void surfaceCapabilityClassCodeV1() {
        assertEquals(9, new SurfaceCapabilityClassCodeV1(0).getFirstMEBit());
        assertEquals(20, new SurfaceCapabilityClassCodeV1(0).getLastMEBit());
        assertEquals(0, new SurfaceCapabilityClassCodeV1(0).getFormatSelector());

        assertTrue(new SurfaceCapabilityClassCodeV1(0x200).isPositionOffsetApplied(), "isPositionOffsetApplied ME 11");
        assertFalse(new SurfaceCapabilityClassCodeV1(0).isPositionOffsetApplied(), "isPositionOffsetApplied ME 11");
        assertTrue(new SurfaceCapabilityClassCodeV1(0x100).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new SurfaceCapabilityClassCodeV1(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new SurfaceCapabilityClassCodeV1(0x20).isB2Low(), "isB2Low ME 15");
        assertFalse(new SurfaceCapabilityClassCodeV1(0).isB2Low(), "isB2Low ME 15");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceCapabilityClassCodeV1(0x800));
        assertThrows(IllegalArgumentException.class, () -> new SurfaceCapabilityClassCodeV1(0x80));
    }

    /** ads-r v2 airborne capability class 0,0 */
    @Test
    public void aDSRAirborneCapabilityClassCodeV2() {
        assertEquals(9, new ADSRAirborneCapabilityClassCodeV2(0).getFirstMEBit());
        assertEquals(24, new ADSRAirborneCapabilityClassCodeV2(0).getLastMEBit());
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV2(0).getFormatSelector());

        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x200).supportsARVReport(), "supportsARVReport ME 15");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).supportsARVReport(), "supportsARVReport ME 15");
        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x100).supportsTSReport(), "supportsTSReport ME 16");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).supportsTSReport(), "supportsTSReport ME 16");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV2(0xC0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV2(0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x20).hasUATIn(), "hasUATIn ME 19");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).hasUATIn(), "hasUATIn ME 19");
        assertTrue(new ADSRAirborneCapabilityClassCodeV2(0x10).hasNICSupplementB(), "hasNICSupplementB ME 20");
        assertFalse(new ADSRAirborneCapabilityClassCodeV2(0).hasNICSupplementB(), "hasNICSupplementB ME 20");

        assertThrows(IllegalArgumentException.class, () -> new ADSRAirborneCapabilityClassCodeV2(0x8000));
    }

    /** ads-b v2 airborne capability class 0,0 */
    @Test
    public void airborneCapabilityClassCodeV2() {
        assertEquals(9, new AirborneCapabilityClassCodeV2(0).getFirstMEBit());
        assertEquals(24, new AirborneCapabilityClassCodeV2(0).getLastMEBit());
        assertEquals(0, new AirborneCapabilityClassCodeV2(0).getFormatSelector());

        assertTrue(new AirborneCapabilityClassCodeV2(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertFalse(new AirborneCapabilityClassCodeV2(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertTrue(new AirborneCapabilityClassCodeV2(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new AirborneCapabilityClassCodeV2(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new AirborneCapabilityClassCodeV2(0x200).supportsARVReport(), "supportsARVReport ME 15");
        assertFalse(new AirborneCapabilityClassCodeV2(0).supportsARVReport(), "supportsARVReport ME 15");
        assertTrue(new AirborneCapabilityClassCodeV2(0x100).supportsTSReport(), "supportsTSReport ME 16");
        assertFalse(new AirborneCapabilityClassCodeV2(0).supportsTSReport(), "supportsTSReport ME 16");
        assertEquals(3, new AirborneCapabilityClassCodeV2(0xC0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertEquals(0, new AirborneCapabilityClassCodeV2(0).getTCReportCapabilityLevelEncoded(), "getTCReportCapabilityLevelEncoded ME 17–18");
        assertTrue(new AirborneCapabilityClassCodeV2(0x20).hasUATIn(), "hasUATIn ME 19");
        assertFalse(new AirborneCapabilityClassCodeV2(0).hasUATIn(), "hasUATIn ME 19");

        assertThrows(IllegalArgumentException.class, () -> new AirborneCapabilityClassCodeV2(0x8000));
    }

    /** ads-b v2 surface capability class 0,0 */
    @Test
    public void surfaceCapabilityClassCodeV2() {
        assertEquals(9, new SurfaceCapabilityClassCodeV2(0).getFirstMEBit());
        assertEquals(20, new SurfaceCapabilityClassCodeV2(0).getLastMEBit());
        assertEquals(0, new SurfaceCapabilityClassCodeV2(0).getFormatSelector());

        assertTrue(new SurfaceCapabilityClassCodeV2(0x100).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new SurfaceCapabilityClassCodeV2(0).has1090ESIn(), "has1090ESIn ME 12");
        assertTrue(new SurfaceCapabilityClassCodeV2(0x20).isB2Low(), "isB2Low ME 15");
        assertFalse(new SurfaceCapabilityClassCodeV2(0).isB2Low(), "isB2Low ME 15");
        assertTrue(new SurfaceCapabilityClassCodeV2(0x10).hasUATIn(), "hasUATIn ME 16");
        assertFalse(new SurfaceCapabilityClassCodeV2(0).hasUATIn(), "hasUATIn ME 16");
        assertEquals(7, new SurfaceCapabilityClassCodeV2(0xE).getNACv(), "getNACv ME 17–19");
        assertEquals(0, new SurfaceCapabilityClassCodeV2(0).getNACv(), "getNACv ME 17–19");
        assertTrue(new SurfaceCapabilityClassCodeV2(0x1).hasNICSupplementC(), "hasNICSupplementC ME 20");
        assertFalse(new SurfaceCapabilityClassCodeV2(0).hasNICSupplementC(), "hasNICSupplementC ME 20");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceCapabilityClassCodeV2(0x800));
    }

    /** ads-r v3 airborne capability class 0,0 */
    @Test
    public void aDSRAirborneCapabilityClassCodeV3() {
        assertEquals(9, new ADSRAirborneCapabilityClassCodeV3(0).getFirstMEBit());
        assertEquals(24, new ADSRAirborneCapabilityClassCodeV3(0).getLastMEBit());
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getFormatSelector());

        assertTrue(new ADSRAirborneCapabilityClassCodeV3(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertFalse(new ADSRAirborneCapabilityClassCodeV3(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertTrue(new ADSRAirborneCapabilityClassCodeV3(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new ADSRAirborneCapabilityClassCodeV3(0).has1090ESIn(), "has1090ESIn ME 12");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV3(0xC00).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV3(0x300).getTransponderSideIndicationEncoded(), "getTransponderSideIndicationEncoded ME 15–16");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getTransponderSideIndicationEncoded(), "getTransponderSideIndicationEncoded ME 15–16");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV3(0xC0).getTxPowerEncoded(), "getTxPowerEncoded ME 17–18");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getTxPowerEncoded(), "getTxPowerEncoded ME 17–18");
        assertTrue(new ADSRAirborneCapabilityClassCodeV3(0x20).hasUATIn(), "hasUATIn ME 19");
        assertFalse(new ADSRAirborneCapabilityClassCodeV3(0).hasUATIn(), "hasUATIn ME 19");
        assertTrue(new ADSRAirborneCapabilityClassCodeV3(0x10).hasNICSupplementB(), "hasNICSupplementB ME 20");
        assertFalse(new ADSRAirborneCapabilityClassCodeV3(0).hasNICSupplementB(), "hasNICSupplementB ME 20");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV3(0xC).getReducedCapabilityEquipmentEncoded(), "getReducedCapabilityEquipmentEncoded ME 21–22");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getReducedCapabilityEquipmentEncoded(), "getReducedCapabilityEquipmentEncoded ME 21–22");
        assertEquals(3, new ADSRAirborneCapabilityClassCodeV3(0x3).getDetectAndAvoidEncoded(), "getDetectAndAvoidEncoded ME 23–24");
        assertEquals(0, new ADSRAirborneCapabilityClassCodeV3(0).getDetectAndAvoidEncoded(), "getDetectAndAvoidEncoded ME 23–24");

        assertThrows(IllegalArgumentException.class, () -> new ADSRAirborneCapabilityClassCodeV3(0x8000));
    }

    /** ads-b v3 airborne capability class 0,0 */
    @Test
    public void airborneCapabilityClassCodeV3() {
        assertEquals(9, new AirborneCapabilityClassCodeV3(0).getFirstMEBit());
        assertEquals(24, new AirborneCapabilityClassCodeV3(0).getLastMEBit());
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getFormatSelector());

        assertTrue(new AirborneCapabilityClassCodeV3(0x2000).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertFalse(new AirborneCapabilityClassCodeV3(0).isCollisionAvoidanceOperational(), "isCollisionAvoidanceOperational ME 11");
        assertTrue(new AirborneCapabilityClassCodeV3(0x1000).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new AirborneCapabilityClassCodeV3(0).has1090ESIn(), "has1090ESIn ME 12");
        assertEquals(3, new AirborneCapabilityClassCodeV3(0xC00).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertEquals(3, new AirborneCapabilityClassCodeV3(0x300).getTransponderSideIndicationEncoded(), "getTransponderSideIndicationEncoded ME 15–16");
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getTransponderSideIndicationEncoded(), "getTransponderSideIndicationEncoded ME 15–16");
        assertEquals(3, new AirborneCapabilityClassCodeV3(0xC0).getTxPowerEncoded(), "getTxPowerEncoded ME 17–18");
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getTxPowerEncoded(), "getTxPowerEncoded ME 17–18");
        assertTrue(new AirborneCapabilityClassCodeV3(0x20).hasUATIn(), "hasUATIn ME 19");
        assertFalse(new AirborneCapabilityClassCodeV3(0).hasUATIn(), "hasUATIn ME 19");
        assertEquals(3, new AirborneCapabilityClassCodeV3(0xC).getReducedCapabilityEquipmentEncoded(), "getReducedCapabilityEquipmentEncoded ME 21–22");
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getReducedCapabilityEquipmentEncoded(), "getReducedCapabilityEquipmentEncoded ME 21–22");
        assertEquals(3, new AirborneCapabilityClassCodeV3(0x3).getDetectAndAvoidEncoded(), "getDetectAndAvoidEncoded ME 23–24");
        assertEquals(0, new AirborneCapabilityClassCodeV3(0).getDetectAndAvoidEncoded(), "getDetectAndAvoidEncoded ME 23–24");

        assertThrows(IllegalArgumentException.class, () -> new AirborneCapabilityClassCodeV3(0x8000));
    }

    /** ads-b v3 surface capability class 0,0 */
    @Test
    public void surfaceCapabilityClassCodeV3() {
        assertEquals(9, new SurfaceCapabilityClassCodeV3(0).getFirstMEBit());
        assertEquals(20, new SurfaceCapabilityClassCodeV3(0).getLastMEBit());
        assertEquals(0, new SurfaceCapabilityClassCodeV3(0).getFormatSelector());

        assertTrue(new SurfaceCapabilityClassCodeV3(0x100).has1090ESIn(), "has1090ESIn ME 12");
        assertFalse(new SurfaceCapabilityClassCodeV3(0).has1090ESIn(), "has1090ESIn ME 12");
        assertEquals(3, new SurfaceCapabilityClassCodeV3(0xC0).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertEquals(0, new SurfaceCapabilityClassCodeV3(0).getADSBReceiverVersionEncoded(), "getADSBReceiverVersionEncoded ME 13–14");
        assertTrue(new SurfaceCapabilityClassCodeV3(0x20).isB2Low(), "isB2Low ME 15");
        assertFalse(new SurfaceCapabilityClassCodeV3(0).isB2Low(), "isB2Low ME 15");
        assertTrue(new SurfaceCapabilityClassCodeV3(0x10).hasUATIn(), "hasUATIn ME 16");
        assertFalse(new SurfaceCapabilityClassCodeV3(0).hasUATIn(), "hasUATIn ME 16");
        assertEquals(7, new SurfaceCapabilityClassCodeV3(0xE).getNACv(), "getNACv ME 17–19");
        assertEquals(0, new SurfaceCapabilityClassCodeV3(0).getNACv(), "getNACv ME 17–19");
        assertTrue(new SurfaceCapabilityClassCodeV3(0x1).hasNICSupplementC(), "hasNICSupplementC ME 20");
        assertFalse(new SurfaceCapabilityClassCodeV3(0).hasNICSupplementC(), "hasNICSupplementC ME 20");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceCapabilityClassCodeV3(0x800));
    }
}
