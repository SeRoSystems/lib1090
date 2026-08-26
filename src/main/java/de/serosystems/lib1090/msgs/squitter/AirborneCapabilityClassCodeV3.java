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
 * Common API for the airborne Capability Class Code of ADS-B version 3. ME 15–18 are redefined here
 * relative to versions 1 and 2, and ME 21–24 are new.
 */
public interface AirborneCapabilityClassCodeV3 extends AirborneCapabilityClassCodeV2V3, CapabilityClassCodeV3 {

    /**
     * The encoded transponder side indication, ED-102B §2.2.3.2.7.2.3.4 TABLE 2-50. New in version 3;
     * the same ME bits carried ARV and TS report capability in versions 1 and 2.
     *
     * @return the encoded transponder side indication, ME bits 15–16
     */
    default byte getTransponderSideIndicationEncoded() {
        return (byte) getMEBits(15, 16);
    }

    /**
     * The encoded transmit power, ED-102B §2.2.3.2.7.2.3.6 TABLE 2-51. New in version 3; the same ME bits
     * carried the TC report capability level in versions 1 and 2.
     *
     * @return the encoded transmit power, ME bits 17–18
     */
    default byte getTxPowerEncoded() {
        return (byte) getMEBits(17, 18);
    }

    /**
     * The encoded Reduced Capability Equipment (RCE) capability, ED-102B §2.2.3.2.7.2.3.11 TABLE 2-53.
     *
     * @return the encoded RCE capability, ME bits 21–22
     */
    default byte getReducedCapabilityEquipmentEncoded() {
        return (byte) getMEBits(21, 22);
    }

    /**
     * The encoded Detect and Avoid (DAA) capability, ED-102B §2.2.3.2.7.2.3.12 TABLE 2-54.
     *
     * @return the encoded DAA capability, ME bits 23–24
     */
    default byte getDetectAndAvoidEncoded() {
        return (byte) getMEBits(23, 24);
    }
}
