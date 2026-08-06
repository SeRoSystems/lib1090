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
import de.serosystems.lib1090.msgs.adsb.NACvMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirspeedHeadingMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-R version 2 airspeed and heading messages
 */
public class AirspeedHeadingV2Msg extends ExtendedSquitter implements Serializable, AirspeedHeadingMsg, IMFMsg, NACvMsg, ADSRMsg {

    private static final long serialVersionUID = -3439568191943485753L;

    private byte messageSubtype;
    private boolean imf;
    private byte navigationAccuracyCategoryEncoded;
    private boolean headingStatusBit;
    private short headingEncoded;
    private boolean trueAirspeed; // 0 = indicated AS, 1 = true AS
    private short airspeedEncoded; // raw encoded airspeed field
    private boolean verticalSource; // 0 = geometric, 1 = barometric
    private boolean verticalRateDown; // 0 = up, 1 = down
    private short verticalRateEncoded; // raw encoded vertical rate field
    private boolean diffBaroAltNegative;
    private short diffBaroAltEncoded; // raw encoded geometric minus barometric altitude difference field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirspeedHeadingV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-R airspeed and heading message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirspeedHeadingV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R airspeed and heading message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirspeedHeadingV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the airspeed and heading msg
     * @throws BadFormatException if message has wrong format
     */
    public AirspeedHeadingV2Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Airspeed and heading messages must have typecode 19.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 3 && messageSubtype != 4)
            throw new BadFormatException("Airspeed and heading messages have subtype 3 or 4.");

        // ME bit 9 is redefined as the IMF flag for ADS-R
        imf = br.readByte(9, 9) == 1;
        navigationAccuracyCategoryEncoded = br.readByte(11, 13);

        headingStatusBit = br.readByte(14, 14) == 1;
        headingEncoded = br.readShort(15, 24);

        trueAirspeed = br.readByte(25, 25) == 1;
        airspeedEncoded = br.readShort(26, 35);

        verticalSource = br.readByte(36, 36) == 1;
        verticalRateDown = br.readByte(37, 37) == 1;
        verticalRateEncoded = br.readShort(38, 46);

        diffBaroAltNegative = br.readByte(49, 49) == 1;
        diffBaroAltEncoded = br.readByte(50, 56);
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public boolean hasHeadingStatusFlag() {
        return headingStatusBit;
    }

    @Override
    public boolean isSupersonic() {
        return messageSubtype == 4;
    }

    @Override
    public byte getNACvEncoded() {
        return navigationAccuracyCategoryEncoded;
    }

    @Override
    public short getAirspeedEncoded() {
        return airspeedEncoded;
    }

    @Override
    public boolean isBarometricVerticalSpeed() {
        return verticalSource;
    }

    @Override
    public boolean isVerticalRateDown() {
        return verticalRateDown;
    }

    @Override
    public short getVerticalRateEncoded() {
        return verticalRateEncoded;
    }

    @Override
    public boolean isDiffBaroAltNegative() {
        return diffBaroAltNegative;
    }

    @Override
    public short getDiffBaroAltEncoded() {
        return diffBaroAltEncoded;
    }

    @Override
    public short getHeadingEncoded() {
        return headingEncoded;
    }

    @Override
    public boolean isTrueAirspeed() {
        return trueAirspeed;
    }

    @Override
    public String toString() {
        return "AirspeedHeadingV2Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", imf=" + imf +
                ", navigationAccuracyCategoryEncoded=" + navigationAccuracyCategoryEncoded +
                ", headingStatusBit=" + headingStatusBit +
                ", headingEncoded=" + headingEncoded +
                ", trueAirspeed=" + trueAirspeed +
                ", airspeedEncoded=" + airspeedEncoded +
                ", verticalSource=" + verticalSource +
                ", verticalRateDown=" + verticalRateDown +
                ", verticalRateEncoded=" + verticalRateEncoded +
                ", diffBaroAltNegative=" + diffBaroAltNegative +
                ", diffBaroAltEncoded=" + diffBaroAltEncoded +
                '}';
    }

}
