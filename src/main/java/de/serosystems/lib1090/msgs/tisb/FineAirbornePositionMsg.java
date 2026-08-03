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

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static de.serosystems.lib1090.decoding.Altitude.decode12BitQBit;

/**
 * Decoder for TIS-B fine airborne position (DO-260B, 2.2.17.3.1).
 */
public class FineAirbornePositionMsg extends ExtendedSquitter implements Serializable, PositionMsg {

    private static final long serialVersionUID = 8691583253646403341L;

    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected FineAirbornePositionMsg() {
    }

    /**
     * @param rawMessage raw TIS-B fine airborne position message as hex string
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public FineAirbornePositionMsg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw TIS-B fine airborne position message as byte array
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
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
            throw new BadFormatException("TIS-B messages must have downlink format 18.");

        if (!((getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18) ||
                (getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)))
            throw new BadFormatException("This is not a TIS-B position message! Wrong format type code.");

        // Table 2-13
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

        BitReader br = BitReader.forBigEndian(getMessage());

        surveillanceStatus = br.readByte(6, 7);
        imf = br.readByte(8, 8) == 1;
        altitudeEncoded = br.readShort(9, 20);

        boolean cprFormat = br.readByte(22, 22) == 1;
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);

        position = CPREncodedPosition.ofAirborne(17, cprFormat, cprEncodedLat, cprEncodedLon,
                Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. Values according to DO-260B Table N-4.
     * <p>
     * The horizontal containment radius is also known as "horizontal protection level".
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     */
    public double getHorizontalContainmentRadiusLimit() {
        return AirbornePosition.typeCodeToHCR(getFormatTypeCode());
    }

    /**
     * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
     * in the operational status message. For version 0 it is derived from the format type code.
     * <p>
     * For a value in meters, use {@link #getPositionUncertainty()}.
     *
     * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
     * {@link AirborneOperationalStatusV1Msg}.
     */
    public byte getNACp() {
        return AirbornePosition.typeCodeToNACp(getFormatTypeCode());
    }

    /**
     * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
     * <p>
     * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
     * {@link AirborneOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-B
     * version 1 and 2.
     *
     * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
     */
    public double getPositionUncertainty() {
        return AirbornePosition.typeCodeToPositionUncertainty(getFormatTypeCode());
    }

    /**
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    public byte getNIC() {
        return AirbornePosition.typeCodeToNIC(getFormatTypeCode());
    }

    /**
     * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
     * <p>
     * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
     * {@link AirborneOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
     * version 1 and 2.
     *
     * @return the source integrity level (SIL) which indicates the probability of exceeding
     * the NIC containment radius.
     */
    public byte getSIL() {
        return AirbornePosition.typeCodeToSIL(getFormatTypeCode());
    }

    /**
     * @return the ICAO Mode A Flag (for address type determination)
     */
    public boolean getIMF() {
        return imf;
    }

    /**
     * @return the surveillance status
     * @see #getSurveillanceStatusDescription()
     */
    public byte getSurveillanceStatus() {
        return surveillanceStatus;
    }

    /**
     * This is a function of the surveillance status field in the position
     * message.
     *
     * @return surveillance status description as defines in DO-260B
     */
    public String getSurveillanceStatusDescription() {
        String[] desc = {
                "No condition information",
                "Permanent alert (emergency condition)",
                "Temporary alert (change in Mode A identity code other than emergency condition)",
                "SPI condition"
        };

        return desc[surveillanceStatus];
    }

    @Override
    public boolean hasValidPosition() {
        return getFormatTypeCode() >= 9;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public boolean hasValidAltitude() {
        return getFormatTypeCode() >= 9;
    }

    @Override
    public Integer getAltitude() {
        if (!hasValidAltitude()) return null;
        return Altitude.decode12BitAltitude(altitudeEncoded);
    }

    @Override
    public Position.AltitudeType getAltitudeType() {
        if (getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18)
            return Position.AltitudeType.BAROMETRIC_ALTITUDE;
        else if (getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)
            return Position.AltitudeType.ABOVE_WGS84_ELLIPSOID;
        else return Position.AltitudeType.UNKNOWN;
    }

    /**
     * Decode Q bit for the altitude according to DO-260B 2.2.3.2.3.4.3
     *
     * @return value of the Q bit or null if message does not contain a valid altitude
     */
    public Boolean hasQBit() {
        if (!hasValidAltitude()) return null;
        return decode12BitQBit(altitudeEncoded);
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

    @Override
    public subtype getType() {
        return subtype.TISB_FINE_AIRBORNE_POSITION;
    }
}
