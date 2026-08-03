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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class SurfacePositionV3Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg {

    private static final long serialVersionUID = -4899716425365685001L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV3Msg() {
    }

    /**
     * @param rawMessage raw ADS-B surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public SurfacePositionV3Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public SurfacePositionV3Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV3Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        SurfacePosition.validateSurfacePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        movement = br.readByte(6, 12);
        headingStatus = br.readByte(13, 13) == 1;
        groundTrack = br.readByte(14, 20);
        position = SurfacePosition.extractCPREncodedPosition(br, movement, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position in ADS-B version 2.
     */
    public double getHorizontalContainmentRadiusLimit(boolean nicSupplementA, boolean nicSupplementC) {
        return SurfacePosition.decodeHCR(getFormatTypeCode(), nicSupplementA, nicSupplementC);
    }

    @Override
    public double getHorizontalContainmentRadiusLimit() {
        return getHorizontalContainmentRadiusLimit(false, false);
    }

    /**
     * Navigation integrity category for ADS-B version 2.
     */
    public byte getNIC(boolean nicSupplementA, boolean nicSupplementC) {
        return SurfacePosition.decodeNIC(getFormatTypeCode(), nicSupplementA, nicSupplementC);
    }

    @Override
    public byte getNIC() {
        return getNIC(false, false);
    }

    @Override
    public byte getNACp() {
        return SurfacePosition.decodeNIC(getFormatTypeCode());
    }

    @Override
    public double getPositionUncertainty() {
        return SurfacePosition.decodeEPU(getFormatTypeCode());
    }

    @Override
    public byte getMovementEncoded() {
        return movement;
    }

    @Override
    public byte getHeadingEncoded() {
        return groundTrack;
    }

    @Override
    public boolean hasValidHeading() {
        return headingStatus;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public boolean hasValidPosition() {
        return horizontalPositionAvailable;
    }

    @Override
    public String toString() {
        return "SurfacePositionV3Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link SurfacePositionV3Msg} that stores the NIC supplement A and C bits, e.g., as
     * obtained from the corresponding operational status message, so that {@link #getNIC()} and
     * {@link #getHorizontalContainmentRadiusLimit()} can take them into account.
     */
    public static class WithNICSupplements extends SurfacePositionV3Msg {

        private static final long serialVersionUID = 6082114735940772881L;

        private final boolean nicSupplementA;
        private final boolean nicSupplementC;

        /**
         * @param rawMessage     raw ADS-B surface position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementC NIC supplement C bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, boolean nicSupplementA, boolean nicSupplementC) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementC);
        }

        /**
         * @param rawMessage     raw ADS-B surface position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementC NIC supplement C bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
         */
        public WithNICSupplements(byte[] rawMessage, Instant timestamp, boolean nicSupplementA, boolean nicSupplementC) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementC);
        }

        /**
         * @param squitter       extended squitter which contains this surface position msg
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementC NIC supplement C bit for this aircraft
         * @throws BadFormatException if message has wrong format
         */
        public WithNICSupplements(ExtendedSquitter squitter, Instant timestamp, boolean nicSupplementA, boolean nicSupplementC) throws BadFormatException {
            super(squitter, timestamp);
            this.nicSupplementA = nicSupplementA;
            this.nicSupplementC = nicSupplementC;
        }

        @Override
        public double getHorizontalContainmentRadiusLimit() {
            return getHorizontalContainmentRadiusLimit(nicSupplementA, nicSupplementC);
        }

        @Override
        public byte getNIC() {
            return getNIC(nicSupplementA, nicSupplementC);
        }
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_SURFACE_POSITION_V3;
    }
}
