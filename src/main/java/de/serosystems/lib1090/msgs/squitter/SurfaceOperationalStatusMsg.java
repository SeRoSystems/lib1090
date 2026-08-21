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

import de.serosystems.lib1090.decoding.OperationalStatus;

/**
 * Common API for ADS-B surface operational status messages.
 */
public interface SurfaceOperationalStatusMsg extends OperationalStatusMsg {

    byte SUBTYPE_CODE = 1;

    /**
     * @return the subtype code, 0 for airborne operational status messages
     */
    default byte getSubtypeCode() {
        return SUBTYPE_CODE;
    }

    /**
     * @return whether 1090ES IN / CDTI is available
     */
    @Override
    boolean has1090ESIn();

    /**
     * @return whether transponder has less than 70 Watts transmit power, ED-102B §2.2.3.2.7.2.3.7
     * ("B2 Low" CC Code)
     */
    boolean hasLowTxPower();

    /**
     * @return true if POA bit is 1, ED-102B §2.2.3.2.7.2.4.7
     */
    boolean hasPositionOffsetApplied();

    /**
     * @return whether TCAS Resolution Advisory (RA) is active, ED-102B §2.2.3.2.7.2.4.2
     */
    boolean hasTCASResolutionAdvisory();

    /**
     * @return whether the IDENT switch is active, ED-102B §2.2.3.2.7.2.4.3
     */
    boolean hasActiveIDENTSwitch();

    /**
     * @return whether ADS-B Transmitting Subsystem is receiving ATC services. This bit ("ME" bit
     * 29) is only defined for ADS-B version 1 systems, ED-102B §N.3.3 (Version One
     * systems define "ME" Bit 29 as "Receiving ATC Services"). ADS-B version 3 systems do not
     * define or report this field: the same bit position is redefined in the current main-body
     * message format as "Mode S Reply Rate Limiting Status", ED-102B §2.2.3.2.7.2.4.4.
     */
    boolean hasReceivingATCServices();

    /**
     * @return the NIC supplement A to the format type code of position messages, ED-102B
     * §2.2.3.2.7.2.6
     */
    boolean hasNICSupplementA();

    /**
     * @return the navigation accuracy for position messages; rather use getPositionUncertainty,
     * ED-102B §2.2.3.2.7.2.7 TABLE 2-68
     */
    byte getNACpEncoded();

    /**
     * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value, ED-102B §2.2.3.2.7.2.7 TABLE 2-68.
     *
     * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
     */
    default double getPositionUncertainty() {
        return OperationalStatus.nacPtoEPU(getNACpEncoded());
    }

    /**
     * @return the source integrity level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
    byte getSILEncoded();

    /**
     * @return raw aircraft vehicle length and width code (4 bit)
     */
    byte getAircraftVehicleLengthAndWidthEncoded();

    /**
     * ED-102B §2.2.3.2.7.2.11 TABLE 2-71. Compatible with ADS-B version 1 and 2
     *
     * @return the airplane's length in meters; -1 for unknown
     */
    default int getAirplaneLength() {
        return OperationalStatus.decodeAirplaneLength(getAircraftVehicleLengthAndWidthEncoded());
    }

    /**
     * ED-102B §2.2.3.2.7.2.11 TABLE 2-71. Compatible with ADS-B version 1 and 2.
     *
     * @return the airplane's width in meters
     */
    default double getAirplaneWidth() {
        return OperationalStatus.decodeAirplaneWidth(getAircraftVehicleLengthAndWidthEncoded());
    }

    /**
     * @return the Track Angle/Heading allows correct interpretation of the data
     * contained in the Heading/Ground Track subfield of ADS-B Surface Position Messages,
     * ED-102B §2.2.3.2.7.2.12
     */
    boolean hasTrackHeading();

    /**
     * @return 0 if horizontal reference direction is the true north, 1 if magnetic north,
     * ED-102B §2.2.3.2.7.2.13 TABLE 2-73
     */
    boolean getHorizontalReferenceDirection();
}
