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
 * An RA in the ACAS X layout, as an ACAS X-compliant system transmits it in a coordination reply, ICAO Annex 10
 * Volume IV (6th edition) §4.3.8.4.2.4.2.2, and as the first part of an RA report, §4.3.8.4.2.2.2: a 10-bit ARA
 * (41-50), the low-level descend inhibit (51-52) and the RA message format (53-54, value 1) before RAC, RAT and MTE.
 * <p>
 * The RAC bits 57 and 58 are reserved for horizontal coordination in this layout, §4.3.8.4.2.2.2.4, rather than the
 * "Do not turn" complements of the TCAS layout.
 */
public class ACASXResolutionAdvisory extends AbstractResolutionAdvisory implements ResolutionAdvisoryState {
    private static final long serialVersionUID = 2873391024462713097L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ACASXResolutionAdvisory() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     */
    public ACASXResolutionAdvisory(long encoded) {
        super(encoded, 50);
    }

    @Override
    public RAMessageFormat getRAMessageFormat() {
        return RAMessageFormat.ACAS_X;
    }

    /**
     * ARA bit 41, §4.3.8.4.2.2.2.1.
     *
     * @return true if the same vertical sense has been generated in a single or multi-threat encounter; false if
     * different vertical senses have been generated in a multi-threat encounter (MTE set) or no RA has been
     * generated (MTE clear)
     */
    public boolean isSameVerticalSense() {
        return getBit(41);
    }

    /**
     * @return whether the RA is crossing, ARA bit 42, i.e. own aircraft is expected to cross the altitude of the
     * intruder before closest approach
     */
    public boolean isCrossing() {
        return getBit(42);
    }

    /**
     * @return true for a downward sense RA (own aircraft intends to pass below the threat), false for an upward sense
     * one, ARA bit 43
     */
    public boolean isDownwardSense() {
        return getBit(43);
    }

    /**
     * The strength of the vertical RA, ARA bits 44-47, §4.3.8.4.2.2.2.1: 0 clear of conflict, 1 monitor vertical speed,
     * 2 level-off; weakening of positive RA, 3 level-off; corrective when climbing/descending, 4 climb/descend at
     * 1 500 ft/min, 5 reversal to climb/descend, 6 increase climb/descend, 7 maintain rate; at current rate
     * &gt; 1 500 ft/min, 8 reversal to maintain, 9 level-off; reversal to corrective negative RA, 10 monitor vertical
     * speed; following descend RA, descend inhibited, 11 monitor vertical speed; reversal to preventive negative RA,
     * 12 and 13 unallocated, 14 preventive multi-threat level off (MTLO) while level, 15 corrective MTLO while
     * climbing/descending.
     *
     * @return the strength code
     */
    public byte getVerticalRAStrengthEncoded() {
        return (byte) getBits(44, 47);
    }

    /**
     * @return the AHRA (horizontal RA), ARA bits 48-50, which ACAS X-compliant systems set to 0
     */
    public byte getHorizontalRAEncoded() {
        return (byte) getBits(48, 50);
    }

    /**
     * @return the LDI (low-level descend inhibit), bits 51-52, §4.3.8.4.2.2.2.2: 0 no descend inhibit, 1 increase
     * descend RAs inhibited, 2 increase descend RAs and descend RAs inhibited, 3 all RAs inhibited
     */
    public byte getLowLevelDescendInhibitEncoded() {
        return (byte) getBits(51, 52);
    }
}
