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

package de.serosystems.lib1090;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalPositionDecodingTest {

    /**
     * Actual testing.
     *
     * @param surface whether surface (true) or airborne (false)
     * @param latEven CPR encoded latitude of even message
     * @param lonEven CPR encoded longitude of even message
     * @param latOdd  CPR encoded latitude of odd message
     * @param lonOdd  CPR encoded longitude of odd message
     * @param ref     reference position, may be null if not surface
     * @param expect  expected decoded position
     */
    private static void testDecode(boolean surface, int latEven, int lonEven, int latOdd, int lonOdd, Position ref, Position expect) {
        CPREncodedPosition cprEven = surface ? CPREncodedPosition.ofSurface(17, false, false, latEven, lonEven, Instant.EPOCH) : CPREncodedPosition.ofAirborne(17, false, latEven, lonEven, Instant.EPOCH);
        CPREncodedPosition cprOdd = surface ? CPREncodedPosition.ofSurface(17, true, false, latOdd, lonOdd, Instant.EPOCH) : CPREncodedPosition.ofAirborne(17, true, latOdd, lonOdd, Instant.EPOCH);

        Position even = cprEven.decodeGlobal(cprOdd, ref);
        Position odd = cprOdd.decodeGlobal(cprEven, ref);

        if (expect == null) {
            assertNull(even);
        } else {
            assertNotNull(even);
            assertEquals(expect.getLatitude(), even.getLatitude(), 0.00015);
            assertEquals(expect.getLongitude(), even.getLongitude(), 0.00015);
        }

        if (expect == null) {
            assertNull(odd);
        } else {
            assertNotNull(odd);
            assertEquals(expect.getLatitude(), odd.getLatitude(), 0.00015);
            assertEquals(expect.getLongitude(), odd.getLongitude(), 0.00015);
        }
    }

    @Test
    void testStraddle() {
        // whether straddling positions are handled properly
        testDecode(false, 0x0afd9, 0x0, 0x0d79c, 0x00000, null, null);
        testDecode(true, 0x0bf7e, 0x0, 0x15e70, 0x00000, new Position(0., 0., 0.), null);
    }

    @Test
    void testAirborne() {
        // one test in each quadrant (defined by the equator and prime meridian)
        {
            // DXB
            Position expected = new Position(55.3657, 25.2532, 0.);
            testDecode(false, 0x06AF1, 0x09C16, 0x04706, 0x04D58, null, expected);
        }
        {
            // LAX
            Position expected = new Position(-118.4085, 33.9416, 0.);
            testDecode(false, 0x1505A, 0x1C43E, 0x12014, 0x06CA5, null, expected);
        }
        {
            // SYD
            Position expected = new Position(151.1753, -33.9399, 0.);
            testDecode(false, 0x0AFCC, 0x1273D, 0x0E011, 0x0503C, null, expected);
        }
        {
            // SCL
            Position expected = new Position(-70.7858, -33.3928, 0.);
            testDecode(false, 0x0DE7B, 0x05658, 0x10DF9, 0x0BB04, null, expected);
        }
    }

    @Test
    void testSurface() {
        // one test in each quadrant (defined by the equator and prime meridian)
        // also: positions cover all longitude candidates
        {
            // DXB
            Position expected = new Position(55.3657, 25.2532, 0.);
            Position ref = new Position(30., 80., 0.);
            testDecode(true, 0x1ABC2, 0x07058, 0x11C19, 0x13560, ref, expected);
        }
        {
            // LAX
            Position expected = new Position(-118.4085, 33.9416, 0.);
            Position ref = new Position(-120., -10., 0.);
            testDecode(true, 0x14166, 0x110F9, 0x0804F, 0x1B296, ref, expected);
        }
        {
            // SYD
            Position expected = new Position(151.1753, -33.9399, 0.);
            Position ref = new Position(120., 10., 0.);
            testDecode(true, 0x0BF2E, 0x09CF4, 0x18043, 0x140EF, ref, expected);
        }
        {
            // SCL
            Position expected = new Position(-70.7858, -33.3928, 0.);
            Position ref = new Position(-110., -10., 0.);
            testDecode(true, 0x179ED, 0x1595F, 0x037E4, 0x0EC11, ref, expected);
        }
        {
            // Special cases: Equator vs Poles
            testDecode(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0., 10., 0.), new Position(0., 0., 0.));
            testDecode(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0., 60., 0.), new Position(0., 90., 0.));
            testDecode(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0., -60., 0.), new Position(0., -90., 0.));
        }
    }

    @Test
    void testSanityChecks() {
        // Test same format (both even)
        CPREncodedPosition cprEven1 = CPREncodedPosition.ofAirborne(17, false, 0x06AF1, 0x09C16, Instant.EPOCH);
        CPREncodedPosition cprEven2 = CPREncodedPosition.ofAirborne(17, false, 0x04706, 0x04D58, Instant.EPOCH);
        assertNull(cprEven1.decodeGlobal(cprEven2, null));

        // Test same format (both odd)
        CPREncodedPosition cprOdd1 = CPREncodedPosition.ofAirborne(17, true, 0x06AF1, 0x09C16, Instant.EPOCH);
        CPREncodedPosition cprOdd2 = CPREncodedPosition.ofAirborne(17, true, 0x04706, 0x04D58, Instant.EPOCH);
        assertNull(cprOdd1.decodeGlobal(cprOdd2, null));

        // Test mixing airborne and surface positions
        CPREncodedPosition cprAirborne = CPREncodedPosition.ofAirborne(17, false, 0x06AF1, 0x09C16, Instant.EPOCH);
        CPREncodedPosition cprSurface = CPREncodedPosition.ofSurface(17, true, false, 0x11C19, 0x13560, Instant.EPOCH);
        assertNull(cprAirborne.decodeGlobal(cprSurface, null));
        assertNull(cprSurface.decodeGlobal(cprAirborne, null));

        // Test surface position without reference position
        CPREncodedPosition cprSurfaceEven = CPREncodedPosition.ofSurface(17, false, false, 0x1ABC2, 0x07058, Instant.EPOCH);
        CPREncodedPosition cprSurfaceOdd = CPREncodedPosition.ofSurface(17, true, false, 0x11C19, 0x13560, Instant.EPOCH);
        assertNull(cprSurfaceEven.decodeGlobal(cprSurfaceOdd, null));
    }

    @Test
    void testAirborneTimingGap() {
        CPREncodedPosition cprEven = CPREncodedPosition.ofAirborne(17, false, 0x06AF1, 0x09C16, Instant.EPOCH);
        CPREncodedPosition cprOdd = CPREncodedPosition.ofAirborne(17, true, 0x04706, 0x04D58, Instant.EPOCH);

        assertEquals(Duration.ofMillis(10_000L), cprEven.maxGap(cprOdd));
        assertEquals(Duration.ofMillis(10_000L), cprOdd.maxGap(cprEven));
    }

    private static void testAirborneTiming(Instant t1, Instant t2, boolean expectSuccess) {
        CPREncodedPosition cprEven = CPREncodedPosition.ofAirborne(17, false, 0x06AF1, 0x09C16, t1);
        CPREncodedPosition cprOdd = CPREncodedPosition.ofAirborne(17, true, 0x04706, 0x04D58, t2);

        Position even = cprEven.decodeGlobal(cprOdd, null);
        Position odd = cprOdd.decodeGlobal(cprEven, null);

        if (expectSuccess) {
            assertNotNull(even);
            assertNotNull(odd);
        } else {
            assertNull(even);
            assertNull(odd);
        }
    }

    @Test
    void testAirborneTimingChecks() {
        testAirborneTiming(Instant.EPOCH, Instant.EPOCH, true);
        testAirborneTiming(Instant.EPOCH, Instant.EPOCH.plusMillis(10_000), true);
        testAirborneTiming(Instant.EPOCH, Instant.EPOCH.plusMillis(10_001), false);
        testAirborneTiming(Instant.EPOCH.plusMillis(10_000), Instant.EPOCH, true);
        testAirborneTiming(Instant.EPOCH.plusMillis(10_001), Instant.EPOCH, false);
    }

    private static void testSurfaceTimingGap(boolean hs1, boolean hs2, Duration expect) {
        CPREncodedPosition cprEven = CPREncodedPosition.ofSurface(17, false, hs1, 0x1ABC2, 0x07058, Instant.EPOCH);
        CPREncodedPosition cprOdd = CPREncodedPosition.ofSurface(17, true, hs2, 0x11C19, 0x13560, Instant.EPOCH);

        assertEquals(expect, cprEven.maxGap(cprOdd));
        assertEquals(expect, cprOdd.maxGap(cprEven));
    }

    @Test
    void testSurfaceTimingGap() {
        testSurfaceTimingGap(false, false, Duration.ofMillis(50_000));
        testSurfaceTimingGap(false, true, Duration.ofMillis(25_000));
        testSurfaceTimingGap(true, false, Duration.ofMillis(25_000));
        testSurfaceTimingGap(true, true, Duration.ofMillis(25_000));
    }

    private static void testSurfaceTiming(Instant t1, boolean hs1, Instant t2, boolean hs2, boolean expectSuccess) {
        Position ref = new Position(30., 80., 0.);

        CPREncodedPosition cprEven = CPREncodedPosition.ofSurface(17, false, hs1, 0x1ABC2, 0x07058, t1);
        CPREncodedPosition cprOdd = CPREncodedPosition.ofSurface(17, true, hs2, 0x11C19, 0x13560, t2);

        Position even = cprEven.decodeGlobal(cprOdd, ref);
        Position odd = cprOdd.decodeGlobal(cprEven, ref);

        if (expectSuccess) {
            assertNotNull(even);
            assertNotNull(odd);
        } else {
            assertNull(even);
            assertNull(odd);
        }
    }

    @Test
    void testSurfaceTimingChecks() {
        testSurfaceTiming(Instant.EPOCH, false, Instant.EPOCH, false, true);
        testSurfaceTiming(Instant.EPOCH.plusMillis(50_001), false, Instant.EPOCH, false, false);

        testSurfaceTiming(Instant.EPOCH, true, Instant.EPOCH, false, true);
        testSurfaceTiming(Instant.EPOCH, true, Instant.EPOCH.plusMillis(25_000), false, true);
        testSurfaceTiming(Instant.EPOCH, true, Instant.EPOCH.plusMillis(25_001), false, false);
        testSurfaceTiming(Instant.EPOCH, false, Instant.EPOCH.plusMillis(25_001), true, false);
        testSurfaceTiming(Instant.EPOCH.plusMillis(25_0001), false, Instant.EPOCH, true, false);
        testSurfaceTiming(Instant.EPOCH.plusMillis(25_0001), true, Instant.EPOCH, false, false);
    }

}
