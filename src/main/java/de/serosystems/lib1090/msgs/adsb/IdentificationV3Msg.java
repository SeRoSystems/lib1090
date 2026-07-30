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
import de.serosystems.lib1090.decoding.Identification;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 3 identification messages
 */
public class IdentificationV3Msg extends ExtendedSquitter implements Serializable, IdentificationMsg {

    private static final long serialVersionUID = -3324768994718230722L;

    private byte emitterCategory;
    private long identificationEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected IdentificationV3Msg() {
    }

    /**
     * @param rawMessage the identification message in hex representation
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public IdentificationV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage the identification message as byte array
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public IdentificationV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this identification msg
     * @throws BadFormatException if message has the wrong typecode
     */
    public IdentificationV3Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        // type code 1 is no longer defined for identification messages in version 3
        if (getFormatTypeCode() < 2 || getFormatTypeCode() > 4)
            throw new BadFormatException("Version 3 identification messages must have typecode of 2-4.");

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
        return Identification.categoryDescription(getFormatTypeCode(), emitterCategory, 3);
    }

    @Override
    public String toString() {
        return "IdentificationV3Msg{" + super.toString() +
                ", categorySet=" + getCategorySet() +
                ", emitterCategory=" + emitterCategory +
                ", identificationEncoded=" + identificationEncoded +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSB_IDENTIFICATION_V3;
    }
}
