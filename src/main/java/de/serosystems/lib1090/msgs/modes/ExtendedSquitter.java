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
import java.util.Arrays;

/**
 * Decoder for the Mode S extended squitter envelope (DF=17/18/19), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.8.6 (DF=17), §3.1.2.8.7 (DF=18) and §3.1.2.8.8 (DF=19).
 * <p>
 * Most extended squitters carry an ME field that starts with a format type code; they are
 * {@link TypeCodedExtendedSquitter}s. Those that don't, such as military extended squitters and the
 * TIS-B coarse position, extend this class directly.
 */
public class ExtendedSquitter extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 3142071849543499458L;

    private byte[] message;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ExtendedSquitter() {
    }

    /**
     * @param rawMessage raw extended squitter as hex string
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ExtendedSquitter(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw extended squitter as byte array
     * @throws BadFormatException     if message is not extended squitter or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
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

        if (getDownlinkFormat() < 17 || getDownlinkFormat() > 19)
            throw new BadFormatException("Message is not an extended squitter");

        // message bits 33-88
        message = Arrays.copyOfRange(getPayload(), 3, 10);
    }

    /**
     * Copy constructor for subclasses
     *
     * @param squitter instance of ExtendedSquitter to copy from
     */
    public ExtendedSquitter(ExtendedSquitter squitter) {
        super(squitter);

        message = squitter.getMessage();
    }

    /**
     * Message bits 33-88, which are the ME field for most extended squitters.
     * <p>
     * <b>The array is not a copy and must not be modified:</b> it is this message's own, shared with every
     * message decoded or copied from it, so a change would alter all of them. It is not copied for
     * performance.
     *
     * @return message bits 33-88 as 7-byte array
     */
    public byte[] getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ExtendedSquitter{" + super.toString() +
                ", message=" + Tools.toHexString(message) +
                '}';
    }

}
