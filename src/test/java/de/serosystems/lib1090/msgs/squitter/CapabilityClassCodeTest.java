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

package de.serosystems.lib1090.msgs.squitter;

import de.serosystems.lib1090.decoding.AbstractMEField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the interface lattice against the layouts of om_cc.md. The stand-ins here are throwaway
 * implementations that declare which interfaces a layout satisfies and nothing else — the point being
 * that every accessor arrives as an inherited default method, so a layout needs no body at all.
 */
class CapabilityClassCodeTest {

    /** ADS-B v3 airborne CC, ME 9-24: the richest layout, four interfaces deep. */
    private static final class AirborneV3 extends AbstractMEField
            implements AirborneCapabilityClassCodeV3 {
        AirborneV3(int encoded) {
            super(encoded, 9, 24);
        }
    }

    /** ADS-B v3 surface CC, ME 9-20. */
    private static final class SurfaceV3 extends AbstractMEField
            implements SurfaceCapabilityClassCodeV2V3, CapabilityClassCodeV3 {
        SurfaceV3(int encoded) {
            super(encoded, 9, 20);
        }
    }

    /** ADS-R v3 airborne CC: as ADS-B, plus NIC supplement B at ME 20. */
    private static final class ADSRAirborneV3 extends AbstractMEField
            implements AirborneCapabilityClassCodeV3, ADSRAirborneCapabilityClassCode {
        ADSRAirborneV3(int encoded) {
            super(encoded, 9, 24);
        }
    }

    /** ADS-B v1 airborne CC, whose selector is 4 bits split across ME 9-10 and ME 13-14. */
    private static final class AirborneV1 extends AbstractMEField
            implements AirborneCapabilityClassCodeV1V2 {
        AirborneV1(int encoded) {
            super(encoded, 9, 24);
        }

        @Override
        public int getFormatSelector() {
            return getMEBits(9, 10) << 2 | getMEBits(13, 14);
        }

        /** Versions 0 and 1 transmit "Not-TCAS", so the unified accessor negates the bit. */
        @Override
        public boolean isCollisionAvoidanceOperational() {
            return !getMEBit(11);
        }
    }

    /** ADS-B v0 "CC4", ME 9-12: a 4-bit field carrying the same two flags. */
    private static final class CC4 extends AbstractMEField implements AirborneCapabilityClassCode {
        CC4(int encoded) {
            super(encoded, 9, 12);
        }

        /** Versions 0 and 1 transmit "Not-TCAS", so the unified accessor negates the bit. */
        @Override
        public boolean isCollisionAvoidanceOperational() {
            return !getMEBit(11);
        }
    }

    @Test
    public void v3AirborneReadsEverySubfield() {
        // ME 11 CA operational, 12 1090ES IN, 13-14 rx version, 15-16 side, 17-18 tx power,
        // 19 UAT IN, 21-22 RCE, 23-24 DAA
        AirborneV3 cc = new AirborneV3(0x2000 | 0x1000 | 0xC00 | 0x200 | 0x80 | 0x20 | 0x8 | 0x1);
        assertTrue(cc.isCollisionAvoidanceOperational());
        assertTrue(cc.has1090ESIn());
        assertEquals(3, cc.getADSBReceiverVersionEncoded());
        assertEquals(0b10, cc.getTransponderSideIndicationEncoded());
        assertEquals(0b10, cc.getTxPowerEncoded());
        assertTrue(cc.hasUATIn());
        assertEquals(0b10, cc.getReducedCapabilityEquipmentEncoded());
        assertEquals(0b01, cc.getDetectAndAvoidEncoded());
        assertEquals(0, cc.getFormatSelector());
    }

    /** The reason hasUATIn() is declared high and implemented low: the bit moves between subtypes. */
    @Test
    public void uatInBitMovesBetweenSubtypes() {
        assertTrue(new AirborneV3(0x20).hasUATIn());     // ME 19 of ME 9-24
        assertFalse(new AirborneV3(0x10).hasUATIn());    // ME 20, reserved when airborne
        assertTrue(new SurfaceV3(0x10).hasUATIn());      // ME 16 of ME 9-20
        assertFalse(new SurfaceV3(0x20).hasUATIn());     // ME 15 is B2 Low

        // and it is reachable polymorphically through the interface that only declares it
        CapabilityClassCodeV2V3 airborne = new AirborneV3(0x20);
        CapabilityClassCodeV2V3 surface = new SurfaceV3(0x10);
        assertTrue(airborne.hasUATIn());
        assertTrue(surface.hasUATIn());
    }

    /** 1090ES IN is ME bit 12 in all three field widths, though the mask differs in each. */
    @Test
    public void sharedSubfieldsAreSharedImplementations() {
        assertTrue(new AirborneV3(0x1000).has1090ESIn());
        assertTrue(new SurfaceV3(0x100).has1090ESIn());
        assertTrue(new CC4(0x1).has1090ESIn());

        // ME 11 is the same bit in both, but v2/v3 send "CA Operational" and v0/v1 its negation
        assertTrue(new AirborneV3(0x2000).isCollisionAvoidanceOperational());
        assertFalse(new AirborneV3(0).isCollisionAvoidanceOperational());
        assertFalse(new CC4(0x2).isCollisionAvoidanceOperational());
        assertTrue(new CC4(0).isCollisionAvoidanceOperational());
    }

    @Test
    public void surfaceOnlySubfields() {
        SurfaceV3 cc = new SurfaceV3(0x20 | 0xE | 0x1);
        assertTrue(cc.isB2Low());
        assertEquals(7, cc.getNACv());
        assertTrue(cc.hasNICSupplementC());
    }

    @Test
    public void v1AirborneSubfieldsAndSplitSelector() {
        AirborneV1 cc = new AirborneV1(0x200 | 0x100 | 0x80);
        assertTrue(cc.supportsARVReport());
        assertTrue(cc.supportsTSReport());
        assertEquals(2, cc.getTCReportCapabilityLevelEncoded());
        assertTrue(cc.supportsTargetChangeReport());
        assertEquals(0, cc.getFormatSelector());

        // a non-zero half of the split selector must show up in the selector value
        assertEquals(0b0010, new AirborneV1(0x800).getFormatSelector());   // ME 13 set
        assertEquals(0b1000, new AirborneV1(0x8000).getFormatSelector());  // ME 9 set
    }

    @Test
    public void adsrAddsOnlyNICSupplementB() {
        ADSRAirborneV3 cc = new ADSRAirborneV3(0x10);
        assertTrue(cc.hasNICSupplementB());
        assertFalse(new ADSRAirborneV3(0x20).hasNICSupplementB());
        // ADS-B airborne v3 has no such accessor at all; ME 20 is reserved there. Note that
        // `instanceof` does not even compile here - javac rejects it as statically impossible,
        // which is a stronger guarantee than this assertion.
        assertFalse(ADSRAirborneCapabilityClassCode.class.isInstance(new AirborneV3(0x10)));
    }

    /** The lattice must be navigable by the supertypes a caller would reasonably hold. */
    @Test
    public void supertypesResolveAsDocumented() {
        AirborneV3 cc = new AirborneV3(0);
        assertInstanceOf(CapabilityClassCode.class, cc);
        assertInstanceOf(CapabilityClassCodeV2V3.class, cc);
        assertInstanceOf(CapabilityClassCodeV3.class, cc);
        assertInstanceOf(AirborneCapabilityClassCode.class, cc);
        assertInstanceOf(AirborneCapabilityClassCodeV2V3.class, cc);
        assertFalse(SurfaceCapabilityClassCode.class.isInstance(cc));
        assertFalse(AirborneCapabilityClassCodeV1V2.class.isInstance(cc));

        SurfaceV3 s = new SurfaceV3(0);
        assertInstanceOf(SurfaceCapabilityClassCode.class, s);
        assertInstanceOf(CapabilityClassCodeV3.class, s);
        assertFalse(AirborneCapabilityClassCode.class.isInstance(s));
        assertFalse(AirborneCapabilityClassCodeV2V3.class.isInstance(s));
    }

    /** A layout cannot reach bits outside its own field. */
    @Test
    public void fieldExtentIsEnforcedPerLayout() {
        SurfaceV3 s = new SurfaceV3(0xFFF);
        assertThrows(IllegalArgumentException.class, () -> s.getMEBit(21));
        assertThrows(IllegalArgumentException.class, () -> new CC4(0xF).getMEBit(13));
    }
}
