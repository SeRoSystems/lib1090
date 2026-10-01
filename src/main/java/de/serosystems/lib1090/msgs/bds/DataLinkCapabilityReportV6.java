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
 * A data link capability report (BDS 1,0) of Mode S subnetwork version 6 or higher, the version of RTCA DO-181F and
 * EUROCAE ED-73F, whose MB bits 41-56 hold, as ICAO Annex 10 Volume IV (6th edition) Table 3-6 shows, the basic
 * dataflash capability (42), the phase overlay capabilities on extended squitter (43) and on Mode S (44), the active
 * transponder side indicator (49-50) and the register 11₁₆ data link capability (continuation) change indicator (51).
 * The DTE sub-address array is in register 11₁₆.
 *
 * @see DataLinkCapabilityReport#decode(byte[])
 */
@SuppressWarnings("unused")
public class DataLinkCapabilityReportV6 extends DataLinkCapabilityReport {
    private static final long serialVersionUID = 6248930516264102187L;

    // Basic Data Flash Capability
    private boolean basicDataFlashCapability;
    // Phase Overlay on Extended Squitter Capability
    private boolean phaseOverlayExtendedSquitterCapability;
    // Phase Overlay on Mode S Capability
    private boolean phaseOverlayModeSCapability;
    // Active Transponder Side Indicator
    private short activeTransponderSideIndicator;
    // Register 1116 Change Flag / Data Link Capability (continuation) Change Indicator
    private boolean changeFlag;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected DataLinkCapabilityReportV6() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @throws BadFormatException if its Mode S subnetwork version number is below 6, see
     *                            {@link DataLinkCapabilityReport#decode(byte[])}
     */
    public DataLinkCapabilityReportV6(byte[] message) throws BadFormatException {
        super(message);
        if (getModeSSubNetworkVersionNumber() < 6)
            throw new BadFormatException(
                    "Subnetwork version " + getModeSSubNetworkVersionNumber() + " has the DTE sub-address layout");

        BitReader b = BitReader.forBigEndian(message);
        basicDataFlashCapability = b.readBoolean(42);
        phaseOverlayExtendedSquitterCapability = b.readBoolean(43);
        phaseOverlayModeSCapability = b.readBoolean(44);
        activeTransponderSideIndicator = b.readShort(49, 50);
        changeFlag = b.readBoolean(51);
    }

    /**
     * MB bit 42, ICAO Annex 10 Volume IV (6th edition) Table 3-6.
     *
     * @return whether the transponder has Basic Dataflash capability
     */
    public boolean isBasicDataFlashCapability() {
        return basicDataFlashCapability;
    }

    /**
     * MB bit 43, ICAO Annex 10 Volume IV (6th edition) Table 3-6.
     *
     * @return whether phase overlay in extended squitter is supported
     */
    public boolean isPhaseOverlayExtendedSquitterCapability() {
        return phaseOverlayExtendedSquitterCapability;
    }

    /**
     * MB bit 44, ICAO Annex 10 Volume IV (6th edition) Table 3-6.
     *
     * @return whether phase overlay for Mode S is supported
     */
    public boolean isPhaseOverlayModeSCapability() {
        return phaseOverlayModeSCapability;
    }

    /**
     * MB bits 49-50, ICAO Annex 10 Volume IV (6th edition) Table 3-6.
     *
     * @return the active transponder side indicator
     */
    public short getActiveTransponderSideIndicator() {
        return activeTransponderSideIndicator;
    }

    /**
     * MB bit 51, the register 11₁₆ data link capability (continuation) change indicator, ICAO Annex 10 Volume IV (6th
     * edition) Table 3-6.
     *
     * @return whether change flag is set
     */
    public boolean isChangeFlag() {
        return changeFlag;
    }

    @Override
    public String toString() {
        return "DataLinkCapabilityReportV6{" + super.toString() +
                ", basicDataFlashCapability=" + basicDataFlashCapability +
                ", phaseOverlayExtendedSquitterCapability=" + phaseOverlayExtendedSquitterCapability +
                ", phaseOverlayModeSCapability=" + phaseOverlayModeSCapability +
                ", activeTransponderSideIndicator=" + activeTransponderSideIndicator +
                ", changeFlag=" + changeFlag +
                '}';
    }
}
