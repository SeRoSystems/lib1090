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
 * Common API for the airborne Capability Class Code of ADS-B versions 2 and 3, supplying the airborne
 * bit position for {@link CapabilityClassCodeV2V3#hasUATIn()}.
 */
public interface AirborneCapabilityClassCodeV2V3 extends AirborneCapabilityClassCode, CapabilityClassCodeV2V3 {

    /**
     * @return true if UAT IN is reported, ME bit 19 for the airborne subtype
     */
    default boolean hasUATIn() {
        return getMEBit(19);
    }

    /**
     * ADS-B versions 2 and 3 transmit this bit as "CA Operational", so it is read directly. Versions 0
     * and 1 transmit its negation as "Not-TCAS" and invert it in their own layouts.
     *
     * @return true if collision avoidance is operational or its state is unknown, ME bit 11
     */
    @Override
    default boolean isCollisionAvoidanceOperational() {
        return getMEBit(11);
    }
}
