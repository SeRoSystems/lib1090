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

package de.serosystems.lib1090.decoding;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AltitudeTest {

    /**
     * The ICAO Annex 10 Volume IV Appendix to Chapter 3 table covers -1000 to 126 700 ft in 100 ft steps.
     */
    private static final int TABLE_ROWS = (126_700 + 1000) / 100 + 1;

    /**
     * In the lowest 500 ft band, C1C2C4 = 001 and 011 would give -1200 and -1100 ft, which the table does not
     * contain; C1C2C4 = 010 is its first row, -1000 ft.
     */
    @Test
    void lowestBand_startsAtTheTable() {
        assertNull(Altitude.decode13BitAltitude((short) 0x0100));
        assertNull(Altitude.decode13BitAltitude((short) 0x0500));
        assertEquals(-1000, Altitude.decode13BitAltitude((short) 0x0400));

        assertNull(Altitude.decode12BitAltitude((short) 0x080));
        assertNull(Altitude.decode12BitAltitude((short) 0x280));
        assertEquals(-1000, Altitude.decode12BitAltitude((short) 0x200));
    }

    /**
     * The accepted 13-bit codes in 100 ft increments (M and Q bit ZERO) decode to exactly the altitudes of the table,
     * each to a different one.
     */
    @Test
    void gillham13Bit_coversExactlyTheTable() {
        Set<Integer> altitudes = new HashSet<>();
        for (int code = 1; code < 0x2000; code++) {
            if ((code & 0x50) != 0) continue; // M or Q bit set
            Integer altitude = Altitude.decode13BitAltitude((short) code);
            if (altitude == null) continue;
            assertTableAltitude(altitude, code);
            assertTrue(altitudes.add(altitude), "altitude " + altitude + " decoded twice");
        }
        assertEquals(TABLE_ROWS, altitudes.size());
    }

    /**
     * The accepted 12-bit codes in 100 ft increments (Q bit ZERO) decode to exactly the altitudes of the table, each
     * to a different one.
     */
    @Test
    void gillham12Bit_coversExactlyTheTable() {
        Set<Integer> altitudes = new HashSet<>();
        for (int code = 1; code < 0x1000; code++) {
            if ((code & 0x10) != 0) continue; // Q bit set
            Integer altitude = Altitude.decode12BitAltitude((short) code);
            if (altitude == null) continue;
            assertTableAltitude(altitude, code);
            assertTrue(altitudes.add(altitude), "altitude " + altitude + " decoded twice");
        }
        assertEquals(TABLE_ROWS, altitudes.size());
    }

    private static void assertTableAltitude(int altitude, int code) {
        String message = "code 0x" + Integer.toHexString(code) + " gives " + altitude;
        assertTrue(altitude >= -1000 && altitude <= 126_700, message);
        assertEquals(0, altitude % 100, message);
    }
}
