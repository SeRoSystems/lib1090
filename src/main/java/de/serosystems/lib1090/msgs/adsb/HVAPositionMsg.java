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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for the position subtype of the ADS-B High Velocity and/or Altitude (HVA) message.
 */
public class HVAPositionMsg extends ExtendedSquitter implements Serializable, HVAMsg {

    private static final long serialVersionUID = 7256103984719305218L;

    private short hvaGeometricAltitudeEncoded; // raw encoded HVA geometric altitude field
    private int hvaLatitudeEncoded; // raw encoded HVA latitude field
    private int hvaLongitudeEncoded; // raw encoded HVA longitude field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected HVAPositionMsg() {
    }

    /**
     * @param rawMessage raw ADS-B HVA message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public HVAPositionMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B HVA message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public HVAPositionMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this HVA position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has a subtype other than 0 (position)
     */
    public HVAPositionMsg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 25)
            throw new BadFormatException("HVA messages must have typecode 25.");

        BitReader br = BitReader.forBigEndian(getMessage());

        byte messageSubtype = br.readByte(6, 7);
        if (messageSubtype != 0)
            throw new UnspecifiedFormatError("HVA position message must have subtype 0, got " + messageSubtype + ".");

        hvaGeometricAltitudeEncoded = br.readShort(8, 19);
        hvaLatitudeEncoded = br.readInt(20, 37);
        hvaLongitudeEncoded = br.readInt(38, 56);
    }

    @Override
    public byte getMessageSubtype() {
        return 0;
    }

    /**
     * @return the raw encoded HVA geometric altitude field
     */
    public short getHVAGeometricAltitudeEncoded() {
        return hvaGeometricAltitudeEncoded;
    }

    /**
     * @return whether the HVA geometric altitude field is available
     */
    public boolean hasHVAGeometricAltitude() {
        return hvaGeometricAltitudeEncoded != 0;
    }

    /**
     * Decode the HVA geometric altitude field into Height Above Ellipsoid (HAE) in feet.
     *
     * @return the geometric altitude (HAE) in feet, {@code 1051500} meaning "equal to or greater than
     * 1051500 ft", or {@code null} if unavailable
     */
    public Integer getHVAGeometricAltitude() {
        if (!hasHVAGeometricAltitude()) return null;

        int n = hvaGeometricAltitudeEncoded;
        if (n <= 1181) return -1000 + (n - 1) * 25;
        if (n <= 2161) return 28500 + (n - 1181) * 15;
        if (n <= 2833) return 43200 + (n - 2161) * 25;
        if (n <= 3133) return 60000 + (n - 2833) * 100;
        if (n <= 4094) return 90000 + (n - 3133) * 1000;
        return 1051500;
    }

    /**
     * @return the raw encoded HVA latitude field, including the sign bit
     */
    public int getHVALatitudeEncoded() {
        return hvaLatitudeEncoded;
    }

    /**
     * @return whether HVA latitude sign indicates a southern latitude
     */
    public boolean isHVALatitudeNegative() {
        return (hvaLatitudeEncoded & 0x20000) != 0;
    }

    /**
     * Decode the HVA latitude field into WGS-84 degrees.
     * <p>
     * Only valid if {@link HVAVelocityMsg#hasPIC()} is true for the corresponding velocity message.
     *
     * @return the latitude in WGS-84 degrees [-90°,90°]
     */
    public double getHVALatitude() {
        int n = hvaLatitudeEncoded & 0x1FFFF;
        double latitude = 90. * n / ((1 << 17) - 1);
        return isHVALatitudeNegative() ? -latitude : latitude;
    }

    /**
     * @return the raw encoded HVA longitude field, including the sign bit
     */
    public int getHVALongitudeEncoded() {
        return hvaLongitudeEncoded;
    }

    /**
     * @return whether HVA longitude sign indicates a western longitude
     */
    public boolean isHVALongitudeNegative() {
        return (hvaLongitudeEncoded & 0x40000) != 0;
    }

    /**
     * Decode the HVA longitude field into WGS-84 degrees.
     * <p>
     * Only valid if {@link HVAVelocityMsg#hasPIC()} is true for the corresponding velocity message.
     *
     * @return the longitude in WGS-84 degrees [-180°,180°]
     */
    public double getHVALongitude() {
        int n = hvaLongitudeEncoded & 0x3FFFF;
        double longitude = 180. * n / ((1 << 18) - 1);
        return isHVALongitudeNegative() ? -longitude : longitude;
    }

    @Override
    public String toString() {
        return "HVAPositionMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", hvaGeometricAltitudeEncoded=" + hvaGeometricAltitudeEncoded +
                ", hvaLatitudeEncoded=" + hvaLatitudeEncoded +
                ", hvaLongitudeEncoded=" + hvaLongitudeEncoded +
                '}';
    }

}
