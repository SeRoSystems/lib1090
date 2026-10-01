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

    /**
     * ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.2: 0 is no estimate, 1 is less than 0.05 NM, and n up to
     * 127 is (n - 1)/10 NM rounded to the nearest 0.1 NM, whose lower bound is reported.
     */
    @Test
    void testRange() throws Exception {
        assertNull(new ThreatIdentityData((short) 0, (short) 0, (short) 0).getRange());
        assertEquals(0.05f, new ThreatIdentityData((short) 0, (short) 1, (short) 0).getRange());
        // integer division used to make this -0.05
        assertEquals(0.35f, new ThreatIdentityData((short) 0, (short) 5, (short) 0).getRange(), 1e-6f);
        assertEquals(12.55f, new ThreatIdentityData((short) 0, (short) 127, (short) 0).getRange(), 1e-6f);
    }

    @Test
    void testBearing() throws Exception {
        assertNull(new ThreatIdentityData((short) 0, (short) 0, (short) 0).getBearing());
        assertArrayEquals(new Float[]{0f, 6f}, new ThreatIdentityData((short) 0, (short) 0, (short) 1).getBearing());
        assertArrayEquals(new Float[]{354f, 360f}, new ThreatIdentityData((short) 0, (short) 0, (short) 60).getBearing());
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
            assertEquals(code, tid.getEncodedBearing());
            assertEquals(0.35f, tid.getRange(), 1e-6f);
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
