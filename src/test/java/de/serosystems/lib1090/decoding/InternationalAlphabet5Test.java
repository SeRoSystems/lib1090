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

import static org.junit.jupiter.api.Assertions.*;

class InternationalAlphabet5Test {

    /**
     * ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8 defines A to Z, space and 0 to 9, and no other 6-bit code.
     */
    @Test
    void isDefined_followsTable38() {
        String defined = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
        int count = 0;
        for (byte code = 0; code < 64; code++) {
            boolean expected = code >= 1 && code <= 26 || code == 32 || code >= 48 && code <= 57;
            assertEquals(expected, InternationalAlphabet5.isDefined(code), "code " + code);
            if (expected) {
                assertTrue(defined.indexOf(InternationalAlphabet5.mapChar(code)) >= 0, "code " + code);
                count++;
            } else {
                assertEquals(' ', InternationalAlphabet5.mapChar(code), "code " + code);
            }
        }
        assertEquals(defined.length(), count);

        assertTrue(InternationalAlphabet5.isDefined(new byte[]{11, 12, 13, 49, 32}));
        assertFalse(InternationalAlphabet5.isDefined(new byte[]{11, 12, 13, 49, 63}));
    }

}
