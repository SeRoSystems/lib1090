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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirAirSurveillanceReplyTest {

    /**
     * Per RI code: operating ACAS, vertical and horizontal resolution capability, and maximum airspeed, ICAO Annex 10
     * Volume IV (6th edition) §4.3.8.4.1.2 and §3.1.2.8.2.2. null where the code does not tell.
     */
    private static final Object[][] EXPECTED = {
            /* 0 no operating ACAS */ {false, false, false, null},
            /* 1 not assigned */ {null, null, null, null},
            /* 2 resolution capability inhibited */ {true, false, false, null},
            /* 3 vertical only */ {true, true, false, null},
            /* 4 vertical and horizontal */ {true, true, true, null},
            /* 5 reserved for passive ACAS */ {null, null, null, null},
            /* 6 reserved for passive ACAS */ {null, null, null, null},
            /* 7 not assigned */ {null, null, null, null},
            /* 8 no maximum airspeed data */ {null, null, null, null},
            /* 9 */ {null, null, null, 75},
            /* 10 */ {null, null, null, 150},
            /* 11 */ {null, null, null, 300},
            /* 12 */ {null, null, null, 600},
            /* 13 */ {null, null, null, 1200},
            /* 14 more than 1200 kt */ {null, null, null, Integer.MAX_VALUE},
            /* 15 not assigned */ {null, null, null, null},
    };

    /**
     * A reply of the given downlink format and length with the given RI (bits 14-17), all other bits zero.
     */
    private static byte[] reply(int df, int bytes, int ri) {
        byte[] raw = new byte[bytes];
        raw[0] = (byte) (df << 3);
        raw[1] = (byte) ((ri >> 1) & 0x07); // bits 14-16, the last three of byte 1
        raw[2] = (byte) ((ri & 1) << 7); // bit 17, the first of byte 2
        return raw;
    }

    private static void assertReplyInformation(AirAirSurveillanceReply reply, int ri) {
        String code = "RI " + ri;
        assertEquals(ri, reply.getReplyInformationEncoded(), code);
        assertEquals(ri >= 8, reply.isAcquisitionReply(), code);
        assertEquals(EXPECTED[ri][0], reply.hasOperatingACAS(), code);
        assertEquals(EXPECTED[ri][1], reply.hasVerticalResolutionCapability(), code);
        assertEquals(EXPECTED[ri][2], reply.hasHorizontalResolutionCapability(), code);
        assertEquals(EXPECTED[ri][3], reply.getMaximumAirspeed(), code);
    }

    @Test
    void everyCode_shortAndLongReply() throws BadFormatException, UnspecifiedFormatError {
        for (int ri = 0; ri < 16; ri++) {
            assertReplyInformation(new ShortACAS(reply(0, 7, ri)), ri);
            assertReplyInformation(new LongACAS(reply(16, 14, ri)), ri);
        }
    }

    /**
     * An acquisition reply (RI 8) says nothing about ACAS; it used to report an operating one.
     */
    @Test
    void acquisitionReply_reportsNoACASStatus() throws BadFormatException, UnspecifiedFormatError {
        ShortACAS acas = new ShortACAS("00040000ABCDEF");

        assertEquals(8, acas.getReplyInformationEncoded());
        assertTrue(acas.isAcquisitionReply());
        assertNull(acas.hasOperatingACAS());
    }
}
