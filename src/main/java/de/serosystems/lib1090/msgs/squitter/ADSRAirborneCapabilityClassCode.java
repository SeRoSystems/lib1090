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
 * Common API for the airborne Capability Class Code as rebroadcast by ADS-R, versions 1 to 3.
 * <p>
 * This is the only protocol-specific interface in the Capability Class hierarchy: ADS-R defines ME bit
 * 20 as NIC supplement B, where ADS-B leaves it reserved. Everything else is shared.
 */
public interface ADSRAirborneCapabilityClassCode extends AirborneCapabilityClassCode {

    /**
     * NIC supplement B, which qualifies the Navigation Integrity Category of airborne position messages.
     *
     * @return the NIC supplement B bit, ME bit 20
     */
    default boolean hasNICSupplementB() {
        return getMEBit(20);
    }
}
