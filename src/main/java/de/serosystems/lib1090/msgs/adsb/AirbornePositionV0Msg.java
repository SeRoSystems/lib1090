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
import de.serosystems.lib1090.msgs.SingleAntennaMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("unused")
public class AirbornePositionV0Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, SingleAntennaMsg {

    private static final long serialVersionUID = 5661463389938495220L;

    private boolean horizontalPositionAvailable;
    private boolean altitudeAvailable;
    private byte surveillanceStatus;

    private boolean singleAntennaFlag;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV0Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirbornePositionV0Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
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
        singleAntennaFlag = br.readByte(8, 8) == 1;
        altitudeEncoded = br.readShort(9, 20);
        altitudeAvailable = altitudeEncoded != 0;
        timeFlag = br.readByte(21, 21) == 1;
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    @Override
    public double getHorizontalContainmentRadiusLimit() {
        return AirbornePosition.typeCodeToHCR(getFormatTypeCode());
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
    public byte getNIC() {
        return AirbornePosition.typeCodeToNIC(getFormatTypeCode());
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
                ", singleAntennaFlag=" + singleAntennaFlag +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_AIRBORN_POSITION_V0;
    }
}
