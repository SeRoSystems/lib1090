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

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * Decoder for extended squitters whose ME field starts with a format type code: ADS-B (DF=17, DF=18 with
 * CF=0/1 and DF=19 with AF=0), fine TIS-B (DF=18 with CF=2/5) and ADS-R (DF=18 with CF=6).
 * The format type code decoded here is the "TC"/"Subtype" subfield defined in ED-102B
 * §2.2.3.2.2 TABLE 2-9; payload content is specified per format type code in ED-102B §2.2.3.2.3
 * through §2.2.3.2.7.
 */
public class TypeCodedExtendedSquitter extends ExtendedSquitter implements Serializable {

    private static final long serialVersionUID = 8396282390353645782L;

    private byte formatTypeCode;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TypeCodedExtendedSquitter() {
    }

    /**
     * @param rawMessage raw extended squitter as hex string
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public TypeCodedExtendedSquitter(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw extended squitter as byte array
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public TypeCodedExtendedSquitter(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this extended squitter
     * @throws BadFormatException if message is not extended squitter with a format type code or
     *                            contains wrong values.
     */
    public TypeCodedExtendedSquitter(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if ((getDownlinkFormat() == 18 && (getFirstField() == 3 || getFirstField() == 4 || getFirstField() == 7)) ||
                (getDownlinkFormat() == 19 && getFirstField() > 0))
            throw new BadFormatException("Message is not an extended squitter with a format type code");

        formatTypeCode = getBitReader().readByte(33, 37);
    }

    /**
     * Copy constructor for subclasses
     *
     * @param squitter instance of TypeCodedExtendedSquitter to copy from
     */
    public TypeCodedExtendedSquitter(TypeCodedExtendedSquitter squitter) {
        super(squitter);

        formatTypeCode = squitter.getFormatTypeCode();
    }

    /**
     * @return The message's format type code, see ICAO Annex 10 Volume IV §3.1.2.8.6
     */
    public byte getFormatTypeCode() {
        return formatTypeCode;
    }

    @Override
    public String toString() {
        return "TypeCodedExtendedSquitter{" + super.toString() +
                ", formatTypeCode=" + formatTypeCode +
                '}';
    }

}
