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

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every field of the surveillance and ACAS replies at the position ICAO Annex 10 Volume IV gives it,
 * bit 1 being the most significant bit of the downlink format.
 */
class ModeSReplyFieldsTest {

    /**
     * Writes {@code value} into bits {@code from} to {@code to} of {@code reply}.
     */
    private static void set(byte[] reply, int from, int to, long value) {
        for (int bit = from; bit <= to; bit++) {
            if (((value >>> (to - bit)) & 1) != 0)
                reply[(bit - 1) / 8] |= (byte) (0x80 >>> ((bit - 1) % 8));
        }
    }

    /**
     * Flight status, downlink request, utility message and a 13-bit altitude or identity code.
     */
    private static byte[] surveillance(int df, int bytes) {
        byte[] reply = new byte[bytes];
        set(reply, 1, 5, df);
        set(reply, 6, 8, 5);
        set(reply, 9, 13, 22);
        set(reply, 14, 19, 45);
        set(reply, 20, 32, 0x0A5B);
        return reply;
    }

    private static final byte[] COMM_B = {0x20, 0x2C, (byte) 0xC3, 0x71, (byte) 0xC3, 0x1D, (byte) 0xE0};

    private static byte[] withCommB(byte[] reply) {
        System.arraycopy(COMM_B, 0, reply, 4, 7);
        return reply;
    }

    @Test
    void testAltitudeReply() throws Exception {
        AltitudeReply reply = new AltitudeReply(surveillance(4, 7));
        assertEquals(5, reply.getFlightStatusEncoded());
        assertTrue(reply.hasSPI());
        assertEquals(22, reply.getDownlinkRequest());
        assertEquals(45, reply.getUtilityMsgEncoded());
        assertEquals(11, reply.getInterrogatorIdentifier());
        assertEquals(1, reply.getIdentifierDesignator());
        assertEquals(0x0A5B, reply.getAltitudeEncoded());
    }

    @Test
    void testCommBAltitudeReply() throws Exception {
        CommBAltitudeReply reply = new CommBAltitudeReply(withCommB(surveillance(20, 14)));
        assertEquals(5, reply.getFlightStatusEncoded());
        assertEquals(22, reply.getDownlinkRequest());
        assertEquals(45, reply.getUtilityMsgEncoded());
        assertEquals(0x0A5B, reply.getAltitudeEncoded());
        assertArrayEquals(COMM_B, reply.getMessage());
    }

    @Test
    void testIdentifyReply() throws Exception {
        IdentifyReply reply = new IdentifyReply(surveillance(5, 7));
        assertEquals(5, reply.getFlightStatusEncoded());
        assertEquals(22, reply.getDownlinkRequest());
        assertEquals(45, reply.getUtilityMsgEncoded());
        assertEquals(0x0A5B, reply.getIdentityEncoded());
    }

    @Test
    void testCommBIdentifyReply() throws Exception {
        CommBIdentifyReply reply = new CommBIdentifyReply(withCommB(surveillance(21, 14)));
        assertEquals(5, reply.getFlightStatusEncoded());
        assertEquals(22, reply.getDownlinkRequest());
        assertEquals(45, reply.getUtilityMsgEncoded());
        assertEquals(0x0A5B, reply.getIdentityEncoded());
        assertArrayEquals(COMM_B, reply.getMessage());
    }

    @Test
    void testShortACAS() throws Exception {
        byte[] raw = new byte[7];
        set(raw, 1, 5, 0);
        set(raw, 6, 6, 1);
        set(raw, 7, 7, 1);
        set(raw, 9, 11, 5);
        set(raw, 14, 17, 11);
        set(raw, 20, 32, 0x0A5B);

        ShortACAS acas = new ShortACAS(raw);
        assertFalse(acas.isAirborne());
        assertTrue(acas.hasCrossLinkCapability());
        assertEquals(5, acas.getSensitivityLevel());
        assertEquals(11, acas.getReplyInformationEncoded());
        assertEquals(0x0A5B, acas.getAltitudeEncoded());
    }

    @Test
    void testLongACAS() throws Exception {
        byte[] raw = new byte[14];
        set(raw, 1, 5, 16);
        set(raw, 9, 11, 3);
        set(raw, 14, 17, 4);
        set(raw, 20, 32, 0x0A5B);
        set(raw, 33, 40, 0x30);
        set(raw, 41, 54, 0x2A55);
        set(raw, 55, 58, 0b1001);
        set(raw, 59, 59, 1);

        LongACAS acas = new LongACAS(raw);
        assertTrue(acas.isAirborne());
        assertEquals(3, acas.getSensitivityLevel());
        assertEquals(4, acas.getReplyInformationEncoded());
        assertEquals(0x0A5B, acas.getAltitudeEncoded());
        assertTrue(acas.hasValidRAC());
        assertEquals(0x2A55, acas.getActiveResolutionAdvisories());
        assertEquals(0b1001, acas.getResolutionAdvisoryComplementEncoded());
        assertTrue(acas.noPassBelow());
        assertFalse(acas.noPassAbove());
        assertTrue(acas.noTurnRight());
        assertTrue(acas.hasTerminated());
        assertFalse(acas.hasMultipleThreats());

        set(raw, 33, 40, 0x31);
        assertFalse(new LongACAS(raw).hasValidRAC());
    }

    /**
     * The interrogator code is what the parity leaves once the CRC is removed: code label 2, code 7.
     */
    @Test
    void testAllCallReply() throws Exception {
        byte[] raw = new byte[7];
        set(raw, 1, 5, 11);
        set(raw, 6, 8, 5);
        set(raw, 9, 32, 0xABCDEF);
        int parity = ModeSDownlinkMsg.calcParityInt(Arrays.copyOf(raw, 4)) ^ 0x27;
        set(raw, 33, 56, parity);

        AllCallReply reply = new AllCallReply(raw);
        assertEquals(5, reply.getCapabilitiesEncoded());
        assertTrue(reply.isAirborne());
        assertEquals(2, reply.getCodeLabelEncoded());
        assertTrue(reply.isSurveillanceID());
        assertEquals(23, reply.getInterrogatorCode());
        assertTrue(reply.hasValidInterrogatorCode());
    }

    /**
     * Every reply's toString() starts with the class and then the downlink message, as in ADS-B.
     */
    @Test
    void testToString() throws Exception {
        assertTrue(new AltitudeReply(surveillance(4, 7)).toString().startsWith("AltitudeReply{ModeSReply{"));
        assertTrue(new LongACAS(new byte[]{(byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}).toString()
                .startsWith("LongACAS{ModeSReply{"));
        assertTrue(new CommDExtendedLengthMsg(new byte[]{(byte) 0xD5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}).toString()
                .startsWith("CommDExtendedLengthMsg{ModeSReply{downlinkFormat=24, firstField=21, "));
        assertTrue(new ExtendedSquitter("8D485020994409940838175B284F").toString()
                .matches("ExtendedSquitter\\{ModeSReply\\{.*}, message=99440994083817, formatTypeCode=19}"));
    }
}
