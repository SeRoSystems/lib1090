package de.serosystems.lib1090.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;

/**
 * BDS 4,0
 */
public class SelectedVerticalIntention extends BDSRegister implements Serializable {

    // Fields
    // ------

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

    public SelectedVerticalIntention(byte[] message) throws BadFormatException {

        super(message);
        setBds(BDSRegister.bdsCode.SELECTED_VERTICAL_INTENTION);

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

    // Override
    // --------

    @Override
    public String toString() {
        return "SelectedVerticalIntention{" +
                "mcpFcuSelectedAltitudeStatus=" + mcpFcuSelectedAltitudeStatus +
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
