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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static de.serosystems.lib1090.decoding.Altitude.decode12BitAltitude;
import static de.serosystems.lib1090.decoding.Altitude.decode12BitQBit;

/**
 * Decoder for TIS-B coarse position (DO-260B, 2.2.17.3.5).
 */
public class CoarsePositionMsg extends ExtendedSquitter implements Serializable, PositionMsg {

    private static final long serialVersionUID = -8532037642870724311L;

    private boolean imf;
    private byte surveillanceStatus;
    private byte svid;
    private short altitudeEncoded;
    private boolean groundTrackStatus;
    private byte groundTrackAngle;
    private byte groundSpeed;
    CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected CoarsePositionMsg() {
    }

    /**
     * @param rawMessage raw TIS-B coarse position message as hex string
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public CoarsePositionMsg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw TIS-B coarse position message as byte array
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public CoarsePositionMsg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the TIS-B position and velocity in low resolution
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public CoarsePositionMsg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18) {
            throw new BadFormatException("TIS-B messages must have downlink format 18.");
        }

        // Table 2-13
        if (getFirstField() != 3)
            throw new BadFormatException("Coarse TIS-B messages must have CF value 3.");

        BitReader br = BitReader.forBigEndian(getMessage());

        imf = br.readByte(1, 1) == 1;
        surveillanceStatus = br.readByte(2, 3);
        svid = br.readByte(4, 7);
        altitudeEncoded = br.readShort(8, 19);
        groundTrackStatus = br.readByte(20, 20) == 1;
        groundTrackAngle = br.readByte(21, 25);
        groundSpeed = br.readByte(26, 31);

        boolean cprFormat = br.readByte(32, 32) == 1;
        short cprEncodedLat = br.readShort(33, 44);
        short cprEncodedLon = br.readShort(45, 56);

        position = CPREncodedPosition.ofAirborne(12, cprFormat, cprEncodedLat, cprEncodedLon,
                Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * @return the surveillance status
     * @see #getSurveillanceStatusDescription()
     */
    public byte getSurveillanceStatus() {
        return surveillanceStatus;
    }

    /**
     * This is a function of the surveillance status field in the position
     * message.
     *
     * @return surveillance status description as defines in DO-260B
     */
    public String getSurveillanceStatusDescription() {
        String[] desc = {
                "No condition information",
                "Permanent alert (emergency condition)",
                "Temporary alert (change in Mode A identity code other than emergency condition)",
                "SPI condition"
        };

        return desc[surveillanceStatus];
    }

    /**
     * @return ID to identify TIS-B site
     */
    public byte getServiceVolumeID() {
        return svid;
    }

    /**
     * @return ground track angle in degrees clockwise from true north
     */
    public Float getGroundTrackAngle() {
        if (!groundTrackStatus) return null;
        return groundTrackAngle * 11.25f;
    }

    /**
     * See also {@link #getMaxGroundSpeed()}.
     *
     * @return ground speed in knots (lower end of possible 32 knots window)
     */
    public Integer getMinGroundSpeed() {
        if (groundSpeed == 0) return null;
        else if (groundSpeed == 1) return 0;
        else return 16 + (groundSpeed - 2) * 32;
    }

    /**
     * See also {@link #getMinGroundSpeed()}.
     *
     * @return ground speed in knots (upper end of possible 32 knots window)
     */
    public Integer getMaxGroundSpeed() {
        if (groundSpeed == 0) return null;
        else if (groundSpeed == 1) return 16;
        else return 16 + (groundSpeed - 1) * 32;
    }

    @Override
    public boolean hasValidPosition() {
        return getFormatTypeCode() >= 9;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public boolean hasValidAltitude() {
        return getFormatTypeCode() >= 9;
    }

    @Override
    public Integer getAltitude() {
        if (!hasValidAltitude()) return null;
        return decode12BitAltitude(altitudeEncoded);
    }

    @Override
    public Position.AltitudeType getAltitudeType() {
        return Position.AltitudeType.BAROMETRIC_ALTITUDE;
    }

    /**
     * Decode Q bit for the altitude according to DO-260B 2.2.3.2.3.4.3
     *
     * @return value of the Q bit or null if message does not contain a valid altitude
     */
    public Boolean hasQBit() {
        if (!hasValidAltitude()) return null;
        return decode12BitQBit(altitudeEncoded);
    }

    @Override
    public String toString() {
        return "CoarsePositionMsg{" + super.toString() +
                ", imf=" + imf +
                ", surveillanceStatus=" + surveillanceStatus +
                ", svid=" + svid +
                ", altitudeEncoded=" + altitudeEncoded +
                ", groundTrackStatus=" + groundTrackStatus +
                ", groundTrackAngle=" + getGroundTrackAngle() +
                ", groundSpeed=" + getMinGroundSpeed() + "-" + getMaxGroundSpeed() +
                ", position=" + position +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.TISB_COARSE_POSITION;
    }
}
