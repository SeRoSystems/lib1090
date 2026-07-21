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
 * Common API for ADS-B airspeed and heading messages across message versions.
 */
public interface AirspeedHeadingMsg extends AirborneVelocityMsg {

    /**
     * This must be checked before retrieving heading information.
     *
     * @return the flag indicates whether heading information is available or not
     */
    boolean hasHeadingStatusFlag();

    /**
     * Must be checked before accessing airspeed!
     *
     * @return whether airspeed info is available
     */
    boolean hasAirspeedInfo();

    /**
     * @return airspeed in knots or null if information is not available. The latter can also be checked using
     * {@link #hasAirspeedInfo()}.
     */
    default Integer getAirspeed() {
        if (!hasAirspeedInfo()) return null;
        int scale = isSupersonic() ? 4 : 1;
        return (getAirspeedEncoded() - 1) * scale;
    }

    /**
     * @return the raw encoded airspeed field
     */
    short getAirspeedEncoded();

    /**
     * @return raw heading field value (10 bit). Check {@link #hasHeadingStatusFlag()} to determine whether this value
     *         is valid.
     */
    short getHeadingEncoded();

    /**
     * @return heading in decimal degrees ([0, 360]). 0° = geographic north or null if no information is available.
     * The latter can also be checked using {@link #hasHeadingStatusFlag()}.
     */
    default Double getHeading() {
        if (!hasHeadingStatusFlag()) return null;
        return getHeadingEncoded() * 360. / 1024.;
    }

    /**
     * @return true if airspeed is true airspeed, false if airspeed is indicated airspeed
     */
    boolean isTrueAirspeed();
}
