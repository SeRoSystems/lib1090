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

import de.serosystems.lib1090.msgs.squitter.AirspeedHeadingMsg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AirspeedHeadingV1MsgTest extends AirspeedHeadingMsgTest {

    @Override
    protected AirspeedHeadingMsg create(String hex) throws Exception {
        return new AirspeedHeadingV1Msg(hex);
    }

    @Test
    public void testNACvRawAndAccuracyBound() throws Exception {
        AirspeedHeadingV1Msg msg = new AirspeedHeadingV1Msg("8DA05F219B06B6AF189400CBC33F");
        assertEquals(0, msg.getNACvEncoded());
        assertEquals(-1.0f, msg.getAccuracyBound());
    }
}
