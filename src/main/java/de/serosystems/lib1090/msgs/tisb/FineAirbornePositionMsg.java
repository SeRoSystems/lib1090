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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
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

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for TIS-B fine airborne position, as defined in ED-102B §2.2.17.3.1 Figure 2-52.
 */
public class FineAirbornePositionMsg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, IMFMsg, TISBMsg {

    private static final long serialVersionUID = -5506126020860066506L;

    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected FineAirbornePositionMsg() {
    }

    /**
     * @param rawMessage raw TIS-B fine airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public FineAirbornePositionMsg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw TIS-B fine airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public FineAirbornePositionMsg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg in high resolution
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public FineAirbornePositionMsg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18");

        // ED-102B §2.2.17.2 TABLE 2-184
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("TIS-B messages must have CF value 2 or 5");

        if (!((getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18) ||
                (getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)))
            throw new BadFormatException("Wrong format type code for TIS-B Fine Airborne Position message");

        BitReader br = BitReader.forBigEndian(getMessage());

        surveillanceStatus = br.readByte(6, 7);
        imf = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);

        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);

        position = CPREncodedPosition.ofAirborne(17, cprFormat, cprEncodedLat, cprEncodedLon,
                Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * {@inheritDoc}
     * <p>
     * ED-102B has the ground station choose this message's TYPE Code in line with §N.3.2.2
     * TABLE N-16, the version 1 mapping, so the code is read against that table together with NIC
     * supplement A — which for TIS-B arrives in the velocity message, ME bit 47, rather than in an
     * operational status message.
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
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    @Override
    public boolean hasValidPosition() {
        return getFormatTypeCode() != 0;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "FineAirbornePositionMsg{" + super.toString() +
                "surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", position=" + position +
                '}';
    }

    /**
     * The same message, told what the decoder knows of the target's NIC supplements.
     * <p>
     * TIS-B carries supplement A in the velocity message rather than in an operational status
     * message, so a position message on its own cannot resolve its own integrity category. A
     * receiver that has seen a velocity message for this target passes what it learned here; one
     * that has not leaves the supplement unknown, and the table answers with the poorest row the
     * type code allows.
     */
    public static class WithNICSupplements extends FineAirbornePositionMsg {

        private static final long serialVersionUID = 8148290120920503961L;

        private NICSupplements nicSupplements;

        /**
         * @param rawMessage     raw TIS-B airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplements what is known of the target's NIC supplements
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message format is not further specified
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, NICSupplements nicSupplements) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplements);
        }

        /**
         * @param rawMessage     raw TIS-B airborne position message as byte array
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
         */
        @Override
        protected NICSupplements getKnownSupplements() {
            return nicSupplements;
        }
    }
}
