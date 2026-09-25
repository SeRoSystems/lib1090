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

import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAlt;
import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAltV0V2;

/**
 * Common API for ADS-B airborne velocity messages across message subtypes, ED-102B §2.2.3.2.6:
 * subtype=1, subsonic, ED-102B §2.2.3.2.6.1; subtype=2, supersonic, ED-102B §2.2.3.2.6.2.
 */
public interface AirborneVelocityMsg {

    /**
     * @return true if the reported velocity uses the supersonic resolution
     */
    boolean isSupersonic();

    /**
     * @return whether the vertical rate field is available
     */
    default boolean hasVerticalRate() {
        return getVerticalRateEncoded() != 0;
    }

    /**
     * @return the raw encoded vertical rate field, ED-102B §2.2.3.2.6.1.12 (subsonic) resp.
     * §2.2.3.2.6.2.12 (supersonic)
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
    default boolean hasDiffBaroAlt() {
        return getDiffBaroAlt().hasDifference();
    }

    /**
     * @return the raw encoded Difference from Barometric Altitude field, ME bits 50-56, ED-102B
     * §2.2.3.2.6.1.15
     */
    short getDiffBaroAltEncoded();

    /**
     * @return true if the Difference from Barometric Altitude is negative
     */
    boolean isDiffBaroAltNegative();

    /**
     * The Difference from Barometric Altitude, geometric minus barometric altitude, as the transmitting
     * version's coding defines it. This default reads the 7-bit field every version transmits, which
     * version 3 keeps compatible; the version 3 messages refine it with their extended coding.
     *
     * @return the difference, never null
     */
    default DiffBaroAlt getDiffBaroAlt() {
        return DiffBaroAltV0V2.of(isDiffBaroAltNegative(), getDiffBaroAltEncoded());
    }
}
