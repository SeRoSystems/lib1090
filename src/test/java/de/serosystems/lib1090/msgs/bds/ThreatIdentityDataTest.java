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

package de.serosystems.lib1090.msgs.bds;

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

    /** A threat identified by its address carries no altitude, range or bearing, and reports none. */
    @Test
    void testIdentifiedByAddress() {
        ThreatIdentityData tid = new ThreatIdentityData(0xabcdef);

        assertTrue(tid.hasTransponderAddress());
        assertEquals(0xabcdef, tid.getIcao24());
        assertNull(tid.getAltitude());
        assertNull(tid.getRange());
        assertNull(tid.getBearing());
    }
}
