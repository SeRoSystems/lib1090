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
 * Common API for the Capability Class Code of ADS-B version 3 operational status messages, airborne
 * and surface alike.
 */
public interface CapabilityClassCodeV3 extends CapabilityClassCodeV2V3 {

    /**
     * The highest ADS-B version the receiving subsystem can use for advisory applications, ED-102B
     * §2.2.3.2.7.2.3.13. New in version 3; the same ME bits are reserved in versions 1 and 2.
     *
     * @return the encoded ADS-B receiver version, ME bits 13–14
     */
    default byte getADSBReceiverVersionEncoded() {
        return (byte) getMEBits(13, 14);
    }
}
