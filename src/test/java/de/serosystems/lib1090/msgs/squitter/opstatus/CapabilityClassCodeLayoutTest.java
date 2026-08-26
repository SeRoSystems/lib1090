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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.msgs.squitter.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The two extreme capability class layouts: ADS-B version 3 airborne (ME 9–24, eight subfields, four
 * interfaces deep) and ADS-B version 0 "CC4" (ME 9–12, two subfields, both era-named). If the
 * interface lattice holds for both ends it holds for the eleven layouts in between.
 * <p>
 * Fields are extracted with {@link BitReader} from real frames, which also pins the ME bit numbering
 * used by the layout classes to the numbering the message decoders already use.
 */
class CapabilityClassCodeLayoutTest {

    /** @return the ME field of a DF=17 frame, i.e. the 7 bytes the message decoders read */
    private static byte[] me(String frame) {
        byte[] raw = Tools.hexStringToByteArray(frame);
        byte[] me = new byte[7];
        System.arraycopy(raw, 4, me, 0, 7);
        return me;
    }

    // ---------------------------------------------------------------- version 3 airborne

    /**
     * Version 3 airborne operational status, CC = 0x3C21: CA Operational and 1090ES IN set, ADS-B
     * receiver version 3, UAT IN set, Detect and Avoid = 1, everything else clear.
     */
    private static final String V3_AIRBORNE = "8D000000F83C2120006000000000";

    @Test
    public void v3AirborneFromFrame() {
        BitReader b = BitReader.forBigEndian(me(V3_AIRBORNE));
        assertEquals(31, b.readByte(1, 5), "format type code");
        assertEquals(0, b.readByte(6, 8), "subtype: airborne");
        assertEquals(3, b.readByte(41, 43), "MOPS version");

        AirborneCapabilityClassCodeV3 cc =
                new AirborneCapabilityClassCodeV3(b.readInt(9, 24));

        assertEquals(0x3C21, cc.getEncoded());
        assertEquals(9, cc.getFirstMEBit());
        assertEquals(24, cc.getLastMEBit());
        assertEquals(16, cc.getWidth());
        assertEquals(0, cc.getFormatSelector());

        assertTrue(cc.isCollisionAvoidanceOperational());     // ME 11
        assertTrue(cc.has1090ESIn());                         // ME 12
        assertEquals(3, cc.getADSBReceiverVersionEncoded());  // ME 13-14
        assertEquals(0, cc.getTransponderSideIndicationEncoded());   // ME 15-16
        assertEquals(0, cc.getTxPowerEncoded());              // ME 17-18
        assertTrue(cc.hasUATIn());                            // ME 19
        assertEquals(0, cc.getReducedCapabilityEquipmentEncoded());  // ME 21-22
        assertEquals(1, cc.getDetectAndAvoidEncoded());       // ME 23-24
    }

    /** All eight subfields are inherited defaults; the class body declares none of them. */
    @Test
    public void v3AirborneNeedsNoAccessorsOfItsOwn() {
        for (java.lang.reflect.Method m : AirborneCapabilityClassCodeV3.class.getDeclaredMethods())
            fail("layout class should declare no methods, found " + m.getName());
    }

    @Test
    public void v3AirborneSatisfiesTheDocumentedSupertypes() {
        AirborneCapabilityClassCodeV3 cc = new AirborneCapabilityClassCodeV3(0);
        assertInstanceOf(CapabilityClassCode.class, cc);
        assertInstanceOf(CapabilityClassCodeV2V3.class, cc);
        assertInstanceOf(CapabilityClassCodeV3.class, cc);
        assertInstanceOf(AirborneCapabilityClassCode.class, cc);
        assertInstanceOf(AirborneCapabilityClassCodeV2V3.class, cc);
        assertFalse(SurfaceCapabilityClassCode.class.isInstance(cc));
        assertFalse(AirborneCapabilityClassCodeV1V2.class.isInstance(cc));
        assertFalse(ADSRAirborneCapabilityClassCode.class.isInstance(cc));
    }

    @Test
    public void v3AirborneRejectsAnotherLayoutsSelector() {
        // ME 9 set => selector 2, which is not this layout
        assertThrows(IllegalArgumentException.class,
                () -> new AirborneCapabilityClassCodeV3(0x8000));
        assertThrows(IllegalArgumentException.class,
                () -> new AirborneCapabilityClassCodeV3(0x10000));   // 17 bits
    }

    // ---------------------------------------------------------------- version 0 CC4

    /** Version 0 status frame from OperationalStatusV0MsgTest: Not-TCAS set, CDTI set. */
    private static final String V0_NOT_TCAS = "8D000000F8300000000000000000";
    /** Version 0 status frame whose ME 9 is set, i.e. a selector this layout does not define. */
    private static final String V0_BAD_SELECTOR = "8D000000F8800000000000000000";

    @Test
    public void v0CC4FromFrame() {
        BitReader b = BitReader.forBigEndian(me(V0_NOT_TCAS));
        assertEquals(0, b.readByte(41, 43), "MOPS version");

        AirborneCapabilityClassCodeV0 cc =
                new AirborneCapabilityClassCodeV0(b.readInt(9, 12));

        assertEquals(0x3, cc.getEncoded());
        assertEquals(4, cc.getWidth());
        assertEquals(0, cc.getFormatSelector());

        assertTrue(cc.getNotTCAS());                          // bit as transmitted, ME 11
        assertFalse(cc.isCollisionAvoidanceOperational());    // its negation
        assertTrue(cc.hasOperationalCDTI());                  // ME 12
        assertTrue(cc.has1090ESIn());                         // same bit, modern name
    }

    /** The negation is what version 0 transmits, so the two accessors must always disagree. */
    @Test
    public void v0PolarityIsInverted() {
        for (int encoded = 0; encoded < 4; encoded++) {   // ME 11 and ME 12 vary, selector stays 0
            AirborneCapabilityClassCodeV0 cc = new AirborneCapabilityClassCodeV0(encoded);
            assertNotEquals(cc.getNotTCAS(), cc.isCollisionAvoidanceOperational(),
                    "encoded=" + encoded);
            assertEquals(cc.hasOperationalCDTI(), cc.has1090ESIn(), "encoded=" + encoded);
        }
    }

    @Test
    public void v0RejectsAnotherLayoutsSelector() {
        BitReader b = BitReader.forBigEndian(me(V0_BAD_SELECTOR));
        int encoded = b.readInt(9, 12);
        assertEquals(0x8, encoded, "ME 9 set");
        assertThrows(IllegalArgumentException.class,
                () -> new AirborneCapabilityClassCodeV0(encoded));
        assertThrows(IllegalArgumentException.class,
                () -> new AirborneCapabilityClassCodeV0(0x10));      // 5 bits
    }

    /**
     * The motivating case for ME-bit addressing: 1090ES IN is ME bit 12 in both layouts, reached by
     * mask 0x1000 in the 16-bit airborne field and 0x1 in the 4-bit CC4 field.
     */
    @Test
    public void sameSubfieldAcrossTheTwoExtremes() {
        // typed as the airborne interface, not the root: the root carries only the raw value and the
        // selector, so that an unrecognized layout can implement it without inheriting subfields
        AirborneCapabilityClassCode wide = new AirborneCapabilityClassCodeV3(0x1000);
        AirborneCapabilityClassCode narrow = new AirborneCapabilityClassCodeV0(0x1);
        assertTrue(wide.has1090ESIn());
        assertTrue(narrow.has1090ESIn());
        assertEquals(16, wide.getWidth());
        assertEquals(4, narrow.getWidth());
    }

    @Test
    public void valueSemanticsAndToString() {
        assertEquals(new AirborneCapabilityClassCodeV3(0x3C21),
                new AirborneCapabilityClassCodeV3(0x3C21));
        assertNotEquals(new AirborneCapabilityClassCodeV3(0x3C21),
                new AirborneCapabilityClassCodeV3(0x3C20));
        assertEquals("AirborneCapabilityClassCodeV3{ME 9-24=0x3c21}",
                new AirborneCapabilityClassCodeV3(0x3C21).toString());
        assertEquals("AirborneCapabilityClassCodeV0{ME 9-12=0x3}",
                new AirborneCapabilityClassCodeV0(0x3).toString());
    }
}
