package de.serosystems.lib1090;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.cpr.CompactPositionReporting;
import org.junit.jupiter.api.Test;

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
    private static void test(boolean surface, int latEven, int lonEven, int latOdd, int lonOdd, Position ref, Position expect) {
        CPREncodedPosition cprEven = new CPREncodedPosition(false, latEven, lonEven, 17, surface, null);
        CPREncodedPosition cprOdd = new CPREncodedPosition(true, latOdd, lonOdd, 17, surface, null);

        Position even = CompactPositionReporting.decodeGlobalPosition(cprEven, cprOdd, ref);
        Position odd = CompactPositionReporting.decodeGlobalPosition(cprOdd, cprEven, ref);

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
        test(false, 0x0afd9, 0x0, 0x0d79c, 0x00000, null, null);
        test(true, 0x0bf7e, 0x0, 0x15e70, 0x00000, new Position(0., 0., 0.), null);
    }

    @Test
    void testAirborne() {
        // one test in each quadrant (defined by the equator and prime meridian)
        {
            // DXB
            Position expected = new Position(55.3657, 25.2532, 0.);
            test(false, 0x06AF1, 0x09C16, 0x04706, 0x04D58, null, expected);
        }
        {
            // LAX
            Position expected = new Position(-118.4085, 33.9416, 0.);
            test(false, 0x1505A, 0x1C43E, 0x12014, 0x06CA5, null, expected);
        }
        {
            // SYD
            Position expected = new Position(151.1753, -33.9399, 0.);
            test(false, 0x0AFCC, 0x1273D, 0x0E011, 0x0503C, null, expected);
        }
        {
            // SCL
            Position expected = new Position(-70.7858, -33.3928, 0.);
            test(false, 0x0DE7B, 0x05658, 0x10DF9, 0x0BB04, null, expected);
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
            test(true, 0x1ABC2, 0x07058, 0x11C19, 0x13560, ref, expected);
        }
        {
            // LAX
            Position expected = new Position(-118.4085, 33.9416, 0.);
            Position ref = new Position(-120., -10., 0.);
            test(true, 0x14166, 0x110F9, 0x0804F, 0x1B296, ref, expected);
        }
        {
            // SYD
            Position expected = new Position(151.1753, -33.9399, 0.);
            Position ref = new Position(120., 10., 0.);
            test(true, 0x0BF2E, 0x09CF4, 0x18043, 0x140EF, ref, expected);
        }
        {
            // SCL
            Position expected = new Position(-70.7858, -33.3928, 0.);
            Position ref = new Position(-110., -10., 0.);
            test(true, 0x179ED, 0x1595F, 0x037E4, 0x0EC11, ref, expected);
        }
        {
            // Special cases: Equator vs Poles
            test(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0.,10.,0.), new Position(0.,0.,0.));
            test(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0.,60.,0.), new Position(0.,90.,0.));
            test(true, 0x00000, 0x00000, 0x00000, 0x00000, new Position(0.,-60.,0.), new Position(0.,-90.,0.));
        }
    }

    @Test
    void testSanityChecks() {
        // Test same format (both even)
        CPREncodedPosition cprEven1 = new CPREncodedPosition(false, 0x06AF1, 0x09C16, 17, false, null);
        CPREncodedPosition cprEven2 = new CPREncodedPosition(false, 0x04706, 0x04D58, 17, false, null);
        assertNull(CompactPositionReporting.decodeGlobalPosition(cprEven1, cprEven2, null));

        // Test same format (both odd)
        CPREncodedPosition cprOdd1 = new CPREncodedPosition(true, 0x06AF1, 0x09C16, 17, false, null);
        CPREncodedPosition cprOdd2 = new CPREncodedPosition(true, 0x04706, 0x04D58, 17, false, null);
        assertNull(CompactPositionReporting.decodeGlobalPosition(cprOdd1, cprOdd2, null));

        // Test mixing airborne and surface positions
        CPREncodedPosition cprAirborne = new CPREncodedPosition(false, 0x06AF1, 0x09C16, 17, false, null);
        CPREncodedPosition cprSurface = new CPREncodedPosition(true, 0x11C19, 0x13560, 17, true, null);
        assertNull(CompactPositionReporting.decodeGlobalPosition(cprAirborne, cprSurface, null));
        assertNull(CompactPositionReporting.decodeGlobalPosition(cprSurface, cprAirborne, null));

        // Test surface position without reference position
        CPREncodedPosition cprSurfaceEven = new CPREncodedPosition(false, 0x1ABC2, 0x07058, 17, true, null);
        CPREncodedPosition cprSurfaceOdd = new CPREncodedPosition(true, 0x11C19, 0x13560, 17, true, null);
        assertNull(CompactPositionReporting.decodeGlobalPosition(cprSurfaceEven, cprSurfaceOdd, null));
    }

}
