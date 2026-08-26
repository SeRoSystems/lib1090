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
 * Common API for ADS-B airborne operational status messages.
 */
public interface AirborneOperationalStatusMsg extends OperationalStatusMsg {

    byte SUBTYPE_CODE = 0;

    /**
     * @return the subtype code, 0 for airborne operational status messages
     */
    default byte getSubtypeCode() {
        return SUBTYPE_CODE;
    }

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
}
