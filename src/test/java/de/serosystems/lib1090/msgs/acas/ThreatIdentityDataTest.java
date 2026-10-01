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

package de.serosystems.lib1090.msgs.acas;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreatIdentityDataTest {

    private static ThreatIdentityData range(int code) {
        return new ThreatIdentityData((short) 0, (short) code, (short) 0);
    }

    private static ThreatIdentityData bearing(int code) {
        return new ThreatIdentityData((short) 0, (short) 0, (short) code);
    }

    /**
     * ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.2: 0 is no estimate, 1 is less than 0.05 NM, n up to 126 is
     * (n - 1)/10 NM ±0.05, and 127 is greater than 12.55 NM. getRange() gives the estimate, getRangeInterval() the
     * range; codes 1 and 127 have no estimate.
     */
    @Test
    void testRange() {
        assertNull(range(0).getRange());
        assertNull(range(0).getRangeInterval());

        assertNull(range(1).getRange());
        assertEquals(Interval.of(Bound.AT_LEAST, 0, Bound.BELOW, 0.05), range(1).getRangeInterval());

        assertEquals(0.1f, range(2).getRange(), 1e-6f);
        assertEquals(Interval.of(Bound.AT_LEAST, 0.05, Bound.AT_MOST, 0.15), range(2).getRangeInterval());
        // integer division used to make this -0.05, and the lower bound once was returned instead of the estimate
        assertEquals(0.4f, range(5).getRange(), 1e-6f);
        assertEquals(12.5f, range(126).getRange(), 1e-6f);
        assertEquals(Interval.of(Bound.AT_LEAST, 12.45, Bound.AT_MOST, 12.55), range(126).getRangeInterval());

        assertNull(range(127).getRange());
        assertEquals(Interval.of(Bound.MORE_THAN, 12.55, Bound.NONE, 0), range(127).getRangeInterval());
    }

    /**
     * ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.3: code n from 1 to 60 is between 6(n - 1) and 6n degrees; getBearing()
     * gives the middle, getBearingInterval() the range.
     */
    @Test
    void testBearing() {
        assertNull(bearing(0).getBearing());
        assertNull(bearing(0).getBearingInterval());

        assertEquals(3f, bearing(1).getBearing());
        assertEquals(Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 6), bearing(1).getBearingInterval());
        assertEquals(357f, bearing(60).getBearing());
        assertEquals(Interval.of(Bound.AT_LEAST, 354, Bound.AT_MOST, 360), bearing(60).getBearingInterval());
    }

    /**
     * ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.3: the bearing codes 61 to 63 are not assigned; they decode as no
     * bearing estimate, and the code stays available.
     */
    @Test
    void unassignedBearing_isNoEstimate() {
        for (short code = 61; code <= 63; code++) {
            ThreatIdentityData tid = new ThreatIdentityData((short) 0, (short) 5, code);
            assertNull(tid.getBearing());
            assertNull(tid.getBearingInterval());
            assertEquals(code, tid.getEncodedBearing());
            assertEquals(0.4f, tid.getRange(), 1e-6f);
        }
    }

    /**
     * Values that do not fit the 7-bit range or 6-bit bearing field are a caller error.
     */
    @Test
    void valuesOutsideTheirFields_areRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ThreatIdentityData((short) 0, (short) 128, (short) 0));
        assertThrows(IllegalArgumentException.class, () -> new ThreatIdentityData((short) 0, (short) -1, (short) 0));
        assertThrows(IllegalArgumentException.class, () -> new ThreatIdentityData((short) 0, (short) 0, (short) 64));
        assertThrows(IllegalArgumentException.class, () -> new ThreatIdentityData((short) 0, (short) 0, (short) -1));
    }

    /**
     * A threat identified by its address carries no altitude, range or bearing, and reports none.
     */
    @Test
    void testIdentifiedByAddress() {
        ThreatIdentityData tid = new ThreatIdentityData(0xabcdef);

        assertTrue(tid.hasTransponderAddress());
        assertEquals(0xabcdef, tid.getIcao24());
        assertNull(tid.getAltitude());
        assertNull(tid.getRange());
        assertNull(tid.getBearing());
    }

    /**
     * ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.2.9.1: the ACAS X altitude is binary, 0 no data, 1 below
     * -950 ft, and n from 2 on at least 100 n - 1150 ft and below 100 n - 1050 ft.
     */
    @Test
    void binaryAltitude() {
        ThreatIdentityData none = ThreatIdentityData.withBinaryAltitude((short) 0, (short) 0, (short) 0);
        assertTrue(none.isAltitudeBinary());
        assertNull(none.getAltitude());
        assertNull(none.getAltitudeInterval());

        ThreatIdentityData below = ThreatIdentityData.withBinaryAltitude((short) 1, (short) 0, (short) 0);
        assertNull(below.getAltitude());
        assertEquals(Interval.of(Bound.NONE, 0, Bound.BELOW, -950), below.getAltitudeInterval());

        ThreatIdentityData lowest = ThreatIdentityData.withBinaryAltitude((short) 2, (short) 0, (short) 0);
        assertEquals(-900, (int) lowest.getAltitude());
        assertEquals(Interval.of(Bound.AT_LEAST, -950, Bound.BELOW, -850), lowest.getAltitudeInterval());

        assertEquals(203_600, (int) ThreatIdentityData.withBinaryAltitude((short) 2047, (short) 0, (short) 0)
                .getAltitude());
        assertThrows(IllegalArgumentException.class,
                () -> ThreatIdentityData.withBinaryAltitude((short) 2048, (short) 0, (short) 0));
    }

    /**
     * A Mode C altitude (TCAS layout) decodes through the Gillham code and has no interval.
     */
    @Test
    void modeCAltitude_hasNoInterval() {
        ThreatIdentityData tid = new ThreatIdentityData((short) 0x0400, (short) 0, (short) 0); // -1000 ft
        assertFalse(tid.isAltitudeBinary());
        assertEquals(-1000, (int) tid.getAltitude());
        assertNull(tid.getAltitudeInterval());
    }

}
