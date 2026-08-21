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

package de.serosystems.lib1090.msgs.adsb;

/**
 * Common API for messages that expose the intent change flag (ICF), "ME" bit 9 of the Airborne
 * Velocity Message (TYPE=19). The "Intent Change Flag" subfield was specified in
 * DO-260B §2.2.3.2.6.1.3 (Subtype=1), §2.2.3.2.6.2.3 (Subtype=2), §2.2.3.2.6.3.3 (Subtype=3) and
 * §2.2.3.2.6.4.3 (Subtype=4); ED-102B removes those subparagraphs ("This section has been
 * removed and is no longer applicable", §2.2.3.2.6.1.3/.1.4) and repurposes "ME" bit 9 as part of
 * the "Extended Difference From Barometric Altitude" subfield (ED-102B §2.2.3.2.6.1.15). The flag
 * remains part of the legacy Version Zero (0) message format for backward compatibility, ED-102B
 * Appendix N, Figure N-4. (ED-102B §2.2.19 "Traffic Uplink Management Message" is a distinct
 * DF=18/CF=4 ground-uplink advisory service and does not cover this flag.)
 */
public interface IntentChangeMsg {

    /**
     * @return true if the aircraft indicates an intent to change altitude or a similar flight status change
     */
    boolean hasChangeIntent();
}
