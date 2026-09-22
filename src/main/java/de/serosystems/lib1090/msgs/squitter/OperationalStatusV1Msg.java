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
 * Common API for ADS-B operational status version 1 messages. The Version One (1) Aircraft
 * Operational Status Message format is documented in ED-102B §N.5.2 Figure N-15.
 */
public interface OperationalStatusV1Msg extends OperationalStatusMsg, SILMsg, NACpMsg {

    /**
     * @return the subtype code, 0 for airborne operational status messages and 1 for surface operational status messages
     */
    byte getSubtypeCode();

    /**
     * @return the NIC supplement A to the format type code of position messages, ED-102B
     * §2.2.3.2.7.2.6
     */
    boolean getNICSupplementA();

    /**
     * @return true if the horizontal reference direction is magnetic north, false if true north,
     * ED-102B §2.2.3.2.7.2.13 TABLE 2-73
     */
    boolean isHeadingReferencedToMagneticNorth();

    /**
     * Version 1 transmits no SIL supplement. It does not leave the basis open either: the probability
     * is per flight hour, which the later versions express by clearing the bit they added.
     *
     * @return false, version 1 having fixed the basis to "per flight hour"
     */
    @Override
    default boolean getSILSupplement() {
        return false;
    }
}
