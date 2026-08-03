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

package de.serosystems.lib1090.msgs.squitter;

/**
 * Common API for ADS-B velocity-over-ground messages across message versions.
 */
public interface VelocityOverGroundMsg extends AirborneVelocityMsg {

    /**
     * @return whether velocity is available
     */
    default boolean hasVelocity() {
        return getWestToEastVelocityEncoded() != 0 && getSouthToNorthVelocityEncoded() != 0;
    }

    /**
     * @return velocity from west to east in knots or null if not available
     */
    default Integer getWestToEastVelocity() {
        if (!hasVelocity()) return null;
        int velocityToEast = (getWestToEastVelocityEncoded() - 1) * (isSupersonic() ? 4 : 1);
        return isVelocityToEastNegative() ? -velocityToEast : velocityToEast;
    }

    /**
     * @return velocity from south to north in knots or null if not available
     */
    default Integer getSouthToNorthVelocity() {
        if (!hasVelocity()) return null;
        int velocityToNorth = (getSouthToNorthVelocityEncoded() - 1) * (isSupersonic() ? 4 : 1);
        return isVelocityToNorthNegative() ? -velocityToNorth : velocityToNorth;
    }

    /**
     * @return track angle in decimal degrees ([0, 360]) clockwise from geographic north or null if not available.
     * The latter can also be checked with {@link #hasVelocity()}.
     */
    default Double getTrueTrackAngle() {
        if (!hasVelocity()) return null;
        Integer westToEastVelocity = getWestToEastVelocity();
        Integer southToNorthVelocity = getSouthToNorthVelocity();
        double angle = Math.toDegrees(Math.atan2(
                westToEastVelocity,
                southToNorthVelocity));

        // if negative => clockwise
        if (angle < 0) return 360 + angle;
        else return angle;
    }

    /**
     * @return speed over ground in knots or null if not available. The latter can also be checked
     * with {@link #hasVelocity()}.
     */
    default Double getGroundSpeed() {
        if (!hasVelocity()) return null;
        return Math.hypot(getSouthToNorthVelocity(), getWestToEastVelocity());
    }

    /**
     * @return the raw encoded velocity from west to east field
     */
    short getWestToEastVelocityEncoded();

    /**
     * @return the raw encoded velocity from south to north field
     */
    short getSouthToNorthVelocityEncoded();

    /**
     * @return true if the velocity from west to east is negative
     */
    boolean isVelocityToEastNegative();

    /**
     * @return true if the velocity from south to north is negative
     */
    boolean isVelocityToNorthNegative();
}
