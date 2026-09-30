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
 * Common API for ADS-B target state and status messages across supported versions.
 */
public interface TargetStateAndStatusMsg extends SILMsg, NACpMsg {

    /**
     * @return whether selected altitude is available, ED-102B §2.2.3.2.7.1.3.2
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
     * @return the encoded selected altitude field value, ED-102B §2.2.3.2.7.1.3.3
     */
    int getSelectedAltitudeEncoded();

    /**
     * @return whether selected heading is available, ED-102B §2.2.3.2.7.1.3.5
     */
    boolean hasSelectedHeading();

    /**
     * The selected heading according to ED-102B §2.2.3.2.7.1.3.7.
     * <p>
     * The message does not indicate whether it refers to true or magnetic north: "Users of the Selected Heading
     * data should be aware that there is no method defined in this version of these MOPS to indicate its
     * reference orientation" (ED-102A and ED-102B §2.2.3.2.7.1.3.7 NOTE 2). Transmitters are encouraged to use
     * magnetic north, the de facto standard, but encode the value active in the flight deck, in either
     * orientation.
     * <p>
     * ED-129C §3.4.4.6.40 nevertheless reports it with the reference of the aircraft's own heading: for versions
     * 1 and 2 the HRD of the airborne operational status message
     * ({@link AirborneOperationalStatusV1V2Msg#isHeadingReferencedToMagneticNorth()}, [REQ 317]), for version 3
     * the heading type of the Wx AIREP alternate weather state message
     * ({@link de.serosystems.lib1090.msgs.adsb.WxAIREPAlternateWeatherStateMsg#getHeadingType()}, [REQ 643]).
     *
     * @return the selected heading in decimal degrees ([0, 360)) clockwise, or {@code null} if unavailable
     */
    Float getSelectedHeading();

    /**
     * Get encoded selected heading including sign bit.
     *
     * @return the encoded selected heading (named target heading track in V1) field value
     * including sign bit, ED-102B §2.2.3.2.7.1.3.6 (sign) and §2.2.3.2.7.1.3.7 (magnitude)
     */
    int getSelectedHeadingEncoded();

    /**
     * @return the raw encoded navigation accuracy category for position, ED-102B §2.2.3.2.7.1.3.8
     */
    @Override
    byte getNACpEncoded();

    /**
     * @return the barometric altitude integrity code (NIC_BARO) indicating whether barometric
     * altitude was cross-checked. This subfield is no longer specified in ED-102B §2.2.3.2.7.1.3.9,
     * which used to define it, is now a "Reserved Section...removed and no longer applicable".
     * The governing former-standard reference is ED-102A §2.2.3.2.7.1.3.9.
     */
    boolean getBarometricAltitudeIntegrityCode();

    /**
     * @return the raw encoded surveillance/source integrity level, ED-102B §2.2.3.2.7.1.3.10
     */
    @Override
    byte getSILEncoded();

    /**
     * Whether TCAS/ACAS, from version 3 on any collision avoidance system, is operational. <b>What
     * {@code true} asserts depends on the version:</b> in versions 2 and 3, operational, since ME bit 53 is
     * set only then (ED-102A Table 2-53, ED-102B §2.2.3.2.7.1.3.17 TABLE 2-44); in version 1, operational
     * <i>or unknown</i>, since the "Capability/Mode Codes" subfield transmits ME bit 52 inverted and a
     * clear bit means that (ICAO Doc 9871 First Edition §D.2.15). {@code false} means not operational in
     * every version.
     *
     * @return true if TCAS is operational, in version 1 also if its state is unknown
     */
    boolean hasOperationalTCAS();
}
