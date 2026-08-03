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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class AirbornePositionV3Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg {

    private static final long serialVersionUID = -7146583920415678231L;

    private boolean horizontalPositionAvailable;
    private boolean altitudeAvailable;
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
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirbornePositionV3Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
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
        nicSupplementB = br.readByte(8, 8) == 1;
        altitudeEncoded = br.readShort(9, 20);
        altitudeAvailable = altitudeEncoded != 0;
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    public boolean hasNICSupplementB() {
        return nicSupplementB;
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position in ADS-B version 3.
     *
     * @param nicSupplementA NIC supplement A bit for this aircraft
     * @param nicSupplementD NIC supplement D (2-bit value) for this aircraft, used for format type
     *                       codes 20-22 (geometric height)
     */
    public double getHorizontalContainmentRadiusLimit(boolean nicSupplementA, byte nicSupplementD) {
        byte formatTypeCode = getFormatTypeCode();
        if (formatTypeCode >= 20 && formatTypeCode <= 22)
            return AirbornePosition.decodeHCR(formatTypeCode, nicSupplementD);
        else
            return AirbornePosition.decodeHCR(formatTypeCode, nicSupplementA, nicSupplementB);
    }

    @Override
    public double getHorizontalContainmentRadiusLimit() {
        return getHorizontalContainmentRadiusLimit(false, (byte) 0);
    }

    /**
     * Navigation integrity category for ADS-B version 3.
     *
     * @param nicSupplementA NIC supplement A bit for this aircraft
     * @param nicSupplementD NIC supplement D (2-bit value) for this aircraft, used for format type
     *                       codes 20-22 (geometric height)
     */
    public byte getNIC(boolean nicSupplementA, byte nicSupplementD) {
        byte formatTypeCode = getFormatTypeCode();
        if (formatTypeCode >= 20 && formatTypeCode <= 22)
            return AirbornePosition.decodeNIC(formatTypeCode, nicSupplementD);
        else
            return AirbornePosition.decodeNIC(formatTypeCode, nicSupplementA, nicSupplementB);
    }

    @Override
    public byte getNIC() {
        return getNIC(false, (byte) 0);
    }

    @Override
    public byte getNACp() {
        return AirbornePosition.typeCodeToNACp(getFormatTypeCode());
    }

    @Override
    public double getPositionUncertainty() {
        return AirbornePosition.typeCodeToPositionUncertainty(getFormatTypeCode());
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
    public boolean hasValidAltitude() {
        return altitudeAvailable;
    }

    @Override
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    @Override
    public String toString() {
        return "AirbornePositionV3Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", altitudeAvailable=" + altitudeAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", nicSupplementB=" + nicSupplementB +
                ", altitudeEncoded=" + altitudeEncoded +
                ", position=" + position +
                '}';
    }

    /**
     * Variant of {@link AirbornePositionV3Msg} that stores the NIC supplement A bit and NIC
     * supplement D value, e.g., as obtained from the corresponding operational status message, so
     * that {@link #getNIC()} and {@link #getHorizontalContainmentRadiusLimit()} can take them into
     * account.
     */
    public static class WithNICSupplements extends AirbornePositionV3Msg {

        private static final long serialVersionUID = 4198736025917482073L;

        private final boolean nicSupplementA;
        private final byte nicSupplementD;

        /**
         * @param rawMessage     raw ADS-B airborne position message as hex string
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementD NIC supplement D (2-bit value) for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
         */
        public WithNICSupplements(String rawMessage, Instant timestamp, boolean nicSupplementA, byte nicSupplementD) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementD);
        }

        /**
         * @param rawMessage     raw ADS-B airborne position message as byte array
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementD NIC supplement D (2-bit value) for this aircraft
         * @throws BadFormatException     if message has wrong format
         * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
         */
        public WithNICSupplements(byte[] rawMessage, Instant timestamp, boolean nicSupplementA, byte nicSupplementD) throws BadFormatException, UnspecifiedFormatError {
            this(new ExtendedSquitter(rawMessage), timestamp, nicSupplementA, nicSupplementD);
        }

        /**
         * @param squitter       extended squitter containing the airborne position msg
         * @param timestamp      timestamp for this position message
         * @param nicSupplementA NIC supplement A bit for this aircraft
         * @param nicSupplementD NIC supplement D (2-bit value) for this aircraft
         * @throws BadFormatException if message has wrong format
         */
        public WithNICSupplements(ExtendedSquitter squitter, Instant timestamp, boolean nicSupplementA, byte nicSupplementD) throws BadFormatException {
            super(squitter, timestamp);
            this.nicSupplementA = nicSupplementA;
            this.nicSupplementD = nicSupplementD;
        }

        @Override
        public double getHorizontalContainmentRadiusLimit() {
            return getHorizontalContainmentRadiusLimit(nicSupplementA, nicSupplementD);
        }

        @Override
        public byte getNIC() {
            return getNIC(nicSupplementA, nicSupplementD);
        }
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_AIRBORN_POSITION_V3;
    }
}
