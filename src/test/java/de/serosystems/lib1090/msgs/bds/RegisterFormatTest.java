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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What tells a register from another one: the code of BDS 1,0, 2,0 and 3,0 in MB bits 1-8, and the format rules of
 * BDS 4,0, 5,0 and 6,0, which carry no code.
 */
class RegisterFormatTest {

    private static byte[] mb(String hex) {
        return Tools.hexStringToByteArray(hex);
    }

    @Test
    void selfIdentifyingRegisters_rejectAnotherCode() {
        assertThrows(BadFormatException.class, () -> DataLinkCapabilityReport.decode(mb("20000000000000")));
        assertThrows(BadFormatException.class, () -> new AircraftIdentification(mb("30000000000000")));
        assertThrows(BadFormatException.class, () -> new ACASActiveResolutionAdvisoryReport(mb("10000000000000")));
        assertThrows(BadFormatException.class, () -> new ACASActiveResolutionAdvisoryReport(mb("31000000000000")));
    }

    /**
     * Doc 9871 Table A-2-64: reserved MB bits 40-47 and 52-53 are ZERO; a field whose status bit is ZERO is ZERO.
     */
    @Test
    void selectedVerticalIntention() {
        assertTrue(new SelectedVerticalIntention(mb("00000000000000")).isConsistent());
        assertTrue(new SelectedVerticalIntention(mb("80080000000000")).isConsistent()); // status 1 with data
        assertFalse(new SelectedVerticalIntention(mb("be80000001fe00")).isConsistent()); // the repro: bits 40-47
        assertFalse(new SelectedVerticalIntention(mb("00000000000030")).isConsistent()); // bits 52-53
        assertFalse(new SelectedVerticalIntention(mb("40000000000000")).isConsistent()); // data without status 1
        assertFalse(new SelectedVerticalIntention(mb("00000000000080")).isConsistent()); // bit 49, no status 48
        assertTrue(new SelectedVerticalIntention(mb("00000000000180")).isConsistent()); // status 48 and bit 49
    }

    /**
     * Doc 9871 Table A-2-80 Note 3: all bits of a parameter that is not available are ZERO.
     */
    @Test
    void trackAndTurn() {
        assertTrue(new TrackAndTurn(mb("00000000000000")).isConsistent());
        assertTrue(new TrackAndTurn(mb("e0000000000000")).isConsistent()); // roll angle with status
        assertFalse(new TrackAndTurn(mb("60000000000000")).isConsistent()); // roll angle without status
        assertFalse(new TrackAndTurn(mb("00000000000001")).isConsistent()); // true airspeed without status 46
    }

    /**
     * Doc 9871 Table A-2-96 and §A.2.1.1: all bits of a field whose status bit is ZERO are ZERO.
     */
    @Test
    void headingAndSpeed() {
        assertTrue(new HeadingAndSpeed(mb("00000000000000")).isConsistent());
        assertTrue(new HeadingAndSpeed(mb("c0000000000000")).isConsistent()); // heading sign with status
        assertFalse(new HeadingAndSpeed(mb("40000000000000")).isConsistent()); // heading sign without status
        assertFalse(new HeadingAndSpeed(mb("00000000000001")).isConsistent()); // vertical velocity without status 46
    }
}
