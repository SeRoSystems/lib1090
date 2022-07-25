package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;
import java.util.Arrays;

/**
 * BDS 3,0
 */
public class ACASActiveResolutionAdvisorReport extends ModeSDownlinkMsg implements Serializable {

    // Fields
    // ------

    // Flight Status
    private byte flightStatus;
    // Downlink Request
    private byte downlinkRequest;
    // Utility Message
    private byte utilityMsg;
    // Altitude Code (if DF20 otherwise null)
    private Short altitudeCode;
    // Identity (if DF21 otherwise null)
    private Short identity;
    // BDS Number
    private short bdsCode;
    // Active Resolution Advisories
    private boolean[] activeResolutionAdvisories;
    // Resolution Advisories Components Record
    private boolean[] resolutionAdvisoriesComponentsRecord;
    // Resolution Advisories Terminated Indicator
    private boolean resolutionAdvisoriesTerminatedIndicator;
    // Multiple Threat Encounter
    private boolean multipleThreatEncounter;
    // Threat Type Indicator
    private short threatTypeIndicator;
    // Threat Identity Data
    private ThreatIdentityData threatIdentityData;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected ACASActiveResolutionAdvisorReport() {}

    public ACASActiveResolutionAdvisorReport(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public ACASActiveResolutionAdvisorReport(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public ACASActiveResolutionAdvisorReport(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.ACAS_ACTIVE_RESOLUTION_ADVISOR_REPORT);

        byte[] payload = getPayload();
        byte[] message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        if (reply.getDownlinkFormat() == 20) {
            this.altitudeCode = extractAltitudeCode(payload);
            this.identity = null;
        } else if (reply.getDownlinkFormat() == 21) {
            this.altitudeCode = null;
            this.identity = extractIdentity(payload);
        } else {
            throw new BadFormatException("Message is not an altitude reply or an identity reply !");
        }

        this.flightStatus = getFirstField();
        this.downlinkRequest = extractDownlinkRequest(payload);
        this.utilityMsg = extractUtilityMessage(payload);

        this.bdsCode = extractBdsCode(message);
        this.activeResolutionAdvisories = extractActiveResolutionAdvisories(message);
        this.resolutionAdvisoriesComponentsRecord = extractResolutionAdvisoriesComponentsRecord(message);
        this.resolutionAdvisoriesTerminatedIndicator = extractResolutionAdvisoryTerminated(message);
        this.multipleThreatEncounter = extractMultipleThreatEncounter(message);
        this.threatTypeIndicator = extractThreatTypeIndicator(message);
        this.threatIdentityData = extractThreatIdentityData(this.threatTypeIndicator, message);

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

    public boolean[] getActiveResolutionAdvisories() {
        return computeActiveResolutionAdvisories(activeResolutionAdvisories, multipleThreatEncounter);
    }

    public boolean[] getResolutionAdvisoriesComponentsRecord() {
        return resolutionAdvisoriesComponentsRecord;
    }

    public boolean isResolutionAdvisoriesTerminatedIndicator() {
        return resolutionAdvisoriesTerminatedIndicator;
    }

    public boolean isMultipleThreatEncounter() {
        return multipleThreatEncounter;
    }

    public short getThreatTypeIndicator() {
        return threatTypeIndicator;
    }

    public ThreatIdentityData getThreatIdentityData() {
        return threatIdentityData;
    }

    // Private static methods
    // ----------------------

    public static short extractBdsCode(byte[] message) {
        int part1 = (message[0] >>> 4) & 0x0F;
        int part2 = message[0] & 0x0F;
        return (short) (part1 * 10 + part2);
    }

    public static boolean[] extractActiveResolutionAdvisories(byte[] message) {

        return new boolean[]{
                ((message[1] >>> 7) & 0x01) == 1,
                ((message[1] >>> 6) & 0x01) == 1,
                ((message[1] >>> 5) & 0x01) == 1,
                ((message[1] >>> 4) & 0x01) == 1,
                ((message[1] >>> 3) & 0x01) == 1,
                ((message[1] >>> 2) & 0x01) == 1,
                ((message[1] >>> 1) & 0x01) == 1,
                (message[1] & 0x01) == 1,
                ((message[2] >>> 7) & 0x01) == 1,
                ((message[2] >>> 6) & 0x01) == 1,
                ((message[2] >>> 5) & 0x01) == 1,
                ((message[2] >>> 4) & 0x01) == 1,
                ((message[2] >>> 3) & 0x01) == 1,
                ((message[2] >>> 2) & 0x01) == 1
        };
    }

    public static boolean[] extractResolutionAdvisoriesComponentsRecord(byte[] message) {

        boolean doNotPassBelow = ((message[2] >>> 1) & 0x01) == 1;
        boolean doNotPassAbove = (message[2] & 0x01) == 1;
        boolean doNotTurnLef = ((message[3] >>> 7) & 0x01) == 1;
        boolean doNotTurnRight = ((message[3] >>> 6) & 0x01) == 1;

        return new boolean[]{doNotPassBelow, doNotPassAbove, doNotTurnLef, doNotTurnRight};

    }

    public static boolean extractResolutionAdvisoryTerminated(byte[] message) {
        return ((message[3] >>> 5) & 0x01) == 1;
    }

    public static boolean extractMultipleThreatEncounter(byte[] message) {
        return ((message[3] >>> 4) & 0x01) == 1;
    }

    public static short extractThreatTypeIndicator(byte[] message) {
        return (short) ((message[3] >>> 2) & 0x03);
    }

    public static ThreatIdentityData extractThreatIdentityData(short threatTypeIndicator, byte[] message) {

        ThreatIdentityData threatIdentityData = null;

        switch (threatTypeIndicator) {

            case 1 :
                long icao = (((message[3] & 0x03) << 22) | ((message[4] & 0xFF) << 14) | ((message[5] & 0xFF) << 6) | ((message[6] >>> 2) & 0x3F)) & 0xFFFFFF;
                threatIdentityData = new ThreatIdentityData(icao, null, null, null);
                break;

            case 2 :
                short altitudeCode = (short) ((((message[3] & 0x03) << 11) | ((message[4] & 0xFF) << 3) | ((message[5] >>> 5) & 0x03)) & 0x1FFF);
                short threatIdentityDataRange = (short) ((((message[5] & 0x1F) << 2) | ((message[6] >>> 6) & 0x03)) & 0x7F);
                short threatIdentityDataBearing = (short) (message[6] & 0x3F);
                threatIdentityData = new ThreatIdentityData(null, altitudeCode, threatIdentityDataRange, threatIdentityDataBearing);
                break;

        }

        return threatIdentityData;

    }

    public static boolean[] computeActiveResolutionAdvisories(boolean[] ara, boolean mte) {
        return (ara[0] || mte) ? Arrays.copyOfRange(ara, 1, 7) : null;
    }

    // Private static methods
    // ----------------------

    private static short extractIdentity(byte[] payload) {
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
        return "ACASActiveResolutionAdvisorReport{" +
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", bdsCode=" + bdsCode +
                ", activeResolutionAdvisories=" + Arrays.toString(activeResolutionAdvisories) +
                ", resolutionAdvisoriesComponentsRecord=" + Arrays.toString(resolutionAdvisoriesComponentsRecord) +
                ", resolutionAdvisoriesTerminatedIndicator=" + resolutionAdvisoriesTerminatedIndicator +
                ", multipleThreatEncounter=" + multipleThreatEncounter +
                ", threatTypeIndicator=" + threatTypeIndicator +
                ", threatIdentityData=" + threatIdentityData +
                '}';
    }

}
