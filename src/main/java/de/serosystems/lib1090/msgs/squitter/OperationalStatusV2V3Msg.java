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
 * Common API for ADS-B operational status version 2 and 3 messages, for both airborne and surface subtype.
 */
public interface OperationalStatusV2V3Msg extends OperationalStatusMsg, SingleAntennaMsg {

    /**
     * @return the subtype code, 0 for airborne operational status messages and 1 for surface operational status messages
     */
    byte getSubtypeCode();

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
    double getPositionUncertainty();

    /**
     * @return the source integrity level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
    byte getSILEncoded();

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
     * 29) is only defined for ADS-B version 1 systems, ED-102B §N.3.3. ADS-B version 3
     * systems do not define or report this field: the same bit position is redefined in the
     * current main-body message format as "Mode S Reply Rate Limiting Status", ED-102B
     * §2.2.3.2.7.2.4.4.
     */
    boolean hasReceivingATCServices();

    /**
     * @return whether aircraft has an UAT receiver, ED-102B §2.2.3.2.7.2.3.9
     */
    boolean hasUATIn();

    /**
     * ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58
     *
     * @return system design assurance, ED-102B §A.1.4.10.14
     */
    byte getSDAEncoded();

    /**
     * ED-102B §2.2.3.2.7.2.14
     *
     * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
     * it's based on "per hour".
     */
    boolean hasSILSupplement();
}
