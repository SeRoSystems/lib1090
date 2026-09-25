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
 * Decoder for the track and turn report (BDS 5,0), as defined in ICAO Doc 9871 (First Edition,
 * AN/464) §A.2 TABLE A-2-80.
 */
@SuppressWarnings("unused")
public class TrackAndTurn extends BDSRegister implements Serializable {
    private static final long serialVersionUID = 4009313088718180688L;

    private static final BDSCode BDS_CODE = new BDSCode(5, 0);

    // Roll Angle
    private boolean rollAngleStatus;
    private boolean rollAngleSign;
    private short rollAngleEncoded;
    // True Track Angle
    private boolean trueTrackAngleStatus;
    private boolean trueTrackAngleSign;
    private short trueTrackAngleEncoded;
    // Ground Speed
    private boolean groundSpeedStatus;
    private short groundSpeedEncoded;
    // Track Angle Rate
    private boolean trackAngleRateStatus;
    private boolean trackAngleRateSign;
    private short trackAngleRateEncoded;
    // True Airspeed
    private boolean trueAirspeedStatus;
    private short trueAirspeedEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TrackAndTurn() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public TrackAndTurn(byte[] message) {
        super(message);
        BitReader b = BitReader.forBigEndian(message);

        rollAngleStatus = b.readBoolean(1);
        rollAngleSign = b.readBoolean(2);
        rollAngleEncoded = b.readShort(3, 11);
        trueTrackAngleStatus = b.readBoolean(12);
        trueTrackAngleSign = b.readBoolean(13);
        trueTrackAngleEncoded = b.readShort(14, 23);
        groundSpeedStatus = b.readBoolean(24);
        groundSpeedEncoded = b.readShort(25, 34);
        trackAngleRateStatus = b.readBoolean(35);
        trackAngleRateSign = b.readBoolean(36);
        trackAngleRateEncoded = b.readShort(37, 45);
        trueAirspeedStatus = b.readBoolean(46);
        trueAirspeedEncoded = b.readShort(47, 56);
    }

    /**
     * @return whether the roll angle is available
     */
    public boolean hasRollAngle() {
        return rollAngleStatus;
    }

    /**
     * @return the roll angle sign bit: true means left wing down
     */
    public boolean getRollAngleSign() {
        return rollAngleSign;
    }

    /**
     * The roll angle value as transmitted, without its sign. The sign and the value together are a
     * two's complement number; {@link #getRollAngle()} interprets them.
     *
     * @return the encoded roll angle value
     */
    public short getRollAngleEncoded() {
        return rollAngleEncoded;
    }

    /**
     * The roll angle in degrees: negative with the left wing down, positive with the right wing down.
     * The resolution is 45/256 degrees and the range is [-90, +90) degrees.
     *
     * @return the roll angle in degrees, or null if not available
     */
    public Float getRollAngle() {
        if (!rollAngleStatus) return null;
        return (float) (twosComplement(rollAngleSign, rollAngleEncoded, 9) * 45.0 / 256);
    }

    /**
     * @return whether the true track angle is available
     */
    public boolean hasTrueTrackAngle() {
        return trueTrackAngleStatus;
    }

    /**
     * @return the true track angle sign bit: true means west, i.e. a track between 180 and
     * 360 degrees clockwise from true north
     */
    public boolean getTrueTrackAngleSign() {
        return trueTrackAngleSign;
    }

    /**
     * The true track angle value as transmitted, without its sign. The sign and the value together
     * are a two's complement number; {@link #getTrueTrackAngle()} interprets them.
     *
     * @return the encoded true track angle value
     */
    public short getTrueTrackAngleEncoded() {
        return trueTrackAngleEncoded;
    }

    /**
     * The true track angle in degrees from true north: positive clockwise, i.e. east of north, and
     * negative counterclockwise, i.e. west of north, so that -45 degrees is a track of 315 degrees.
     * The resolution is 90/512 degrees and the range is [-180, +180) degrees.
     *
     * @return the true track angle in degrees, or null if not available
     */
    public Float getTrueTrackAngle() {
        if (!trueTrackAngleStatus) return null;
        return (float) (twosComplement(trueTrackAngleSign, trueTrackAngleEncoded, 10) * 90.0 / 512);
    }

    /**
     * @return whether the ground speed is available
     */
    public boolean hasGroundSpeed() {
        return groundSpeedStatus;
    }

    /**
     * @return the encoded ground speed
     */
    public short getGroundSpeedEncoded() {
        return groundSpeedEncoded;
    }

    /**
     * The ground speed in knots, at a resolution of 2 knots and in the range [0, 2046] knots.
     *
     * @return the ground speed in knots, or null if not available
     */
    public Integer getGroundSpeed() {
        if (!groundSpeedStatus) return null;
        return groundSpeedEncoded * 2;
    }

    /**
     * @return whether the track angle rate is available
     */
    public boolean hasTrackAngleRate() {
        return trackAngleRateStatus;
    }

    /**
     * @return the track angle rate sign bit: true means the track angle is decreasing,
     * i.e. the aircraft is turning left
     */
    public boolean getTrackAngleRateSign() {
        return trackAngleRateSign;
    }

    /**
     * The track angle rate value as transmitted, without its sign. The sign and the value together
     * are a two's complement number; {@link #getTrackAngleRate()} interprets them.
     *
     * @return the encoded track angle rate value
     */
    public short getTrackAngleRateEncoded() {
        return trackAngleRateEncoded;
    }

    /**
     * The rate of change of the true track angle in degrees per second: positive while the track
     * angle increases, i.e. turning right, and negative while it decreases, i.e. turning left. Table
     * A-2-80 only says "1 = Minus"; the direction follows from Doc 9871 Table C-1-5, which gives the
     * register's input, ARINC 429 label 335, a positive sense of clockwise.
     * The resolution is 1/32 degrees per second and the range is [-16, +16) degrees per second.
     *
     * @return the track angle rate in degrees per second, or null if not available
     */
    public Float getTrackAngleRate() {
        if (!trackAngleRateStatus) return null;
        return (float) (twosComplement(trackAngleRateSign, trackAngleRateEncoded, 9) * 8.0 / 256);
    }

    /**
     * @return whether the true airspeed is available
     */
    public boolean hasTrueAirspeed() {
        return trueAirspeedStatus;
    }

    /**
     * @return the encoded true airspeed
     */
    public short getTrueAirspeedEncoded() {
        return trueAirspeedEncoded;
    }

    /**
     * The true airspeed in knots, at a resolution of 2 knots and in the range [0, 2046] knots.
     *
     * @return the true airspeed in knots, or null if not available
     */
    public Integer getTrueAirspeed() {
        if (!trueAirspeedStatus) return null;
        return trueAirspeedEncoded * 2;
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "TrackAndTurn{" + super.toString() +
                ", rollAngleStatus=" + rollAngleStatus +
                ", rollAngleSign=" + rollAngleSign +
                ", rollAngleEncoded=" + rollAngleEncoded +
                ", trueTrackAngleStatus=" + trueTrackAngleStatus +
                ", trueTrackAngleSign=" + trueTrackAngleSign +
                ", trueTrackAngleEncoded=" + trueTrackAngleEncoded +
                ", groundSpeedStatus=" + groundSpeedStatus +
                ", groundSpeedEncoded=" + groundSpeedEncoded +
                ", trackAngleRateStatus=" + trackAngleRateStatus +
                ", trackAngleRateSign=" + trackAngleRateSign +
                ", trackAngleRateEncoded=" + trackAngleRateEncoded +
                ", trueAirspeedStatus=" + trueAirspeedStatus +
                ", trueAirspeedEncoded=" + trueAirspeedEncoded +
                '}';
    }

}
