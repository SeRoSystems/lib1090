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
import de.serosystems.lib1090.msgs.QualifiedAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoarsePositionMsgTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    /** DF=18 with CF=3, the coarse position format, whose IMF is ME bit 1. */
    private static CoarsePositionMsg coarsePosition(boolean imf) throws Exception {
        byte[] me = new byte[7];
        me[0] = (byte) (imf ? 0x80 : 0x00);
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
}
