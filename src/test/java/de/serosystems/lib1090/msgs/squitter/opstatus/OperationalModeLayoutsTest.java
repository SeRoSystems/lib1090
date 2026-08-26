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
 * Every operational mode layout, checked subfield by subfield against the tables of om_cc.md,
 * generated from that document in the same way as {@link CapabilityClassLayoutsTest}.
 */
class OperationalModeLayoutsTest {

    /** ads-b v2 airborne operational mode 0,0 */
    @Test
    public void airborneOperationalModeCodeV2() {
        assertEquals(25, new AirborneOperationalModeCodeV2(0x0).getFirstMEBit());
        assertEquals(40, new AirborneOperationalModeCodeV2(0x0).getLastMEBit());
        assertEquals(0, new AirborneOperationalModeCodeV2(0x0).getFormatSelector());

        assertTrue(new AirborneOperationalModeCodeV2(0x2000).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertFalse(new AirborneOperationalModeCodeV2(0x0).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertTrue(new AirborneOperationalModeCodeV2(0x1000).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertFalse(new AirborneOperationalModeCodeV2(0x0).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertTrue(new AirborneOperationalModeCodeV2(0x800).isReceivingATCServices(), "isReceivingATCServices ME 29");
        assertFalse(new AirborneOperationalModeCodeV2(0x0).isReceivingATCServices(), "isReceivingATCServices ME 29");
        assertTrue(new AirborneOperationalModeCodeV2(0x400).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertFalse(new AirborneOperationalModeCodeV2(0x0).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertEquals(3, new AirborneOperationalModeCodeV2(0x300).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(0, new AirborneOperationalModeCodeV2(0x0).getSDAEncoded(), "getSDAEncoded ME 31–32");

        assertThrows(IllegalArgumentException.class, () -> new AirborneOperationalModeCodeV2(0x4000));
    }

    /** ads-b v3 airborne operational mode 0,0 */
    @Test
    public void airborneOperationalModeCodeV3() {
        assertEquals(25, new AirborneOperationalModeCodeV3(0x0).getFirstMEBit());
        assertEquals(40, new AirborneOperationalModeCodeV3(0x0).getLastMEBit());
        assertEquals(0, new AirborneOperationalModeCodeV3(0x0).getFormatSelector());

        assertTrue(new AirborneOperationalModeCodeV3(0x2000).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertFalse(new AirborneOperationalModeCodeV3(0x0).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertTrue(new AirborneOperationalModeCodeV3(0x1000).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertFalse(new AirborneOperationalModeCodeV3(0x0).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertTrue(new AirborneOperationalModeCodeV3(0x800).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertFalse(new AirborneOperationalModeCodeV3(0x0).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertTrue(new AirborneOperationalModeCodeV3(0x400).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertFalse(new AirborneOperationalModeCodeV3(0x0).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertEquals(3, new AirborneOperationalModeCodeV3(0x300).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(0, new AirborneOperationalModeCodeV3(0x0).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(127, new AirborneOperationalModeCodeV3(0xFE).getCollisionAvoidanceCoordinationCapabilityBitsEncoded(), "getCollisionAvoidanceCoordinationCapabilityBitsEncoded ME 33–39");
        assertEquals(0, new AirborneOperationalModeCodeV3(0x0).getCollisionAvoidanceCoordinationCapabilityBitsEncoded(), "getCollisionAvoidanceCoordinationCapabilityBitsEncoded ME 33–39");
        assertTrue(new AirborneOperationalModeCodeV3(0x1).isRemainWellClearActive(), "isRemainWellClearActive ME 40");
        assertFalse(new AirborneOperationalModeCodeV3(0x0).isRemainWellClearActive(), "isRemainWellClearActive ME 40");

        assertThrows(IllegalArgumentException.class, () -> new AirborneOperationalModeCodeV3(0x4000));
    }

    /** ads-b v1 airborne operational mode 0,0 */
    @Test
    public void operationalModeCodeV1() {
        assertEquals(25, new OperationalModeCodeV1(0x0).getFirstMEBit());
        assertEquals(40, new OperationalModeCodeV1(0x0).getLastMEBit());
        assertEquals(0, new OperationalModeCodeV1(0x0).getFormatSelector());

        assertTrue(new OperationalModeCodeV1(0x2000).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertFalse(new OperationalModeCodeV1(0x0).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertTrue(new OperationalModeCodeV1(0x1000).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertFalse(new OperationalModeCodeV1(0x0).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertTrue(new OperationalModeCodeV1(0x800).isReceivingATCServices(), "isReceivingATCServices ME 29");
        assertFalse(new OperationalModeCodeV1(0x0).isReceivingATCServices(), "isReceivingATCServices ME 29");

        assertThrows(IllegalArgumentException.class, () -> new OperationalModeCodeV1(0x4000));
    }

    /** ads-b v2 surface operational mode 0,0 */
    @Test
    public void surfaceOperationalModeCodeV2() {
        assertEquals(25, new SurfaceOperationalModeCodeV2(0x0).getFirstMEBit());
        assertEquals(40, new SurfaceOperationalModeCodeV2(0x0).getLastMEBit());
        assertEquals(0, new SurfaceOperationalModeCodeV2(0x0).getFormatSelector());

        assertTrue(new SurfaceOperationalModeCodeV2(0x2000).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertFalse(new SurfaceOperationalModeCodeV2(0x0).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertTrue(new SurfaceOperationalModeCodeV2(0x1000).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertFalse(new SurfaceOperationalModeCodeV2(0x0).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertTrue(new SurfaceOperationalModeCodeV2(0x800).isReceivingATCServices(), "isReceivingATCServices ME 29");
        assertFalse(new SurfaceOperationalModeCodeV2(0x0).isReceivingATCServices(), "isReceivingATCServices ME 29");
        assertTrue(new SurfaceOperationalModeCodeV2(0x400).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertFalse(new SurfaceOperationalModeCodeV2(0x0).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertEquals(3, new SurfaceOperationalModeCodeV2(0x300).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(0, new SurfaceOperationalModeCodeV2(0x0).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(255, new SurfaceOperationalModeCodeV2(0xFF).getGPSAntennaOffsetEncoded(), "getGPSAntennaOffsetEncoded ME 33–40");
        assertEquals(0, new SurfaceOperationalModeCodeV2(0x0).getGPSAntennaOffsetEncoded(), "getGPSAntennaOffsetEncoded ME 33–40");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceOperationalModeCodeV2(0x4000));
    }

    /** ads-b v3 surface operational mode 0,0 */
    @Test
    public void surfaceOperationalModeCodeV3() {
        assertEquals(25, new SurfaceOperationalModeCodeV3(0x0).getFirstMEBit());
        assertEquals(40, new SurfaceOperationalModeCodeV3(0x0).getLastMEBit());
        assertEquals(0, new SurfaceOperationalModeCodeV3(0x0).getFormatSelector());

        assertTrue(new SurfaceOperationalModeCodeV3(0x2000).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertFalse(new SurfaceOperationalModeCodeV3(0x0).isTCASResolutionAdvisoryActive(), "isTCASResolutionAdvisoryActive ME 27");
        assertTrue(new SurfaceOperationalModeCodeV3(0x1000).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertFalse(new SurfaceOperationalModeCodeV3(0x0).isIDENTSwitchActive(), "isIDENTSwitchActive ME 28");
        assertTrue(new SurfaceOperationalModeCodeV3(0x800).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertFalse(new SurfaceOperationalModeCodeV3(0x0).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertTrue(new SurfaceOperationalModeCodeV3(0x400).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertFalse(new SurfaceOperationalModeCodeV3(0x0).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertEquals(3, new SurfaceOperationalModeCodeV3(0x300).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(0, new SurfaceOperationalModeCodeV3(0x0).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(255, new SurfaceOperationalModeCodeV3(0xFF).getGPSAntennaOffsetEncoded(), "getGPSAntennaOffsetEncoded ME 33–40");
        assertEquals(0, new SurfaceOperationalModeCodeV3(0x0).getGPSAntennaOffsetEncoded(), "getGPSAntennaOffsetEncoded ME 33–40");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceOperationalModeCodeV3(0x4000));
    }

    /** ads-b v3 surface operational mode 0,1 */
    @Test
    public void surfaceOperationalModeCodeV3Format1() {
        assertEquals(25, new SurfaceOperationalModeCodeV3Format1(0x4000).getFirstMEBit());
        assertEquals(40, new SurfaceOperationalModeCodeV3Format1(0x4000).getLastMEBit());
        assertEquals(1, new SurfaceOperationalModeCodeV3Format1(0x4000).getFormatSelector());

        assertTrue(new SurfaceOperationalModeCodeV3Format1(0x4800).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertFalse(new SurfaceOperationalModeCodeV3Format1(0x4000).isModeSReplyRateLimitingActive(), "isModeSReplyRateLimitingActive ME 29");
        assertTrue(new SurfaceOperationalModeCodeV3Format1(0x4400).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertFalse(new SurfaceOperationalModeCodeV3Format1(0x4000).hasSingleAntenna(), "hasSingleAntenna ME 30");
        assertEquals(3, new SurfaceOperationalModeCodeV3Format1(0x4300).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(0, new SurfaceOperationalModeCodeV3Format1(0x4000).getSDAEncoded(), "getSDAEncoded ME 31–32");
        assertEquals(31, new SurfaceOperationalModeCodeV3Format1(0x401F).getTransponderAntennaOffsetEncoded(), "getTransponderAntennaOffsetEncoded ME 36–40");
        assertEquals(0, new SurfaceOperationalModeCodeV3Format1(0x4000).getTransponderAntennaOffsetEncoded(), "getTransponderAntennaOffsetEncoded ME 36–40");

        assertThrows(IllegalArgumentException.class, () -> new SurfaceOperationalModeCodeV3Format1(0x0));
    }

    /**
     * Format 1 reserves ME 27–28 and is only transmitted while neither indication is active, so both
     * report a constant rather than a bit — unlike a redefined bit, the value is knowable.
     */
    @Test
    public void format1ReportsConstantsForTheReservedIndications() {
        for (int bits = 0; bits < 4; bits++) {
            SurfaceOperationalModeCodeV3Format1 om =
                    new SurfaceOperationalModeCodeV3Format1(0x4000 | bits << 12);
            assertFalse(om.isTCASResolutionAdvisoryActive(), "bits=" + bits);
            assertFalse(om.isIDENTSwitchActive(), "bits=" + bits);
        }
    }

    /** ED-102B TABLE 2-59 and TABLE 2-60, derived from the ME 33–40 offset field. */
    @Test
    public void surfaceAntennaOffsetsAreDerived() {
        // ME 33 clear (left) with zero magnitude means "no data", not zero metres
        assertNull(new SurfaceOperationalModeCodeV3(0).getLateralAxisGPSAntennaOffset());
        assertNull(new SurfaceOperationalModeCodeV3(0).getLongitudinalAxisGPSAntennaOffset());

        // ME 34-35 = 2, direction left => +4 m
        assertEquals(4, new SurfaceOperationalModeCodeV3(0x40).getLateralAxisGPSAntennaOffset());
        // ME 33 set (right), ME 34-35 = 2 => -4 m
        assertEquals(-4, new SurfaceOperationalModeCodeV3(0xC0).getLateralAxisGPSAntennaOffset());
        // ME 36-40 = 1 => 0 m, = 31 => 60 m ("or above")
        assertEquals(0, new SurfaceOperationalModeCodeV3(0x1).getLongitudinalAxisGPSAntennaOffset());
        assertEquals(60, new SurfaceOperationalModeCodeV3(0x1F).getLongitudinalAxisGPSAntennaOffset());

        // the whole field reading exactly 1 is the Position Offset Applied encoding
        assertTrue(new SurfaceOperationalModeCodeV3(0x1).isPositionOffsetApplied());
        assertFalse(new SurfaceOperationalModeCodeV3(0x2).isPositionOffsetApplied());
        assertTrue(new SurfaceOperationalModeCodeV2(0x1).isPositionOffsetApplied());
    }
}
