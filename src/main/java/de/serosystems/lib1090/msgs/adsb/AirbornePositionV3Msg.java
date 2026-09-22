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
import de.serosystems.lib1090.decoding.NICSupplements;
import de.serosystems.lib1090.decoding.NavigationCharacteristics;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV3;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.NICSupplementBMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B airborne position messages (version 3), as defined in ED-102B §2.2.3.2.3.1 TABLE 2-11.
 */
public class AirbornePositionV3Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, NICSupplementBMsg, ADSBMsg {

    private static final long serialVersionUID = -7146583920415678231L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;
    private boolean nicSupplementB;
    private short altitudeEncoded;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV3Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV3Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV3Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV3Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        nicSupplementB = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    @Override
    public boolean getNICSupplementB() {
        return nicSupplementB;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supplement B is ME bit 8 of this message, so it is read from here and overrides whatever is
     * given; the others are transmitted elsewhere and a plain instance knows none of them.
     */
    @Override
    public NavigationCharacteristics getNavigationCharacteristics(NICSupplements nicSupplements) {
        return NavigationCharacteristicsV3.forAirborneFormatTypeCode(
                getFormatTypeCode(), nicSupplements.withB(nicSupplementB));
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
        return "AirbornePositionV3Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", nicSupplementB=" + nicSupplementB +
                ", altitudeEncoded=" + altitudeEncoded +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link AirbornePositionV3Msg} that carries what is known of its target's NIC supplements,
     * as accumulated from the messages that transmit them, so that {@link #getNICEncoded()} and
     * {@link #getContainmentRadius()} report the row those supplements select rather than the worst
     * the format type code allows.
     */
    public static class WithNICSupplements extends AirbornePositionV3Msg {

        private static final long serialVersionUID = 4198736025917482073L;

        private NICSupplements nicSupplements;

        /**
         * @param rawMessage     raw ADS-B airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplements what is known of the target's NIC supplements
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, NICSupplements nicSupplements) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplements);
        }

        /**
         * @param rawMessage     raw ADS-B airborne position message as byte array
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
