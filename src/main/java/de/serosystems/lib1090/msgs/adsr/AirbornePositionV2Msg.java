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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV2;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R airborne position messages (version 2), as defined in ED-102A.
 */
public class AirbornePositionV2Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 6290709359481466246L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV2Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV2Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV2Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        imf = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on, ED-102A TABLE 2-14.
     *
     * @return the containment radius, worst case for whichever supplements are not known
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics().getContainmentRadius();
    }

    /**
     * @param nicSupplementA NIC supplement A bit for this aircraft
     * @param nicSupplementB NIC supplement B bit for this aircraft
     * @return the containment radius that type code and supplements select
     */
    public ContainmentRadius getContainmentRadius(boolean nicSupplementA, boolean nicSupplementB) {
        return characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementB))
                .getContainmentRadius();
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position, for a caller that knows the
     * NIC supplements. ADS-R carries both in the operational status message.
     *
     * @param nicSupplementA NIC supplement A bit for this aircraft
     * @param nicSupplementB NIC supplement B bit for this aircraft
     * @return the guaranteed upper bound on the containment radius in meters, as described by
     * {@link #getHorizontalContainmentRadiusLimit()}
     */
    public double getHorizontalContainmentRadiusLimit(boolean nicSupplementA, boolean nicSupplementB) {
        return getContainmentRadius(nicSupplementA, nicSupplementB).getGuaranteedUpperBound();
    }

    /**
     * Navigation integrity category for ADS-R version 2.
     *
     * @param nicSupplementA NIC supplement A bit for this aircraft
     * @param nicSupplementB NIC supplement B bit for this aircraft
     * @return the NIC that type code and supplements select
     */
    public byte getNIC(boolean nicSupplementA, boolean nicSupplementB) {
        return characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementB)).getNIC();
    }

    @Override
    public byte getNIC() {
        return getNavigationCharacteristics().getNIC();
    }

    /**
     * Everything the format type code and the NIC supplements say about this position, ED-102A
     * TABLE 2-14.
     * <p>
     * ADS-R carries both supplements in the operational status message, so a plain instance knows
     * neither and reports the poorest row the type code allows. {@link WithNICSupplements} knows them
     * and overrides this.
     *
     * @return the row this message's format type code and known supplements select
     */
    protected NavigationCharacteristicsV2 getNavigationCharacteristics() {
        return characteristics(NICSupplement.UNKNOWN, NICSupplement.UNKNOWN);
    }

    protected NavigationCharacteristicsV2 characteristics(NICSupplement nicSupplementA,
                                                          NICSupplement nicSupplementB) {
        return NavigationCharacteristicsV2.forAirborneFormatTypeCode(
                getFormatTypeCode(), nicSupplementA, nicSupplementB);
    }

    @Override
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    @Override
    public boolean getIMF() {
        return imf;
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
        return "AirbornePositionV2Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link AirbornePositionV2Msg} that stores the NIC supplement A and B bits, e.g., as obtained from
     * the corresponding operational status message, so that {@link #getNIC()} and
     * {@link #getHorizontalContainmentRadiusLimit()} can take them into account.
     */
    public static class WithNICSupplements extends AirbornePositionV2Msg {

        private static final long serialVersionUID = 8207454912036617409L;

        private boolean nicSupplementA;
        private boolean nicSupplementB;

        /**
         * @param rawMessage     raw ADS-R airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementB NIC supplement B bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, boolean nicSupplementA, boolean nicSupplementB) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementB);
        }

        /**
         * @param rawMessage     raw ADS-R airborne position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementB NIC supplement B bit for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(byte[] rawMessage, Instant timestamp, boolean nicSupplementA, boolean nicSupplementB) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementB);
        }

        /**
         * @param squitter       extended squitter containing the airborne position msg
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementB NIC supplement B bit for this aircraft
         * @throws BadFormatException if message has wrong format
         */
        public WithNICSupplements(ExtendedSquitter squitter, Instant timestamp, boolean nicSupplementA, boolean nicSupplementB) throws BadFormatException {
            super(squitter, timestamp);
            this.nicSupplementA = nicSupplementA;
            this.nicSupplementB = nicSupplementB;
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
            return this.characteristics(NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementB));
        }
    }
}
