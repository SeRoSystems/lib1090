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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for TIS-B/ADS-R Management Message, as defined in ED-102B §2.2.19.2.3.
 */
public class ManagementMessage extends ExtendedSquitter implements Serializable, TISBMsg {

    private static final long serialVersionUID = 6266047064873866100L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ManagementMessage() {
    }

    /**
     * @param rawMessage raw TIS-B identification and category message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if the underlying extended squitter has a format that is not further
     *                                 specified (see {@link ExtendedSquitter#ExtendedSquitter(String)}); the
     *                                 Management Message Bit Field itself is fully enumerated by ED-102B
     *                                 §2.2.19.2.3 TABLE 2-187, with values 8-31 coded as reserved for future service
     */
    public ManagementMessage(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw TIS-B identity and category message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if the underlying extended squitter has a format that is not further
     *                                 specified (see {@link ExtendedSquitter#ExtendedSquitter(byte[])}); the
     *                                 Management Message Bit Field itself is fully enumerated by ED-102B
     *                                 §2.2.19.2.3 TABLE 2-187, with values 8-31 coded as reserved for future service
     */
    public ManagementMessage(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the identity and category message
     * @throws BadFormatException if message has wrong format
     */
    public ManagementMessage(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18.");

        // ED-102B §2.2.17.2 TABLE 2-184
        if (getFirstField() != 4)
            throw new BadFormatException("TIS-B management messages must have CF value 6.");

        // not specified further
    }

    @Override
    public String toString() {
        return "ManagementMessage{" + super.toString() + '}';
    }

}
