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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Map<String, Boolean> map = CommonUsageGICBCapabilityReport.extractCommonGICBCapabilityReport(msg);

        assertTrue(map.get("BDS05"));
        assertTrue(map.get("BDS06"));
        assertTrue(map.get("BDS07"));
        assertTrue(map.get("BDS08"));
        assertTrue(map.get("BDS09"));
        assertFalse(map.get("BDS0A"));
        assertTrue(map.get("BDS20"));
        assertFalse(map.get("BDS21"));

        assertTrue(map.get("BDS40"));
        assertFalse(map.get("BDS41"));
        assertFalse(map.get("BDS42"));
        assertFalse(map.get("BDS43"));
        assertFalse(map.get("BDS44"));
        assertFalse(map.get("BDS45"));
        assertFalse(map.get("BDS48"));
        assertTrue(map.get("BDS50"));

        assertTrue(map.get("BDS51"));
        assertTrue(map.get("BDS52"));
        assertFalse(map.get("BDS53"));
        assertFalse(map.get("BDS54"));
        assertFalse(map.get("BDS55"));
        assertFalse(map.get("BDS56"));
        assertFalse(map.get("BDS5F"));
        assertTrue(map.get("BDS60"));

        assertFalse(map.get("BDSE1"));
        assertFalse(map.get("BDSE2"));
        assertFalse(map.get("BDSF1"));

    }

}
