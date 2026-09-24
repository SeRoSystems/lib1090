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
import de.serosystems.lib1090.Tools;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The decoder's choice of emergency and priority status class by the version an earlier operational
 * status message established. Versions 0 and 1 share a format but not a meaning, so they must not
 * share a class.
 */
class EmergencyOrPriorityStatusDispatchTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    private static byte[] frame(byte[] me) {
        StringBuilder hex = new StringBuilder("8D485020");
        for (byte b : me) hex.append(String.format("%02X", b));
        return Tools.hexStringToByteArray(hex + "000000");
    }

    /** Airborne operational status, whose ME 41-43 tell the decoder which version to assume. */
    private static byte[] operationalStatus(int version) {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3);
        me[5] = (byte) (version << 5);
        return frame(me);
    }

    private static byte[] emergencyOrPriorityStatus() {
        byte[] me = new byte[7];
        me[0] = (byte) (28 << 3 | 1);
        return frame(me);
    }

    private static Class<?> decodedAtVersion(int version) throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(operationalStatus(version), T);
        return decoder.decode(emergencyOrPriorityStatus(), T).getClass();
    }

    @Test
    void testDispatch() throws Exception {
        assertEquals(EmergencyOrPriorityStatusV0Msg.class,
                new StatefulModeSDecoder().decode(emergencyOrPriorityStatus(), T).getClass());
        assertEquals(EmergencyOrPriorityStatusV0Msg.class, decodedAtVersion(0));
        assertEquals(EmergencyOrPriorityStatusV1Msg.class, decodedAtVersion(1));
        assertEquals(EmergencyOrPriorityStatusV2Msg.class, decodedAtVersion(2));
        assertEquals(EmergencyOrPriorityStatusV3Msg.class, decodedAtVersion(3));
    }
}
