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
 * Common API for the Operational Mode Code of ADS-B versions 2 and 3, airborne and surface alike.
 */
public interface OperationalModeCodeV2V3 extends KnownOperationalModeCode {

    /**
     * Whether the transmitting system uses a single antenna, ED-102B §2.2.3.2.7.2.4.5. Unlike most of this
     * field, this is a fixed property of the installation.
     *
     * @return true if a single antenna is used, ME bit 30
     */
    default boolean hasSingleAntenna() {
        return getMEBit(30);
    }

    /**
     * The encoded System Design Assurance (SDA), ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58.
     *
     * @return the encoded SDA, ME bits 31–32
     */
    default byte getSDAEncoded() {
        return (byte) getMEBits(31, 32);
    }
}
