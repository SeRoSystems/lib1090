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

package de.serosystems.lib1090.msgs.acas;

/**
 * What the threat identity data (TID) of an ACAS resolution advisory report or broadcast contains, independent of
 * the layout. The threat type indicator (TTI) that says so has different codes in the two layouts: 2 bits in the
 * TCAS layout, ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.5 (0 no identity data, 1 address, 2 altitude, range and
 * bearing, 3 not assigned), 1 bit in the ACAS X layout, §4.3.8.4.2.2.2.8 (0 altitude, range and bearing, 1 address).
 */
public enum ThreatIdentityType {

    /**
     * TID contains no identity data.
     */
    NONE,
    /**
     * TID contains the 24-bit aircraft address of the threat.
     */
    ADDRESS,
    /**
     * TID contains the altitude, range and bearing of a threat that is not Mode S-equipped.
     */
    ALTITUDE_RANGE_BEARING,
    /**
     * The TTI code is not assigned, or the message format defines no layout for TID.
     */
    NOT_ASSIGNED;

    /**
     * @param format the RA message format
     * @param tti    the encoded threat type indicator of that format
     * @return what TID contains
     */
    public static ThreatIdentityType forEncoded(RAMessageFormat format, int tti) {
        switch (format) {
            case TCAS_II:
                switch (tti) {
                    case 0:
                        return NONE;
                    case 1:
                        return ADDRESS;
                    case 2:
                        return ALTITUDE_RANGE_BEARING;
                    default:
                        return NOT_ASSIGNED;
                }
            case ACAS_X:
                return tti == 1 ? ADDRESS : ALTITUDE_RANGE_BEARING;
            default:
                return NOT_ASSIGNED;
        }
    }
}
