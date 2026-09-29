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

package de.serosystems.lib1090.msgs.tisb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ED-102B §2.2.17.3.4.1 decodes ME bit 36 of the TIS-B velocity messages, labeled "GEO Flag" in FIGURE 2-55, as
 * the ADS-B "Source Bit for Vertical Rate": set means barometric (TABLE 2-23). The same bit selects the difference
 * from barometric altitude, when set, or NACv and SIL, when clear, for ME bits 48-56.
 */
class VerticalRateSourceTest {

    /**
     * A TIS-B velocity message (DF=18, CF=2, TYPE Code 19) of the given subtype, with ME bit 36 as given and
     * NACv 2 when it is clear or a difference from barometric altitude code of 5 when it is set.
     */
    private static String message(int subtype, boolean geoFlag) {
        long me = 19L << 51 | (long) subtype << 48 | 101L << 21; // N/S velocity 100 kt
        if (geoFlag) me |= 1L << 20 | 5L;
        else me |= 2L << 6;
        return "924840D6" + String.format("%014X", me) + "000000";
    }

    @Test
    void velocityOverGround() throws Exception {
        VelocityOverGroundMsg barometric = new VelocityOverGroundMsg(message(1, true));
        assertTrue(barometric.isBarometricVerticalSpeed());
        assertTrue(barometric.hasDiffBaroAlt());
        assertNull(barometric.getNACvEncoded());

        VelocityOverGroundMsg geometric = new VelocityOverGroundMsg(message(1, false));
        assertFalse(geometric.isBarometricVerticalSpeed());
        assertFalse(geometric.hasDiffBaroAlt());
        assertEquals(2, geometric.getNACvEncoded().intValue());
    }

    @Test
    void airspeedHeading() throws Exception {
        assertTrue(new AirspeedHeadingMsg(message(3, true)).isBarometricVerticalSpeed());
        assertFalse(new AirspeedHeadingMsg(message(3, false)).isBarometricVerticalSpeed());
    }

}
