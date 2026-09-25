/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.BitReader;

import java.io.Serializable;

/**
 * Decoder for the heading and speed report (BDS 6,0), as defined in ICAO Doc 9871 (First
 * Edition, AN/464) §A.2 TABLE A-2-96.
 */
@SuppressWarnings("unused")
public class HeadingAndSpeed extends BDSRegister implements Serializable {
    private static final long serialVersionUID = -4234774022351033835L;

    private static final BDSCode BDS_CODE = new BDSCode(6, 0);

    // Magnetic Heading
    private boolean magneticHeadingStatus;
    private boolean magneticHeadingSign;
    private short magneticHeadingEncoded;
    // Indicated Airspeed
    private boolean indicatedAirspeedStatus;
    private short indicatedAirspeedEncoded;
    // Mach Number
    private boolean machNumberStatus;
    private short machNumberEncoded;
    // Barometric Altitude Rate
    private boolean barometricAltitudeRateStatus;
    private boolean barometricAltitudeRateSign;
    private short barometricAltitudeRateEncoded;
    // Inertial Vertical Rate
    private boolean inertialVerticalRateStatus;
    private boolean inertialVerticalRateSign;
    private short inertialVerticalRateEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected HeadingAndSpeed() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public HeadingAndSpeed(byte[] message) {
        super(message);
        BitReader b = BitReader.forBigEndian(message);

        magneticHeadingStatus = b.readBoolean(1);
        magneticHeadingSign = b.readBoolean(2);
        magneticHeadingEncoded = b.readShort(3, 12);
        indicatedAirspeedStatus = b.readBoolean(13);
        indicatedAirspeedEncoded = b.readShort(14, 23);
        machNumberStatus = b.readBoolean(24);
        machNumberEncoded = b.readShort(25, 34);
        barometricAltitudeRateStatus = b.readBoolean(35);
        barometricAltitudeRateSign = b.readBoolean(36);
        barometricAltitudeRateEncoded = b.readShort(37, 45);
        inertialVerticalRateStatus = b.readBoolean(46);
        inertialVerticalRateSign = b.readBoolean(47);
        inertialVerticalRateEncoded = b.readShort(48, 56);
    }

    /**
     * @return whether the magnetic heading is available
     */
    public boolean hasMagneticHeading() {
        return magneticHeadingStatus;
    }

    /**
     * @return the magnetic heading sign bit: true means west, i.e. a heading between 180 and 360
     * degrees clockwise from magnetic north
     */
    public boolean getMagneticHeadingSign() {
        return magneticHeadingSign;
    }

    /**
     * The magnetic heading value as transmitted, without its sign. The sign and the value together
     * are a two's complement number; {@link #getMagneticHeading()} interprets them.
     *
     * @return the encoded magnetic heading value
     */
    public short getMagneticHeadingEncoded() {
        return magneticHeadingEncoded;
    }

    /**
     * The magnetic heading in degrees from magnetic north: positive clockwise, i.e. east of north,
     * and negative counterclockwise, i.e. west of north, so that -45 degrees is a heading of 315
     * degrees. The resolution is 90/512 degrees and the range is [-180, +180) degrees.
     *
     * @return the magnetic heading in degrees, or null if not available
     */
    public Float getMagneticHeading() {
        if (!magneticHeadingStatus) return null;
        return (float) (twosComplement(magneticHeadingSign, magneticHeadingEncoded, 10) * 90.0 / 512);
    }

    /**
     * @return whether the indicated airspeed is available
     */
    public boolean hasIndicatedAirspeed() {
        return indicatedAirspeedStatus;
    }

    /**
     * @return the encoded indicated airspeed
     */
    public short getIndicatedAirspeedEncoded() {
        return indicatedAirspeedEncoded;
    }

    /**
     * The indicated airspeed in knots, at a resolution of 1 knot and in the range [0, 1023] knots.
     *
     * @return the indicated airspeed in knots, or null if not available
     */
    public Short getIndicatedAirspeed() {
        if (!indicatedAirspeedStatus) return null;
        return indicatedAirspeedEncoded;
    }

    /**
     * @return whether the Mach number is available
     */
    public boolean hasMachNumber() {
        return machNumberStatus;
    }

    /**
     * @return the encoded Mach number
     */
    public short getMachNumberEncoded() {
        return machNumberEncoded;
    }

    /**
     * The Mach number, at a resolution of 2.048/512 = 0.004 and in the range [0, 4.092].
     *
     * @return the Mach number, or null if not available
     */
    public Float getMachNumber() {
        if (!machNumberStatus) return null;
        return (float) (machNumberEncoded * 2.048 / 512);
    }

    /**
     * @return whether the barometric altitude rate is available
     */
    public boolean hasBarometricAltitudeRate() {
        return barometricAltitudeRateStatus;
    }

    /**
     * @return the barometric altitude rate sign bit: true means below, i.e. descending
     */
    public boolean getBarometricAltitudeRateSign() {
        return barometricAltitudeRateSign;
    }

    /**
     * The barometric altitude rate value as transmitted, without its sign. The sign and the value
     * together are a two's complement number; {@link #getBarometricAltitudeRate()} interprets them.
     *
     * @return the encoded barometric altitude rate value
     */
    public short getBarometricAltitudeRateEncoded() {
        return barometricAltitudeRateEncoded;
    }

    /**
     * The barometric altitude rate in feet per minute, derived solely from barometric measurement:
     * positive when climbing, negative when descending. The resolution is 32 feet per minute and the
     * range is [-16384, +16352] feet per minute.
     *
     * @return the barometric altitude rate in feet per minute, or null if not available
     */
    public Integer getBarometricAltitudeRate() {
        if (!barometricAltitudeRateStatus) return null;
        return twosComplement(barometricAltitudeRateSign, barometricAltitudeRateEncoded, 9) * 32;
    }

    /**
     * @return whether the inertial vertical rate is available
     */
    public boolean hasInertialVerticalRate() {
        return inertialVerticalRateStatus;
    }

    /**
     * @return the inertial vertical rate sign bit: true means below, i.e. descending
     */
    public boolean getInertialVerticalRateSign() {
        return inertialVerticalRateSign;
    }

    /**
     * The inertial vertical rate value as transmitted, without its sign. The sign and the value
     * together are a two's complement number; {@link #getInertialVerticalRate()} interprets them.
     *
     * @return the encoded inertial vertical rate value
     */
    public short getInertialVerticalRateEncoded() {
        return inertialVerticalRateEncoded;
    }

    /**
     * The inertial vertical rate in feet per minute, which Doc 9871 calls the inertial vertical
     * velocity and which carries baro-inertial information where that is available: positive when
     * climbing, negative when descending. The resolution is 32 feet per minute and the range is
     * [-16384, +16352] feet per minute.
     *
     * @return the inertial vertical rate in feet per minute, or null if not available
     */
    public Integer getInertialVerticalRate() {
        if (!inertialVerticalRateStatus) return null;
        return twosComplement(inertialVerticalRateSign, inertialVerticalRateEncoded, 9) * 32;
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "HeadingAndSpeed{" + super.toString() +
                ", magneticHeadingStatus=" + magneticHeadingStatus +
                ", magneticHeadingSign=" + magneticHeadingSign +
                ", magneticHeadingEncoded=" + magneticHeadingEncoded +
                ", indicatedAirspeedStatus=" + indicatedAirspeedStatus +
                ", indicatedAirspeedEncoded=" + indicatedAirspeedEncoded +
                ", machNumberStatus=" + machNumberStatus +
                ", machNumberEncoded=" + machNumberEncoded +
                ", barometricAltitudeRateStatus=" + barometricAltitudeRateStatus +
                ", barometricAltitudeRateSign=" + barometricAltitudeRateSign +
                ", barometricAltitudeRateEncoded=" + barometricAltitudeRateEncoded +
                ", inertialVerticalRateStatus=" + inertialVerticalRateStatus +
                ", inertialVerticalRateSign=" + inertialVerticalRateSign +
                ", inertialVerticalRateEncoded=" + inertialVerticalRateEncoded +
                '}';
    }

}
