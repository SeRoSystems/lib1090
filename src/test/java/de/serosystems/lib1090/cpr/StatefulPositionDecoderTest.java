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

package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.msgs.adsb.AirbornePositionV0Msg;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StatefulPositionDecoderTest {

    // the well-known pair for 40621D, which decodes globally to 52.2658N 3.9389E
    private static final String EVEN = "8D40621D58C382D690C8AC2863A7";
    private static final String ODD = "8D40621D58C386435CC412692AD6";
    private static final Instant T0 = Instant.ofEpochSecond(1_600_000_000L);

    private static CPREncodedPosition cpr(String raw, Instant timestamp) throws Exception {
        return new AirbornePositionV0Msg(raw, timestamp).getCPREncodedPosition();
    }

    /**
     * The position a decode returns is the caller's: changing it does not move the reference the next local decode
     * of the same target uses.
     */
    @Test
    void changingTheReturnedPositionLeavesTheReferenceAlone() throws Exception {
        StatefulPositionDecoder untouched = new StatefulPositionDecoder(true);
        StatefulPositionDecoder changed = new StatefulPositionDecoder(true);
        for (StatefulPositionDecoder decoder : new StatefulPositionDecoder[]{untouched, changed})
            assertNull(decoder.decodePosition(cpr(EVEN, T0), null));

        assertNotNull(untouched.decodePosition(cpr(ODD, T0.plusSeconds(1)), null));
        Position returned = changed.decodePosition(cpr(ODD, T0.plusSeconds(1)), null);
        assertEquals(52.2658, returned.getLatitude(), 1e-4);
        returned.setLatitude(returned.getLatitude() + 5);
        returned.setLongitude(returned.getLongitude() + 5);

        // 30 s later the even frame is too old to pair with, so the next odd frame is decoded locally
        Position expected = untouched.decodePosition(cpr(ODD, T0.plusSeconds(31)), null);
        Position actual = changed.decodePosition(cpr(ODD, T0.plusSeconds(31)), null);
        assertEquals(expected.getLatitude(), actual.getLatitude(), 1e-9);
        assertEquals(expected.getLongitude(), actual.getLongitude(), 1e-9);
        assertEquals(52.2658, actual.getLatitude(), 1e-4);
    }

}
