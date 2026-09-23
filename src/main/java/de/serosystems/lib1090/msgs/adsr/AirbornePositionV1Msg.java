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
import de.serosystems.lib1090.decoding.quality.ContainmentRadius;
import de.serosystems.lib1090.decoding.quality.NICSupplements;
import de.serosystems.lib1090.decoding.quality.NavigationCharacteristics;
import de.serosystems.lib1090.decoding.quality.NavigationCharacteristicsV1;
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
 * Decoder for ADS-R airborne position messages (version 1), as defined in DO-260A.
 */
public class AirbornePositionV1Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 2487388116642019598L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-R airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV1Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-R airborne position message as byte array
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
        imf = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Version 1 reads supplement A only, the single bit it defines. It is transmitted in the
     * operational status message rather than here, so a plain instance knows none.
     */
    @Override
    public NavigationCharacteristics getNavigationCharacteristics(NICSupplements nicSupplements) {
        return NavigationCharacteristicsV1.forFormatTypeCode(getFormatTypeCode(), nicSupplements);
    }

    @Override
    public byte getNICEncoded() {
        return getNavigationCharacteristics(getKnownSupplements()).getNICEncoded();
    }

    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics(getKnownSupplements()).getContainmentRadius();
    }

    /**
     * What this message knows of its target's NIC supplements on its own, which is what the
     * no-argument accessors report with. A supplement it does not carry stays unknown, and the
     * tables answer that with the poorest row it allows.
     *
     * @return the supplements this message knows
     */
    protected NICSupplements getKnownSupplements() {
        return NICSupplements.none();
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
        return "AirbornePositionV1Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link AirbornePositionV1Msg} that carries what is known of its target's NIC supplements,
     * as accumulated from the messages that transmit them, so that {@link #getNICEncoded()} and
     * {@link #getContainmentRadius()} report the row those supplements select rather than the worst
     * the format type code allows.
     */
    public static class WithNICSupplements extends AirbornePositionV1Msg {

        private static final long serialVersionUID = 3719285034411628907L;

        private NICSupplements nicSupplements;

        /**
         * @param rawMessage     raw ADS-R airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplements what is known of the target's NIC supplements
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, NICSupplements nicSupplements) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplements);
        }

        /**
         * @param rawMessage     raw ADS-R airborne position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplements what is known of the target's NIC supplements
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(byte[] rawMessage, Instant timestamp, NICSupplements nicSupplements) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplements);
        }

        /**
         * @param squitter       extended squitter containing the airborne position msg
         * @param timestamp      timestamp for this position message
         * @param nicSupplements what is known of the target's NIC supplements
         * @throws BadFormatException if message has wrong format
         */
        public WithNICSupplements(ExtendedSquitter squitter, Instant timestamp, NICSupplements nicSupplements) throws BadFormatException {
            super(squitter, timestamp);
            this.nicSupplements = nicSupplements;
        }

        /**
         * protected no-arg constructor e.g. for serialization with Kryo
         **/
        protected WithNICSupplements() {
        }

        /**
         * {@inheritDoc}
         * <p>
         * These are the supplements this variant was given. A supplement the message carries itself
         * still overrides them, since it describes this very position.
         */
        @Override
        protected NICSupplements getKnownSupplements() {
            return nicSupplements;
        }
    }
}
