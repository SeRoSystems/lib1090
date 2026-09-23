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
 * Common API for the Operational Mode Code of ADS-B versions 1 and 2. ADS-B version 3 redefines
 * ME bit 29, so this subfield does not survive into it.
 */
public interface OperationalModeCodeV1V2 extends KnownOperationalModeCode {

    /**
     * Whether the ADS-B transmitting subsystem is receiving ATC (Air Traffic Control) services,
     * ED-102B §N.3.3. Version 3
     * redefines the same bit as Mode S reply rate limiting status, ED-102B §2.2.3.2.7.2.4.4.
     *
     * @return true if ATC services are being received, ME bit 29
     */
    default boolean isReceivingATCServices() {
        return getMEBit(29);
    }

    /**
     * The subfield under the name the earlier standards give it: a <b>TCAS/ACAS resolution advisory</b>,
     * ED-102B §2.2.3.2.7.2.4.2. Version 3 generalizes the same bit to "CA RA Active", which is why
     * {@link #isCollisionAvoidanceResolutionAdvisoryActive()} is the unified name.
     * <p>
     * Declared on this interface rather than on each layout class, which the era-accessor rule otherwise
     * calls for, because this interface's layout-set is exactly the set of layouts using the older term:
     * versions 1 and 2, all subtypes and both protocols. There is therefore no version it can leak to.
     *
     * @return true if a TCAS/ACAS resolution advisory is active, ME bit 27
     */
    default boolean isTCASResolutionAdvisoryActive() {
        return getMEBit(27);
    }

    /**
     * {@inheritDoc}
     *
     * @see #isTCASResolutionAdvisoryActive() the name versions 1 and 2 use for this subfield
     */
    @Override
    default boolean isCollisionAvoidanceResolutionAdvisoryActive() {
        return isTCASResolutionAdvisoryActive();
    }
}
