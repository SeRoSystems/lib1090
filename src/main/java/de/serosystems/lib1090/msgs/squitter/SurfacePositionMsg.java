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

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.decoding.NavigationCharacteristics;

/**
 * Common API for ADS-B surface position messages.
 */
public interface SurfacePositionMsg extends PositionMsg, NavigationCharacteristics {

    @Override
    byte getNIC();

    /**
     * Movement field as encoded in the surface position message.
     *
     * @return encoded movement field
     */
    byte getMovementEncoded();

    /**
     * Heading field as encoded in the surface position message.
     *
     * @return encoded heading field
     */
    byte getHeadingEncoded();

    /**
     * @return whether ground speed is available
     */
    default boolean hasGroundSpeed() {
        byte movement = getMovementEncoded();
        return movement >= 1 && movement <= 124;
    }

    /**
     * @return speed in knots or null if ground speed is not available
     */
    default Double getGroundSpeed() {
        return SurfacePosition.groundSpeed(getMovementEncoded());
    }

    /**
     * @return speed resolution in knots or null if ground speed is not available
     */
    default Double getGroundSpeedResolution() {
        return SurfacePosition.groundSpeedResolution(getMovementEncoded());
    }

    /**
     * @return whether valid heading is available
     */
    boolean hasValidHeading();

    /**
     * @return heading in decimal degrees ([0, 360]), or null if heading is not available
     */
    default Double getHeading() {
        if (!hasValidHeading()) return null;
        return getHeadingEncoded() * 360D / 128D;
    }

    @Override
    default boolean hasValidAltitude() {
        return true;
    }

    @Override
    default Integer getAltitude() {
        return 0;
    }

    @Override
    default Position.AltitudeType getAltitudeType() {
        return Position.AltitudeType.ABOVE_GROUND_LEVEL;
    }
}
