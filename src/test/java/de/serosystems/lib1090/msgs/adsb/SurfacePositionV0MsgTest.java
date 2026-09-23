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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.quality.SourceIntegrityLevel;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SurfacePositionV0MsgTest extends SurfacePositionMsgTest {

    // SURF_POS with its type code raised from 7 to 8, the one surface type code that guarantees no
    // containment radius. Everything else, parity included, is left as it is.
    private static final String SURF_POS_TYPE_CODE_8 = "8c" + "3c4dc6" + "401c07331b029e" + "b308de";

    @Override
    protected SurfacePositionMsg create(String hex) throws Exception {
        return new SurfacePositionV0Msg(hex, Instant.EPOCH);
    }

    @Test
    void testHasTimeFlag() throws Exception {
        final SurfacePositionV0Msg sPos = new SurfacePositionV0Msg(SURF_POS, Instant.EPOCH);

        assertFalse(sPos.hasTimeFlag());
    }

    @Test
    void testGetSIL() throws Exception {
        final SurfacePositionV0Msg sPos = new SurfacePositionV0Msg(SURF_POS, Instant.EPOCH);

        assertEquals(2, sPos.getSILEncoded());
        assertEquals(SourceIntegrityLevel.AT_MOST_1E_MINUS_5, sPos.getSourceIntegrityLevel());
    }

    /**
     * Type code 8 reports no containment radius at all, so it carries no integrity either: ED-102B
     * §2.4.8.1.16 TABLE 2-304 gives it SIL 0, as it does type code 0.
     */
    @Test
    void testGetSILWithoutContainmentRadius() throws Exception {
        final SurfacePositionV0Msg sPos = new SurfacePositionV0Msg(SURF_POS_TYPE_CODE_8, Instant.EPOCH);

        assertEquals(8, sPos.getFormatTypeCode());
        assertEquals(0, sPos.getNICEncoded());
        assertEquals(0, sPos.getSILEncoded());
        assertEquals(SourceIntegrityLevel.UNKNOWN_OR_ABOVE_1E_MINUS_3, sPos.getSourceIntegrityLevel());
    }
}
