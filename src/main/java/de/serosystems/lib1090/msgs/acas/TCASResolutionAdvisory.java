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
 * An RA in the TCAS layout, as a TCAS II version 7 system transmits it in a coordination reply, ICAO Annex 10 Volume
 * IV (6th edition) §4.3.8.4.2.4.2.1, and as the first part of an RA report, §4.3.8.4.2.2.1.
 * <p>
 * The 14-bit ARA (41-54) has meanings that depend on bit 41 and MTE, §4.3.8.4.2.2.1.1: with bit 41 set, bits 42-47
 * are corrective, downward sense, increased rate, sense reversal, altitude crossing and positive; with bit 41 clear
 * and MTE set, they are the corrections and climb or descend requirements of a multi-threat RA. Bits 48-54 are
 * reserved for ACAS III, which is why the RA message format of this layout reads as TCAS.
 */
public class TCASResolutionAdvisory extends AbstractResolutionAdvisory implements ResolutionAdvisoryState {
    private static final long serialVersionUID = 6590163224823104871L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TCASResolutionAdvisory() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     */
    public TCASResolutionAdvisory(long encoded) {
        super(encoded, 54);
    }

    @Override
    public RAMessageFormat getRAMessageFormat() {
        return RAMessageFormat.TCAS_II;
    }

    /**
     * @return whether the RAC "Do not turn left" is active, bit 57, §4.3.8.4.2.2.1.2
     */
    public boolean isDoNotTurnLeftActive() {
        return getBit(57);
    }

    /**
     * @return whether the RAC "Do not turn right" is active, bit 58, §4.3.8.4.2.2.1.2
     */
    public boolean isDoNotTurnRightActive() {
        return getBit(58);
    }
}
