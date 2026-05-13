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
 * Common API for ADS-B target state and status messages across supported versions.
 */
public interface TargetStateAndStatusMsg {

	/**
	 * @return whether selected altitude information is available
	 */
	boolean hasSelectedAltitudeInfo();

	/**
	 * @return the selected altitude in feet, or {@code null} if unavailable
	 */
	Integer getSelectedAltitude();

	/**
	 * Get raw selected altitude.
	 * <br>
	 * Note: the interpretation is different in V1 and V2+.
	 *
	 * @return the raw selected altitude field value
	 */
	int getSelectedAltitudeRaw();

	/**
	 * @return whether selected heading information is available
	 */
	boolean hasSelectedHeadingInfo();

	/**
	  * The selected heading info according to DO-260B 2.2.3.2.7.1.3.7
	  * <p>
	  * Look at {@link SurfaceOperationalStatusV1Msg#getHorizontalReferenceDirection()} resp.
	  * {@link AirborneOperationalStatusV1Msg#getHorizontalReferenceDirection()} to determine whether this heading
	  * is referring to true north or magnetic north.
	  * If information is not available, assume magnetic north as the de-facto standard.
	  *
	  * @return the selected heading in decimal degrees ([0, 360]) clockwise, or {@code null} if unavailable
	 */
	Float getSelectedHeading();

	/**
	 * @return the navigation accuracy category for position
	 */
	byte getNACp();

	/**
	 * @return the barometric altitude integrity code indicating whether barometric altitude was cross-checked
	 */
	boolean getBarometricAltitudeIntegrityCode();

	/**
	 * @return the surveillance/source integrity level
	 */
	byte getSIL();

	/**
	 * @return true if TCAS is operational, false otherwise
	 */
	boolean hasOperationalTCAS();
}
