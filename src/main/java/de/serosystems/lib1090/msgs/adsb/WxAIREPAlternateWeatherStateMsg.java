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
import de.serosystems.lib1090.msgs.squitter.WxAIREPWeatherMsg;

import java.io.Serializable;

/**
 * Decoder for the alternate weather state subtype (2) of the ADS-B Wx AIREP message, introduced in ADS-B version 3.
 */
public class WxAIREPAlternateWeatherStateMsg extends ExtendedSquitter implements Serializable, WxAIREPWeatherMsg, ADSBMsg {

    private static final long serialVersionUID = 6791345208317645902L;

    private byte icingStatusEncoded; // raw encoded icing status field
    private short rollAngleEncoded; // raw encoded roll angle field
    private boolean headingType;
    private short headingEncoded; // raw encoded heading field
    private boolean airTemperatureType;
    private short airTemperatureEncoded; // raw encoded air temperature field
    private boolean airspeedType;
    private short airspeedEncoded; // raw encoded airspeed field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected WxAIREPAlternateWeatherStateMsg() {
    }

    /**
     * @param rawMessage raw ADS-B Wx AIREP message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.3.2.7.6.5 (p. 147) Figure 2-19 (p. 136)
     */
    public WxAIREPAlternateWeatherStateMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B Wx AIREP message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.3.2.7.6.5 (p. 147) Figure 2-19 (p. 136)
     */
    public WxAIREPAlternateWeatherStateMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Wx AIREP alternate weather state message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has a subtype other than 2 (alternate weather state), ED-102B §2.2.3.2.7.6.5 (p. 147) Figure 2-19 (p. 136)
     */
    public WxAIREPAlternateWeatherStateMsg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 26)
            throw new BadFormatException("Wx AIREP messages must have typecode 26.");

        BitReader br = BitReader.forBigEndian(getMessage());

        byte messageSubtype = br.readByte(6, 7);
        if (messageSubtype != 2)
            throw new UnspecifiedFormatError("Wx AIREP alternate weather state message must have subtype 2, got " + messageSubtype + ".");

        icingStatusEncoded = br.readByte(8, 12);
        rollAngleEncoded = br.readShort(13, 22);
        headingType = br.readBoolean(23);
        headingEncoded = br.readShort(24, 35);
        airTemperatureType = br.readBoolean(36);
        airTemperatureEncoded = br.readShort(37, 44);
        airspeedType = br.readBoolean(45);
        airspeedEncoded = br.readShort(46, 55);
    }

    @Override
    public byte getMessageSubtype() {
        return 2;
    }

    @Override
    public byte getIcingStatusEncoded() {
        return icingStatusEncoded;
    }

    /**
     * @return the raw encoded roll angle field
     */
    public short getRollAngleEncoded() {
        return rollAngleEncoded;
    }

    /**
     * @return whether the roll angle field is available
     */
    public boolean hasRollAngle() {
        return rollAngleEncoded != 0;
    }

    /**
     * Decode the roll angle field in degrees. The returned value is a lower bound; a value less
     * than -90 denotes "below -90°".
     *
     * @return the roll angle in degrees, or {@code null} if unavailable
     */
    public Double getRollAngle() {
        if (!hasRollAngle()) return null;
        return -90 + (rollAngleEncoded - 2) * 180. / 1021;
    }

    /**
     * @return the raw heading type bit: {@code false} means true north, {@code true} means
     * magnetic north
     */
    public boolean getHeadingType() {
        return headingType;
    }

    /**
     * @return the raw encoded heading field
     */
    public short getHeadingEncoded() {
        return headingEncoded;
    }

    /**
     * @return whether the heading field is available
     */
    public boolean hasHeading() {
        return headingEncoded != 0;
    }

    /**
     * Decode the heading field.
     *
     * @return a lower bound for the heading in degrees [0,360), or {@code null} if unavailable
     * @see #getHeadingType() to determine whether this is relative to true or magnetic north
     */
    public Double getHeading() {
        if (!hasHeading()) return null;
        return (headingEncoded - 1) * 360. / 4095;
    }

    @Override
    public boolean getAirTemperatureType() {
        return airTemperatureType;
    }

    @Override
    public short getAirTemperatureEncoded() {
        return airTemperatureEncoded;
    }

    @Override
    public boolean getAirspeedType() {
        return airspeedType;
    }

    @Override
    public short getAirspeedEncoded() {
        return airspeedEncoded;
    }

    @Override
    public String toString() {
        return "WxAIREPAlternateWeatherStateMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", icingStatusEncoded=" + icingStatusEncoded +
                ", rollAngleEncoded=" + rollAngleEncoded +
                ", headingType=" + headingType +
                ", headingEncoded=" + headingEncoded +
                ", airTemperatureType=" + airTemperatureType +
                ", airTemperatureEncoded=" + airTemperatureEncoded +
                ", airspeedType=" + airspeedType +
                ", airspeedEncoded=" + airspeedEncoded +
                '}';
    }

}
