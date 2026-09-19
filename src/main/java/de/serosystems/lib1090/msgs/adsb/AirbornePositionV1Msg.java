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
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV1;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;
import de.serosystems.lib1090.msgs.squitter.SingleAntennaMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B airborne position messages (version 1), as defined in ED-102B §2.2.3.2.3.1 TABLE 2-11.
 */
public class AirbornePositionV1Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, SingleAntennaMsg, ADSBMsg {

    private static final long serialVersionUID = 7082589986335644703L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;
    private boolean singleAntennaFlag;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV1Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV1Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV1Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        singleAntennaFlag = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
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
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    @Override
    public boolean hasSingleAntenna() {
        return singleAntennaFlag;
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
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    @Override
    public String toString() {
        return "AirbornePositionV1Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", singleAntennaFlag=" + singleAntennaFlag +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link AirbornePositionV1Msg} that stores the NIC supplement A bit, e.g., as
     * obtained from the corresponding operational status message, so that {@link #getNIC()} and
     * {@link #getHorizontalContainmentRadiusLimit()} can take it into account.
     */
    public static class WithNICSupplementA extends AirbornePositionV1Msg {

        private static final long serialVersionUID = 7082589986335644703L;

        private boolean nicSupplementA;

        /**
         * @param rawMessage     raw ADS-B airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplementA(String rawMessage, Instant timestamp, boolean nicSupplementA) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA);
        }

        /**
         * @param rawMessage     raw ADS-B airborne position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplementA(byte[] rawMessage, Instant timestamp, boolean nicSupplementA) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA);
        }

        /**
         * @param squitter       extended squitter containing the airborne position msg
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
