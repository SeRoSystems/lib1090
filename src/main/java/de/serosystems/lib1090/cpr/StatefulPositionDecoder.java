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

package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

import java.time.Duration;
import java.time.Instant;

/**
 * Stateful decoder for positions. Use this one to decode positions.
 */
public class StatefulPositionDecoder implements PositionDecoder {
    /**
     * Maximum distance [m] to receiver
     */
    private static final int MAX_DIST_TO_SENDER = 700000; // 700km

    private CPREncodedPosition lastEven;
    private CPREncodedPosition lastOdd;
    private Position lastPosition;
    private Instant lastTime;
    private int numReasonable; // number of successive reasonable messages
    private final boolean disableSpeedTest;

    /**
     * Default constructor that uses speed test
     */
    public StatefulPositionDecoder() {
        this(false);
    }

    /**
     * Constructor that allows disabling of speed-based reasonableness test. Use this
     * if you have a network of heterogeneous receivers with variable data delay and
     * no deduplication.
     *
     * @param disableSpeedTest true if speed test should not be applied
     */
    public StatefulPositionDecoder(boolean disableSpeedTest) {
        this.disableSpeedTest = disableSpeedTest;
    }

    /**
     * Decodes position with speed estimation-based reasonableness test.
     *
     * @param cpr      CPR encoded position
     * @param receiver position of the receiver for surface decoding and to check if received position was more than 700km away;
     *                 null disables checks and surface decoding
     * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in feet. altitude might be null
     * if unavailable. On error, the returned position is null. Check the .isReasonable() flag before using
     * the position.
     */
    @Override
    public Position decodePosition(CPREncodedPosition cpr, Position receiver) {
        if (cpr == null) return null;

        // get last position in complementary format for global decoding
        CPREncodedPosition lastOther = cpr.isOddFormat() ? lastEven : lastOdd;

        // store position message for global decoding
        if (cpr.isOddFormat()) lastOdd = cpr;
        else lastEven = cpr;

        // only use receiver as reference for surface positions (might be too far away for airborne)
        Position refPos = lastPosition != null ? lastPosition : (cpr.isSurface() ? receiver : null);

        Position newPosition = cpr.decodePosition(lastOther, refPos);

        if (newPosition == null) return null;

        //////// apply some additional (stateful) reasonableness tests //////////

        // check if it's realistic that the target covered this distance (faster than 1000 knots?)
        if (!disableSpeedTest && lastPosition != null && lastTime != null) {
            double td = Duration.between(lastTime, cpr.getTimestamp()).abs().toMillis() / 1_000.;
            double groundSpeed = newPosition.haversine(lastPosition) / td; // in meters per second

            if (groundSpeed > 514.4)
                newPosition.setReasonable(false);
        }

        lastPosition = newPosition;
        lastTime = cpr.getTimestamp();

        if (!newPosition.isReasonable()) numReasonable = 0; // reset
            // at least 2 good messages before we declare reasonable
        else if (numReasonable++ < 2) newPosition.setReasonable(false);

        // apply additional reasonableness test
        if (receiver != null && receiver.haversine(newPosition) > MAX_DIST_TO_SENDER) {
            newPosition.setReasonable(false);
            numReasonable = 0;
        }

        return newPosition;
    }

}
