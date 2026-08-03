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

public interface SurfacePositionMsg extends PositionMsg {

    /**
     * @return the message's format type code.
     */
    byte getFormatTypeCode();

    /**
     * Navigation accuracy category.
     * <p>
     * In ADS-B version 0 this is derived from the format type code. For version 1+ aircraft,
     * it is typically provided by the corresponding operational status message.
     *
     * @return navigation accuracy category according to DO-260B
     */
    byte getNACp();

    /**
     * Estimated horizontal accuracy derived from NACp.
     *
     * @return the estimated position uncertainty in meters, or -1 for unknown
     */
    double getPositionUncertainty();

    /**
     * Navigation integrity category. The default implementation assumes no supplements,
     * which may not be accurate for ADS-B version 1 and 2 aircraft that set them.
     *
     * @return navigation integrity category according to DO-260B
     */
    byte getNIC();

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. The default
     * implementation assumes no supplements, which may not be accurate for ADS-B version
     * 1 and 2 aircraft that set them.
     *
     * @return horizontal containment radius limit in meters
     */
    double getHorizontalContainmentRadiusLimit();

    /**
     * Source integrity level.
     *
     * @return the source integrity level
     */
    default byte getSIL() {
        return (byte) (getFormatTypeCode() == 0 ? 0 : 2);
    }

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
