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
 * Common API for messages that expose the NIC supplement B bit. For ADS-B, this is carried by
 * airborne position messages themselves (version 2 and up), ED-102B §2.2.3.2.3.3; for ADS-R,
 * airborne position messages lose this bit to the IMF flag, so it is instead carried by the
 * airborne operational status message, ED-102B §2.2.18.4.7.
 */
public interface NICSupplementBMsg {

    /**
     * @return NIC supplement B, ED-102B §2.2.3.2.3.3
     */
    boolean hasNICSupplementB();
}
