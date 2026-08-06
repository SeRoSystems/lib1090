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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for TIS-B velocity message (DO-260B, 2.2.17.3.4).
 */
public class VelocityOverGroundMsg extends ExtendedSquitter implements Serializable, AirborneVelocityMsg, de.serosystems.lib1090.msgs.squitter.VelocityOverGroundMsg, TISBMsg {

    private static final long serialVersionUID = -2121820203874488709L;

    private byte messageSubtype;
    private boolean imf;
    private byte nacp;
    private boolean velocityToEastNegative; // 0 = east, 1 = west
    private short velocityToEastEncoded; // raw encoded velocity-to-east field
    private boolean velocityToNorthNegative; // 0 = north, 1 = south
    private short velocityToNorthEncoded; // raw encoded velocity-to-north field

    private boolean verticalRateDown; // 0 = up, 1 = down
    private short verticalRateEncoded; // raw encoded vertical rate field

    private boolean geoFlag;
    private boolean diffBaroAltNegative;
    private short diffBaroAltEncoded;
    private byte nacv;
    private byte sil;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected VelocityOverGroundMsg() {
    }

    /**
     * @param rawMessage raw TIS-B velocity message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public VelocityOverGroundMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw TIS-B velocity message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public VelocityOverGroundMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the velocity message
     * @throws BadFormatException if message has wrong format
     */
    public VelocityOverGroundMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18.");

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Velocity messages must have typecode 19.");

        // Table 2-13
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 1 && messageSubtype != 2) {
            throw new BadFormatException("Ground speed messages have subtype 1 or 2.");
        }

        imf = br.readBoolean(9);
        nacp = br.readByte(10, 13);

        velocityToEastNegative = br.readBoolean(14);
        velocityToEastEncoded = br.readShort(15, 24);

        velocityToNorthNegative = br.readBoolean(25);
        velocityToNorthEncoded = br.readShort(26, 35);

        // 0 = no geo data available, 1 = geo data available
        geoFlag = br.readBoolean(36);

        verticalRateDown = br.readBoolean(37);
        verticalRateEncoded = br.readShort(38, 46);

        if (geoFlag) {
            diffBaroAltNegative = br.readBoolean(49);
            diffBaroAltEncoded = br.readByte(50, 56);
        } else {
            nacv = br.readByte(48, 50);
            sil = br.readByte(51, 52);
        }
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public boolean hasGeoFlag() {
        return geoFlag;
    }

    @Override
    public boolean hasDiffBaroAlt() {
        return hasGeoFlag() && diffBaroAltEncoded != 0;
    }

    @Override
    public short getDiffBaroAltEncoded() {
        return diffBaroAltEncoded;
    }

    @Override
    public boolean isDiffBaroAltNegative() {
        return diffBaroAltNegative;
    }

    /**
     * @return If supersonic, velocity has only 4 kts accuracy, otherwise 1 kt
     */
    public boolean isSupersonic() {
        return messageSubtype == 2;
    }

    @Override
    public Byte getNACv() {
        return hasGeoFlag() ? null : nacv;
    }

    /**
     * @return true if the velocity from west to east is negative (i.e. towards the west)
     */
    public boolean isVelocityToEastNegative() {
        return velocityToEastNegative;
    }

    /**
     * @return the raw encoded velocity from west to east field
     */
    public short getWestToEastVelocityEncoded() {
        return velocityToEastEncoded;
    }

    /**
     * @return true if the velocity from south to north is negative (i.e. towards the south)
     */
    public boolean isVelocityToNorthNegative() {
        return velocityToNorthNegative;
    }

    /**
     * @return the raw encoded velocity from south to north field
     */
    public short getSouthToNorthVelocityEncoded() {
        return velocityToNorthEncoded;
    }

    /**
     * @return true if the vertical rate is negative
     */
    public boolean isVerticalRateDown() {
        return verticalRateDown;
    }

    @Override
    public boolean isBarometricVerticalSpeed() {
        return false;
    }

    @Override
    public short getVerticalRateEncoded() {
        return verticalRateEncoded;
    }

    /**
     * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
     * in the operational status message. For version 0 it is derived from the format type code.
     *
     * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
     * {@link AirborneOperationalStatusV1Msg}. Returns null if not available.
     */
    public byte getNACp() {
        return nacp;
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
     * the NIC containment radius. Returns null if not available.
     */
    public Byte getSIL() {
        return hasGeoFlag() ? null : sil;
    }

    @Override
    public String toString() {
        return "VelocityOverGroundMsg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", imf=" + imf +
                ", nacp=" + nacp +
                ", velocityToEastNegative=" + velocityToEastNegative +
                ", velocityToEastEncoded=" + velocityToEastEncoded +
                ", velocityToNorthNegative=" + velocityToNorthNegative +
                ", velocityToNorthEncoded=" + velocityToNorthEncoded +
                ", verticalRateDown=" + verticalRateDown +
                ", verticalRateEncoded=" + verticalRateEncoded +
                ", geoFlag=" + geoFlag +
                ", diffBaroAltNegative=" + diffBaroAltNegative +
                ", diffBaroAltEncoded=" + diffBaroAltEncoded +
                ", nacv=" + nacv +
                ", sil=" + sil +
                '}';
    }

}
