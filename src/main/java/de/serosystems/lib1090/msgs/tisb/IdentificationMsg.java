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
import de.serosystems.lib1090.decoding.Identification;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for TIS-B Identification and Category Message (DO-260B, 2.2.17.3.3).
 */
public class IdentificationMsg extends ExtendedSquitter implements Serializable, de.serosystems.lib1090.msgs.squitter.IdentificationMsg {

    private static final long serialVersionUID = 6991597271679287771L;

    private byte emitterCategory;
    private long identificationEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected IdentificationMsg() {
    }

    /**
     * @param rawMessage raw TIS-B identification and category message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public IdentificationMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw TIS-B identity and category message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public IdentificationMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the identity and category message
     * @throws BadFormatException if message has wrong format
     */
    public IdentificationMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18.");

        if (getFormatTypeCode() < 1 || getFormatTypeCode() > 4)
            throw new BadFormatException("Identification messages must have typecode of 1-4.");

        // Table 2-13
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

        BitReader b = BitReader.forBigEndian(getMessage());
        emitterCategory = b.readByte(6, 8);
        identificationEncoded = b.readLong(9, 56);
    }

    @Override
    public byte getEmitterCategoryEncoded() {
        return emitterCategory;
    }

    @Override
    public long getIdentificationEncoded() {
        return identificationEncoded;
    }

    @Override
    public String getEmitterCategory() {
        // TIS-B messages carry no ADS-B version information; TIS-B was introduced with v1, so fall back on that
        return Identification.categoryDescription(getFormatTypeCode(), emitterCategory, 1);
    }

    @Override
    public String toString() {
        return "IdentificationMsg{" + super.toString() +
                ", categorySet=" + getCategorySet() +
                ", emitterCategory=" + emitterCategory +
                ", identificationEncoded=" + identificationEncoded +
                '}';
    }

}
