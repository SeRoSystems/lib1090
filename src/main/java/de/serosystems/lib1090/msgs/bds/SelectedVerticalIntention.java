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
 * Decoder for selected vertical intention (BDS 4,0)
 */
@SuppressWarnings("unused")
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
    protected SelectedVerticalIntention() { }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public SelectedVerticalIntention(byte[] message) {

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

    /**
     * @return the MCP/FCU selected altitude. The data shall be derived from the mode control panel/flight control unit
     * or equivalent equipment. Alerting devices may be used to provide data if it is not available from “control”
     * equipment.
     * The value range is [0, 65520] feet
     */
    public Integer getMcpFcuSelectedAltitude() {
        return computeSelectedAltitude(mcpFcuSelectedAltitudeStatus, mcpFcuSelectedAltitudeValue);
    }

    /**
     * @return the FMS selected altitude. The data shall de derived from the flight management system or equivalent
     * equipment managing the vertical profile of the aircraft.
     * The value range is [0, 65520] feet
     */
    public Integer getFmsSelectedAltitude() {
        return computeSelectedAltitude(fmsSelectedAltitudeStatus, fmsSelectedAltitudeValue);
    }

    /**
     * @return the barometric pressure setting.The value range is [0, 410] mb
     */
    public Float getBarometricPressureSetting() {
        return computeBarometricPressureSetting(barometricPressureSettingStatus, barometricPressureSettingValue);
    }

    /**
     * @return whether the vertical navigation mode is active or not
     */
    public Boolean isVnav() {
        return computeOthers(otherStatus, vnavValue);
    }

    /**
     * @return whether the altitude hold mode is active or not
     */
    public Boolean isAltHold() {
        return computeOthers(otherStatus, altHoldValue);
    }

    /**
     * @return whether the approach mode is active or not
     */
    public Boolean isApproach() {
        return computeOthers(otherStatus, approachValue);
    }

    /**
     * @return the target altitude source
     * <ul>
     *     <li> 0 signifies unknown</li>
     *     <li> 1 signifies aircraft altitude </li>
     *     <li> 2 signifies FCU/MCP selected altitude </li>
     *     <li> 3 signifies FMS selected altitud </li>
     * </ul>
     */
    public Short getTargetAltSource() {
        return computeTargetAltSource(targetAltSourceStatus, targetAltSourceValue);
    }

    // static methods
    // ---------------------

    static boolean extractMcpFcuSelectedAltitudeStatus(byte[] message) {
        return ((message[0] >>> 7) & 0x01) == 1;
    }

    static int extractMcpFcuSelectedAltitudeValue(byte[] message) {
        return (((message[0]&0x7F)<<5) | ((message[1]>>>3)&0x1F)) & 0xFFF;
    }

    static boolean extractFmsSelectedAltitudeStatus(byte[] message) {
        return ((message[1] >>> 2 ) & 0x01) == 1;
    }

    static int extractFmsSelectedAltitudeValue(byte[] message) {
        return (((message[1]&0x03)<<10) | ((message[2] << 2) & 0x3FF) | ((message[3] >>> 6) & 0x03)) & 0xFFF;
    }

    static boolean extractBarometricPressureSettingStatus(byte[] message) {
        return ((message[3] >>> 5) & 0x01) == 1;
    }

    static float extractBarometricPressureSettingValue(byte[] message) {
        return (((message[3]&0x1F)<<7) | ((message[4] >>> 1)&0x7F)) & 0xFFF;
    }

    static boolean extractOtherStatus(byte[] message) {
        return (message[5] & 0x01) == 1;
    }

    static boolean extractVnavValue(byte[] message) {
        return ((message[6] >>> 7) & 0x1) == 1;
    }

    static boolean extractAltHoldValue(byte[] message) {
        return ((message[6] >>> 6) & 0x1) == 1;
    }

    static boolean extractApproachValue(byte[] message) {
        return ((message[6] >>> 5) & 0x1) == 1;
    }

    static boolean extractTargetAltSourceStatus(byte[] message) {
        return ((message[6] >>> 2) & 0x1) == 1;
    }

    static short extractTargetAltSourceValue(byte[] message) {
        return (short) (message[6] & 0x3);
    }

    static Integer computeSelectedAltitude(boolean status, int value) {
        return status ? value * 16 : null;
    }

    static Float computeBarometricPressureSetting(boolean status, float value) {
        return status ? value * 0.1F + 800 : null;
    }

    static Boolean computeOthers(boolean status, boolean value) {
        return status ? value : null;
    }

    static Short computeTargetAltSource(boolean status, short value) {
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
