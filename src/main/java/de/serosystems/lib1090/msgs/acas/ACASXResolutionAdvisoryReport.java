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
 * An RA report in the ACAS X layout, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.2: the RA, the continuation
 * bit (61), a 1-bit TTI (62), a 24-bit TID (63-86), which holds the threat's aircraft address or its binary altitude,
 * range and bearing, and the designation and suppression indicators (87, 88).
 */
public class ACASXResolutionAdvisoryReport extends ACASXResolutionAdvisory implements ResolutionAdvisoryReport {
    private static final long serialVersionUID = 5327812095611684150L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ACASXResolutionAdvisoryReport() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     */
    public ACASXResolutionAdvisoryReport(long encoded) {
        super(encoded);
    }

    /**
     * @return the CNT (continuation bit), bit 61, §4.3.8.4.2.2.2.7: whether a follow-on RA report exists with
     * additional information
     */
    public boolean hasFollowOnMessage() {
        return getBit(61);
    }

    /**
     * @return the 1-bit TTI, bit 62, §4.3.8.4.2.2.2.8: 0 altitude, range and bearing, 1 aircraft address
     */
    @Override
    public byte getThreatTypeEncoded() {
        return (byte) getBits(62, 62);
    }

    /**
     * @return the 24-bit TID, bits 63-86, §4.3.8.4.2.2.2.9
     */
    @Override
    public int getThreatIdentityEncoded() {
        return getBits(63, 86);
    }

    @Override
    public ThreatIdentityData getThreatIdentityData() {
        if (getThreatIdentityType() == ThreatIdentityType.ADDRESS)
            return new ThreatIdentityData(getBits(63, 86));
        return ThreatIdentityData.withBinaryAltitude(
                (short) getBits(63, 73), // TIDA, binary
                (short) getBits(74, 80), // TIDR
                (short) getBits(81, 86)); // TIDB
    }

    /**
     * @return the DSI (designation indicator), bit 87, §4.3.8.4.2.2.2.10: whether the threat in TID is designated for
     * ACAS Xo and the designation is applied
     */
    public boolean isThreatDesignated() {
        return getBit(87);
    }

    /**
     * The SPI (suppression indicator), bit 88, §4.3.8.4.2.2.2.11. In a single-threat encounter, it tells whether the RA
     * is suppressed (not announced to the flight crew); in a multi-threat encounter (see
     * {@link #isMultipleThreatEncounter()}), whether another threat than the one in TID is designated for ACAS Xo and
     * the designation is applied.
     *
     * @return the suppression indicator
     */
    public boolean getSuppressionIndicator() {
        return getBit(88);
    }
}
