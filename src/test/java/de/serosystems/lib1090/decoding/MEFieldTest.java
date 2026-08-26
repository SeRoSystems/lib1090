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

package de.serosystems.lib1090.decoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MEFieldTest {

    /** Capability Class Code, airborne: ME 9-24 read as 16 bits. */
    private static final class AirborneCC extends AbstractMEField {
        AirborneCC(int encoded) {
            super(encoded, 9, 24);
        }
    }

    /** Capability Class Code, surface: ME 9-20 read as 12 bits. */
    private static final class SurfaceCC extends AbstractMEField {
        SurfaceCC(int encoded) {
            super(encoded, 9, 20);
        }
    }

    /** Operational Mode Code: ME 25-40 read as 16 bits. */
    private static final class OM extends AbstractMEField {
        OM(int encoded) {
            super(encoded, 25, 40);
        }
    }

    /** Version 0 capability class field "CC4": ME 9-12 read as 4 bits. */
    private static final class CC4 extends AbstractMEField {
        CC4(int encoded) {
            super(encoded, 9, 12);
        }
    }

    @Test
    public void extentIsReported() {
        AirborneCC cc = new AirborneCC(0);
        assertEquals(9, cc.getFirstMEBit());
        assertEquals(24, cc.getLastMEBit());
        assertEquals(16, cc.getWidth());
        assertEquals(4, new CC4(0).getWidth());
    }

    /**
     * The point of ME-bit addressing: "1090ES IN" is ME bit 12 in every layout, but the mask differs
     * per field width — 0x1000 airborne, 0x100 surface, 0x1 for the version 0 CC4 field. One accessor
     * expression covers all three.
     */
    @Test
    public void sameMEBitAcrossDifferentFieldWidths() {
        assertTrue(new AirborneCC(0x1000).getMEBit(12));
        assertTrue(new SurfaceCC(0x100).getMEBit(12));
        assertTrue(new CC4(0x1).getMEBit(12));

        assertFalse(new AirborneCC(~0x1000 & 0xFFFF).getMEBit(12));
        assertFalse(new SurfaceCC(~0x100 & 0xFFF).getMEBit(12));
        assertFalse(new CC4(~0x1 & 0xF).getMEBit(12));
    }

    @Test
    public void masksMatchTheDocumentedTables() {
        // airborne CC, ME 9-24: mask bit k is ME bit 24-k
        assertEquals(0xC000, new AirborneCC(0xFFFF).getMEBits(9, 10) << 14);
        assertTrue(new AirborneCC(0x2000).getMEBit(11));                  // CA Operational
        assertEquals(3, new AirborneCC(0xC00).getMEBits(13, 14));         // ADS-B Receiver Version
        assertEquals(3, new AirborneCC(0x3).getMEBits(23, 24));           // Detect and Avoid
        // surface CC, ME 9-20: mask bit k is ME bit 20-k
        assertEquals(7, new SurfaceCC(0xE).getMEBits(17, 19));            // NACv
        assertTrue(new SurfaceCC(0x1).getMEBit(20));                      // NIC supplement C
        // OM, ME 25-40: mask bit k is ME bit 40-k
        assertTrue(new OM(0x800).getMEBit(29));                           // Mode S Reply Rate Limiting
        assertEquals(3, new OM(0x300).getMEBits(31, 32));                 // SDA
        assertEquals(0x7F, new OM(0xFE).getMEBits(33, 39));               // CCCB
        assertTrue(new OM(0x1).getMEBit(40));                             // Remain Well Clear
    }

    @Test
    public void multiBitRangesAreRightAligned() {
        // ME 15-16 set to binary 10 within the airborne CC field
        assertEquals(0b10, new AirborneCC(0x200).getMEBits(15, 16));
        assertEquals(0xFF, new OM(0xFF).getMEBits(33, 40));
        assertEquals(0xFFFF, new OM(0xFFFF).getMEBits(25, 40));
    }

    @Test
    public void rangesOutsideTheFieldAreRejected() {
        AirborneCC cc = new AirborneCC(0xFFFF);
        assertThrows(IllegalArgumentException.class, () -> cc.getMEBit(8));
        assertThrows(IllegalArgumentException.class, () -> cc.getMEBit(25));
        assertThrows(IllegalArgumentException.class, () -> cc.getMEBits(20, 30));
        assertThrows(IllegalArgumentException.class, () -> cc.getMEBits(16, 15));

        // the OM field starts where the airborne CC field ends; neither can read the other's bits
        OM om = new OM(0xFFFF);
        assertThrows(IllegalArgumentException.class, () -> om.getMEBit(24));
        assertThrows(IllegalArgumentException.class, () -> om.getMEBit(41));
    }

    @Test
    public void malformedFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CC4(0x10));      // 5 bits into a 4-bit field
        assertThrows(IllegalArgumentException.class, () -> new SurfaceCC(0x1000));
        assertThrows(IllegalArgumentException.class, () -> new AirborneCC(0x10000));
    }

    @Test
    public void valueSemantics() {
        assertEquals(new AirborneCC(0x1234), new AirborneCC(0x1234));
        assertEquals(new AirborneCC(0x1234).hashCode(), new AirborneCC(0x1234).hashCode());
        assertNotEquals(new AirborneCC(0x1234), new AirborneCC(0x1235));
        // same encoded value, different layout: not equal
        assertNotEquals(new AirborneCC(0x123), new OM(0x123));
        assertEquals("AirborneCC{ME 9-24=0x1234}", new AirborneCC(0x1234).toString());
    }
}
