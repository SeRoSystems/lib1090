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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CoarsePositionMsgTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    /**
     * DF=18 with CF=3, the coarse position format, whose IMF is ME bit 1.
     */
    private static CoarsePositionMsg coarsePosition(boolean imf) throws Exception {
        return coarsePosition(imf, 0);
    }

    /**
     * As {@link #coarsePosition(boolean)}, with the ground speed code in ME bits 26-31.
     */
    private static CoarsePositionMsg coarsePosition(boolean imf, int groundSpeed) throws Exception {
        byte[] me = new byte[7];
        me[0] = (byte) (imf ? 0x80 : 0x00);
        me[3] = (byte) (groundSpeed << 1);
        StringBuilder hex = new StringBuilder("93485020");
        for (byte b : me) hex.append(String.format("%02X", b));
        return new CoarsePositionMsg(Tools.hexStringToByteArray(hex + "000000"), T);
    }

    /**
     * IMF clear means the AA field holds an ICAO 24-bit address, set means a Mode A code and track file
     * number — the same convention as the fine TIS-B messages with CF=2. Earlier releases read it the
     * other way round for CF=3 alone.
     */
    @Test
    void testIMFSelectsTheAddressType() throws Exception {
        CoarsePositionMsg icao = coarsePosition(false);
        assertFalse(icao.getIMF());
        assertEquals(QualifiedAddress.Type.ICAO24, icao.getAddress().getType());

        CoarsePositionMsg modeA = coarsePosition(true);
        assertTrue(modeA.getIMF());
        assertEquals(QualifiedAddress.Type.MODEA_TRACK, modeA.getAddress().getType());
    }

    /**
     * ED-102A §2.2.17.3.5.7 Table 2-111: code 0 is no information, code 1 below 16 kt, codes 2 to 62 are 32 kt wide,
     * and code 63 is at least 1968 kt with no upper end.
     */
    @Test
    void testGroundSpeed() throws Exception {
        assertNull(coarsePosition(false, 0).getGroundSpeed());

        Interval slow = coarsePosition(false, 1).getGroundSpeed();
        assertEquals(Bound.AT_LEAST, slow.getLowerBound());
        assertEquals(0, slow.getLower());
        assertEquals(Bound.BELOW, slow.getUpperBound());
        assertEquals(16, slow.getUpper());

        Interval two = coarsePosition(false, 2).getGroundSpeed();
        assertEquals(16, two.getLower());
        assertEquals(48, two.getUpper());

        Interval sixtyTwo = coarsePosition(false, 62).getGroundSpeed();
        assertEquals(Bound.AT_LEAST, sixtyTwo.getLowerBound());
        assertEquals(1936, sixtyTwo.getLower());
        assertEquals(Bound.BELOW, sixtyTwo.getUpperBound());
        assertEquals(1968, sixtyTwo.getUpper());

        CoarsePositionMsg max = coarsePosition(false, 63);
        assertEquals(63, max.getGroundSpeedEncoded());
        Interval fast = max.getGroundSpeed();
        assertEquals(Bound.AT_LEAST, fast.getLowerBound());
        assertEquals(1968, fast.getLower());
        assertEquals(Bound.NONE, fast.getUpperBound());
    }
}
