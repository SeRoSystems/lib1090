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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentificationTest {

    /**
     * Arguments outside the table are rejected as illegal, not by an ArrayIndexOutOfBoundsException.
     */
    @Test
    void categoryDescription_rejectsArgumentsOutsideTheTable() {
        assertThrows(IllegalArgumentException.class, () -> Identification.categoryDescription((byte) 0, (byte) 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Identification.categoryDescription((byte) 5, (byte) 0, 2));
        assertThrows(IllegalArgumentException.class, () -> Identification.categoryDescription((byte) 4, (byte) -1, 2));
        assertThrows(IllegalArgumentException.class, () -> Identification.categoryDescription((byte) 4, (byte) 8, 2));
        assertThrows(IllegalArgumentException.class, () -> Identification.categoryDescription((byte) 4, (byte) 0, 8));
    }

    /**
     * ED-102B TABLE 2-16 with the version 0 and version 3 exceptions to the version 2 mapping.
     */
    @Test
    void categoryDescription_followsTheTableAndItsExceptions() {
        assertEquals("Rotorcraft", Identification.categoryDescription((byte) 4, (byte) 7, 2));
        assertEquals("No ADS-B Emitter Category Information",
                Identification.categoryDescription((byte) 1, (byte) 0, 2));
        assertEquals("Reserved", Identification.categoryDescription((byte) 1, (byte) 7, 2));
        assertEquals("Line Obstacle", Identification.categoryDescription((byte) 2, (byte) 5, 2));
        assertEquals("Fixed Ground or Tethered Obstruction", Identification.categoryDescription((byte) 2, (byte) 3, 0));
        assertEquals("MTOW >= 300000 lbs", Identification.categoryDescription((byte) 4, (byte) 5, 3));
        // versions above 3 are decoded like version 3
        assertEquals("MTOW >= 300000 lbs", Identification.categoryDescription((byte) 4, (byte) 5, 7));
    }

}
