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
import de.serosystems.lib1090.msgs.squitter.AirspeedHeadingMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 1 airspeed and heading messages, as defined in ED-102B Appendix N §N.5.2 Figure N-12 (legacy format retained for backward compatibility).
 */
public class AirspeedHeadingV1Msg extends ExtendedSquitter implements Serializable, AirspeedHeadingMsg, AirborneVelocityV1Msg, ADSBMsg {

    private static final long serialVersionUID = -1927465310985612337L;

    private byte messageSubtype;
    private boolean intentChange;
    private boolean ifrCapability;
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
    protected AirspeedHeadingV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airspeed and heading message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has an airspeed/heading subtype that is no longer specified in ED-102B: Airborne Velocity Subtypes 3 and 4 are reserved per ED-102B §2.2.3.2.6.5 and were last specified by DO-260B §2.2.3.2.6.3 and §2.2.3.2.6.4; the Version One legacy format is preserved for backward compatibility in ED-102B §N.5.2 Figure N-12
     */
    public AirspeedHeadingV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B airspeed and heading message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has an airspeed/heading subtype that is no longer specified in ED-102B: Airborne Velocity Subtypes 3 and 4 are reserved per ED-102B §2.2.3.2.6.5 and were last specified by DO-260B §2.2.3.2.6.3 and §2.2.3.2.6.4; the Version One legacy format is preserved for backward compatibility in ED-102B §N.5.2 Figure N-12
     */
    public AirspeedHeadingV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the airspeed and heading msg
     * @throws BadFormatException if message has wrong format
     */
    public AirspeedHeadingV1Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Airspeed and heading messages must have typecode 19.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 3 && messageSubtype != 4)
            throw new BadFormatException("Airspeed and heading messages have subtype 3 or 4.");

        intentChange = br.readBoolean(9);
        ifrCapability = br.readBoolean(10);
        navigationAccuracyCategoryEncoded = br.readByte(11, 13);

        headingStatusBit = br.readBoolean(14);
        headingEncoded = br.readShort(15, 24);

        trueAirspeed = br.readBoolean(25);
        airspeedEncoded = br.readShort(26, 35);

        verticalSource = br.readBoolean(36);
        verticalRateDown = br.readBoolean(37);
        verticalRateEncoded = br.readShort(38, 46);

        diffBaroAltNegative = br.readBoolean(49);
        diffBaroAltEncoded = br.readByte(50, 56);
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
    public boolean hasChangeIntent() {
        return intentChange;
    }

    @Override
    public boolean hasIFRCapability() {
        return ifrCapability;
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
        return "AirspeedHeadingV1Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", intentChange=" + intentChange +
                ", ifrCapability=" + ifrCapability +
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
