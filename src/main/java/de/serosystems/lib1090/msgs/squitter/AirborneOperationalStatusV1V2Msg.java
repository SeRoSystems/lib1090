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
 * Common API for ADS-B airborne operational status version 1 and 2 messages.
 */
public interface AirborneOperationalStatusV1V2Msg extends AirborneOperationalStatusMsg {

    /**
     * @return the barometric altitude integrity code which indicates whether barometric altitude was cross-checked
     */
    boolean getBarometricAltitudeIntegrityCode();

    /**
     * @return 0 if horizontal reference direction is the true north, 1 if magnetic north
     */
    boolean getHorizontalReferenceDirection();

    /**
     * @return whether aircraft has capability of sending messages to support Air-Referenced Velocity Reports
     */
    boolean hasAirReferencedVelocity();

    /**
     * @return whether aircraft has capability of sending messages to support Target State Reports
     */
    boolean hasTargetStateReport();

    /**
     * @return whether target change reports are supported
     */
    default boolean supportsTargetChangeReport() {
        byte targetChangeReportCapability = getTargetChangeReportCapabilityEncoded();
        return targetChangeReportCapability == 1 || targetChangeReportCapability == 2;
    }

    /**
     * Get target change report capability.
     * <ul>
     *     <li>0: Not supported</li>
     *     <li>1: Supports TC+0 only</li>
     *     <li>2: Supports multiple TCs</li>
     *     <li>3: Reserved</li>
     * </ul>
     *
     * @return target change report capability
     */
    byte getTargetChangeReportCapabilityEncoded();
}
