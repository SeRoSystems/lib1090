package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * BDS 1,0
 */
public class DataLinkCapabilityReport extends ModeSDownlinkMsg implements Serializable {

    // Fields
    // ------

    private byte flightStatus;
    // Downlink Request
    private byte downlinkRequest;
    // Utility Message
    private byte utilityMsg;
    // Altitude Code (if DF20 otherwise null)
    private Short altitudeCode;
    // Identity (if DF21 otherwise null)
    private Short identity;
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

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected DataLinkCapabilityReport() {
    }

    public DataLinkCapabilityReport(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public DataLinkCapabilityReport(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public DataLinkCapabilityReport(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.DATALINK_CAPABILITY_REPORT);

        byte[] payload = getPayload();
        byte[] message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        if (getDownlinkFormat() == 20) {
            this.altitudeCode = extractAltitudeCode(payload);
            this.identity = null;
        } else if (getDownlinkFormat() == 21) {
            this.altitudeCode = null;
            this.identity = extractIdentity(payload);
        } else {
            throw new BadFormatException("Message is not an altitude reply or an identity reply !");
        }

        this.flightStatus = getFirstField();
        this.downlinkRequest = extractDownlinkRequest(payload);
        this.utilityMsg = extractUtilityMessage(payload);

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

    public byte getFlightStatus() {
        return flightStatus;
    }

    public byte getDownlinkRequest() {
        return downlinkRequest;
    }

    public byte getUtilityMsg() {
        return utilityMsg;
    }

    public Short getAltitudeCode() {
        return altitudeCode;
    }

    public Short getIdentity() {
        return identity;
    }

    public short getBdsCode() {
        return bdsCode;
    }

    public boolean isContinuationFlag() {
        return continuationFlag;
    }

    public boolean isTcasOperationalCoordinationMessage() {
        return tcasOperationalCoordinationMessage;
    }

    public short getTcasExtendedVersionNumber() {
        return tcasExtendedVersionNumber;
    }

    public boolean isOverlayCommandCapability() {
        return overlayCommandCapability;
    }

    public boolean isTcasInterfaceOperational() {
        return tcasInterfaceOperational;
    }

    public short getModeSSubNetworkVersionNumber() {
        return modeSSubNetworkVersionNumber;
    }

    public boolean isTransponderEnhancedProtocolIndicator() {
        return transponderEnhancedProtocolIndicator;
    }

    public boolean isModeSSpecificServicesCapability() {
        return modeSSpecificServicesCapability;
    }

    public short getUelmAverageThroughputCapability() {
        return uelmAverageThroughputCapability;
    }

    public short getDelmThroughputCapability() {
        return delmThroughputCapability;
    }

    public boolean isAircraftIdentificationCapability() {
        return aircraftIdentificationCapability;
    }

    public boolean isSquitterCapabilitySubfield() {
        return squitterCapabilitySubfield;
    }

    public boolean isSurveillanceIdentifierCode() {
        return surveillanceIdentifierCode;
    }

    public boolean isCommonUsageGicb() {
        return commonUsageGicb;
    }

    public boolean isTcasHybridSurveillanceCapability() {
        return tcasHybridSurveillanceCapability;
    }

    public boolean isTcasRataCapability() {
        return tcasRataCapability;
    }

    public short getTcasVersionNumber() {
        return tcasVersionNumber;
    }

    public boolean isBasicDataFlashCapability() {
        return basicDataFlashCapability;
    }

    public boolean isPhaseOverlayExtendedSquitterCapability() {
        return phaseOverlayExtendedSquitterCapability;
    }

    public boolean isPhaseOverlayModeSCapability() {
        return phaseOverlayModeSCapability;
    }

    public boolean isEnhancedSurveillanceCapability() {
        return enhancedSurveillanceCapability;
    }

    public short getActiveTransponderSideIndicator() {
        return activeTransponderSideIndicator;
    }

    public boolean isChangeFlag() {
        return changeFlag;
    }

    // Public static methods
    // ---------------------

    public static short extractBdsCode(byte[] message) {
        return (short) (((message[0] >>> 4) & 0x0F) * 10 + (message[0] & 0x0F));
    }

    public static boolean extractContinuationFlag(byte[] message) {
        return ((message[1] >>> 7) & 0x1) == 1;
    }

    public static boolean extractTcasOperationalCoordinationMessage(byte[] message) {
        return ((message[1] >>> 6) & 0x1) == 1;
    }

    public static short extractTcasExtendedVersionNumber(byte[] message) {
        return (short) ((message[1] >>> 2) & 0xF);
    }

    public static boolean extractOverlayCommandCapability(byte[] message) {
        return ((message[1] >>> 1) & 0x1) == 1;
    }

    public static boolean extractTcasInterfaceOperational(byte[] message) {
        return (message[1] & 0x1) == 1;
    }

    public static short extractModeSSubNetworkVersionNumber(byte[] message) {
        return (short) ((message[2] >>> 1) & 0x7F);
    }

    public static boolean extractTransponderEnhancedProtocolIndicator(byte[] message) {
        return (message[2] & 0x01) == 1;
    }

    public static boolean extractModeSSpecificServicesCapability(byte[] message) {
        return ((message[3] >>> 7) & 0x1) == 1;
    }

    public static short extractUelmAverageThroughputCapability(byte[] message) {
        return (short) ((message[3] >>> 4) & 0x07);
    }

    public static short extractDelmThroughputCapability(byte[] message) {
        return (short) (message[3] & 0x0F);
    }

    public static boolean extractAircraftIdentificationCapability(byte[] message) {
        return ((message[4] >>> 7) & 0x01) == 1;
    }

    public static boolean extractSquitterCapabilitySubfield(byte[] message) {
        return ((message[4] >>> 6) & 0x01) == 1;
    }

    public static boolean extractSurveillanceIdentifierCode(byte[] message) {
        return ((message[4] >>> 5) & 0x01) == 1;
    }

    public static boolean extractCommonUsageGICB(byte[] message) {
        return ((message[4] >>> 4) & 0x01) == 1;
    }

    public static boolean extractTcasHybridSurveillanceCapability(byte[] message) {
        return ((message[4] >>> 3) & 0x01) == 1;
    }

    public static boolean extractTcasRataCapability(byte[] message) {
        return ((message[4] >>> 2) & 0x01) == 1;
    }

    public static short extractTcasVersionNumber(byte[] message) {
        return (short) (message[4] & 0x03);
    }

    public static boolean extractBasicDataFlashCapability(byte[] message) {
        return ((message[5] >>> 6) & 0x01) == 1;
    }

    public static boolean extractPhaseOverlayExtendedSquitterCapability(byte[] message) {
        return ((message[5] >>> 5) & 0x01) == 1;
    }

    public static boolean extractPhaseOverlayModeSCapability(byte[] message) {
        return ((message[5] >>> 4) & 0x01) == 1;
    }

    public static boolean extractEnhancedSurveillanceCapability(byte[] message) {
        return ((message[5] >>> 1) & 0x01) == 1;
    }

    public static short extractActiveTransponderSideIndicator(byte[] message) {
        return (short) ((message[6] >>> 6) & 0x03);
    }

    public static boolean extractChangeFlag(byte[] message) {
        return ((message[5] >>> 5) & 0x01) == 1;
    }

    // Private static methods
    // ----------------------

    private short extractIdentity(byte[] payload) {
        return (short) ((payload[1] << 8 | (payload[2] & 0xFF)) & 0x1FFF);
    }

    private static short extractAltitudeCode(byte[] payload) {
        return (short) ((payload[1] << 8 | payload[2] & 0xFF) & 0x1FFF);
    }

    private static byte extractUtilityMessage(byte[] payload) {
        return (byte) ((payload[0]&0x7)<<3 | (payload[1]>>>5)&0x7);
    }

    private static byte extractDownlinkRequest(byte[] payload) {
        return (byte) ((payload[0]>>>3) & 0x1F);
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "DataLinkCapabilityReport{" +
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", bdsCode=" + bdsCode +
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
