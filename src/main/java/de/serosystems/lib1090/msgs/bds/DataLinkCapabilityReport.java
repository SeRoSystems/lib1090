package de.serosystems.lib1090.msgs.bds;

import java.io.Serializable;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoder for Datalink Capability Report (BDS 1,0)
 */
@SuppressWarnings("unused")
public class DataLinkCapabilityReport extends BDSRegister implements Serializable {

    // Fields
    // ------

    // BDS Code
    private short bdsCode;
    // Register 1116 Continuation Flag
    private boolean continuationFlag;
    // TCAS Operational Coordination Message Transmit Capability
    private boolean tcasOperationalCoordinationMessage;
    // TCAS Extended Version Number
    private short tcasExtendedVersionNumber;
    // Overlay Command Capability
    private boolean overlayCommandCapability;
    // TCAS Interface Operational
    private boolean tcasInterfaceOperational;
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
    // TCAS Hybrid Surveillance Capability
    private boolean tcasHybridSurveillanceCapability;
    // TCAS RA/TA Capability
    private boolean tcasRataCapability;
    // TCAS Version Number
    private short tcasVersionNumber;
    // Basic Data Flash Capability
    private boolean basicDataFlashCapability;
    // Phase Overlay on Extended Squitter Capability
    private boolean phaseOverlayExtendedSquitterCapability;
    // Phase Overlay on Mode S Capability
    private boolean phaseOverlayModeSCapability;
    // Enhanced Surveillance (EHS) Capability
    private boolean enhancedSurveillanceCapability;
    // Active Transponder Side Indicator
    private short activeTransponderSideIndicator;
    // Register 1116 Change Flag / Data Link Capability (continuation) Change Indicator
    private boolean changeFlag;

    // Constructors
    // ------------

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected DataLinkCapabilityReport() { }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public DataLinkCapabilityReport(byte[] message) {

        super(message);
        setBds(BDSRegister.bdsCode.DATA_LINK_CAPABILITY_REPORT);

        this.bdsCode = extractBdsCode(message);
        this.continuationFlag = extractContinuationFlag(message);
        this.tcasOperationalCoordinationMessage = extractTcasOperationalCoordinationMessage(message);
        this.tcasExtendedVersionNumber = extractTcasExtendedVersionNumber(message);
        this.overlayCommandCapability = extractOverlayCommandCapability(message);
        this.tcasInterfaceOperational = extractTcasInterfaceOperational(message);
        this.modeSSubNetworkVersionNumber = extractModeSSubNetworkVersionNumber(message);
        this.transponderEnhancedProtocolIndicator = extractTransponderEnhancedProtocolIndicator(message);
        this.modeSSpecificServicesCapability = extractModeSSpecificServicesCapability(message);
        this.uelmAverageThroughputCapability = extractUelmAverageThroughputCapability(message);
        this.delmThroughputCapability = extractDelmThroughputCapability(message);
        this.aircraftIdentificationCapability = extractAircraftIdentificationCapability(message);
        this.squitterCapabilitySubfield = extractSquitterCapabilitySubfield(message);
        this.surveillanceIdentifierCode = extractSurveillanceIdentifierCode(message);
        this.commonUsageGicb = extractCommonUsageGICB(message);
        this.tcasHybridSurveillanceCapability = extractTcasHybridSurveillanceCapability(message);
        this.tcasRataCapability = extractTcasRataCapability(message);
        this.tcasVersionNumber = extractTcasVersionNumber(message);
        this.basicDataFlashCapability = extractBasicDataFlashCapability(message);
        this.phaseOverlayExtendedSquitterCapability = extractPhaseOverlayExtendedSquitterCapability(message);
        this.phaseOverlayModeSCapability = extractPhaseOverlayModeSCapability(message);
        this.enhancedSurveillanceCapability = extractEnhancedSurveillanceCapability(message);
        this.activeTransponderSideIndicator = extractActiveTransponderSideIndicator(message);
        this.changeFlag = extractChangeFlag(message);

    }

    // Getters
    // -------

    /**
     * @return whether the subsequent register shall be extracted
     */
    public boolean isContinuationFlag() {
        return continuationFlag;
    }

    /**
     * @return whether it's a TCAS operational coordination message
     */
    public boolean isTcasOperationalCoordinationMessage() {
        return tcasOperationalCoordinationMessage;
    }

    /**
     * @return the extended TCAS version number
     */
    public short getTcasExtendedVersionNumber() {
        return tcasExtendedVersionNumber;
    }

    /**
     * @return The Overlay Command Capability (OCC)
     * <ul>
     * <li> 0 signifies no overlay command capability </li>
     * <li> 1 signifies overlay command capability </li>
     * </ul>
     */
    public boolean isOverlayCommandCapability() {
        return overlayCommandCapability;
    }

    /**
     * @return whether the transponder TCAS interface is operational and the transponder is receiving TCAS RI=2, 3 or 4
     */
    public boolean isTcasInterfaceOperational() {
        return tcasInterfaceOperational;
    }

    /**
     * @return The Mode-S Subnetwork Version Number
     * <ul>
     *     <li> 0 signifies Mode-S subnetwork not available
     *     <li> 1 signifies ICAO Doc 9688 (1996) </li>
     *     <li> 2 signifies ICAO Doc 9688 (1998) </li>
     *     <li> 3 signifies ICAO Annex 10, Vol III, Amendment 77 </li>
     *     <li> 4 signifies ICAO Doc 9871, Edition 1 </li>
     *     <li> 5 signifies ICAO Doc 9871, Edition 2 </li>
     *     <li> 6-127 Reserved </li>
     * </ul>
     */
    public short getModeSSubNetworkVersionNumber() {
        return modeSSubNetworkVersionNumber;
    }

    /**
     * @return whether the enhanced protocol indicator is set to 0 or 1
     * <ul>
     *     <li> 0 signifies a Level 2 to 4 transponder </li>
     *     <li> 1 signifies a Level 5 transponder </li>
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
     *  <li> 0 signifies No UELM Capability </li>
     *  <li> 1 signifies 16 UELM segments in 1 second <li>
     *  <li> 2 signifies 16 UELM segments in 500 ms <li>
     *  <li> 3 signifies 16 UELM segments in 250 ms <li>
     *  <li> 4 signifies 16 UELM segments in 125 ms <li>
     *  <li> 5 signifies 16 UELM segments in 60 ms <li>
     *  <li> 6 signifies 16 UELM segments in 30 ms <li>
     *  <li> 7 signifies Unassigned <li>
     *  </ul>
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
     *  <li> 0 signifies No DELM Capability </li>
     *  <li> 1 signifies One 4 segment DELM every second </li>
     *  <li> 2 signifies One 8 segment DELM every second </li>
     *  <li> 3 signifies One 16 segment DELM every second </li>
     *  <li> 4 signifies One 16 segment DELM every 500 ms </li>
     *  <li> 5 signifies One 16 segment DELM every 250 ms </li>
     *  <li> 6 signifies One 16 segment DELM every 125 ms </li>
     *  <li> 7-15 signifies Unassigned </li>
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
     * <li> 0 signifies no surveillance identifier code capability </li>
     * <li> 1 signifies surveillance identifier code capability </li>
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
     * Bit 37 shall be set to 1 to indicate the capability of Hybrid Surveillance,
     * and set to 0 to indicate that there is no Hybrid Surveillance capability.
     *
     * @return whether TCAS Hybrid Surveillance Capability is set to 0 or 1
     */
    public boolean isTcasHybridSurveillanceCapability() {
        return tcasHybridSurveillanceCapability;
    }

    /**
     * Bit 38 shall be set to 1 to indicate that the TCAS is generating both TAs and RAs,
     * and set to 0 to indicate the generation of TAs only.
     *
     * @return whether TCAS RA/TA capability is set to 0 or 1
     */
    public boolean isTcasRataCapability() {
        return tcasRataCapability;
    }

    /**
     * @return TCAS version number
     * <ul>
     *     <li> 0 signifies DO-185 (6.04A) </li>
     *     <li> 1 signifies DO-185A </li>
     *     <li> 2 signifies DO-185B </li>
     *     <li> 3 signifies reserved for future versions </li>
     * </ul>
     */
    public short getTcasVersionNumber() {
        return tcasVersionNumber;
    }

    /**
     * @return whether the transponder has Basic Dataflash capability
     */
    public boolean isBasicDataFlashCapability() {
        return basicDataFlashCapability;
    }

    /**
     * @return whether phase overlay in extended squitter is supported
     */
    public boolean isPhaseOverlayExtendedSquitterCapability() {
        return phaseOverlayExtendedSquitterCapability;
    }

    /**
     * @return whether phase overlay for Mode S is supported
     */
    public boolean isPhaseOverlayModeSCapability() {
        return phaseOverlayModeSCapability;
    }

    /**
     * @return whether the transponder has Enhanced Surveillance capability
     */
    public boolean isEnhancedSurveillanceCapability() {
        return enhancedSurveillanceCapability;
    }

    public short getActiveTransponderSideIndicator() {
        return activeTransponderSideIndicator;
    }

    /**
     * @return whether change flag is set
     */
    public boolean isChangeFlag() {
        return changeFlag;
    }

    // static methods
    // ---------------------


    static boolean extractContinuationFlag(byte[] message) {
        return ((message[1] >>> 7) & 0x1) == 1;
    }

    static boolean extractTcasOperationalCoordinationMessage(byte[] message) {
        return ((message[1] >>> 6) & 0x1) == 1;
    }

    static short extractTcasExtendedVersionNumber(byte[] message) {
        return (short) ((message[1] >>> 2) & 0xF);
    }

    static boolean extractOverlayCommandCapability(byte[] message) {
        return ((message[1] >>> 1) & 0x1) == 1;
    }

    static boolean extractTcasInterfaceOperational(byte[] message) {
        return (message[1] & 0x1) == 1;
    }

    static short extractModeSSubNetworkVersionNumber(byte[] message) {
        return (short) ((message[2] >>> 1) & 0x7F);
    }

    static boolean extractTransponderEnhancedProtocolIndicator(byte[] message) {
        return (message[2] & 0x01) == 1;
    }

    static boolean extractModeSSpecificServicesCapability(byte[] message) {
        return ((message[3] >>> 7) & 0x1) == 1;
    }

    static short extractUelmAverageThroughputCapability(byte[] message) {
        return (short) ((message[3] >>> 4) & 0x07);
    }

    static short extractDelmThroughputCapability(byte[] message) {
        return (short) (message[3] & 0x0F);
    }

    static boolean extractAircraftIdentificationCapability(byte[] message) {
        return ((message[4] >>> 7) & 0x01) == 1;
    }

    static boolean extractSquitterCapabilitySubfield(byte[] message) {
        return ((message[4] >>> 6) & 0x01) == 1;
    }

    static boolean extractSurveillanceIdentifierCode(byte[] message) {
        return ((message[4] >>> 5) & 0x01) == 1;
    }

    static boolean extractCommonUsageGICB(byte[] message) {
        return ((message[4] >>> 4) & 0x01) == 1;
    }

    static boolean extractTcasHybridSurveillanceCapability(byte[] message) {
        return ((message[4] >>> 3) & 0x01) == 1;
    }

    static boolean extractTcasRataCapability(byte[] message) {
        return ((message[4] >>> 2) & 0x01) == 1;
    }

    static short extractTcasVersionNumber(byte[] message) {
        return (short) (message[4] & 0x03);
    }

    static boolean extractBasicDataFlashCapability(byte[] message) {
        return ((message[5] >>> 6) & 0x01) == 1;
    }

    static boolean extractPhaseOverlayExtendedSquitterCapability(byte[] message) {
        return ((message[5] >>> 5) & 0x01) == 1;
    }

    static boolean extractPhaseOverlayModeSCapability(byte[] message) {
        return ((message[5] >>> 4) & 0x01) == 1;
    }

    static boolean extractEnhancedSurveillanceCapability(byte[] message) {
        return ((message[5] >>> 1) & 0x01) == 1;
    }

    static short extractActiveTransponderSideIndicator(byte[] message) {
        return (short) ((message[6] >>> 6) & 0x03);
    }

    static boolean extractChangeFlag(byte[] message) {
        return ((message[5] >>> 5) & 0x01) == 1;
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "DataLinkCapabilityReport{" +
                "bdsCode=" + bdsCode +
                ", continuationFlag=" + continuationFlag +
                ", tcasOperationalCoordinationMessage=" + tcasOperationalCoordinationMessage +
                ", tcasExtendedVersionNumber=" + tcasExtendedVersionNumber +
                ", overlayCommandCapability=" + overlayCommandCapability +
                ", tcasInterfaceOperational=" + tcasInterfaceOperational +
                ", modeSSubNetworkVersionNumber=" + modeSSubNetworkVersionNumber +
                ", transponderEnhancedProtocolIndicator=" + transponderEnhancedProtocolIndicator +
                ", modeSSpecificServicesCapability=" + modeSSpecificServicesCapability +
                ", uelmAverageThroughputCapability=" + uelmAverageThroughputCapability +
                ", delmThroughputCapability=" + delmThroughputCapability +
                ", aircraftIdentificationCapability=" + aircraftIdentificationCapability +
                ", squitterCapabilitySubfield=" + squitterCapabilitySubfield +
                ", surveillanceIdentifierCode=" + surveillanceIdentifierCode +
                ", commonUsageGicb=" + commonUsageGicb +
                ", tcasHybridSurveillanceCapability=" + tcasHybridSurveillanceCapability +
                ", tcasRataCapability=" + tcasRataCapability +
                ", tcasVersionNumber=" + tcasVersionNumber +
                ", basicDataFlashCapability=" + basicDataFlashCapability +
                ", phaseOverlayExtendedSquitterCapability=" + phaseOverlayExtendedSquitterCapability +
                ", phaseOverlayModeSCapability=" + phaseOverlayModeSCapability +
                ", enhancedSurveillanceCapability=" + enhancedSurveillanceCapability +
                ", activeTransponderSideIndicator=" + activeTransponderSideIndicator +
                ", changeFlag=" + changeFlag +
                '}';
    }
}
