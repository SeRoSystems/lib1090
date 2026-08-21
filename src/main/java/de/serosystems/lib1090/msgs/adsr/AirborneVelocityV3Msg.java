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
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-R version 3 airborne velocity messages
 */
public class AirborneVelocityV3Msg extends ExtendedSquitter implements Serializable, VelocityOverGroundMsg, IMFMsg, NACvMsg, ADSRMsg {

    private static final long serialVersionUID = 5523975668402875445L;

    private byte messageSubtype;
    private boolean imf;
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
    private byte nicSupplementD; // only present if diffBaroAltEncoded == 0
    private short extendedDiffBaroAltEncoded; // only present if diffBaroAltEncoded != 0; 10-bit value, see getter

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneVelocityV3Msg() {
    }

    /**
     * @param rawMessage raw ADS-R velocity-over-ground message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.4 Figure 2-59
     */
    public AirborneVelocityV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R velocity-over-ground message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.4 Figure 2-59
     */
    public AirborneVelocityV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this velocity over ground msg
     * @throws BadFormatException if message has wrong format
     */
    public AirborneVelocityV3Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Velocity messages must have typecode 19.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 1 && messageSubtype != 2)
            throw new BadFormatException("Ground speed messages have subtype 1 or 2.");

        // ME bit 9 is redefined as the IMF flag for ADS-R
        imf = br.readBoolean(9);
        navigationAccuracyCategoryEncoded = br.readByte(11, 13);

        velocityToEastNegative = br.readBoolean(14);
        velocityToEastEncoded = br.readShort(15, 24);

        velocityToNorthNegative = br.readBoolean(25);
        velocityToNorthEncoded = br.readShort(26, 35);

        verticalSource = br.readBoolean(36);
        verticalRateDown = br.readBoolean(37);
        verticalRateEncoded = br.readShort(38, 46);

        diffBaroAltNegative = br.readBoolean(49);
        diffBaroAltEncoded = br.readByte(50, 56);

        // In ADS-B version 3, ME bits 47-48 are re-purposed: if the Difference from Barometric
        // Altitude is unavailable (i.e. its encoded field is 0), they carry NIC supplement D
        // instead. If it is available, they contribute to an extended, 11-bit encoding of the
        // Difference from Barometric Altitude, together with ME bits 9 and 10. For ADS-R, ME bit
        // 9 is redefined as the IMF flag (see above), so this extended encoding effectively loses
        // its least significant bit, leaving a 10-bit encoding with reduced resolution.
        if (diffBaroAltEncoded == 0) {
            nicSupplementD = br.readByte(47, 48);
        } else {
            extendedDiffBaroAltEncoded = (short) (
                    (br.readByte(10, 10) << 9) |
                            (br.readByte(47, 48) << 7) |
                            diffBaroAltEncoded
            );
        }
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public boolean hasVerticalRate() {
        return verticalRateEncoded != 0;
    }

    @Override
    public boolean hasDiffBaroAlt() {
        if (hasNICSupplementD()) return false;
        // the top 3 bits are a reserved, must-be-zero combination only in the compat zone;
        // in the extended zone (diffBaroAltEncoded == 0x7F) they carry legitimate data
        return diffBaroAltEncoded == 0x7F || (extendedDiffBaroAltEncoded & 0x380) == 0;
    }

    @Override
    public Double getDiffBaroAlt() {
        if (!hasDiffBaroAlt()) return null;

        double diffBaroAlt;
        if (diffBaroAltEncoded != 0x7F) {
            diffBaroAlt = (extendedDiffBaroAltEncoded - 1) * 25.0;
        } else {
            int upper = (extendedDiffBaroAltEncoded >>> 7) & 0x7;
            diffBaroAlt = upper * 200 + 3150;
        }

        return isDiffBaroAltNegative() ? -diffBaroAlt : diffBaroAlt;
    }

    @Override
    public boolean isDiffBaroAltSaturated() {
        if (!hasDiffBaroAlt()) return false;
        return extendedDiffBaroAltEncoded == 0x3ff;
    }

    @Override
    public Double getDiffBaroAltMidpoint() {
        if (!hasDiffBaroAlt()) return null;
        if (isDiffBaroAltSaturated()) return 4450.;

        double diffBaroAlt;
        int upper = (extendedDiffBaroAltEncoded >>> 7) & 0x7;
        if (extendedDiffBaroAltEncoded == 0x7f)
            diffBaroAlt = 3193.75;
        else if (diffBaroAltEncoded != 0x7F || upper == 0)
            diffBaroAlt = (extendedDiffBaroAltEncoded - 1) * 25.0;
        else
            diffBaroAlt = upper * 200 + 3150;

        return isDiffBaroAltNegative() ? -diffBaroAlt : diffBaroAlt;
    }

    @Override
    public boolean isSupersonic() {
        return messageSubtype == 2;
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

    /**
     * @return whether NIC supplement D is available
     */
    public boolean hasNICSupplementD() {
        return diffBaroAltEncoded == 0;
    }

    /**
     * @return NIC supplement D (2-bit value), only meaningful if {@link #hasNICSupplementD()}
     */
    public byte getNICSupplementD() {
        return nicSupplementD;
    }

    /**
     * @return the extended, 10-bit encoding of the Difference from Barometric Altitude, spread
     * across ME bit 10, ME bits 47-48 and the regular {@link #getDiffBaroAltEncoded()} field. For
     * ADS-B, this field has an eleventh, least significant bit contributed by ME bit 9; for ADS-R,
     * ME bit 9 is redefined as the IMF flag, so that bit is unavailable here, halving the
     * resolution of this field. Only meaningful if {@link #hasDiffBaroAlt()}
     */
    public short getExtendedDiffBaroAltEncoded() {
        return extendedDiffBaroAltEncoded;
    }

    @Override
    public String toString() {
        return "AirborneVelocityV3Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", imf=" + imf +
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
                ", nicSupplementD=" + nicSupplementD +
                ", extendedDiffBaroAltEncoded=" + extendedDiffBaroAltEncoded +
                '}';
    }

}
