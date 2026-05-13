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

import static org.junit.jupiter.api.Assertions.*;

public class TrackAndTurnTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b10000001, (byte) 0b10010101, (byte) 0b00010101, (byte) 0b00110110, (byte) 0b11100000,
                (byte) 0b00100100, (byte) 0b11010100
        };

    }

    @Test
    public void rollAngle(){

        boolean status = TrackAndTurn.extractRollAngleStatus(msg);
        boolean sign = TrackAndTurn.extractRollAngleSign(msg);
        short value = TrackAndTurn.extractRollAngleValue(msg);
        Float rollAngle = TrackAndTurn.computeRollAngle(true, true, value);

        assertTrue(status);
        assertFalse(sign);
        assertEquals(12, value);
        assertNotNull(rollAngle);
        assertEquals(-87.9, rollAngle, 0.1);

    }

    @Test
    public void trueTrackAngle() {

        boolean status = TrackAndTurn.extractTrueTrackAngleStatus(msg);
        boolean sign = TrackAndTurn.extractTrueTrackAngleSign(msg);
        short value = TrackAndTurn.extractTrueTrackAngleValue(msg);
        Float trueTrackAngle = TrackAndTurn.computeTrueTrackAngle(true, true, value);

        assertTrue(status);
        assertFalse(sign);
        assertEquals(650, value);
        assertNotNull(trueTrackAngle);
        assertEquals(-65.7, trueTrackAngle, 0.1);

    }

    @Test
    public void groundSpeed() {

        boolean status = TrackAndTurn.extractGroundSpeedStatus(msg);
        short value = TrackAndTurn.extractGroundSpeedValue(msg);
        Integer groundSpeed = TrackAndTurn.computeGroundSpeed(true, value);

        assertTrue(status);
        assertEquals(219, value);
        assertNotNull(groundSpeed);
        assertEquals(438, groundSpeed.intValue());

    }

    @Test
    public void trackAngleRate() {

        boolean status = TrackAndTurn.extractTrackAngleRateStatus(msg);
        boolean sign = TrackAndTurn.extractTrackAngleRateSign(msg);
        short value = TrackAndTurn.extractTrackAngleRateValue(msg);
        Float trackAngleRate = TrackAndTurn.computeTrackAngleRate(true, true, value);

        assertTrue(status);
        assertFalse(sign);
        assertEquals(4, value);
        assertNotNull(trackAngleRate);
        assertEquals(-15.8, trackAngleRate, 0.1);

    }

    @Test
    public void trueAirSpeed() {

        boolean status = TrackAndTurn.extractTrueAirSpeedStatus(msg);
        short value = TrackAndTurn.extractTrueAirSpeedValue(msg);
        Integer trueAirspeed = TrackAndTurn.computeTrueAirSpeed(true, value);

        assertTrue(status);
        assertEquals(212, value);
        assertNotNull(trueAirspeed);
        assertEquals(424, trueAirspeed.intValue());

    }

}
