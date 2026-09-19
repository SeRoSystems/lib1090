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
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV1;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B surface position messages (version 1), as defined in ED-102B §2.2.3.2.4 Figure 2-6.
 */
public class SurfacePositionV1Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, PositionMsgWithTime, ADSBMsg {

    private static final long serialVersionUID = 5381651494408125465L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-B surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV1Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV1Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV1Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        SurfacePosition.validateSurfacePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        movement = br.readByte(6, 12);
        headingStatus = br.readBoolean(13);
        groundTrack = br.readByte(14, 20);
        timeFlag = br.readBoolean(21);
        position = SurfacePosition.extractCPREncodedPosition(br, movement, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The horizontal containment radius limit for a caller that knows the NIC supplement.
     *
     * @param nicSupplement the NIC supplement as transmitted in the operational status message
     * @return the guaranteed upper bound on the containment radius in meters, as described by
     * {@link #getHorizontalContainmentRadiusLimit()}
     */
    public double getHorizontalContainmentRadiusLimit(boolean nicSupplement) {
        return getContainmentRadius(nicSupplement).getGuaranteedUpperBound();
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on, ED-102B §N.5.4 TABLE N-16.
     *
     * @return the containment radius, worst case where the NIC supplement is not known
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics().getContainmentRadius();
    }

    /**
     * @param nicSupplement the NIC supplement as transmitted in the operational status message
     * @return the containment radius that type code and supplement select
     */
    public ContainmentRadius getContainmentRadius(boolean nicSupplement) {
        return NavigationCharacteristicsV1.forFormatTypeCode(getFormatTypeCode(), NICSupplement.of(nicSupplement))
                .getContainmentRadius();
    }

    /**
     * Navigation integrity category for ADS-B version 1.
     *
     * @param nicSupplement the NIC supplement as transmitted in the operational status message
     * @return the NIC that type code and supplement select
     */
    public byte getNIC(boolean nicSupplement) {
        return NavigationCharacteristicsV1.forFormatTypeCode(getFormatTypeCode(), NICSupplement.of(nicSupplement)).getNIC();
    }

    @Override
    public byte getNIC() {
        return getNavigationCharacteristics().getNIC();
    }

    /**
     * Everything the format type code and the NIC supplement say about this position.
     * <p>
     * The supplement is transmitted in the operational status message rather than here, so a plain
     * instance does not know it and reports the worst row the type code allows — which is not the
     * same as assuming the supplement is clear, since at type code 13 a clear supplement is the
     * better of the two rows. {@link WithNICSupplementA} knows it and overrides this.
     *
     * @return the row this message's format type code and known supplements select
     */
    protected NavigationCharacteristicsV1 getNavigationCharacteristics() {
        return NavigationCharacteristicsV1.forFormatTypeCode(getFormatTypeCode(), NICSupplement.UNKNOWN);
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
    public boolean hasTimeFlag() {
        return timeFlag;
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
        return "SurfacePositionV1Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link SurfacePositionV1Msg} that stores the NIC supplement A bit, e.g., as
     * obtained from the corresponding operational status message, so that {@link #getNIC()} and
     * {@link #getHorizontalContainmentRadiusLimit()} can take it into account.
     */
    public static class WithNICSupplementA extends SurfacePositionV1Msg {

        private static final long serialVersionUID = 5381651494408125465L;

        private boolean nicSupplementA;

        /**
         * @param rawMessage     raw ADS-B surface position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplementA(String rawMessage, Instant timestamp, boolean nicSupplementA) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA);
        }

        /**
         * @param rawMessage     raw ADS-B surface position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplementA(byte[] rawMessage, Instant timestamp, boolean nicSupplementA) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA);
        }

        /**
         * @param squitter       extended squitter which contains this surface position msg
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @throws BadFormatException if message has wrong format
         */
        public WithNICSupplementA(ExtendedSquitter squitter, Instant timestamp, boolean nicSupplementA) throws BadFormatException {
            super(squitter, timestamp);
            this.nicSupplementA = nicSupplementA;
        }

        /**
         * protected no-arg constructor e.g. for serialization with Kryo
         **/
        protected WithNICSupplementA() {
        }

        /**
         * {@inheritDoc}
         * <p>
         * The supplement is known here, so the row it selects is reported rather than the worst the
         * type code allows.
         */
        @Override
        protected NavigationCharacteristicsV1 getNavigationCharacteristics() {
            return NavigationCharacteristicsV1.forFormatTypeCode(getFormatTypeCode(), NICSupplement.of(nicSupplementA));
        }
    }

}
