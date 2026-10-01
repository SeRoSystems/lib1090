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
 * The collision avoidance system a data link capability report (BDS 1,0) announces in its bits 43-46 (MB bits 11-14),
 * ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3.
 */
public enum ACASType {

    /**
     * 0000: "TCAS Version 7.1-compliant and other systems defined by bits 71 and 72", see
     * {@link de.serosystems.lib1090.msgs.bds.DataLinkCapabilityReport#getACASVersionNumber()}.
     */
    TCAS_OR_OTHER,
    /**
     * 0001: "ACAS Xa (RTCA/DO-385 and EUROCAE/ED-256)".
     */
    ACAS_XA,
    /**
     * 0010 to 1111: "Reserved for ACAS III".
     */
    ACAS_III;

    /**
     * @param encoded the 4-bit field
     * @return the collision avoidance system it stands for
     * @throws IllegalArgumentException if the value is not 0 to 15
     */
    public static ACASType forEncoded(int encoded) {
        if (encoded < 0 || encoded > 15)
            throw new IllegalArgumentException("The ACAS type is a 4-bit field, got " + encoded);
        switch (encoded) {
            case 0:
                return TCAS_OR_OTHER;
            case 1:
                return ACAS_XA;
            default:
                return ACAS_III;
        }
    }
}
