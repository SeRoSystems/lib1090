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
import de.serosystems.lib1090.msgs.squitter.IdentificationMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 0 identification messages, as defined in ED-102B §2.2.3.2.5 Figure 2-7. Individual callsign characters are
 * decoded by {@link Identification#identificationDigits(long)}, whose 6-bit IA-5 alphabet
 * is cited in ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8.
 */
public class IdentificationV0Msg extends ExtendedSquitter implements Serializable, IdentificationMsg, ADSBMsg {

    private static final long serialVersionUID = 3475444849066416732L;

    private byte emitterCategory;
    private long identificationEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected IdentificationV0Msg() {
    }

    /**
     * @param rawMessage the identification message in hex representation
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public IdentificationV0Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage the identification message as byte array
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public IdentificationV0Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this identification msg
     * @throws BadFormatException if message has the wrong typecode
     */
    public IdentificationV0Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() < 1 || getFormatTypeCode() > 4)
            throw new BadFormatException("Identification messages must have typecode of 1-4.");

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
        return Identification.categoryDescription(getFormatTypeCode(), emitterCategory, 0);
    }

    @Override
    public String toString() {
        return "IdentificationV0Msg{" + super.toString() +
                ", categorySet=" + getCategorySet() +
                ", emitterCategory=" + emitterCategory +
                ", identificationEncoded=" + identificationEncoded +
                '}';
    }

}
