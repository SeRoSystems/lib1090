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
 * An RA report in the TCAS layout, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.1: the RA, a 2-bit TTI (61-62)
 * and a 26-bit TID (63-88), which holds the threat's aircraft address or its Mode C altitude, range and bearing.
 */
public class TCASResolutionAdvisoryReport extends TCASResolutionAdvisory implements ResolutionAdvisoryReport {
    private static final long serialVersionUID = -1719734567106286455L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TCASResolutionAdvisoryReport() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     */
    public TCASResolutionAdvisoryReport(long encoded) {
        super(encoded);
    }

    /**
     * @return the 2-bit TTI, bits 61-62, §4.3.8.4.2.2.1.5: 0 no identity data, 1 aircraft address, 2 altitude, range
     * and bearing, 3 not assigned
     */
    @Override
    public byte getThreatTypeEncoded() {
        return (byte) getBits(61, 62);
    }

    /**
     * @return the 26-bit TID, bits 63-88, §4.3.8.4.2.2.1.6; with an address, the address is in bits 63-86 and bits 87
     * and 88 are zero
     */
    @Override
    public int getThreatIdentityEncoded() {
        return getBits(63, 88);
    }

    @Override
    public ThreatIdentityData getThreatIdentityData() {
        switch (getThreatIdentityType()) {
            case ADDRESS:
                return new ThreatIdentityData(getBits(63, 86));
            case ALTITUDE_RANGE_BEARING:
                return new ThreatIdentityData(
                        (short) getBits(63, 75), // TIDA, Mode C code
                        (short) getBits(76, 82), // TIDR
                        (short) getBits(83, 88)); // TIDB
            default:
                return null;
        }
    }
}
