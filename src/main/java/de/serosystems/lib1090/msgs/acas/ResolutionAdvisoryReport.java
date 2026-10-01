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
 * The content of an ACAS RA report, the register BDS 3,0 and the 1090ES RA broadcast: an RA with the identity of the
 * most recently declared threat, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.1 (TCAS layout) and
 * §4.3.8.4.2.2.2 (ACAS X layout).
 */
public interface ResolutionAdvisoryReport extends ResolutionAdvisoryState {

    /**
     * @return the TTI (threat type indicator) as transmitted; its size and codes depend on the layout, see
     * {@link #getThreatIdentityType()} for what they mean
     */
    byte getThreatTypeEncoded();

    /**
     * @return what the threat identity data contains
     */
    default ThreatIdentityType getThreatIdentityType() {
        return ThreatIdentityType.forEncoded(getRAMessageFormat(), getThreatTypeEncoded());
    }

    /**
     * @return the TID (threat identity data) subfield as transmitted; its size depends on the layout
     */
    int getThreatIdentityEncoded();

    /**
     * @return the threat's aircraft address or its altitude, range and bearing, or null if the threat identity data
     * holds neither, see {@link #getThreatIdentityType()}
     */
    ThreatIdentityData getThreatIdentityData();
}
