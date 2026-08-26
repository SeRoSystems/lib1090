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
 * Common API for the surface Capability Class Code of ADS-B versions 2 and 3.
 */
public interface SurfaceCapabilityClassCodeV2V3 extends SurfaceCapabilityClassCode, CapabilityClassCodeV2V3 {

    /**
     * @return true if UAT IN is reported, ME bit 16 for the surface subtype
     */
    default boolean hasUATIn() {
        return getMEBit(16);
    }

    /**
     * The encoded Navigation Accuracy Category for velocity (NACv).
     *
     * @return the NACv value, ME bits 17–19
     */
    default byte getNACv() {
        return (byte) getMEBits(17, 19);
    }

    /**
     * NIC supplement C, which qualifies the Navigation Integrity Category of surface position messages.
     *
     * @return the NIC supplement C bit, ME bit 20
     */
    default boolean hasNICSupplementC() {
        return getMEBit(20);
    }
}
