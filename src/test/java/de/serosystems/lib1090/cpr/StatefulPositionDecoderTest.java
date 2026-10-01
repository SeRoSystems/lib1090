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

    /**
     * Airborne CPR encoding with 17 bits, ED-102B §A.1.7.3.
     */
    private static CPREncodedPosition encode(double lat, double lon, boolean odd, Instant timestamp) {
        int i = odd ? 1 : 0;
        double dLat = 360.0 / (60 - i);
        int yz = (int) Math.floor((1 << 17) * mod(lat, dLat) / dLat + 0.5);
        double rLat = dLat * (yz / (double) (1 << 17) + Math.floor(lat / dLat));
        double dLon = 360.0 / Math.max(nl(rLat) - i, 1);
        int xz = (int) Math.floor((1 << 17) * mod(lon, dLon) / dLon + 0.5);
        return CPREncodedPosition.ofAirborne(17, odd, yz % (1 << 17), xz % (1 << 17), timestamp);
    }

    private static double mod(double x, double y) {
        return x - y * Math.floor(x / y);
    }

    private static int nl(double lat) {
        if (lat == 0) return 59;
        if (Math.abs(lat) == 87) return 2;
        if (Math.abs(lat) > 87) return 1;
        double a = 1 - Math.cos(Math.PI / 30);
        double b = Math.pow(Math.cos(Math.toRadians(lat)), 2);
        return (int) Math.floor(2 * Math.PI / Math.acos(1 - a / b));
    }

    /**
     * A track at 40N 10E, whose last position, at T0 + 3 s, is reasonable.
     */
    private static StatefulPositionDecoder track() {
        StatefulPositionDecoder decoder = new StatefulPositionDecoder();
        for (int s = 0; s < 4; s++) decoder.decodePosition(encode(40, 10, s % 2 == 1, T0.plusSeconds(s)), null);
        return decoder;
    }

    /**
     * After two hours, the target is 4° further north, more than half an even zone of 6°. Decoded locally against
     * the old position, it came out at 38N, flagged reasonable. Now it is decoded as a new target: no position
     * without a pair, and one not yet reasonable with it.
     */
    @Test
    void anOldPositionIsNoReference() throws Exception {
        StatefulPositionDecoder decoder = track();
        assertTrue(decoder.decodePosition(encode(40, 10, false, T0.plusSeconds(4)), null).isReasonable());

        StatefulPositionDecoder returned = track();
        assertNull(returned.decodePosition(encode(44, 10, false, T0.plusSeconds(7200)), null));
        Position global = returned.decodePosition(encode(44, 10, true, T0.plusSeconds(7201)), null);
        assertEquals(44, global.getLatitude(), 1e-3);
        assertEquals(10, global.getLongitude(), 1e-3);
        assertFalse(global.isReasonable());
    }

    /**
     * The last position stays the reference for 120 s.
     */
    @Test
    void theReferenceExpiresAfter120Seconds() throws Exception {
        Position local = track().decodePosition(encode(40.1, 10, false, T0.plusSeconds(3 + 120)), null);
        assertEquals(40.1, local.getLatitude(), 1e-3);
        assertTrue(local.isReasonable());

        assertNull(track().decodePosition(encode(40.1, 10, false, T0.plusSeconds(3 + 121)), null));
    }

}
