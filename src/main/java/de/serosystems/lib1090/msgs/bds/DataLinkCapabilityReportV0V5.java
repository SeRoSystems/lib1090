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

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;

/**
 * A data link capability report (BDS 1,0) of Mode S subnetwork version 0 to 5, ICAO Doc 9871 First and Second Edition
 * §A.2 Table A-2-16, whose MB bits 41-56 are the "Bit array indicating the support status of DTE Sub-addresses 0 to
 * 15". From version 6 on, the array is in register 11₁₆ (ICAO Annex 10 Volume IV (6th edition), Note to Table 3-6).
 *
 * @see DataLinkCapabilityReport#decode(byte[])
 */
@SuppressWarnings("unused")
public class DataLinkCapabilityReportV0V5 extends DataLinkCapabilityReport {
    private static final long serialVersionUID = -1407756223985214863L;

    // Support status of DTE sub-addresses 0 to 15, MB bits 41-56
    private int dteSubAddressSupport;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected DataLinkCapabilityReportV0V5() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @throws BadFormatException if its Mode S subnetwork version number is 6 or higher, see
     *                            {@link DataLinkCapabilityReport#decode(byte[])}
     */
    public DataLinkCapabilityReportV0V5(byte[] message) throws BadFormatException {
        super(message);
        if (getModeSSubNetworkVersionNumber() >= 6)
            throw new BadFormatException(
                    "Subnetwork version " + getModeSSubNetworkVersionNumber() + " has the DO-181F/ED-73F layout");

        dteSubAddressSupport = BitReader.forBigEndian(message).readInt(41, 56);
    }

    /**
     * @return MB bits 41-56 as transmitted: the DTE sub-address array, sub-address 0 in the most significant bit
     */
    public int getDTESubAddressSupportEncoded() {
        return dteSubAddressSupport;
    }

    /**
     * The support status of DTE sub-addresses 0 to 15, MB bits 41-56, ICAO Doc 9871 First and Second Edition §A.2
     * Table A-2-16: "Starting from the MSB, each subsequent bit position shall represent the DTE subaddress in the
     * range from 0 to 15".
     *
     * @return per DTE sub-address, index 0 to 15, whether it is supported
     */
    public boolean[] getDTESubAddressSupport() {
        boolean[] supported = new boolean[16];
        for (int subAddress = 0; subAddress < 16; subAddress++)
            supported[subAddress] = ((dteSubAddressSupport >>> (15 - subAddress)) & 1) == 1;
        return supported;
    }

    @Override
    public String toString() {
        return "DataLinkCapabilityReportV0V5{" + super.toString() +
                ", dteSubAddressSupport=" + dteSubAddressSupport +
                '}';
    }
}
