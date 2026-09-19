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
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.SingleAntennaMsg;
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

    /**
     * Without the supplement the message reports the worst row its type code allows, which is not the
     * same as assuming the supplement is clear. At type code 13 a clear supplement is the better of
     * the two rows — Rc &lt; 926 m against &lt; 1111.2 m — so assuming it would overstate the position.
     */
    @Test
    void v1WithoutSupplementReportsTheWorstCase() throws Exception {
        // the message above with its type code changed from 11 to 13
        AirbornePositionV1Msg msg = new AirbornePositionV1Msg(
                new ExtendedSquitter(Tools.hexStringToByteArray("8D40058B68C901375147EFD09357")), Instant.EPOCH);

        assertEquals(13, msg.getFormatTypeCode());

        assertEquals(926.0, msg.getHorizontalContainmentRadiusLimit(false));
        assertEquals(1111.2, msg.getHorizontalContainmentRadiusLimit(true));
        assertEquals(1111.2, msg.getHorizontalContainmentRadiusLimit());

        assertEquals(ContainmentRadius.BELOW_1111_2, msg.getContainmentRadius());
        assertEquals(6, msg.getNIC());
    }

    /**
     * The variant that knows the supplement reports the row it selects, including the better one.
     */
    @Test
    void v1WithSupplementReportsTheSelectedRow() throws Exception {
        AirbornePositionV1Msg msg = new AirbornePositionV1Msg.WithNICSupplementA(
                new ExtendedSquitter(Tools.hexStringToByteArray("8D40058B68C901375147EFD09357")), Instant.EPOCH, false);

        assertEquals(ContainmentRadius.BELOW_926, msg.getContainmentRadius());
        assertEquals(926.0, msg.getHorizontalContainmentRadiusLimit());
        assertEquals(6, msg.getNIC());
    }
}
