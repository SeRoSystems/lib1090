package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * BDS 6,0
 */
public class HeadingAndSpeed extends ModeSDownlinkMsg implements Serializable {

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

    public HeadingAndSpeed(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public HeadingAndSpeed(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public HeadingAndSpeed(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.HEADING_AND_SPEED);

        byte[] payload = getPayload();
        byte[] message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        if (getDownlinkFormat() == 20) {
            this.altitudeCode = (short) ((payload[1] << 8 | payload[2] & 0xFF) & 0x1FFF);
            this.identity = null;
        } else if (getDownlinkFormat() == 21) {
            this.altitudeCode = null;
            this.identity = (short) ((payload[1] << 8 | (payload[2] & 0xFF)) & 0x1FFF);
        } else {
            throw new BadFormatException("Message is not an altitude reply or an identity reply !");
        }

        this.flightStatus = getFirstField();
        this.downlinkRequest = (byte) ((payload[0] >>> 3) & 0x1F);
        this.utilityMsg = (byte) ((payload[0] & 0x7) << 3 | (payload[1] >>> 5) & 0x7);

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
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", magneticHeadingStatus=" + magneticHeadingStatus +
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
