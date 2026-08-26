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
 * Common API for the Capability Class Code of ADS-B version 2 and 3 operational status messages,
 * airborne and surface alike.
 */
public interface CapabilityClassCodeV2V3 extends CapabilityClassCode {

    /**
     * Whether the aircraft has ADS-B UAT receive capability ("UAT IN"), ED-102B §2.2.3.2.7.2.3.9.
     * <p>
     * Declared here rather than implemented, because this is the one subfield whose ME bit differs
     * between subtypes: ME 19 airborne, ME 16 surface. See
     * {@link AirborneCapabilityClassCodeV2V3} and {@link SurfaceCapabilityClassCodeV2V3}.
     *
     * @return true if UAT IN is reported
     */
    boolean hasUATIn();
}
