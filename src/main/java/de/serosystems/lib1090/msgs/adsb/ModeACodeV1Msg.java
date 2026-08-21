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
import de.serosystems.lib1090.msgs.squitter.ModeACodeMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 1 Mode A code messages. The standards-defined Mode A Code field
 * lives in the Extended Squitter Aircraft Status Message, TYPE=28 Subtype=1 (Emergency/Priority
 * Status Message), ED-102B §2.2.3.2.7.8.1 Figure 2-20; the "Mode A Code" subfield itself is
 * specified in §2.2.3.2.7.8.1.2. See the constructors below for why this class instead decodes
 * an out-of-specification TYPE=23 Subtype=7 field. (ED-102B §2.2.19 "Traffic Uplink Management
 * Message" is a distinct DF=18/CF=4 ground-uplink advisory service and does not cover this field.)
 */
public class ModeACodeV1Msg extends ExtendedSquitter implements Serializable, ModeACodeMsg, ADSBMsg {

    private static final long serialVersionUID = 5076521250730290369L;

    private byte messageSubtype;
    private short modeACode;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ModeACodeV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-B Mode A code message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if the underlying squitter format is not further specified -- ED-102B
     *      §2.2.3.2.3.1 TABLE 2-11 marks TYPE=23 Subtype 1-7 Reserved, and the former standard DO-260B
     *      §2.2.3.2.3.1 TABLE 2-14 reserves the identical TYPE=23 Subtype 1-7 range, so this TYPE=23
     *      Subtype=7 "Mode A Code" encoding was never formally specified by any edition of the DO-260/ED-102
     *      series; this class decodes an out-of-specification field observed in the field. The
     *      standards-defined Mode A Code field is instead carried in the Aircraft Status Message TYPE=28
     *      Subtype=1, ED-102B §2.2.3.2.7.8.1.2 Figure 2-20
     */
    public ModeACodeV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B Mode A code message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if the underlying squitter format is not further specified -- ED-102B
     *      §2.2.3.2.3.1 TABLE 2-11 marks TYPE=23 Subtype 1-7 Reserved, and the former standard DO-260B
     *      §2.2.3.2.3.1 TABLE 2-14 reserves the identical TYPE=23 Subtype 1-7 range, so this TYPE=23
     *      Subtype=7 "Mode A Code" encoding was never formally specified by any edition of the DO-260/ED-102
     *      series; this class decodes an out-of-specification field observed in the field. The
     *      standards-defined Mode A Code field is instead carried in the Aircraft Status Message TYPE=28
     *      Subtype=1, ED-102B §2.2.3.2.7.8.1.2 Figure 2-20
     */
    public ModeACodeV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Mode A code message
     * @throws BadFormatException if message has wrong format
     */
    public ModeACodeV1Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 23)
            throw new BadFormatException("Mode A code messages must have typecode 23.");

        BitReader reader = BitReader.forBigEndian(this.getMessage());
        messageSubtype = reader.readByte(6, 8);
        if (messageSubtype != 7)
            throw new BadFormatException("Mode A code messages must have subtype 7.");

        modeACode = reader.readShort(9, 21);
    }

    /**
     * @return the subtype code of the message (should always be 7)
     */
    public byte getSubtype() {
        return messageSubtype;
    }

    /**
     * @return the four-digit Mode A (4096) code
     */
    @Override
    public short getModeACode() {
        return modeACode;
    }

    @Override
    public String toString() {
        return "ModeACodeV1Msg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", modeACode=" + modeACode +
                '}';
    }

}
