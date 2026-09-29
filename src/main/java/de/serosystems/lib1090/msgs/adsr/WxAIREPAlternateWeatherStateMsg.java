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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.WxAIREP;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.WxAIREPWeatherMsg;

import java.io.Serializable;

/**
 * Decoder for the alternate weather state subtype (2) of the ADS-R Wx AIREP message, as defined in
 * ED-102B §2.2.18.4.8 Figure 2-66 (introduced in ADS-B version 3).
 */
public class WxAIREPAlternateWeatherStateMsg extends TypeCodedExtendedSquitter implements Serializable, WxAIREPWeatherMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 3893550384868621639L;

    private byte icingStatusEncoded; // raw encoded icing status field
    private short rollAngleEncoded; // raw encoded roll angle field
    private boolean headingType;
    private short headingEncoded; // raw encoded heading field
    private boolean airTemperatureType;
    private short airTemperatureEncoded; // raw encoded air temperature field
    private boolean airspeedType;
    private short airspeedEncoded; // raw encoded airspeed field
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected WxAIREPAlternateWeatherStateMsg() {
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public WxAIREPAlternateWeatherStateMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public WxAIREPAlternateWeatherStateMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Wx AIREP alternate weather state message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public WxAIREPAlternateWeatherStateMsg(TypeCodedExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);
        ADSRMsg.checkADSR(this);

        BitReader br = BitReader.forBigEndian(getMessage());
        WxAIREP.validateFormat(getFormatTypeCode(), br, 2, "alternate weather state");

        icingStatusEncoded = br.readByte(8, 12);
        rollAngleEncoded = br.readShort(13, 22);
        headingType = br.readBoolean(23);
        headingEncoded = br.readShort(24, 35);
        airTemperatureType = br.readBoolean(36);
        airTemperatureEncoded = br.readShort(37, 44);
        airspeedType = br.readBoolean(45);
        airspeedEncoded = br.readShort(46, 55);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = br.readBoolean(56);
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
        return WxAIREP.rollAngle(rollAngleEncoded);
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
        return WxAIREP.heading(headingEncoded);
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
    public boolean getIMF() {
        return imf;
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
                ", imf=" + imf +
                '}';
    }

}
