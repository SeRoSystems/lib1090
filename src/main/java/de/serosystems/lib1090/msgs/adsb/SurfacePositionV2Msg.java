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
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV2;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B surface position messages (version 2), as defined in ED-102B §2.2.3.2.4 Figure 2-6.
 */
public class SurfacePositionV2Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, PositionMsgWithTime, ADSBMsg {

    private static final long serialVersionUID = 5656362138678398539L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-B surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV2Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV2Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV2Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
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
     * The position error, i.e., 95% accuracy for the horizontal position in ADS-B version 2, for a
     * caller that knows both NIC supplements.
     *
     * @param nicSupplementA NIC supplement A from the operational status message
     * @param nicSupplementC NIC supplement C from the operational status message
     * @return the guaranteed upper bound on the containment radius in meters, as described by
     * {@link #getHorizontalContainmentRadiusLimit()}
     */
    public double getHorizontalContainmentRadiusLimit(boolean nicSupplementA, boolean nicSupplementC) {
        return getContainmentRadius(nicSupplementA, nicSupplementC).getGuaranteedUpperBound();
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on.
     * <p>
     * Both surface supplements travel in the operational status message, so a plain instance knows
     * neither and reports the poorest row the type code allows.
     *
     * @return the containment radius
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics().getContainmentRadius();
    }

    /**
     * @param nicSupplementA NIC supplement A from the operational status message
     * @param nicSupplementC NIC supplement C from the operational status message
     * @return the containment radius that type code and supplements select
     */
    public ContainmentRadius getContainmentRadius(boolean nicSupplementA, boolean nicSupplementC) {
        return characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementC))
                .getContainmentRadius();
    }

    /**
     * Navigation integrity category for ADS-B version 2.
     *
     * @param nicSupplementA NIC supplement A from the operational status message
     * @param nicSupplementC NIC supplement C from the operational status message
     * @return the NIC that type code and supplements select
     */
    public byte getNIC(boolean nicSupplementA, boolean nicSupplementC) {
        return characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementC)).getNIC();
    }

    @Override
    public byte getNIC() {
        return getNavigationCharacteristics().getNIC();
    }

    /**
     * Everything the format type code and the NIC supplements say about this position.
     * <p>
     * Both supplements are transmitted in the operational status message rather than here, so a plain
     * instance knows neither. {@link WithNICSupplements} knows them and overrides this.
     *
     * @return the row this message's format type code and known supplements select
     */
    protected NavigationCharacteristicsV2 getNavigationCharacteristics() {
        return characteristics(NICSupplement.UNKNOWN, NICSupplement.UNKNOWN);
    }

    protected NavigationCharacteristicsV2 characteristics(NICSupplement nicSupplementA,
                                                        NICSupplement nicSupplementC) {
        return NavigationCharacteristicsV2.forSurfaceFormatTypeCode(
                getFormatTypeCode(), nicSupplementA, nicSupplementC);
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
        return "SurfacePositionV2Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link SurfacePositionV2Msg} that stores the NIC supplement A and C bits, e.g., as
     * obtained from the corresponding operational status message, so that {@link #getNIC()} and
     * {@link #getHorizontalContainmentRadiusLimit()} can take them into account.
     */
    public static class WithNICSupplements extends SurfacePositionV2Msg {

        private static final long serialVersionUID = 5656362138678398539L;

        private boolean nicSupplementA;
        private boolean nicSupplementC;

        /**
         * @param rawMessage     raw ADS-B surface position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementC NIC supplement C bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
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
         * @throws UnspecifiedFormatError if message format is not further specified
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

        /**
         * protected no-arg constructor e.g. for serialization with Kryo
         **/
        protected WithNICSupplements() {
        }

        /**
         * {@inheritDoc}
         * <p>
         * Both supplements are known here, so the row they select is reported.
         */
        @Override
        protected NavigationCharacteristicsV2 getNavigationCharacteristics() {
            return this.characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementC));
        }
    }

}
