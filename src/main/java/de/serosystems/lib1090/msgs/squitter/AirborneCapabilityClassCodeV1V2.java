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
 * Common API for the airborne Capability Class Code of ADS-B versions 1 and 2. ADS-B version 3
 * redefines ME 15–18, so none of these subfields survive into it.
 */
public interface AirborneCapabilityClassCodeV1V2 extends AirborneCapabilityClassCode {

    /**
     * Whether the aircraft can send messages supporting Air-Referenced Velocity (ARV) reports — "ARV Report
     * Capability".
     *
     * @return true if ARV reports are supported, ME bit 15
     */
    default boolean supportsARVReport() {
        return getMEBit(15);
    }

    /**
     * Whether the aircraft can send messages supporting Target State (TS) reports — "TS Report
     * Capability".
     *
     * @return true if TS reports are supported, ME bit 16
     */
    default boolean supportsTSReport() {
        return getMEBit(16);
    }

    /**
     * The encoded "TC Report Capability Level".
     * <ul>
     *     <li>0: not supported</li>
     *     <li>1: supports TC+0 only</li>
     *     <li>2: supports multiple TCs</li>
     *     <li>3: reserved</li>
     * </ul>
     *
     * @return the TC report capability level, ME bits 17–18
     */
    default byte getTCReportCapabilityLevelEncoded() {
        return (byte) getMEBits(17, 18);
    }

    /**
     * @return whether target change reports are supported at all
     */
    default boolean supportsTargetChangeReport() {
        byte level = getTCReportCapabilityLevelEncoded();
        return level == 1 || level == 2;
    }
}
