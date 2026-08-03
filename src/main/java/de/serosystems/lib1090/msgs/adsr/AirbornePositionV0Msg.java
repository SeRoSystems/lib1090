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

import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R airborne position messages version 0.
 */
public class AirbornePositionV0Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg {

    private static final long serialVersionUID = 1178987247553752100L;

    private boolean horizontalPositionAvailable;
    private boolean altitudeAvailable;
    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV0Msg() {
    }

    /**
     * @param rawMessage raw ADS-R airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirbornePositionV0Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-R airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirbornePositionV0Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV0Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        imf = br.readByte(8, 8) == 1;
        altitudeEncoded = br.readShort(9, 20);
        altitudeAvailable = altitudeEncoded != 0;
        timeFlag = br.readByte(21, 21) == 1;
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    @Override
    public double getHorizontalContainmentRadiusLimit() {
        return AirbornePosition.typeCodeToHCR(getFormatTypeCode());
    }

    /**
     * Navigation accuracy category according to DO-260B Table N-7. In ADS-R version 1+ this information is contained
     * in the operational status message. For version 0 it is derived from the format type code.
     * <p>
     * For a value in meters, use {@link #getPositionUncertainty()}.
     *
     * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
     * {@link AirborneOperationalStatusV1Msg}.
     */
    @Override
    public byte getNACp() {
        return AirbornePosition.typeCodeToNACp(getFormatTypeCode());
    }

    @Override
    public double getPositionUncertainty() {
        return AirbornePosition.typeCodeToPositionUncertainty(getFormatTypeCode());
    }

    @Override
    public byte getNIC() {
        return AirbornePosition.typeCodeToNIC(getFormatTypeCode());
    }

    @Override
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    /**
     * @return the ICAO Mode A Flag (for address type determination)
     */
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
    public boolean hasValidAltitude() {
        return altitudeAvailable;
    }

    @Override
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    @Override
    public String toString() {
        return "AirbornePositionV0Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", altitudeAvailable=" + altitudeAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_AIRBORN_POSITION_V0;
    }
}
