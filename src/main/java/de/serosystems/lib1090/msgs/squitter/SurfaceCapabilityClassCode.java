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
 * Common API for the surface Capability Class Code, ME 9–20, of ADS-B versions 1 to 3.
 */
public interface SurfaceCapabilityClassCode extends KnownCapabilityClassCode {

    /**
     * Whether a Class B2 ground vehicle is transmitting with less than 70 watts — the subfield the
     * standard calls "B2 Low", ED-102B §2.2.3.2.7.2.3.7.
     * <p>
     * Note that the bit is only meaningful for Class B2 ground vehicles, and reports what the
     * transmitter is doing rather than a fixed property of the installation.
     *
     * @return true if a Class B2 ground vehicle transmits below 70 W, ME bit 15
     */
    default boolean hasLowTxPower() {
        return getMEBit(15);
    }
}
