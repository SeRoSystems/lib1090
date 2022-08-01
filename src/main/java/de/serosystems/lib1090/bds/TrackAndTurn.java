package de.serosystems.lib1090.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;

/**
 * BDS 5,0
 */
public class TrackAndTurn extends BDSRegister implements Serializable {

    // Fields
    // ------

    // Roll Angle
    private boolean rollAngleStatus;
    private boolean rollAngleSign;
    private short rollAngleValue;
    // True Track Angle
    private boolean trueTrackAngleStatus;
    private boolean trueTrackAngleSign;
    private short trueTrackAngleValue;
    // Ground Speed
    private boolean groundSpeedStatus;
    private short groundSpeedValue;
    // Track Angle Rate
    private boolean trackAngleRateStatus;
    private boolean trackAngleRateSign;
    private short trackAngleRateValue;
    // True Airspeed
    private boolean trueAirSpeedStatus;
    private short trueAirSpeedValue;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected TrackAndTurn() {
    }

    public TrackAndTurn(byte[] message) throws BadFormatException {

        super(message);
        setBds(BDSRegister.bdsCode.TRACK_AND_TURN_REPORT);

        this.rollAngleStatus = extractRollAngleStatus(message);
        this.rollAngleSign = extractRollAngleSign(message);
        this.rollAngleValue = extractRollAngleValue(message);
        this.trueTrackAngleStatus = extractTrueTrackAngleStatus(message);
        this.trueTrackAngleSign = extractTrueTrackAngleSign(message);
        this.trueTrackAngleValue = extractTrueTrackAngleValue(message);
        this.groundSpeedStatus = extractGroundSpeedStatus(message);
        this.groundSpeedValue = extractGroundSpeedValue(message);
        this.trackAngleRateStatus = extractTrackAngleRateStatus(message);
        this.trackAngleRateSign = extractTrackAngleRateSign(message);
        this.trackAngleRateValue = extractTrackAngleRateValue(message);
        this.trueAirSpeedStatus = extractTrueAirSpeedStatus(message);
        this.trueAirSpeedValue = extractTrueAirSpeedValue(message);

    }

    // Getters
    // -------

    public Float getRollAngle() {
        return computeRollAngle(rollAngleStatus, rollAngleSign, rollAngleValue);
    }

    public Float getTrueTrackAngle() {
        return computeTrueTrackAngle(trueTrackAngleStatus, trueTrackAngleSign, trueTrackAngleValue);
    }

    public Integer getGroundSpeed() {
        return computeGroundSpeed(groundSpeedStatus, groundSpeedValue);
    }

    public Float getTrackAngleRate() {
        return computeTrackAngleRate(trackAngleRateStatus, trackAngleRateSign, trackAngleRateValue);
    }

    public Integer getTrueAirspeed() {
        return computeTrueAirSpeed(trueAirSpeedStatus, trueAirSpeedValue);
    }

    // Public static methods
    // ---------------------

    public static boolean extractRollAngleStatus(byte[] message) {
        return ((message[0] >>> 7) & 0x1) == 1;
    }

    public static boolean extractRollAngleSign(byte[] message) {
        return ((message[0] >>> 6) & 0x1) == 1;
    }

    public static short extractRollAngleValue(byte[] message) {
        return (short) ((((message[0] & 0x03F) << 3) | ((message[1] >>> 5) & 0x07)) & 0x1FF);
    }

    public static boolean extractTrueTrackAngleStatus(byte[] message) {
        return ((message[1] >>> 4) & 0x1) == 1;
    }

    public static boolean extractTrueTrackAngleSign(byte[] message) {
        return ((message[1] >>> 3) & 0x1) == 1;
    }

    public static short extractTrueTrackAngleValue(byte[] message) {
        return (short) ((((message[1] & 0x007) << 7) | ((message[2] >>> 1) & 0x07F)) & 0x3FF);
    }

    public static boolean extractGroundSpeedStatus(byte[] message) {
        return (message[2] & 0x01) == 1;
    }

    public static short extractGroundSpeedValue(byte[] message) {
        return (short) ((((message[3] & 0x0FF) << 2) | ((message[4] >>> 6) & 0x03)) & 0x3FF);
    }

    public static boolean extractTrackAngleRateStatus(byte[] message) {
        return ((message[4] >>> 5) & 0x1) == 1;
    }

    public static boolean extractTrackAngleRateSign(byte[] message) {
        return ((message[4] >>> 4) & 0x1) == 1;
    }

    public static short extractTrackAngleRateValue(byte[] message) {
        return (short) ((((message[4] & 0x00F) << 5) | ((message[5] >>> 3) & 0x01F)) & 0x1FF);
    }

    public static boolean extractTrueAirSpeedStatus(byte[] message) {
        return ((message[5] >>> 2) & 0x01) == 1;
    }

    public static short extractTrueAirSpeedValue(byte[] message) {
        return (short) ((((message[5] & 0x03) << 8) | (message[6] & 0xFF)) & 0x3FF);
    }

    public static Float computeRollAngle(boolean rollAngleStatus, boolean rollAngleSign, short rollAngleValue) {
        return rollAngleStatus ? rollAngleSign ? (float) ((-Math.pow(2,9) + rollAngleValue) * 45 / 256) : rollAngleValue * 45 / 256 : null;
    }

    public static Float computeTrueTrackAngle(boolean trueTrackAngleStatus, boolean trueTrackAngleSign, short trueTrackAngleValue) {
        return trueTrackAngleStatus ? trueTrackAngleSign ? (float) ((-Math.pow(2,10) + trueTrackAngleValue) * 90 / 512) : trueTrackAngleValue * 90 / 512 : null;
    }

    public static Integer computeGroundSpeed(boolean groundSpeedStatus, short groundSpeedValue) {
        return groundSpeedStatus ? groundSpeedValue * 2 : null;
    }

    public static Float computeTrackAngleRate(boolean trackAngleRateStatus, boolean trackAngleRateSign, short trackAngleRateValue) {
        return trackAngleRateStatus ? trackAngleRateSign ? (float) ((-Math.pow(2, 9) + trackAngleRateValue) * 8 / 256) : trackAngleRateValue * 8 / 256 :  null;
    }

    public static Integer computeTrueAirSpeed(boolean trueAirspeedStatus, short trueAirspeedValue) {
        return trueAirspeedStatus ? trueAirspeedValue * 2 : null;
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "TrackAndTurn{" +
                "rollAngleStatus=" + rollAngleStatus +
                ", rollAngleSign=" + rollAngleSign +
                ", rollAngleValue=" + rollAngleValue +
                ", trueTrackAngleStatus=" + trueTrackAngleStatus +
                ", trueTrackAngleSign=" + trueTrackAngleSign +
                ", trueTrackAngleValue=" + trueTrackAngleValue +
                ", groundSpeedStatus=" + groundSpeedStatus +
                ", groundSpeedValue=" + groundSpeedValue +
                ", trackAngleRateStatus=" + trackAngleRateStatus +
                ", trackAngleRateSign=" + trackAngleRateSign +
                ", trackAngleRateValue=" + trackAngleRateValue +
                ", trueAirSpeedStatus=" + trueAirSpeedStatus +
                ", trueAirSpeedValue=" + trueAirSpeedValue +
                '}';
    }

}
