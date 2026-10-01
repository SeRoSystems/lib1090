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
import de.serosystems.lib1090.msgs.acas.ACASType;

/**
 * Decoder for the data link capability report (BDS 1,0), following the layout of ICAO Annex 10 Volume IV (6th
 * edition) §3.1.2.6.10.2.2.1.3 Table 3-6, with the ACAS bits (MB bits 11-14, 16 and 37-40) as §4.3.8.4.2.2.3 defines
 * them. Bit numbers in this class are MB bit numbers; the Annex's message bit numbers are 32 higher. DO-181F and
 * ED-73F (Table B-3-16a) define the same layout.
 * <p>
 * MB bits 41-56 have two layouts, which the Mode S subnetwork version number selects, so this class holds MB bits 9-40
 * and {@link #decode(byte[])} picks the subclass for the rest:
 * <ul>
 *     <li>{@link DataLinkCapabilityReportV6}, from version 6 on, the version of DO-181F and ED-73F: the basic
 *     dataflash and phase overlay capabilities, the active transponder side and the change indicator</li>
 *     <li>{@link DataLinkCapabilityReportV0V5}, below version 6, ICAO Doc 9871 First and Second Edition §A.2 Table
 *     A-2-16: the support status of DTE sub-addresses 0 to 15, which Table 3-6 has moved to register 11₁₆ (Note to
 *     Table 3-6)</li>
 * </ul>
 * <p>
 * Whether the transponder supports the enhanced surveillance registers 4,0, 5,0 and 6,0 is not reported here, but in
 * the common usage GICB capability report (BDS 1,7), see {@link CommonUsageGICBCapabilityReport}; MB bit 36,
 * {@link #isCommonUsageGicb()}, toggles each time that report changes.
 */
@SuppressWarnings("unused")
public abstract class DataLinkCapabilityReport extends BDSRegister {
    private static final long serialVersionUID = 2607206512004324831L;

    private static final BDSCode BDS_CODE = new BDSCode(1, 0);

    // Register 1116 Continuation Flag
    private boolean continuationFlag;
    // Operational Coordination Message (OCM) Transmit Capability
    private boolean ocmTransmitCapability;
    // ACAS type
    private short acasTypeEncoded;
    // Overlay Command Capability
    private boolean overlayCommandCapability;
    // ACAS operating
    private boolean acasOperating;
    // Mode S Sub Network Version Number
    private short modeSSubNetworkVersionNumber;
    // Transponder Enhanced Protocol Indicator
    private boolean transponderEnhancedProtocolIndicator;
    // Mode S Specific Services Capability
    private boolean modeSSpecificServicesCapability;
    // Uplink ELM Average Throughput Capability
    private short uelmAverageThroughputCapability;
    // Downlink ELM Throughput Capability
    private short delmThroughputCapability;
    // Aircraft Identification Capability
    private boolean aircraftIdentificationCapability;
    // Squitter Capability Subfield
    private boolean squitterCapabilitySubfield;
    // Surveillance Identifier Code
    private boolean surveillanceIdentifierCode;
    // Common Usage GICB
    private boolean commonUsageGicb;
    // ACAS Hybrid Surveillance Capability
    private boolean acasHybridSurveillanceCapability;
    // ACAS generating TAs and RAs
    private boolean acasGeneratingRAs;
    // ACAS Version Number
    private short acasVersionNumber;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected DataLinkCapabilityReport() {
    }

    /**
     * Decodes a data link capability report in the layout its Mode S subnetwork version number selects.
     *
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @return a {@link DataLinkCapabilityReportV6} from subnetwork version 6 on, a {@link DataLinkCapabilityReportV0V5}
     * below it
     * @throws BadFormatException if MB bits 1-8 are not 0001 0000, BDS1 = 1 and BDS2 = 0 (ICAO Annex 10 Volume IV
     *                            §3.1.2.6.10.2.2.2, ICAO Doc 9871 §A.2 Table A-2-16)
     */
    public static DataLinkCapabilityReport decode(byte[] message) throws BadFormatException {
        return subNetworkVersionNumber(message) >= 6
                ? new DataLinkCapabilityReportV6(message)
                : new DataLinkCapabilityReportV0V5(message);
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @return the Mode S subnetwork version number, MB bits 17-23
     */
    static short subNetworkVersionNumber(byte[] message) {
        return BitReader.forBigEndian(message).readShort(17, 23);
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @throws BadFormatException if MB bits 1-8 are not 0001 0000, BDS1 = 1 and BDS2 = 0 (ICAO Annex 10 Volume IV
     *                            §3.1.2.6.10.2.2.2, ICAO Doc 9871 §A.2 Table A-2-16)
     */
    protected DataLinkCapabilityReport(byte[] message) throws BadFormatException {
        super(message);
        requireBDSCode();

        BitReader b = BitReader.forBigEndian(message);

        continuationFlag = b.readBoolean(9);
        ocmTransmitCapability = b.readBoolean(10);
        acasTypeEncoded = b.readShort(11, 14);
        overlayCommandCapability = b.readBoolean(15);
        acasOperating = b.readBoolean(16);
        modeSSubNetworkVersionNumber = b.readShort(17, 23);
        transponderEnhancedProtocolIndicator = b.readBoolean(24);
        modeSSpecificServicesCapability = b.readBoolean(25);
        uelmAverageThroughputCapability = b.readShort(26, 28);
        delmThroughputCapability = b.readShort(29, 32);
        aircraftIdentificationCapability = b.readBoolean(33);
        squitterCapabilitySubfield = b.readBoolean(34);
        surveillanceIdentifierCode = b.readBoolean(35);
        commonUsageGicb = b.readBoolean(36);
        acasHybridSurveillanceCapability = b.readBoolean(37);
        acasGeneratingRAs = b.readBoolean(38);
        acasVersionNumber = (short) (b.readShort(40, 40) << 1 | b.readShort(39, 39));
    }

    /**
     * @return whether the subsequent register shall be extracted
     */
    public boolean isContinuationFlag() {
        return continuationFlag;
    }

    /**
     * The operational coordination message (OCM) transmit capability, MB bit 10, ICAO Annex 10 Volume IV Table 3-6.
     *
     * @return whether the installation can transmit the operational coordination message, the 1090ES CAS operational
     * coordination message (TYPE Code 28, subtype 3,
     * {@link de.serosystems.lib1090.msgs.adsb.CASOperationalCoordinationMsg})
     */
    public boolean isOCMTransmitCapability() {
        return ocmTransmitCapability;
    }

    /**
     * @return the collision avoidance system as transmitted, MB bits 11-14, ICAO Annex 10 Volume IV (6th edition)
     * §4.3.8.4.2.2.3: 0 TCAS version 7.1 and other systems defined by {@link #getACASVersionNumber()}, 1 ACAS Xa
     * (RTCA/DO-385 and EUROCAE/ED-256), 2 to 15 reserved for ACAS III
     * @see #getACASType()
     */
    public short getACASTypeEncoded() {
        return acasTypeEncoded;
    }

    /**
     * @return the collision avoidance system, MB bits 11-14, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3
     */
    public ACASType getACASType() {
        return ACASType.forEncoded(acasTypeEncoded);
    }

    /**
     * Defined in DO-181E §2.2.19.1.12.6.2.
     *
     * @return The Overlay Command Capability (OCC)
     * <ul>
     *     <li>0: no overlay command capability</li>
     *     <li>1: overlay command capability</li>
     * </ul>
     */
    public boolean isOverlayCommandCapability() {
        return overlayCommandCapability;
    }

    /**
     * MB bit 16, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3; DO-181E §2.2.22.1.2.2.4 sets it while the
     * transponder's ACAS interface is operational and the transponder is receiving RI 2, 3 or 4 from ACAS.
     *
     * @return true if ACAS is operating, false if ACAS has failed or is on standby
     */
    public boolean isACASOperating() {
        return acasOperating;
    }

    /**
     * @return The Mode-S Subnetwork Version Number
     * <ul>
     *     <li>0: Mode-S subnetwork not available</li>
     *     <li>1: ICAO Doc 9688 (1996)</li>
     *     <li>2: ICAO Doc 9688 (1998)</li>
     *     <li>3: ICAO Annex 10, Volume III, Amendment 77</li>
     *     <li>4: ICAO Doc 9871, Edition 1 (RTCA DO-181D, EUROCAE ED-73C)</li>
     *     <li>5: ICAO Doc 9871, Edition 2 (RTCA DO-181E, EUROCAE ED-73E)</li>
     *     <li>6: RTCA DO-181F and EUROCAE ED-73F, which assign it to an "ICAO Doc 9871, Edition 3"; Doc 9871 Edition 2
     *     still lists 6 as reserved</li>
     *     <li>7-127: Reserved</li>
     * </ul>
     * The version selects the layout of MB bits 41-56, see {@link #decode(byte[])}.
     */
    public short getModeSSubNetworkVersionNumber() {
        return modeSSubNetworkVersionNumber;
    }

    /**
     * @return whether the enhanced protocol indicator is set to 0 or 1
     * <ul>
     *     <li>0: a Level 2 to 4 transponder</li>
     *     <li>1: a Level 5 transponder</li>
     * </ul>
     */
    public boolean isTransponderEnhancedProtocolIndicator() {
        return transponderEnhancedProtocolIndicator;
    }

    /**
     * When the mode s specific services capability is set to 1, it shall indicate that at least one Mode-S specific
     * service (other than GICB services related to registers 0216, 0316, 0416, 1016, 1716 to 1C16, 2016 and 3016)
     * is supported and the particular capability reports shall be checked.
     *
     * @return whether the mode s specific services capability is set to 0 or 1
     */
    public boolean isModeSSpecificServicesCapability() {
        return modeSSpecificServicesCapability;
    }

    /**
     * Uplink ELM average throughput capability shall be coded as follows:
     * <ul>
     *     <li>0: No UELM Capability</li>
     *     <li>1: 16 UELM segments in 1 second</li>
     *     <li>2: 16 UELM segments in 500 ms</li>
     *     <li>3: 16 UELM segments in 250 ms</li>
     *     <li>4: 16 UELM segments in 125 ms</li>
     *     <li>5: 16 UELM segments in 60 ms</li>
     *     <li>6: 16 UELM segments in 30 ms</li>
     *     <li>7: Unassigned</li>
     * </ul>
     *
     * @return The uplink ELM average throughput capability
     */
    public short getUelmAverageThroughputCapability() {
        return uelmAverageThroughputCapability;
    }

    /**
     * Downlink ELM throughput capability contains the maximum number of ELM segments that the transponder can deliver
     * in response to a single requesting interrogation (UF = 24).
     * Downlink ELM throughput capability shall be coded as follows:
     * <ul>
     *     <li>0: No DELM Capability</li>
     *     <li>1: One 4 segment DELM every second</li>
     *     <li>2: One 8 segment DELM every second</li>
     *     <li>3: One 16 segment DELM every second</li>
     *     <li>4: One 16 segment DELM every 500 ms</li>
     *     <li>5: One 16 segment DELM every 250 ms</li>
     *     <li>6: One 16 segment DELM every 125 ms</li>
     *     <li>7-15: Unassigned</li>
     * </ul>
     *
     * @return The downlink ELM throughput capability
     */
    public short getDelmThroughputCapability() {
        return delmThroughputCapability;
    }

    /**
     * @return the availability of Aircraft Identification data. It shall be set by the transponder if the data comes
     * to the transponder through a separate interface and not through the ADLP.
     */
    public boolean isAircraftIdentificationCapability() {
        return aircraftIdentificationCapability;
    }

    /**
     * The squitter capability subfield shall be set to 1 if both Registers 0516 and 0616 have been updated within
     * the last ten, plus or minus one, seconds. Otherwise, it shall be set to 0
     *
     * @return The squitter capability subfield
     */
    public boolean isSquitterCapabilitySubfield() {
        return squitterCapabilitySubfield;
    }

    /**
     * @return The surveillance identifier code
     * <ul>
     *     <li>0: no surveillance identifier code capability</li>
     *     <li>1: surveillance identifier code capability</li>
     * </ul>
     */
    public boolean isSurveillanceIdentifierCode() {
        return surveillanceIdentifierCode;
    }

    /**
     * Bit 36 shall be toggled each time the common usage GICB capability report (Register 1716) changes.
     * To avoid the generation of too many broadcast capability report changes,
     * Register 1716 shall be sampled at approximately one minute intervals to check for changes.
     *
     * @return whether the common usage GICB capability report is set to true or false
     */
    public boolean isCommonUsageGicb() {
        return commonUsageGicb;
    }

    /**
     * MB bit 37, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3. Hybrid surveillance limits the active
     * interrogations of ACAS (§4.5.1); the ability to decode extended squitters alone does not set this bit.
     *
     * @return whether hybrid surveillance is fitted and operational
     */
    public boolean isACASHybridSurveillanceCapability() {
        return acasHybridSurveillanceCapability;
    }

    /**
     * MB bit 38, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3.
     *
     * @return true if ACAS is generating TAs and RAs, false if it is generating TAs only
     */
    public boolean isACASGeneratingRAs() {
        return acasGeneratingRAs;
    }

    /**
     * MB bits 39-40, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.3, whose table lists bit 40 (message bit 72)
     * first, i.e. as the most significant bit.
     *
     * @return ACAS version number
     * <ul>
     *     <li>0: RTCA/DO-185 (pre-ACAS)</li>
     *     <li>1: RTCA/DO-185A</li>
     *     <li>2: RTCA/DO-185B and EUROCAE ED-143</li>
     *     <li>3: all later systems, which the ACAS unit and software part numbers in registers E5₁₆ and E6₁₆ identify
     *     (§4.3.8.4.2.8)</li>
     * </ul>
     */
    public short getACASVersionNumber() {
        return acasVersionNumber;
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "DataLinkCapabilityReport{" + super.toString() +
                ", continuationFlag=" + continuationFlag +
                ", ocmTransmitCapability=" + ocmTransmitCapability +
                ", acasTypeEncoded=" + acasTypeEncoded +
                ", overlayCommandCapability=" + overlayCommandCapability +
                ", acasOperating=" + acasOperating +
                ", modeSSubNetworkVersionNumber=" + modeSSubNetworkVersionNumber +
                ", transponderEnhancedProtocolIndicator=" + transponderEnhancedProtocolIndicator +
                ", modeSSpecificServicesCapability=" + modeSSpecificServicesCapability +
                ", uelmAverageThroughputCapability=" + uelmAverageThroughputCapability +
                ", delmThroughputCapability=" + delmThroughputCapability +
                ", aircraftIdentificationCapability=" + aircraftIdentificationCapability +
                ", squitterCapabilitySubfield=" + squitterCapabilitySubfield +
                ", surveillanceIdentifierCode=" + surveillanceIdentifierCode +
                ", commonUsageGicb=" + commonUsageGicb +
                ", acasHybridSurveillanceCapability=" + acasHybridSurveillanceCapability +
                ", acasGeneratingRAs=" + acasGeneratingRAs +
                ", acasVersionNumber=" + acasVersionNumber +
                '}';
    }
}
