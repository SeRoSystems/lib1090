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
import de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 2 velocity-over-ground messages
 */
public class VelocityOverGroundV2Msg extends ExtendedSquitter implements Serializable, VelocityOverGroundMsg, AirborneVelocityV2Msg {

    private static final long serialVersionUID = 3067581442639572810L;

    private byte messageSubtype;
    private boolean intentChange;
    private byte navigationAccuracyCategoryEncoded;
    private boolean velocityToEastNegative; // 0 = positive (east), 1 = negative (west)
    private short velocityToEastEncoded; // raw encoded velocity-to-east field
    private boolean velocityToNorthNegative; // 0 = positive (north), 1 = negative (south)
    private short velocityToNorthEncoded; // raw encoded velocity-to-north field
    private boolean verticalSource; // 0 = geometric, 1 = barometric
    private boolean verticalRateDown; // 0 = up, 1 = down
    private short verticalRateEncoded; // raw encoded vertical rate field
    private boolean diffBaroAltNegative;
    private short diffBaroAltEncoded; // raw encoded geometric minus barometric altitude difference field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected VelocityOverGroundV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-B velocity-over-ground message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public VelocityOverGroundV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B velocity-over-ground message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public VelocityOverGroundV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this velocity over ground msg
     * @throws BadFormatException if message has wrong format
     */
    public VelocityOverGroundV2Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Velocity messages must have typecode 19.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 1 && messageSubtype != 2)
            throw new BadFormatException("Ground speed messages have subtype 1 or 2.");

        intentChange = br.readByte(9, 9) == 1;
        navigationAccuracyCategoryEncoded = br.readByte(11, 13);

        velocityToEastNegative = br.readByte(14, 14) == 1;
        velocityToEastEncoded = br.readShort(15, 24);

        velocityToNorthNegative = br.readByte(25, 25) == 1;
        velocityToNorthEncoded = br.readShort(26, 35);

        verticalSource = br.readByte(36, 36) == 1;
        verticalRateDown = br.readByte(37, 37) == 1;
        verticalRateEncoded = br.readShort(38, 46);

        diffBaroAltNegative = br.readByte(49, 49) == 1;
        diffBaroAltEncoded = br.readByte(50, 56);
    }

    @Override
    public boolean isSupersonic() {
        return messageSubtype == 2;
    }

    @Override
    public boolean hasChangeIntent() {
        return intentChange;
    }

    @Override
    public byte getNACvEncoded() {
        return navigationAccuracyCategoryEncoded;
    }

    @Override
    public boolean isBarometricVerticalSpeed() {
        return verticalSource;
    }

    @Override
    public boolean isVelocityToEastNegative() {
        return velocityToEastNegative;
    }

    @Override
    public boolean isVelocityToNorthNegative() {
        return velocityToNorthNegative;
    }

    @Override
    public boolean isVerticalRateDown() {
        return verticalRateDown;
    }

    @Override
    public boolean isDiffBaroAltNegative() {
        return diffBaroAltNegative;
    }

    @Override
    public short getWestToEastVelocityEncoded() {
        return velocityToEastEncoded;
    }

    @Override
    public short getSouthToNorthVelocityEncoded() {
        return velocityToNorthEncoded;
    }

    @Override
    public short getVerticalRateEncoded() {
        return verticalRateEncoded;
    }

    @Override
    public short getDiffBaroAltEncoded() {
        return diffBaroAltEncoded;
    }

    @Override
    public String toString() {
        return "VelocityOverGroundV2Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", intentChange=" + intentChange +
                ", navigationAccuracyCategoryEncoded=" + navigationAccuracyCategoryEncoded +
                ", velocityToEastNegative=" + velocityToEastNegative +
                ", velocityToEastEncoded=" + velocityToEastEncoded +
                ", velocityToNorthNegative=" + velocityToNorthNegative +
                ", velocityToNorthEncoded=" + velocityToNorthEncoded +
                ", verticalSource=" + verticalSource +
                ", verticalRateDown=" + verticalRateDown +
                ", verticalRateEncoded=" + verticalRateEncoded +
                ", diffBaroAltNegative=" + diffBaroAltNegative +
                ", diffBaroAltEncoded=" + diffBaroAltEncoded +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_VELOCITY_V2;
    }
}
