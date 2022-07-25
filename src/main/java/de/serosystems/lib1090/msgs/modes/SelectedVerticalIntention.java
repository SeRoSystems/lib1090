package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * BDS 4,0
 */
public class SelectedVerticalIntention extends ModeSDownlinkMsg implements Serializable {

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
    // MCP/FCU Selected Altitude Status
    private boolean mcpFcuSelectedAltitudeStatus;
    // MCP/FCU Selected Altitude
    private int mcpFcuSelectedAltitudeValue;
    // FMS Selected Altitude Status
    private boolean fmsSelectedAltitudeStatus;
    // FMS Selected Altitude
    private int fmsSelectedAltitudeValue;
    // Barometric Pressure Setting Status
    private boolean barometricPressureSettingStatus;
    // Barometric Pressure Setting
    private float barometricPressureSettingValue;
    // VNAV, Alt Hold and Approach Status
    private boolean otherStatus;
    // VNAV
    private boolean vnavValue;
    // Alt Hold
    private boolean altHoldValue;
    // Approach
    private boolean approachValue;
    // Target alt source Status
    private boolean targetAltSourceStatus;
    // Target alt source
    private short targetAltSourceValue;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected SelectedVerticalIntention() {
    }

    public SelectedVerticalIntention(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public SelectedVerticalIntention(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public SelectedVerticalIntention(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.SELECTED_VERTICAL_INTENTION);


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

        this.mcpFcuSelectedAltitudeStatus = extractMcpFcuSelectedAltitudeStatus(message);
        this.mcpFcuSelectedAltitudeValue = extractMcpFcuSelectedAltitudeValue(message);
        this.fmsSelectedAltitudeStatus = extractFmsSelectedAltitudeStatus(message);
        this.fmsSelectedAltitudeValue = extractFmsSelectedAltitudeValue(message);
        this.barometricPressureSettingStatus = extractBarometricPressureSettingStatus(message);
        this.barometricPressureSettingValue = extractBarometricPressureSettingValue(message);
        this.otherStatus = extractOtherStatus(message);
        this.vnavValue = extractVnavValue(message);
        this.altHoldValue = extractAltHoldValue(message);
        this.approachValue = extractApproachValue(message);
        this.targetAltSourceStatus = extractTargetAltSourceStatus(message);
        this.targetAltSourceValue = extractTargetAltSourceValue(message);

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

    public short getAltitudeCode() {
        return altitudeCode;
    }

    public short getIdentity() {
        return identity;
    }

    public Integer getMcpFcuSelectedAltitudeValue() {
        return computeSelectedAltitude(mcpFcuSelectedAltitudeStatus, mcpFcuSelectedAltitudeValue);
    }

    public Integer getFmsSelectedAltitudeValue() {
        return computeSelectedAltitude(fmsSelectedAltitudeStatus, fmsSelectedAltitudeValue);
    }

    public Float getBarometricPressureSettingValue() {
        return computeBarometricPressureSetting(barometricPressureSettingStatus, barometricPressureSettingValue);
    }

    public Boolean isVnav() {
        return computeOthers(otherStatus, vnavValue);
    }

    public Boolean isAltHold() {
        return computeOthers(otherStatus, altHoldValue);
    }

    public Boolean isApproach() {
        return computeOthers(otherStatus, approachValue);
    }

    public Short getTargetAltSourceValue() {
        return computeTargetAltSource(targetAltSourceStatus, targetAltSourceValue);
    }

    // Public static methods
    // ---------------------

    public static boolean extractMcpFcuSelectedAltitudeStatus(byte[] message) {
        return ((message[0] >>> 7) & 0x01) == 1;
    }

    public static int extractMcpFcuSelectedAltitudeValue(byte[] message) {
        return (((message[0]&0x7F)<<5) | ((message[1]>>>3)&0x1F)) & 0xFFF;
    }

    public static boolean extractFmsSelectedAltitudeStatus(byte[] message) {
        return ((message[1] >>> 2 ) & 0x01) == 1;
    }

    public static int extractFmsSelectedAltitudeValue(byte[] message) {
        return (((message[1]&0x03)<<10) | ((message[2] << 2) & 0x3FF) | ((message[3] >>> 6) & 0x03)) & 0xFFF;
    }

    public static boolean extractBarometricPressureSettingStatus(byte[] message) {
        return ((message[3] >>> 5) & 0x01) == 1;
    }

    public static float extractBarometricPressureSettingValue(byte[] message) {
        return (((message[3]&0x1F)<<7) | ((message[4] >>> 1)&0x7F)) & 0xFFF;
    }

    public static boolean extractOtherStatus(byte[] message) {
        return (message[5] & 0x01) == 1;
    }

    public static boolean extractVnavValue(byte[] message) {
        return ((message[6] >>> 7) & 0x1) == 1;
    }

    public static boolean extractAltHoldValue(byte[] message) {
        return ((message[6] >>> 6) & 0x1) == 1;
    }

    public static boolean extractApproachValue(byte[] message) {
        return ((message[6] >>> 5) & 0x1) == 1;
    }

    public static boolean extractTargetAltSourceStatus(byte[] message) {
        return ((message[6] >>> 2) & 0x1) == 1;
    }

    public static short extractTargetAltSourceValue(byte[] message) {
        return (short) (message[6] & 0x3);
    }

    public static Integer computeSelectedAltitude(boolean status, int value) {
        return status ? value * 16 : null;
    }

    public static Float computeBarometricPressureSetting(boolean status, float value) {
        return status ? value * 0.1F + 800 : null;
    }

    public static Boolean computeOthers(boolean status, boolean value) {
        return status ? value : null;
    }

    public static Short computeTargetAltSource(boolean status, short value) {
        return status ? value : null;
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
        return "SelectedVerticalIntention{" +
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", mcpFcuSelectedAltitudeStatus=" + mcpFcuSelectedAltitudeStatus +
                ", mcpFcuSelectedAltitudeValue=" + mcpFcuSelectedAltitudeValue +
                ", fmsSelectedAltitudeStatus=" + fmsSelectedAltitudeStatus +
                ", fmsSelectedAltitudeValue=" + fmsSelectedAltitudeValue +
                ", barometricPressureSettingStatus=" + barometricPressureSettingStatus +
                ", barometricPressureSettingValue=" + barometricPressureSettingValue +
                ", otherStatus=" + otherStatus +
                ", vnavValue=" + vnavValue +
                ", altHoldValue=" + altHoldValue +
                ", approachValue=" + approachValue +
                ", targetAltSourceStatus=" + targetAltSourceStatus +
                ", targetAltSourceValue=" + targetAltSourceValue +
                '}';
    }

}
