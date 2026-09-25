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
import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAltV3;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.NACvMsg;
import de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 3 airborne velocity messages, as defined in ED-102B §2.2.3.2.6
 * Figure 2-8, in particular §2.2.3.2.6.1/§2.2.3.2.6.2 (ground-speed variant, Subtypes 1 and 2 —
 * the only variant that remains valid in ADS-B version 3). The airspeed-and-heading variant (Subtypes 3 and 4) is reserved and no longer specified starting
 * with ED-102B §2.2.3.2.6.5, having last been specified by DO-260B §2.2.3.2.6.3/§2.2.3.2.6.4.
 */
public class AirborneVelocityV3Msg extends ExtendedSquitter implements Serializable, VelocityOverGroundMsg, NACvMsg, ADSBMsg {

    private static final long serialVersionUID = -5216437408671309124L;

    private byte messageSubtype;
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
    private int extendedDiffBaroAltEncoded; // ME bits 10, 47-48, 50-56 and 9

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneVelocityV3Msg() {
    }

    /**
     * @param rawMessage raw ADS-B velocity-over-ground message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneVelocityV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B velocity-over-ground message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
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

        if (getFormatTypeCode() != 19)
            throw new BadFormatException("Airborne Velocity messages must have typecode 19");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 1 && messageSubtype != 2)
            throw new BadFormatException("Velocity Over Ground messages must have subtype 1 or 2");

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
        // instead. Otherwise they contribute to an extended, 11-bit encoding of the Difference from
        // Barometric Altitude, together with ME bits 9 and 10.
        if (diffBaroAltEncoded == 0)
            nicSupplementD = br.readByte(47, 48);
        extendedDiffBaroAltEncoded = br.readShort(10, 10) << 10
                | br.readShort(47, 48) << 8
                | diffBaroAltEncoded << 1
                | br.readShort(9, 9);
    }

    @Override
    public boolean hasVerticalRate() {
        return verticalRateEncoded != 0;
    }

    /**
     * @return the difference as the 11-bit extended coding of ED-102B TABLE 2-27 reports it
     */
    @Override
    public DiffBaroAltV3 getDiffBaroAlt() {
        return DiffBaroAltV3.of(diffBaroAltNegative, extendedDiffBaroAltEncoded);
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
     * @return the extended, 11-bit encoding of the Difference from Barometric Altitude, ME bits 10,
     * 47-48, 50-56 and 9, most significant first. Bits 47-48 are NIC supplement D where
     * {@link #getDiffBaroAltEncoded()} is 0.
     */
    public int getExtendedDiffBaroAltEncoded() {
        return extendedDiffBaroAltEncoded;
    }

    @Override
    public String toString() {
        return "AirborneVelocityV3Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
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
