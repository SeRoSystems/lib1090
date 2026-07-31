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
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionV1MsgTest extends AirbornePositionMsgTest {

    @Override
    protected AirbornePositionMsg create(String hex) throws Exception {
        return new AirbornePositionV1Msg(Tools.hexStringToByteArray(hex), Instant.EPOCH);
    }

    @Test
    void v1UsesExplicitNicSupplement() throws Exception {
        AirbornePositionV1Msg msg = new AirbornePositionV1Msg(
                new ExtendedSquitter(Tools.hexStringToByteArray("8D40058B58C901375147EFD09357")), Instant.EPOCH);

        assertEquals(9, msg.getNIC(true));
        assertEquals(8, msg.getNIC(false));
        assertEquals(75.0, msg.getHorizontalContainmentRadiusLimit(true));
        assertEquals(185.2, msg.getHorizontalContainmentRadiusLimit(false));
        assertInstanceOf(SingleAntennaMsg.class, msg);
        assertFalse(msg.hasSingleAntenna());
        assertTrue(msg.toString().contains("AirbornePositionV1Msg{"));
        assertTrue(msg.toString().contains("singleAntennaFlag="));
    }
}
