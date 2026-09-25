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

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.msgs.squitter.AirborneVelocityMsg;
import de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class VelocityOverGroundMsgTest {

    protected abstract VelocityOverGroundMsg create(String hex) throws Exception;

    @Test
    public void testGroundSpeed_485020() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertTrue(msg.hasVelocity());
        assertEquals(159.0, msg.getGroundSpeed(), 1.0);
    }

    @Test
    public void testTrackAngle_485020() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertEquals(182.88, msg.getTrueTrackAngle(), 0.01);
    }

    @Test
    public void testVerticalRate_485020() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertTrue(msg.hasVerticalRate());
        assertEquals(-832, msg.getVerticalRate().intValue());
    }

    @Test
    public void testImplementsAirborneVelocityMessage() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertInstanceOf(AirborneVelocityMsg.class, msg);
    }

    @Test
    public void testGeoMinusBaro_485020() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertTrue(msg.hasDiffBaroAlt());
        assertEquals(550, msg.getDiffBaroAlt().getValue().intValue());
    }

    @Test
    public void testGroundSpeed_45AC2D() throws Exception {
        VelocityOverGroundMsg msg = create("8d45ac2d9904d910613f94ba81b5");
        assertEquals(252.0, msg.getGroundSpeed(), 1.0);
    }

    @Test
    public void testHeading_45AC2D() throws Exception {
        VelocityOverGroundMsg msg = create("8d45ac2d9904d910613f94ba81b5");
        assertEquals(301.04, msg.getTrueTrackAngle(), 0.01);
    }

    @Test
    public void testVerticalRatePositive_45AC2D() throws Exception {
        VelocityOverGroundMsg msg = create("8d45ac2d9904d910613f94ba81b5");
        assertEquals(4992, msg.getVerticalRate().intValue());
    }

    @Test
    public void testGeoMinusBaroNegative_45AC2D() throws Exception {
        VelocityOverGroundMsg msg = create("8d45ac2d9904d910613f94ba81b5");
        assertEquals(-475, msg.getDiffBaroAlt().getValue().intValue());
    }

    @Test
    public void testSmallPositiveVerticalRate_451E8B() throws Exception {
        VelocityOverGroundMsg msg = create("8D451E8B99019699C00B0A81F36E");
        assertTrue(msg.hasVerticalRate());
        assertEquals(64, msg.getVerticalRate().intValue());
    }

    @Test
    public void testVerticalRate64_3461cf_a() throws Exception {
        VelocityOverGroundMsg msg = create("8d3461cf9908388930080f948ea1");
        assertEquals(64, msg.getVerticalRate().intValue());
        assertEquals(350, msg.getDiffBaroAlt().getValue().intValue());
    }

    @Test
    public void testVerticalRate128_3461cf_b() throws Exception {
        VelocityOverGroundMsg msg = create("8d3461cf9908558e100c1071eb67");
        assertEquals(128, msg.getVerticalRate().intValue());
        assertEquals(375, msg.getDiffBaroAlt().getValue().intValue());
    }

    @Test
    public void testVerticalRate960_3461cf_c() throws Exception {
        VelocityOverGroundMsg msg = create("8d3461cf99085a8f10400f80e6ac");
        assertEquals(960, msg.getVerticalRate().intValue());
    }

    @Test
    public void testVerticalRateNeg64_394c0f() throws Exception {
        VelocityOverGroundMsg msg = create("8d394c0f990c4932780838866883");
        assertEquals(-64, msg.getVerticalRate().intValue());
        assertEquals(1375, msg.getDiffBaroAlt().getValue().intValue());
    }

    @Test
    public void testSpeedType() throws Exception {
        VelocityOverGroundMsg msg = create("8D485020994409940838175B284F");
        assertFalse(msg.isSupersonic(), "Ground speed messages should not be supersonic");
    }

    /**
     * The message with its Difference from Barometric Altitude replaced: the sign in ME 49 and the
     * 7-bit magnitude in ME 50-56, which together are the last ME byte.
     */
    static String withDiffBaroAlt(String hex, boolean negative, int encoded) {
        return hex.substring(0, 20) + String.format("%02X", (negative ? 0x80 : 0) | encoded) + hex.substring(22);
    }

    /**
     * The 7-bit coding of versions 0 to 2: code L is (L - 1) * 25 ft rounded, 1 covers [0, 12.5] ft, and
     * only 127 is unbounded, as every difference above 3137.5 ft; 0 reports nothing. The sign mirrors it.
     */
    static void assertDiffBaroAltCoding(AirborneVelocityMsg unavailable, AirborneVelocityMsg smallest,
                                        AirborneVelocityMsg largestBounded, AirborneVelocityMsg highest,
                                        AirborneVelocityMsg highestNegative) {
        assertFalse(unavailable.hasDiffBaroAlt());
        assertNull(unavailable.getDiffBaroAlt().getDifference());

        assertTrue(smallest.hasDiffBaroAlt());
        assertEquals(0., smallest.getDiffBaroAlt().getValue());
        assertEquals(0., smallest.getDiffBaroAlt().getDifference().getLower());
        assertEquals(12.5, smallest.getDiffBaroAlt().getDifference().getUpper());

        assertEquals(3125., largestBounded.getDiffBaroAlt().getValue());
        assertEquals(3137.5, largestBounded.getDiffBaroAlt().getDifference().getUpper());

        assertEquals(Bound.NONE, highest.getDiffBaroAlt().getDifference().getUpperBound());
        assertEquals(3137.5, highest.getDiffBaroAlt().getDifference().getLower());
        assertEquals(3137.5, highest.getDiffBaroAlt().getValue());

        assertEquals(Bound.NONE, highestNegative.getDiffBaroAlt().getDifference().getLowerBound());
        assertEquals(-3137.5, highestNegative.getDiffBaroAlt().getDifference().getUpper());
    }

    @Test
    public void testDiffBaroAltCoding() throws Exception {
        String hex = "8D485020994409940838175B284F";
        assertDiffBaroAltCoding(
                create(withDiffBaroAlt(hex, false, 0)),
                create(withDiffBaroAlt(hex, false, 1)),
                create(withDiffBaroAlt(hex, false, 126)),
                create(withDiffBaroAlt(hex, false, 127)),
                create(withDiffBaroAlt(hex, true, 127)));
    }
}
