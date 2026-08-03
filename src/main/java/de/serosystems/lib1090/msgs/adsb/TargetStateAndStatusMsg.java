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

import de.serosystems.lib1090.decoding.OperationalStatus;

/**
 * Common API for ADS-B target state and status messages across supported versions.
 */
public interface TargetStateAndStatusMsg {

    /**
     * @return whether selected altitude is available
     */
    boolean hasSelectedAltitude();

    /**
     * @return the selected altitude in feet, or {@code null} if unavailable
     */
    Integer getSelectedAltitude();

    /**
     * Get encoded selected altitude.
     * <br>
     * Note: the interpretation is different in V1 and V2+.
     *
     * @return the encoded selected altitude field value
     */
    int getSelectedAltitudeEncoded();

    /**
     * @return whether selected heading is available
     */
    boolean hasSelectedHeading();

    /**
     * The selected heading according to DO-260B 2.2.3.2.7.1.3.7
     * <p>
     * Look at {@link SurfaceOperationalStatusV1Msg#getHorizontalReferenceDirection()} resp.
     * {@link AirborneOperationalStatusV1Msg#getHorizontalReferenceDirection()} to determine whether this heading
     * is referring to true north or magnetic north.
     * If not available, assume magnetic north as the de-facto standard.
     *
     * @return the selected heading in decimal degrees ([0, 360]) clockwise, or {@code null} if unavailable
     */
    Float getSelectedHeading();

    /**
     * Get encoded selected heading including sign bit.
     *
     * @return the encoded selected heading (named target heading track in V1) field value including sign bit
     */
    int getSelectedHeadingEncoded();

    /**
     * @return the navigation accuracy category for position
     */
    byte getNACpEncoded();

    /**
     * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value, see table A-13 in RCTA DO-260B
     *
     * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
     */
    default double getPositionUncertainty() {
        return OperationalStatus.nacPtoEPU(getNACpEncoded());
    }

    /**
     * @return the barometric altitude integrity code indicating whether barometric altitude was cross-checked
     */
    boolean getBarometricAltitudeIntegrityCode();

    /**
     * @return the surveillance/source integrity level
     */
    byte getSILEncoded();

    /**
     * @return true if TCAS is operational, false otherwise
     */
    boolean hasOperationalTCAS();
}
