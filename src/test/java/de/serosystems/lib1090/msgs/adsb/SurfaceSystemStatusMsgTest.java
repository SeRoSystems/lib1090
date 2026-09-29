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

import de.serosystems.lib1090.StatefulModeSDecoder;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceSystemStatusMsgTest {

    // TYPE Code 24, subtype 1, with 123456789ABC in ME bits 9-56
    private static final String MESSAGE = "8D4840D6C1123456789ABC000000";

    /**
     * ED-102B §2.2.3.2.7.4.3.1: the 48-bit Surface System Status subfield is available raw.
     */
    @Test
    void surfaceSystemStatusIsAvailableRaw() throws Exception {
        SurfaceSystemStatusMsg msg = new SurfaceSystemStatusMsg(MESSAGE);
        assertEquals(0x123456789ABCL, msg.getSurfaceSystemStatusEncoded());
        assertTrue(msg.toString().contains("surfaceSystemStatus=123456789abc"), msg.toString());
    }

    /**
     * The decoder dispatches TYPE Code 24 subtype 1 to this class.
     */
    @Test
    void decoderDispatchesTypeCode24Subtype1() throws Exception {
        StatefulModeSDecoder decoder = StatefulModeSDecoder.builder()
                .checkParity(false) // the synthetic message carries no valid parity
                .build();
        assertInstanceOf(SurfaceSystemStatusMsg.class, decoder.decode(MESSAGE, Instant.EPOCH));
    }

}
