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
 * Common API for ADS-B airspeed and heading messages across message versions. These are the
 * Airborne Velocity Message subtypes 3 (subsonic) and 4 (supersonic), which were removed from
 * the ADS-B message set with ED-102B: §2.2.3.2.6.3 and §2.2.3.2.6.4 are explicit
 * "Reserved Section...removed and no longer applicable" placeholders, and §2.2.3.2.6.5 states
 * that ADS-B Airborne Velocity Messages "are not specified for Subtypes 3, 4, 5, 6 or 7", with a
 * NOTE right after the Subtype=2 subfields adding that Subtypes "3" and "4" "should not be
 * assigned in future versions until versions 2 and earlier are no longer supported". The governing
 * former-standard reference is ED-102A §2.2.3.2.6.3 (subtype=3, subsonic) resp. ED-102A
 * §2.2.3.2.6.4 (subtype=4, supersonic).
 */
public interface AirspeedHeadingMsg extends AirborneVelocityMsg {

    /**
     * This must be checked before retrieving heading.
     *
     * @return the flag indicates whether heading is available or not, ED-102A §2.2.3.2.6.3.6
     * (subsonic) resp. §2.2.3.2.6.4.6 (supersonic)
     */
    boolean hasHeadingStatusFlag();

    /**
     * @return whether airspeed is available
     */
    default boolean hasAirspeed() {
        return getAirspeedEncoded() != 0;
    }

    /**
     * @return airspeed in knots or null if not available. The latter can also be checked using
     * {@link #hasAirspeed()}.
     */
    default Integer getAirspeed() {
        if (!hasAirspeed()) return null;
        int scale = isSupersonic() ? 4 : 1;
        return (getAirspeedEncoded() - 1) * scale;
    }

    /**
     * @return the raw encoded airspeed field, ED-102A §2.2.3.2.6.3.9 (subsonic) resp. §2.2.3.2.6.4.9
     * (supersonic)
     */
    short getAirspeedEncoded();

    /**
     * @return raw heading field value (10 bit). Check {@link #hasHeadingStatusFlag()} to determine
     * whether this value is valid. ED-102A §2.2.3.2.6.3.7 (subsonic) resp. §2.2.3.2.6.4.7
     * (supersonic)
     */
    short getHeadingEncoded();

    /**
     * @return heading in decimal degrees ([0, 360]). 0° = geographic north or null if not available.
     * The latter can also be checked using {@link #hasHeadingStatusFlag()}.
     */
    default Double getHeading() {
        if (!hasHeadingStatusFlag()) return null;
        return getHeadingEncoded() * 360. / 1024.;
    }

    /**
     * @return true if airspeed is true airspeed, false if airspeed is indicated airspeed,
     * ED-102A §2.2.3.2.6.3.8 (subsonic) resp. §2.2.3.2.6.4.8 (supersonic)
     */
    boolean isTrueAirspeed();
}
