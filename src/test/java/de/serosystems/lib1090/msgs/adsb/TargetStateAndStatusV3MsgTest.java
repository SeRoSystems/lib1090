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

package de.serosystems.lib1090.msgs.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TargetStateAndStatusV3MsgTest {

    /**
     * ED-102B TABLE 2-34: the top selected altitude code means 65456 ft or more and is flagged as such; the code
     * below it is not.
     */
    @Test
    void topSelectedAltitudeCodeIsSaturated() throws Exception {
        TargetStateAndStatusV3Msg top = new TargetStateAndStatusV3Msg("8D4840D6EA7FF000000000000000"); // 2047
        assertTrue(top.isSelectedAltitudeSaturated());
        assertEquals(65472, top.getSelectedAltitude().intValue());

        TargetStateAndStatusV3Msg below = new TargetStateAndStatusV3Msg("8D4840D6EA7FE000000000000000"); // 2046
        assertFalse(below.isSelectedAltitudeSaturated());
        assertEquals(65440, below.getSelectedAltitude().intValue());
    }

}
