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
 * Common API for ADS-B operational status version 1 messages.
 */
public interface OperationalStatusV1Msg extends OperationalStatusMsg {

	/**
	 * @return the subtype code, 0 for airborne operational status messages and 1 for surface operational status messages
	 */
	byte getSubtypeCode();

	/**
	 * @return the NIC supplement A to the format type code of position messages
	 */
	boolean hasNICSupplementA();

	/**
	 * @return the navigation accuracy for position messages; rather use getPositionUncertainty
	 */
	byte getNACp();

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value.
	 *
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	default double getPositionUncertainty() {
		return OperationalStatus.nacPtoEPU(getNACp());
	}

	/**
	 * @return the source integrity level (SIL)
	 */
	byte getSIL();

	/**
	 * @return whether TCAS Resolution Advisory (RA) is active
	 */
	boolean hasTCASResolutionAdvisory();

	/**
	 * @return whether the IDENT switch is active
	 */
	boolean hasActiveIDENTSwitch();

	/**
	 * @return whether ADS-B Transmitting Subsystem is receiving ATC services.
	 */
	boolean hasReceivingATCServices();

	/**
	 * @return 0 if horizontal reference direction is the true north, 1 if magnetic north
	 */
	boolean getHorizontalReferenceDirection();
}
