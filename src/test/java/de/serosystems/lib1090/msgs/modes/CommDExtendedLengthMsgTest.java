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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CommDExtendedLengthMsgTest {

    private static final int ADDRESS = 0xabcdef;

    /**
     * A DF 24 reply: DF 11 in bits 1-2, the spare bit 3, KE in bit 4 and ND in bits 5-8, an 80-bit MD
     * field, and the parity overlaid with {@link #ADDRESS}.
     */
    private static byte[] reply(boolean spare, boolean ke, int nd) {
        byte[] reply = new byte[14];
        reply[0] = (byte) (0xC0 | (spare ? 0x20 : 0) | (ke ? 0x10 : 0) | nd);
        for (int i = 1; i <= 10; i++)
            reply[i] = (byte) (0x11 * i);

        int parity = ModeSDownlinkMsg.calcParityInt(Arrays.copyOf(reply, 11)) ^ ADDRESS;
        reply[11] = (byte) (parity >>> 16);
        reply[12] = (byte) (parity >>> 8);
        reply[13] = (byte) parity;
        return reply;
    }

    /**
     * KE and ND sit in the first byte, which the downlink format normalization used to collapse:
     * KE read as never set and ND lost its most significant bit.
     */
    @Test
    void testControlAndSegmentNumber() throws Exception {
        for (boolean ke : new boolean[]{false, true}) {
            for (int nd = 0; nd <= 15; nd++) {
                CommDExtendedLengthMsg msg = new CommDExtendedLengthMsg(reply(false, ke, nd));
                String context = "KE=" + ke + ", ND=" + nd;

                assertEquals(24, msg.getDownlinkFormat(), context);
                assertEquals(ke, msg.isAck(), context);
                assertEquals(nd, msg.getSequenceNumber(), context);
            }
        }
    }

    /**
     * The address is the parity with the CRC of the first 88 bits removed, so it depends on KE and
     * ND being kept: with either lost, the CRC came out wrong and so did the address.
     */
    @Test
    void testAddressAndMessageSurviveEveryControlAndSegmentNumber() throws Exception {
        for (boolean ke : new boolean[]{false, true}) {
            for (int nd = 0; nd <= 15; nd++) {
                byte[] reply = reply(false, ke, nd);
                CommDExtendedLengthMsg msg = new CommDExtendedLengthMsg(reply);
                String context = "KE=" + ke + ", ND=" + nd;

                assertEquals(ADDRESS, msg.getAddress().getAddress(), context);
                assertEquals(Tools.toHexString(reply), msg.getHexMessage(), context);
                assertArrayEquals(Arrays.copyOfRange(reply, 1, 11), msg.getMessage(), context);
            }
        }
    }

    @Test
    void testSpareBitMustBeClear() {
        BadFormatException e = assertThrows(BadFormatException.class,
                () -> new CommDExtendedLengthMsg(reply(true, false, 0)));
        assertTrue(e.getMessage().contains("must be 0"), e.getMessage());
    }
}
