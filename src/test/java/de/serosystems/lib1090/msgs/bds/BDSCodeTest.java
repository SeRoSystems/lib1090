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

class BDSCodeTest {

    @Test
    void testParts() {
        BDSCode code = new BDSCode(6, 0);
        assertEquals(6, code.getBDS1());
        assertEquals(0, code.getBDS2());
    }

    /** Doc 9871 writes each part as a hexadecimal digit. */
    @Test
    void testToString() {
        assertEquals("6,0", new BDSCode(6, 0).toString());
        assertEquals("5,F", new BDSCode(5, 15).toString());
        assertEquals("E,1", new BDSCode(14, 1).toString());
    }

    @Test
    void testEquality() {
        assertEquals(new BDSCode(1, 7), new BDSCode(1, 7));
        assertEquals(new BDSCode(1, 7).hashCode(), new BDSCode(1, 7).hashCode());
        assertNotEquals(new BDSCode(1, 7), new BDSCode(7, 1));
    }

    @Test
    void testPartsMustFitFourBits() {
        assertThrows(IllegalArgumentException.class, () -> new BDSCode(16, 0));
        assertThrows(IllegalArgumentException.class, () -> new BDSCode(0, -1));
    }

    @Test
    void testEveryRegisterReportsItsCode() throws Exception {
        byte[] message = new byte[7];

        assertEquals(new BDSCode(1, 0), new DataLinkCapabilityReport(message).getBDSCode());
        assertEquals(new BDSCode(1, 7), new CommonUsageGICBCapabilityReport(message).getBDSCode());
        assertEquals(new BDSCode(2, 0), new AircraftIdentification(message).getBDSCode());
        assertEquals(new BDSCode(3, 0), new ACASActiveResolutionAdvisoryReport(message).getBDSCode());
        assertEquals(new BDSCode(4, 0), new SelectedVerticalIntention(message).getBDSCode());
        assertEquals(new BDSCode(5, 0), new TrackAndTurn(message).getBDSCode());
        assertEquals(new BDSCode(6, 0), new HeadingAndSpeed(message).getBDSCode());
    }
}
