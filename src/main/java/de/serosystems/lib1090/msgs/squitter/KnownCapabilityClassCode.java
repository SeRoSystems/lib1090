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
 * A Capability Class (CC) Code whose format selector selects a layout this library models.
 * <p>
 * {@code instanceof KnownCapabilityClassCode} is the test for "was this field decodable?", the
 * counterpart of the {@code opstatus.UnknownCapabilityClassCode} a selector outside the standard
 * yields. It carries the one subfield every defined layout has, so a caller who only wants that need
 * not know which subtype or version produced the message.
 */
public interface KnownCapabilityClassCode extends CapabilityClassCode {

    /**
     * Whether the aircraft has ADS-B 1090ES (1090 MHz Extended Squitter) receive capability — "1090ES IN"
     * from ADS-B version 1
     * onwards, called "CDTI Traffic Display" in version 0.
     *
     * @return true if 1090ES IN is reported, ME bit 12
     */
    default boolean has1090ESIn() {
        return getMEBit(12);
    }
}
