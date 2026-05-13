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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.decoding.AirborneVelocity;

/**
 * Common API for ADS-R airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMessage {

	/**
	 * @return the ICAO Mode A Flag used for address type determination
	 */
	boolean getIMF();

	/**
	 * Note: only defined for ADS-R version 0 and 1.
	 * @return true if the aircraft reports IFR capability
	 */
	boolean hasIFRCapability();

	/**
	 * @return the raw encoded Navigation Accuracy Category for velocity
	 */
	byte getNACv();

	/**
	 * @return the interpreted 95% horizontal velocity accuracy in m/s, or -1 if unknown or greater than 10m/s
	 */
	default float getAccuracyBound() {
		return AirborneVelocity.decodeAccuracyBound(getNACv());
	}

	/**
	 * @return whether the vertical rate field is available
	 */
	boolean hasVerticalRateInfo();

	/**
	 * @return whether the reported vertical speed is barometric
	 */
	boolean isBarometricVerticalSpeed();

	/**
	 * @return the vertical rate in feet/min, or {@code null} if unavailable
	 */
	Integer getVerticalRate();

	/**
	 * @return whether the geometric minus barometric altitude difference is available
	 */
	boolean hasGeoMinusBaroInfo();

	/**
	 * @return the geometric minus barometric altitude difference in feet, or {@code null} if unavailable
	 */
	Integer getGeoMinusBaro();
}
