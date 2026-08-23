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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.WxAIREPWeatherMsg;

import java.io.Serializable;

/**
 * Decoder for the weather state subtype (1) of the ADS-R Wx AIREP message, as defined in
 * ED-102B §2.2.18.4.8 Figure 2-65 (introduced in ADS-B version 3).
 */
public class WxAIREPWeatherStateMsg extends ExtendedSquitter implements Serializable, WxAIREPWeatherMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = -2471111531154032256L;

    private byte icingStatusEncoded; // raw encoded icing status field
    private byte windQualityIndicatorEncoded; // raw encoded wind quality indicator field
    private short windSpeedEncoded; // raw encoded wind speed field
    private short windDirectionEncoded; // raw encoded wind direction field
    private boolean airTemperatureType;
    private short airTemperatureEncoded; // raw encoded air temperature field
    private boolean airspeedType;
    private short airspeedEncoded; // raw encoded airspeed field
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected WxAIREPWeatherStateMsg() {
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.8 Figure 2-65
     */
    public WxAIREPWeatherStateMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.8 Figure 2-65
     */
    public WxAIREPWeatherStateMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Wx AIREP weather state message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has a subtype other than 1 (weather state); see ED-102B §2.2.18.4.8 Figure 2-65
     */
    public WxAIREPWeatherStateMsg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 26)
            throw new BadFormatException("Wx AIREP messages must have typecode 26.");

        BitReader br = BitReader.forBigEndian(getMessage());

        byte messageSubtype = br.readByte(6, 7);
        if (messageSubtype != 1)
            throw new UnspecifiedFormatError("Wx AIREP weather state message must have subtype 1, got " + messageSubtype + ".");

        icingStatusEncoded = br.readByte(8, 12);
        windQualityIndicatorEncoded = br.readByte(13, 15);
        windSpeedEncoded = br.readShort(16, 23);
        windDirectionEncoded = br.readShort(24, 33);
        airTemperatureType = br.readBoolean(36);
        airTemperatureEncoded = br.readShort(37, 44);
        airspeedType = br.readBoolean(45);
        airspeedEncoded = br.readShort(46, 55);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = br.readBoolean(56);
    }

    @Override
    public byte getMessageSubtype() {
        return 1;
    }

    @Override
    public byte getIcingStatusEncoded() {
        return icingStatusEncoded;
    }

    /**
     * @return the raw encoded wind quality indicator field
     */
    public byte getWindQualityIndicatorEncoded() {
        return windQualityIndicatorEncoded;
    }

    /**
     * @return the raw encoded wind speed field
     */
    public short getWindSpeedEncoded() {
        return windSpeedEncoded;
    }

    /**
     * @return whether the wind speed field is available
     */
    public boolean hasWindSpeed() {
        return windSpeedEncoded != 0;
    }

    /**
     * Decode the wind speed field.
     *
     * @return a lower bound for the wind speed in knots, or {@code null} if unavailable
     */
    public Short getWindSpeed() {
        if (!hasWindSpeed()) return null;
        return (short) (windSpeedEncoded - 1);
    }

    /**
     * @return the raw encoded wind direction field
     */
    public short getWindDirectionEncoded() {
        return windDirectionEncoded;
    }

    /**
     * @return whether the wind direction field is available
     */
    public boolean hasWindDirection() {
        return windDirectionEncoded != 0;
    }

    /**
     * Decode the wind direction field. The direction is clockwise relative to true north.
     *
     * @return a lower bound for the wind direction in degrees [0,360) clockwise from true north,
     * or {@code null} if unavailable
     */
    public Double getWindDirection() {
        if (!hasWindDirection()) return null;
        return (windDirectionEncoded - 1) / 1023. * 360;
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
        return "WxAIREPWeatherStateMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", icingStatusEncoded=" + icingStatusEncoded +
                ", windQualityIndicatorEncoded=" + windQualityIndicatorEncoded +
                ", windSpeedEncoded=" + windSpeedEncoded +
                ", windDirectionEncoded=" + windDirectionEncoded +
                ", airTemperatureType=" + airTemperatureType +
                ", airTemperatureEncoded=" + airTemperatureEncoded +
                ", airspeedType=" + airspeedType +
                ", airspeedEncoded=" + airspeedEncoded +
                ", imf=" + imf +
                '}';
    }

}
