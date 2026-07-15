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
 * Common API for ADS-B operational status messages.
 */
public interface OperationalStatusMsg {

    /**
     * @return whether 1090ES IN is available
     */
    boolean has1090ESIn();

    /**
     * The version number of the formats and protocols in use on the aircraft installation.
     * <ul>
     *     <li>0: Conformant to DO-260/ED-102 and DO-242</li>
     *     <li>1: Conformant to DO-260A and DO-242A</li>
     *     <li>2: Conformant to DO-260B/ED-102A and DO-242B</li>
     *     <li>3-7: reserved</li>
     * </ul>
     *
     * @return the version number
     */
    byte getVersion();
}
