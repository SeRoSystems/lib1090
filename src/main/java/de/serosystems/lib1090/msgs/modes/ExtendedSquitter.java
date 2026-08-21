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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * Decoder for Mode S extended squitters, the DF=17/18/19 envelope defined in
 * ICAO Annex 10 Volume IV §3.1.2.8.6 (DF=17), §3.1.2.8.7 (DF=18) and §3.1.2.8.8 (DF=19).
 * The format type code decoded here is the "TC"/"Subtype" subfield defined in ED-102B
 * §2.2.3.2.2 TABLE 2-9 (DF=17/18 ADS-B/TIS-B/ADS-R); payload content is specified per
 * format type code in ED-102B §2.2.3.2.3 through §2.2.3.2.7, or, for DF=19 military
 * extended squitters, by ICAO Annex 10 Volume IV §3.1.2.8.8.2.
 */
public class ExtendedSquitter extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 8396282390353645782L;

    private byte[] message;
    private byte format_type_code;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ExtendedSquitter() {
    }

    /**
     * @param rawMessage raw extended squitter as hex string
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — the "TC"/"Subtype" subfield is defined in ED-102B §2.2.3.2.2 TABLE 2-9 with its per-type payload content in ED-102B §2.2.3.2.3 through §2.2.3.2.7 for DF=17/18 ADS-B/TIS-B/ADS-R content, or ICAO Annex 10 Volume IV §3.1.2.8.8.2 for DF=19 military extended squitters
     */
    public ExtendedSquitter(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw extended squitter as byte array
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — the "TC"/"Subtype" subfield is defined in ED-102B §2.2.3.2.2 TABLE 2-9 with its per-type payload content in ED-102B §2.2.3.2.3 through §2.2.3.2.7 for DF=17/18 ADS-B/TIS-B/ADS-R content, or ICAO Annex 10 Volume IV §3.1.2.8.8.2 for DF=19 military extended squitters
     */
    public ExtendedSquitter(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this extended squitter
     * @throws BadFormatException if message is not extended squitter or
     *                            contains wrong values.
     */
    public ExtendedSquitter(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() < 17 || getDownlinkFormat() > 19 ||
                getDownlinkFormat() == 18 && (getFirstField() == 4 || getFirstField() == 7) ||
                getDownlinkFormat() == 19 && getFirstField() > 0)
            throw new BadFormatException("Message is not an extended squitter!");

        byte[] payload = getPayload();

        // extract ADS-B message
        message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        format_type_code = (byte) ((message[0] >>> 3) & 0x1F);
    }

    /**
     * Copy constructor for subclasses
     *
     * @param squitter instance of ExtendedSquitter to copy from
     */
    public ExtendedSquitter(ExtendedSquitter squitter) {
        super(squitter);

        message = squitter.getMessage();
        format_type_code = squitter.getFormatTypeCode();
    }

    /**
     * @return The message's format type code, see ICAO Annex 10 Volume IV §3.1.2.8.6
     */
    public byte getFormatTypeCode() {
        return format_type_code;
    }

    /**
     * @return The message as 7-byte array
     */
    public byte[] getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tExtendedSquitter{" +
                "message=" + Tools.toHexString(message) +
                ", format_type_code=" + format_type_code +
                '}';
    }

}
