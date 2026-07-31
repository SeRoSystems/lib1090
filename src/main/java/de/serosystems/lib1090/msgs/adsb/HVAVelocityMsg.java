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
 * Decoder for the velocity subtype (1) of the ADS-B High Velocity and/or Altitude (HVA) message,
 * introduced with typecode 25 in ADS-B version 3.
 */
public class HVAVelocityMsg extends ExtendedSquitter implements Serializable, HVAMsg {

    private static final long serialVersionUID = -1934875602817346095L;

    private byte positionIntegrityCategory; // raw encoded PIC field
    private boolean eastWestDirectionBit;
    private short hvaEastWestVelocityEncoded; // raw encoded HVA East/West velocity field
    private boolean northSouthDirectionBit;
    private short hvaNorthSouthVelocityEncoded; // raw encoded HVA North/South velocity field
    private boolean verticalRateSignBit;
    private short hvaVerticalRateEncoded; // raw encoded HVA vertical rate field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected HVAVelocityMsg() {
    }

    /**
     * @param rawMessage raw ADS-B HVA message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public HVAVelocityMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B HVA message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public HVAVelocityMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this HVA velocity message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has a subtype other than 1 (velocity)
     */
    public HVAVelocityMsg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 25)
            throw new BadFormatException("HVA messages must have typecode 25.");

        BitReader br = BitReader.forBigEndian(getMessage());

        byte messageSubtype = br.readByte(6, 7);
        if (messageSubtype != 1)
            throw new UnspecifiedFormatError("HVA velocity message must have subtype 1, got " + messageSubtype + ".");

        positionIntegrityCategory = br.readByte(18, 21);
        eastWestDirectionBit = br.readByte(22, 22) == 1;
        hvaEastWestVelocityEncoded = br.readShort(23, 33);
        northSouthDirectionBit = br.readByte(34, 34) == 1;
        hvaNorthSouthVelocityEncoded = br.readShort(35, 45);
        verticalRateSignBit = br.readByte(46, 46) == 1;
        hvaVerticalRateEncoded = br.readShort(47, 56);
    }

    @Override
    public byte getMessageSubtype() {
        return 1;
    }

    /**
     * @return the raw encoded PIC field (Position Integrity Category)
     */
    public byte getPIC() {
        return positionIntegrityCategory;
    }

    /**
     * @return whether the PIC field is available
     */
    public boolean hasPIC() {
        return positionIntegrityCategory != 0;
    }

    /**
     * Decode the position integrity category (PIC) field into a Radius of Containment (Rc) bound
     * in meters: the actual containment radius is smaller than the returned value.
     *
     * @return the Radius of Containment bound in meters, or {@code null} if unavailable or reserved
     */
    public Double getRadiusOfContainment() {
        switch (positionIntegrityCategory) {
            case 1:
                return 37040.;
            case 2:
                return 18520.;
            case 3:
                return 14816.;
            case 4:
                return 7408.;
            case 5:
                return 3704.;
            case 6:
                return 1852.;
            case 7:
                return 1111.2;
            case 8:
                return 926.;
            case 9:
                return 555.6;
            case 10:
                return 370.4;
            case 11:
                return 185.2;
            case 12:
                return 75.;
            case 13:
                return 25.;
            case 14:
                return 7.5;
            default:
                return null; // 0: unknown, 15: reserved
        }
    }

    /**
     * @return the raw East/West direction bit
     */
    public boolean isEastWestDirectionBit() {
        return eastWestDirectionBit;
    }

    /**
     * @return the raw encoded HVA East/West velocity field
     */
    public short getHVAEastWestVelocityEncoded() {
        return hvaEastWestVelocityEncoded;
    }

    /**
     * @return whether the HVA East/West velocity field is available
     */
    public boolean hasHVAEastWestVelocity() {
        return hvaEastWestVelocityEncoded != 0;
    }

    /**
     * Decode the HVA East/West velocity field into knots.
     *
     * @return the East/West velocity in knots, {@code 15690} meaning "equal to or greater than
     * 15690 kt", negated if {@link #isEastWestDirectionBit()} is set, or {@code null} if unavailable
     */
    public Integer getHVAEastWestVelocity() {
        return decodeVelocity(hvaEastWestVelocityEncoded, isEastWestDirectionBit());
    }

    /**
     * @return the raw North/South direction bit
     */
    public boolean isNorthSouthDirectionBit() {
        return northSouthDirectionBit;
    }

    /**
     * @return the raw encoded HVA North/South velocity field
     */
    public short getHVANorthSouthVelocityEncoded() {
        return hvaNorthSouthVelocityEncoded;
    }

    /**
     * @return whether the HVA North/South velocity field is available
     */
    public boolean hasHVANorthSouthVelocity() {
        return hvaNorthSouthVelocityEncoded != 0;
    }

    /**
     * Decode the HVA North/South velocity field into knots.
     *
     * @return the North/South velocity in knots, {@code 15690} meaning "equal to or greater than
     * 15690 kt", negated if {@link #isNorthSouthDirectionBit()} is set, or {@code null} if unavailable
     */
    public Integer getHVANorthSouthVelocity() {
        return decodeVelocity(hvaNorthSouthVelocityEncoded, isNorthSouthDirectionBit());
    }

    /**
     * Decode an HVA East/West or North/South velocity field (shared encoding) into knots.
     *
     * @param n        the raw encoded velocity field
     * @param negative whether the corresponding direction bit is set
     * @return the velocity in knots, {@code 15690} meaning "equal to or greater than 15690 kt",
     * negated if {@code negative} is set, or {@code null} if unavailable
     */
    private static Integer decodeVelocity(short n, boolean negative) {
        if (n == 0) return null;

        int velocity;
        if (n <= 1023) velocity = (n - 1) * 4;
        else if (n <= 1762) velocity = 4088 + (n - 1023) * 8;
        else if (n <= 2046) velocity = 10000 + (n - 1762) * 20;
        else velocity = 15690;

        return negative ? -velocity : velocity;
    }

    /**
     * @return the raw vertical rate sign bit
     */
    public boolean isVerticalRateSignBit() {
        return verticalRateSignBit;
    }

    /**
     * @return the raw encoded HVA vertical rate field
     */
    public short getHVAVerticalRateEncoded() {
        return hvaVerticalRateEncoded;
    }

    /**
     * @return whether the HVA vertical rate field is available
     */
    public boolean hasHVAVerticalRate() {
        return hvaVerticalRateEncoded != 0;
    }

    /**
     * Decode the HVA vertical rate field into feet per minute.
     *
     * @return the vertical rate in feet per minute, {@code 415258} meaning "equal to or greater
     * than 415258 fpm", negated if {@link #isVerticalRateSignBit()} is set, or {@code null} if
     * unavailable
     */
    public Integer getHVAVerticalRate() {
        if (!hasHVAVerticalRate()) return null;

        short n = hvaVerticalRateEncoded;
        int verticalRate;
        if (n <= 314) verticalRate = (n - 1) * 64;
        else if (n <= 678) verticalRate = 20032 + (n - 314) * 128;
        else if (n <= 1022) verticalRate = 66624 + (n - 678) * 1012;
        else verticalRate = 415258;

        return isVerticalRateSignBit() ? -verticalRate : verticalRate;
    }

    @Override
    public String toString() {
        return "HVAVelocityMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", picEncoded=" + positionIntegrityCategory +
                ", eastWestDirectionBit=" + eastWestDirectionBit +
                ", hvaEastWestVelocityEncoded=" + hvaEastWestVelocityEncoded +
                ", northSouthDirectionBit=" + northSouthDirectionBit +
                ", hvaNorthSouthVelocityEncoded=" + hvaNorthSouthVelocityEncoded +
                ", verticalRateSignBit=" + verticalRateSignBit +
                ", hvaVerticalRateEncoded=" + hvaVerticalRateEncoded +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_HVA_VELOCITY;
    }
}
