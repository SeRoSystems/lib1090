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
 * The RA report of a TCAS II version 6.04 system (FAA TSO-C119A), RTCA DO-185B §2.2.3.9.3.2.3.1.2: a 14-bit ARA
 * (41-54) of dedicated condition bits and the RAC (55-58); bits 59-88 are not assigned.
 * <p>
 * The ARA bits, which are set while the condition is active, are 41 climb, 42 don't descend, 43 don't descend faster
 * than 500 ft/min, 44 don't descend faster than 1000 ft/min, 45 don't descend faster than 2000 ft/min, 46 descend,
 * 47 don't climb, 48 don't climb faster than 500 ft/min, 49 don't climb faster than 1000 ft/min, 50 don't climb faster
 * than 2000 ft/min, 51 turn left, 52 turn right, 53 don't turn left and 54 don't turn right. Systems without
 * horizontal resolution capability set bits 51-54 to zero, which is also why the RA message format reads as TCAS.
 * <p>
 * Nothing in the message tells this layout from the TCAS version 7 one; {@link ResolutionAdvisories#report(long)}
 * assumes it for a report in the TCAS layout whose bits 59-88 are all zero.
 */
public class TCAS6ResolutionAdvisory extends AbstractResolutionAdvisory {
    private static final long serialVersionUID = -3180392170566416224L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TCAS6ResolutionAdvisory() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     */
    public TCAS6ResolutionAdvisory(long encoded) {
        super(encoded, 54);
    }

    @Override
    public RAMessageFormat getRAMessageFormat() {
        return RAMessageFormat.TCAS_II;
    }

    /**
     * @return whether the RAC "Do not turn left" is active, bit 57
     */
    public boolean isDoNotTurnLeftActive() {
        return getBit(57);
    }

    /**
     * @return whether the RAC "Do not turn right" is active, bit 58
     */
    public boolean isDoNotTurnRightActive() {
        return getBit(58);
    }
}
