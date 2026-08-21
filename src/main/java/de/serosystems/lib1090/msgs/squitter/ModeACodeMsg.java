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

import de.serosystems.lib1090.decoding.Identity;

/**
 * Common API for ADS-B messages that expose a Mode A code. The standards-defined home for this
 * field is the Extended Squitter Aircraft Status Message, TYPE=28 Subtype=1 (Emergency/Priority
 * Status Message), ED-102B §2.2.3.2.7.8.1 Figure 2-20; the "Mode A Code" subfield itself is
 * specified in §2.2.3.2.7.8.1.2. (ED-102B §2.2.19 "Traffic Uplink Management Message" is a
 * distinct DF=18/CF=4 ground-uplink advisory service and does not cover this field; it is not
 * TYPE=28 in any subtype.)
 */
public interface ModeACodeMsg {

    /**
     * @return the four-digit Mode A (4096) code, ED-102B §2.2.3.2.7.8.1.2
     */
    short getModeACode();

    /**
     * @return decoded Mode A code as four digits
     */
    default String getIdentity() {
        return Identity.decodeIdentity(getModeACode());
    }
}
