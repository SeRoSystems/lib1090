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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommonUsageGICBCapabilityReportTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {
        msg = new byte[]{
                (byte) 0b11111010, (byte) 0b10000001, (byte) 0b11000001, (byte) 0b00000000, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };
    }

    @Test
    public void commonGICBCapabilityReport() {
        Map<BDSCode, Boolean> map = new CommonUsageGICBCapabilityReport(msg).getCommonUsageGICBCapabilityReport();

        assertTrue(map.get(new BDSCode(0, 5)));
        assertTrue(map.get(new BDSCode(0, 6)));
        assertTrue(map.get(new BDSCode(0, 7)));
        assertTrue(map.get(new BDSCode(0, 8)));
        assertTrue(map.get(new BDSCode(0, 9)));
        assertFalse(map.get(new BDSCode(0, 0xA)));
        assertTrue(map.get(new BDSCode(2, 0)));
        assertFalse(map.get(new BDSCode(2, 1)));

        assertTrue(map.get(new BDSCode(4, 0)));
        assertFalse(map.get(new BDSCode(4, 1)));
        assertFalse(map.get(new BDSCode(4, 2)));
        assertFalse(map.get(new BDSCode(4, 3)));
        assertFalse(map.get(new BDSCode(4, 4)));
        assertFalse(map.get(new BDSCode(4, 5)));
        assertFalse(map.get(new BDSCode(4, 8)));
        assertTrue(map.get(new BDSCode(5, 0)));

        assertTrue(map.get(new BDSCode(5, 1)));
        assertTrue(map.get(new BDSCode(5, 2)));
        assertFalse(map.get(new BDSCode(5, 3)));
        assertFalse(map.get(new BDSCode(5, 4)));
        assertFalse(map.get(new BDSCode(5, 5)));
        assertFalse(map.get(new BDSCode(5, 6)));
        assertFalse(map.get(new BDSCode(5, 0xF)));
        assertTrue(map.get(new BDSCode(6, 0)));

        assertFalse(map.get(new BDSCode(0xE, 1)));
        assertFalse(map.get(new BDSCode(0xE, 2)));
        assertFalse(map.get(new BDSCode(0xF, 1)));
    }

    /** The map follows the register list of Table A-2-23, which is also what toString() prints. */
    @Test
    public void followsTheTableOrder() {
        Map<BDSCode, Boolean> map = new CommonUsageGICBCapabilityReport(msg).getCommonUsageGICBCapabilityReport();

        assertEquals(27, map.size());
        assertEquals(new BDSCode(0, 5), new ArrayList<>(map.keySet()).get(0));
        assertEquals(new BDSCode(0xF, 1), new ArrayList<>(map.keySet()).get(26));
        assertTrue(map.toString().startsWith("{0,5=true, 0,6=true, 0,7=true, 0,8=true, 0,9=true, 0,A=false, 2,0=true"));
    }

    /** A register the report does not cover is not a key, so it reads as null rather than false. */
    @Test
    public void uncoveredRegisterIsNotAKey() {
        Map<BDSCode, Boolean> map = new CommonUsageGICBCapabilityReport(msg).getCommonUsageGICBCapabilityReport();

        assertNull(map.get(new BDSCode(1, 0)));
    }

    @Test
    public void isUnmodifiable() {
        Map<BDSCode, Boolean> map = new CommonUsageGICBCapabilityReport(msg).getCommonUsageGICBCapabilityReport();

        assertThrows(UnsupportedOperationException.class, () -> map.put(new BDSCode(1, 0), true));
    }

}
