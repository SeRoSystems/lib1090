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

import de.serosystems.lib1090.msgs.squitter.AirborneVelocityMsg;
import de.serosystems.lib1090.msgs.squitter.AirspeedHeadingMsg;

import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class AirspeedHeadingMsgTest {

    protected abstract AirspeedHeadingMsg create(String hex) throws Exception;

    @Test
    public void testHeadingPrecision() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertTrue(msg.hasHeadingStatusFlag());
        assertEquals(243.984375, msg.getHeading(), 0.001);
    }

    @Test
    public void testAirspeed() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertTrue(msg.hasAirspeed());
        assertEquals(375, msg.getAirspeed().intValue());
    }

    @Test
    public void testTrueAirspeed() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertTrue(msg.isTrueAirspeed());
    }

    @Test
    public void testImplementsAirborneVelocityMessage() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertInstanceOf(AirborneVelocityMsg.class, msg);
    }

    @Test
    public void testVerticalRate() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertTrue(msg.hasVerticalRate());
        assertEquals(-2304, msg.getVerticalRate().intValue());
    }

    @Test
    public void testGeoMinusBaroUnavailable() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertFalse(msg.hasDiffBaroAlt(), "geo-minus-baro should not be available when raw field is 0");
    }

    @Test
    public void testGeoMinusBaroReturnsNullWhenUnavailable() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertNull(msg.getDiffBaroAlt(), "getDiffBaroAlt() should return null when unavailable");
    }

    @Test
    public void testIcaoExtraction() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertInstanceOf(ExtendedSquitter.class, msg);
        assertEquals("a05f21", ((ExtendedSquitter) msg).getAddress().getHexAddress());
    }

    @Test
    public void testSubtype3NotSupersonic() throws Exception {
        AirspeedHeadingMsg msg = create("8DA05F219B06B6AF189400CBC33F");
        assertFalse(msg.isSupersonic());
    }

    @Test
    public void testNoHeadingAvailable() throws Exception {
        AirspeedHeadingMsg msg = create("8d4400cd9b0000b4f87000e71a10");
        assertFalse(msg.hasHeadingStatusFlag());
        assertNull(msg.getHeading());
    }
}
