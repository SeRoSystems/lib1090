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
 * Decoder for heading and speed (BDS 6,0)
 */
@SuppressWarnings("unused")
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
    // Mach Number
    private boolean machNumberStatus;
    private short machNumberValue;
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
    protected HeadingAndSpeed() { }


    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public HeadingAndSpeed(byte[] message) {

        super(message);
        setBds(BDSRegister.bdsCode.HEADING_AND_SPEED_REPORT);

        this.magneticHeadingStatus = extractMagneticHeadingStatus(message);
        this.magneticHeadingSign = extractMagneticHeadingSign(message);
        this.magneticHeadingValue = extractMagneticHeadingValue(message);
        this.indicatedAirspeedStatus = extractIndicatedAirspeedStatus(message);
        this.indicatedAirspeedValue = extractIndicatedAirspeedValue(message);
        this.machNumberStatus = extractMatchNumberStatus(message);
        this.machNumberValue = extractMatchNumberValue(message);
        this.barometricAltitudeRateStatus = extractBarometricAltitudeRateStatus(message);
        this.barometricAltitudeRateSign = extractBarometricAltitudeRateSign(message);
        this.barometricAltitudeRateValue = extractBarometricAltitudeRateValue(message);
        this.inertialVerticalRateStatus = extractInertialVerticalRateStatus(message);
        this.inertialVerticalRateSign = extractInertialVerticalRateSign(message);
        this.inertialVerticalRateValue = extractInertialVerticalRateValue(message);

    }

    // Getters
    // -------

    /**
     * @return the magnetic heading.
     * The value range is [-180, +180] degrees
     */
    public Float getMagneticHeading() {
        return computeMagneticHeading(magneticHeadingStatus, magneticHeadingSign, magneticHeadingValue);
    }

    /**
     * @return the indicated airspeed
     * The value range is [0, 1023] knots
     */
    public Short getIndicatedAirspeed() {
        return computeIndicatedAirspeed(indicatedAirspeedStatus, indicatedAirspeedValue);
    }

    /**
     * @return the mach number
     * The value range is [0, 4.092] MACH
     */
    public Float getMachNumber() {
        return computeMatchNumber(machNumberStatus, machNumberValue);
    }

    /**
     * @return the barometric altitude rate
     * The value range is [-16384, +16352] feet/minute
     */
    public Integer getBarometricAltitudeRate() {
        return computeBarometricAltitude(barometricAltitudeRateStatus, barometricAltitudeRateSign, barometricAltitudeRateValue);
    }

    /**
     * @return the inertial vertical rate.
     * The value range is [-16384, +16352] feet/minute
     */
    public Integer getInertialVerticalRate() {
        return computeInertialVerticalRate(inertialVerticalRateStatus, inertialVerticalRateSign, inertialVerticalRateValue);
    }

    // static methods
    // ---------------------

    static boolean extractMagneticHeadingStatus(byte[] message) {
        return ((message[0] >>> 7) & 0x01) == 1;
    }

    static boolean extractMagneticHeadingSign(byte[] message) {
        return ((message[0] >>> 6) & 0x01) == 1;
    }

    static short extractMagneticHeadingValue(byte[] message) {
        return (short) ((((message[0] & 0x3F) << 4) | ((message[1] >>> 4) & 0x0F)) & 0x3FF);
    }

    static boolean extractIndicatedAirspeedStatus(byte[] message) {
        return ((message[1] >>> 3) & 0x01) == 1;
    }

    static short extractIndicatedAirspeedValue(byte[] message) {
        return (short) ((((message[1] & 0x07) << 7) | ((message[2] >>> 1) & 0x7F)) & 0x3FF);
    }

    static boolean extractMatchNumberStatus(byte[] message) {
        return (message[2] & 0x01) == 1;
    }

    static short extractMatchNumberValue(byte[] message) {
        return (short) (((message[3] << 2) | ((message[4] >>> 6) & 0x03)) & 0x3FF);
    }

    static boolean extractBarometricAltitudeRateStatus(byte[] message) {
        return ((message[4] >>> 5) & 0x01) == 1;
    }

    static boolean extractBarometricAltitudeRateSign(byte[] message) {
        return ((message[4] >>> 4) & 0x01) == 1;
    }

    static short extractBarometricAltitudeRateValue(byte[] message) {
        // FIXME Junzi p 132
        return (short) ((((message[4] & 0x0F) << 5) | ((message[5] >>> 3) & 0x1F)) & 0x1FF);
    }

    static boolean extractInertialVerticalRateStatus(byte[] message) {
        return ((message[5] >>> 2) & 0x01) == 1;
    }

    static boolean extractInertialVerticalRateSign(byte[] message) {
        return ((message[5] >>> 1) & 0x01) == 1;
    }

    static short extractInertialVerticalRateValue(byte[] message) {
        return (short) ((((message[5] & 0x01) << 8) | (message[6] & 0xFF)) & 0x1FF);
    }

    static Float computeMagneticHeading(boolean status, boolean sign, short value) {
        return status ? (sign ? (float) ((-Math.pow(2, 10) + value) * 90 / 512) : value * 90 / 512) : null;
    }

    static Short computeIndicatedAirspeed(boolean status, short value) {
        return status ? value : null;
    }

    static Float computeMatchNumber(boolean status, short value) {
        return status ? (float) (value * 2.048 / 512) : null;
    }

    static Integer computeBarometricAltitude(boolean status, boolean sign, short value) {
        return status ? (sign ? (int) ((-Math.pow(2, 9) + value) * 32) : value * 32) : null;
    }

    static Integer computeInertialVerticalRate(boolean status, boolean sign, short value) {
        return status ? (sign ? (int) ((-Math.pow(2, 9) + value) * 32) : value * 32) : null;
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
                ", matchNumberStatus=" + machNumberStatus +
                ", matchNumberValue=" + machNumberValue +
                ", barometricAltitudeRateStatus=" + barometricAltitudeRateStatus +
                ", barometricAltitudeRateSign=" + barometricAltitudeRateSign +
                ", barometricAltitudeRateValue=" + barometricAltitudeRateValue +
                ", inertialVerticalRateStatus=" + inertialVerticalRateStatus +
                ", inertialVerticalRateSign=" + inertialVerticalRateSign +
                ", inertialVerticalRateValue=" + inertialVerticalRateValue +
                '}';
    }

}
