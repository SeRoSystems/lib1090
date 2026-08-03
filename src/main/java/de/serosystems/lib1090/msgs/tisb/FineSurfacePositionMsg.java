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
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static de.serosystems.lib1090.decoding.SurfacePosition.*;

/**
 * Decoder for TIS-B fine surface position (DO-260B, 2.2.17.3.2).
 */
public class FineSurfacePositionMsg extends ExtendedSquitter implements Serializable, PositionMsg {

    private static final long serialVersionUID = 8325609209771059717L;

    private byte movement;
    private boolean headingStatus; // is heading valid?
    private byte groundTrack;
    private boolean imf;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected FineSurfacePositionMsg() {
    }

    /**
     * @param rawMessage raw TIS-B fine surface position message as hex string
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public FineSurfacePositionMsg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw TIS-B fine surface position message as byte array
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public FineSurfacePositionMsg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the surface position msg in high resolution
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public FineSurfacePositionMsg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18) {
            throw new BadFormatException("TIS-B messages must have downlink format 18.");
        }

        if (getFormatTypeCode() < 5 || getFormatTypeCode() > 8)
            throw new BadFormatException("Invalid format type code (" + getFormatTypeCode() + ") for surface positions.");

        // Table 2-13
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

        BitReader br = BitReader.forBigEndian(getMessage());

        movement = br.readByte(6, 12);
        headingStatus = br.readByte(13, 13) == 1;
        groundTrack = br.readByte(14, 20);

        imf = br.readByte(21, 21) == 1;
        boolean cprFormat = br.readByte(22, 22) == 1;
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);

        boolean highGroundSpeed = movement == 0 || movement > 49;
        position = CPREncodedPosition.ofSurface(17, cprFormat, highGroundSpeed, cprEncodedLat, cprEncodedLon,
                Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. Values according to DO-260B Table N-4.
     * <p>
     * The horizontal containment radius is also known as "horizontal protection level".
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     */
    public double getHorizontalContainmentRadiusLimit() {
        return decodeHCR(getFormatTypeCode());

    }

    /**
     * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
     * in the operational status message. For version 0 it is derived from the format type code.
     * <p>
     * For a value in meters, use {@link #getPositionUncertainty()}.
     *
     * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
     * {@link AirborneOperationalStatusV1Msg}.
     */
    public byte getNACp() {
        return this.getNIC();
    }

    /**
     * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
     * <p>
     * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to those of {@link SurfaceOperationalStatusV1Msg}'s and
     * {@link SurfaceOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-B
     * version 1 and 2.
     *
     * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
     */
    public double getPositionUncertainty() {
        return decodeEPU(getFormatTypeCode());
    }

    /**
     * @return Navigation integrity category. A NIC of 0 means "unknown". Values according to DO-260B Table N-4.
     */
    public byte getNIC() {
        return SurfacePosition.decodeNIC(getFormatTypeCode());
    }

    /**
     * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
     * <p>
     * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to those of {@link SurfaceOperationalStatusV1Msg}'s and
     * {@link SurfaceOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
     * version 1 and 2.
     *
     * @return the source integrity level (SIL) which indicates the probability of exceeding
     * the NIC containment radius.
     */
    public byte getSIL() {
        return (byte) (getFormatTypeCode() == 0 ? 0 : 2);
    }

    /**
     * @return whether ground speed information is available
     */
    public boolean hasGroundSpeed() {
        return movement >= 1 && movement <= 124;
    }

    /**
     * @return speed in knots or null if ground speed is not available. The latter can also be checked with
     * {@link #hasGroundSpeed()}.
     */
    public Double getGroundSpeed() {
        return groundSpeed(movement);
    }

    /**
     * @return speed resolution (accuracy) in knots or null if ground speed is not available. The latter can also be
     * checked with {@link #hasGroundSpeed()}.
     */
    public Double getGroundSpeedResolution() {
        return groundSpeedResolution(movement);
    }

    /**
     * @return whether valid heading information is available
     */
    public boolean hasValidHeading() {
        return headingStatus;
    }

    /**
     * @return heading in decimal degrees ([0, 360]). 0° = geographic north. Returns null if heading is not available.
     * This can also be checked using {@link #hasValidHeading()}
     */
    public Double getHeading() {
        if (!headingStatus) return null;

        return groundTrack * 360D / 128D;
    }

    /**
     * @return the ICAO Mode A Flag (for address type determination)
     */
    public boolean getIMF() {
        return imf;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public boolean hasValidPosition() {
        return true;
    }

    @Override
    public boolean hasValidAltitude() {
        return true;
    }

    @Override
    public Integer getAltitude() {
        return 0;
    }

    @Override
    public Position.AltitudeType getAltitudeType() {
        return Position.AltitudeType.ABOVE_GROUND_LEVEL;
    }

    @Override
    public String toString() {
        return "FineSurfacePositionMsg{" + super.toString() +
                "movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", imf=" + imf +
                ", position=" + position +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.TISB_FINE_SURFACE_POSITION;
    }
}
