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

/**
 * Common API for ADS-B airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMsg {

    /**
     * @return true if the reported velocity uses the supersonic resolution
     */
    boolean isSupersonic();

    /**
     * @return whether the vertical rate field is available
     */
    boolean hasVerticalRate();

    /**
     * @return the raw encoded vertical rate field
     */
    short getVerticalRateEncoded();

    /**
     * @return true if the vertical rate is negative
     */
    boolean isVerticalRateDown();

    /**
     * @return whether the reported vertical speed is barometric
     */
    boolean isBarometricVerticalSpeed();

    /**
     * @return the vertical rate in feet/min, or {@code null} if unavailable
     */
    default Integer getVerticalRate() {
        if (!hasVerticalRate()) return null;
        int verticalRate = (getVerticalRateEncoded() - 1) * 64;
        return isVerticalRateDown() ? -verticalRate : verticalRate;
    }

    /**
     * @return whether the Difference from Barometric Altitude is available
     */
    boolean hasDiffBaroAlt();

    /**
     * @return the raw encoded Difference from Barometric Altitude field
     */
    short getDiffBaroAltEncoded();

    /**
     * @return true if the Difference from Barometric Altitude is negative
     */
    boolean isDiffBaroAltNegative();

    /**
     * @return the Difference from Barometric Altitude, i.e. geometric minus barometric altitude difference in feet, or {@code null} if unavailable
     */
    default Integer getDiffBaroAlt() {
        if (!hasDiffBaroAlt()) return null;
        int diffBaroAlt = (getDiffBaroAltEncoded() - 1) * 25;
        return isDiffBaroAltNegative() ? -diffBaroAlt : diffBaroAlt;
    }
}
