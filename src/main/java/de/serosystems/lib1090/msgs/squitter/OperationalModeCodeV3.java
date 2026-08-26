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
 * Common API for the Operational Mode Code of ADS-B version 3, all subtypes and both formats.
 */
public interface OperationalModeCodeV3 extends OperationalModeCodeV2V3 {

    /**
     * Whether the transponder has activated its Mode S reply rate limiting mechanism, ED-102B
     * §2.2.3.2.7.2.4.4. New in version 3, replacing the "Receiving ATC Services" indication that
     * versions 1 and 2 carried at the same bit.
     *
     * @return true if Mode S reply rate limiting is active, ME bit 29
     */
    default boolean isModeSReplyRateLimitingActive() {
        return getMEBit(29);
    }
}
