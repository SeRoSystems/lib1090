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

package de.serosystems.lib1090.decoding;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.movement.Movement;
import de.serosystems.lib1090.decoding.movement.MovementV0V1;
import de.serosystems.lib1090.decoding.movement.MovementV2V3;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ED-102B §2.2.10.3.2 pairs an even and an odd surface position message within 50 seconds, or within
 * 25 seconds if the ground speed in either is greater than 25 knots or unknown.
 */
class SurfacePositionTest {

    private static final Duration SHORT = Duration.ofSeconds(25);
    private static final Duration LONG = Duration.ofSeconds(50);

    private static Duration pairingWindow(Movement movement) {
        CPREncodedPosition position = SurfacePosition.extractCPREncodedPosition(
                BitReader.forBigEndian(new byte[7]), movement, Instant.EPOCH);
        return position.maxGap(position);
    }

    private static Duration v0v1(int encoded) {
        return pairingWindow(MovementV0V1.forEncoded((byte) encoded));
    }

    private static Duration v2v3(int encoded) {
        return pairingWindow(MovementV2V3.forEncoded((byte) encoded));
    }

    /**
     * Code 48 is (24, 25] and code 49 (25, 26], so the table decides it exactly. The code threshold
     * used before, above 49, left code 49 on the long window although its speed certainly exceeds
     * 25 knots.
     */
    @Test
    void testVersion2And3() {
        assertEquals(LONG, v2v3(1));
        assertEquals(LONG, v2v3(48));
        assertEquals(SHORT, v2v3(49));
        assertEquals(SHORT, v2v3(124));
    }

    /**
     * Code 49 is [25, 26), which may or may not exceed 25 knots; it counts as greater, the shorter
     * window being the safe one. Code 48 is [24, 25) and cannot.
     */
    @Test
    void testVersion0And1() {
        assertEquals(LONG, v0v1(1));
        assertEquals(LONG, v0v1(48));
        assertEquals(SHORT, v0v1(49));
        assertEquals(SHORT, v0v1(124));
    }

    /** No movement information and the reserved codes leave the speed unknown. */
    @Test
    void testUnknownGroundSpeed() {
        for (int encoded : new int[]{0, 125, 126, 127}) {
            assertEquals(SHORT, v0v1(encoded), "v0/v1 " + encoded);
            assertEquals(SHORT, v2v3(encoded), "v2/v3 " + encoded);
        }
    }
}
