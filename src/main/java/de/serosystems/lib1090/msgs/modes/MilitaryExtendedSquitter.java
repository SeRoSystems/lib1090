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
 * Decoder for Mode S military extended squitters (DF19)<br>
 * Note: this format is practically unspecified by ED-102B; ICAO Annex 10 Volume IV
 * §3.1.2.8.8.2 defines only the 3-bit Application Field (AF) as fully reserved
 * (values 0-7), while ICAO Doc 9871 TABLE A-2-242 (BDS code F,2, "Military
 * applications") records the AF values actually assigned in practice: 0 = reserved
 * for civil extended squitter formats, 1 = reserved for formation flight,
 * 2 = reserved for military applications, 3-7 = reserved.
 */
public class MilitaryExtendedSquitter extends ExtendedSquitter implements Serializable {

    private static final long serialVersionUID = 2459913562133769670L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected MilitaryExtendedSquitter() {
    }

    /**
     * @param rawMessage raw military extended squitter as hex string
     * @throws BadFormatException     if message is not military extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — DF=19 military
     *                                extended squitters are a growth option for military applications not
     *                                covered by ED-102B; see ICAO Annex 10 Volume IV §3.1.2.8.8.2
     */
    public MilitaryExtendedSquitter(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw military extended squitter as byte array
     * @throws BadFormatException     if message is not military extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — DF=19 military
     *                                extended squitters are a growth option for military applications not
     *                                covered by ED-102B; see ICAO Annex 10 Volume IV §3.1.2.8.8.2
     */
    public MilitaryExtendedSquitter(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this military extended squitter
     * @throws BadFormatException if message is not a military extended squitter
     */
    public MilitaryExtendedSquitter(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 19)
            throw new BadFormatException("Message is not a military extended squitter!");
    }

    /**
     * Copy constructor for subclasses
     *
     * @param squitter instance of MilitaryExtendedSquitter to copy from
     */
    public MilitaryExtendedSquitter(MilitaryExtendedSquitter squitter) {
        super(squitter);
    }

}
