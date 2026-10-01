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
 * An ACAS resolution advisory with the RA terminated indicator and the multiple threat encounter indicator: the
 * content of a coordination reply, and the part of an RA report before the threat identity, in the TCAS layout,
 * ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.1 and §4.3.8.4.2.4.2.1, and in the ACAS X layout,
 * §4.3.8.4.2.2.2 and §4.3.8.4.2.4.2.2. TCAS II version 6.04 systems transmit neither indicator.
 */
public interface ResolutionAdvisoryState extends ResolutionAdvisory {

    /**
     * RAT (RA terminated indicator), bit 59, §4.3.8.4.2.2.1.3 and §4.3.8.4.2.2.2.5. A terminated RA is still
     * reported for 18±1 s (§4.3.11.4.1).
     *
     * @return whether the RA indicated by ARA has been terminated, false if ACAS is currently generating it
     */
    default boolean isTerminated() {
        return getBit(59);
    }

    /**
     * MTE (multiple threat encounter), bit 60, §4.3.8.4.2.2.1.4 and §4.3.8.4.2.2.2.6.
     *
     * @return whether two or more simultaneous threats are being processed by the ACAS threat resolution logic
     */
    default boolean isMultipleThreatEncounter() {
        return getBit(60);
    }
}
