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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R airborne position messages version 2 (DO-260B).
 */
public class AirbornePositionV2Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 6290709359481466246L;

    private boolean horizontalPositionAvailable;
    private boolean altitudeAvailable;
    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    private boolean nicSupplementA;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirbornePositionV2Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
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
        altitudeAvailable = altitudeEncoded != 0;
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * @param nicSupplementA Navigation Integrity Category (NIC) supplement from operational status message.
     *                       Otherwise worst case is assumed for containment radius limit and NIC.
     */
    public void setNICSupplementA(boolean nicSupplementA) {
        this.nicSupplementA = nicSupplementA;
    }

    /**
     * @return NIC supplement that was set before
     */
    public boolean hasNICSupplementA() {
        return nicSupplementA;
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. For the navigation accuracy category
     * (NACp) see {@link AirborneOperationalStatusV2Msg}. According to DO-260B Table 2-14.
     * <p>
     * The horizontal containment radius is also known as "horizontal protection level".
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     * If aircraft uses ADS-B version 2, set NIC supplement A from Operational Status Message
     * for better precision. Otherwise, we'll be pessimistic.
     */
    @Override
    public double getHorizontalContainmentRadiusLimit() {
        switch (getFormatTypeCode()) {
            case 0:
            case 18:
            case 22:
                return -1;
            case 9:
            case 20:
                return 7.5;
            case 10:
            case 21:
                return 25;
            case 11:
                return 185.2;
            case 12:
                return 370.4;
            case 13:
                return hasNICSupplementA() ? 1111.2 : 555.6;
            case 14:
                return 1852;
            case 15:
                return 3704;
            case 16:
                return 14816;
            case 17:
                return 37040;
            default:
                return -1;
        }
    }

    /**
     * According to DO-260B Table 2-14.
     *
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    @Override
    public byte getNIC() {
        switch (getFormatTypeCode()) {
            case 0:
            case 18:
            case 22:
                return 0;
            case 9:
            case 20:
                return 11;
            case 10:
            case 21:
                return 10;
            case 11:
                return 8;
            case 12:
                return 7;
            case 13:
                return 6;
            case 14:
                return 5;
            case 15:
                return 4;
            case 16:
                return 2;
            case 17:
                return 1;
            default:
                return 0;
        }
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
        return "AirbornePositionV2Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", altitudeAvailable=" + altitudeAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                ", nicSupplementA=" + nicSupplementA +
                '}';
    }

}
