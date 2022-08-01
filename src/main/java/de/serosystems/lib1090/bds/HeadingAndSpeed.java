package de.serosystems.lib1090.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;

/**
 * BDS 6,0
 */
public class HeadingAndSpeed extends BDSRegister implements Serializable {

    // Fields
    // ------

    // Magnetic Heading
    private boolean magneticHeadingStatus;
    private boolean magneticHeadingSign;
    private short magneticHeadingValue;
    // Indicated Airspeed
    private boolean indicatedAirspeedStatus;
    private short indicatedAirspeedValue;
    // Match Number
    private boolean matchNumberStatus;
    private short matchNumberValue;
    // Barometric Altitude Rate
    private boolean barometricAltitudeRateStatus;
    private boolean barometricAltitudeRateSign;
    private short barometricAltitudeRateValue;
    // Inertial Vertical Rate
    private boolean inertialVerticalRateStatus;
    private boolean inertialVerticalRateSign;
    private short inertialVerticalRateValue;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected HeadingAndSpeed() {
    }


    public HeadingAndSpeed(byte[] message) throws BadFormatException {

        super(message);
        setBds(BDSRegister.bdsCode.HEADING_AND_SPEED_REPORT);

        this.magneticHeadingStatus = extractMagneticHeadingStatus(message);
        this.magneticHeadingSign = extractMagneticHeadingSign(message);
        this.magneticHeadingValue = extractMagneticHeadingValue(message);
        this.indicatedAirspeedStatus = extractIndicatedAirspeedStatus(message);
        this.indicatedAirspeedValue = extractIndicatedAirspeedValue(message);
        this.matchNumberStatus = extractMatchNumberStatus(message);
        this.matchNumberValue = extractMatchNumberValue(message);
        this.barometricAltitudeRateStatus = extractBarometricAltitudeRateStatus(message);
        this.barometricAltitudeRateSign = extractBarometricAltitudeRateSign(message);
        this.barometricAltitudeRateValue = extractBarometricAltitudeRateValue(message);
        this.inertialVerticalRateStatus = extractInertialVerticalRateStatus(message);
        this.inertialVerticalRateSign = extractInertialVerticalRateSign(message);
        this.inertialVerticalRateValue = extractInertialVerticalRateValue(message);

    }

    // Getters
    // -------

    public Float getMagneticHeading() {
        return computeMagneticHeading(magneticHeadingStatus, magneticHeadingSign, magneticHeadingValue);
    }

    public Short getIndicatedAirspeed() {
        return computeIndicatedAirspeed(indicatedAirspeedStatus, indicatedAirspeedValue);
    }

    public Float getMatchNumber() {
        return computeMatchNumber(matchNumberStatus, matchNumberValue);
    }

    public Integer getBarometricAltitudeRate() {
        return computeBarometricAltitude(barometricAltitudeRateStatus, barometricAltitudeRateSign, barometricAltitudeRateValue);
    }

    public Integer getInertialVerticalRate() {
        return computeInertialVerticalRate(inertialVerticalRateStatus, inertialVerticalRateSign, inertialVerticalRateValue);
    }

    // Public static methods
    // ---------------------

    public static boolean extractMagneticHeadingStatus(byte[] message) {
        return ((message[0] >>> 7) & 0x01) == 1;
    }

    public static boolean extractMagneticHeadingSign(byte[] message) {
        return ((message[0] >>> 6) & 0x01) == 1;
    }

    public static short extractMagneticHeadingValue(byte[] message) {
        return (short) ((((message[0] & 0x3F) << 4) | ((message[1] >>> 4) & 0x0F)) & 0x3FF);
    }

    public static boolean extractIndicatedAirspeedStatus(byte[] message) {
        return ((message[1] >>> 3) & 0x01) == 1;
    }

    public static short extractIndicatedAirspeedValue(byte[] message) {
        return (short) ((((message[1] & 0x07) << 7) | ((message[2] >>> 1) & 0x7F)) & 0x3FF);
    }

    public static boolean extractMatchNumberStatus(byte[] message) {
        return (message[2] & 0x01) == 1;
    }

    public static short extractMatchNumberValue(byte[] message) {
        return (short) (((message[3] << 2) | ((message[4] >>> 6) & 0x03)) & 0x3FF);
    }

    public static boolean extractBarometricAltitudeRateStatus(byte[] message) {
        return ((message[4] >>> 5) & 0x01) == 1;
    }

    public static boolean extractBarometricAltitudeRateSign(byte[] message) {
        return ((message[4] >>> 4) & 0x01) == 1;
    }

    public static short extractBarometricAltitudeRateValue(byte[] message) {
        return (short) ((((message[4] & 0x0F) >>> 5) | ((message[5] >>> 3) & 0x1F)) & 0x1FF);
    }

    public static boolean extractInertialVerticalRateStatus(byte[] message) {
        return ((message[5] >>> 2) & 0x01) == 1;
    }

    public static boolean extractInertialVerticalRateSign(byte[] message) {
        return ((message[5] >>> 1) & 0x01) == 1;
    }

    public static short extractInertialVerticalRateValue(byte[] message) {
        return (short) ((((message[5] & 0x01) << 8) | (message[6] & 0xFF)) & 0x1FF);
    }

    public static Float computeMagneticHeading(boolean status, boolean sign, short value) {
        return status ? sign ? (float) ((-Math.pow(2, 10) + value) * 90 / 512) : value * 90 / 512 : null;
    }

    public static Short computeIndicatedAirspeed(boolean status, short value) {
        return status ? value : null;
    }

    public static Float computeMatchNumber(boolean status, short value) {
        return status ? (float) (value * 2.048 / 512) : null;
    }

    public static Integer computeBarometricAltitude(boolean status, boolean sign, short value) {
        return status ? sign ? (int) ((-Math.pow(2, 9) + value) * 32) : value * 32 : null;
    }

    public static Integer computeInertialVerticalRate(boolean status, boolean sign, short value) {
        return status ? sign ? (int) ((-Math.pow(2, 9) + value) * 32) : value * 32 : null;
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "HeadingAndSpeed{" +
                "magneticHeadingStatus=" + magneticHeadingStatus +
                ", magneticHeadingSign=" + magneticHeadingSign +
                ", magneticHeadingValue=" + magneticHeadingValue +
                ", indicatedAirspeedStatus=" + indicatedAirspeedStatus +
                ", indicatedAirspeedValue=" + indicatedAirspeedValue +
                ", matchNumberStatus=" + matchNumberStatus +
                ", matchNumberValue=" + matchNumberValue +
                ", barometricAltitudeRateStatus=" + barometricAltitudeRateStatus +
                ", barometricAltitudeRateSign=" + barometricAltitudeRateSign +
                ", barometricAltitudeRateValue=" + barometricAltitudeRateValue +
                ", inertialVerticalRateStatus=" + inertialVerticalRateStatus +
                ", inertialVerticalRateSign=" + inertialVerticalRateSign +
                ", inertialVerticalRateValue=" + inertialVerticalRateValue +
                '}';
    }

}
