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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.msgs.SingleAntennaMsg;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionV0MsgTest extends AirbornePositionMsgTest {

    @Override
    protected AirbornePositionMsg create(String hex) throws Exception {
        return new AirbornePositionV0Msg(Tools.hexStringToByteArray(hex), Instant.EPOCH);
    }

    @Test
    public void testSingleAntennaFlagIsExposed() throws Exception {
        AirbornePositionV0Msg msg = new AirbornePositionV0Msg(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357"), Instant.EPOCH);

        assertInstanceOf(SingleAntennaMsg.class, msg);
        assertFalse(msg.hasSingleAntenna());
        assertTrue(msg.toString().contains("singleAntennaFlag="));
    }
}
