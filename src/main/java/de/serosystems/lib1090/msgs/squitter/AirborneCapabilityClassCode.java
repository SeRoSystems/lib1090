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
 * Common API for the airborne Capability Class Code, ME 9–24, of every ADS-B version. Version 0 is
 * included: its 4-bit "CC4" field carries this subfield at the same ME bit.
 */
public interface AirborneCapabilityClassCode extends KnownCapabilityClassCode {

    /**
     * Whether the collision avoidance system is operational. The subfield is called "CA Operational"
     * in ADS-B version 3 and "Not-TCAS" in versions 0 and 1, where the bit is transmitted inverted.
     * <p>
     * <b>What {@code true} asserts depends on the version:</b> in versions 2 and 3, operational, since
     * the bit is "CA Operational" or "TCAS Operational" and set only then; in versions 0 and 1, operational
     * <i>or unknown</i>, since a clear "Not-TCAS" bit means that (ED-102B TABLE N-8). {@code false} means
     * not operational in every version. Use the era-accurate accessor on the concrete layout to read the
     * bit as transmitted.
     *
     * <b>Declared here rather than implemented</b>, because the polarity differs: versions 2 and 3
     * transmit the bit as "CA Operational", versions 0 and 1 as its negation. A shared default would
     * silently return inverted values for any layout that forgot to override it.
     *
     * @return true if collision avoidance is operational, in versions 0 and 1 also if its state is unknown,
     * ME bit 11
     */
    boolean isCollisionAvoidanceOperational();
}
