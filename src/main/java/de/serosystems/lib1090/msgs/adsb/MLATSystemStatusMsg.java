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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Decoder for Surface System Status messages, as defined in ED-102B §2.2.3.2.7.4: TYPE Code=24, Subtype=1
 * ("Surface System Status (Allocated for national use)", ED-102B §2.2.3.2.7.4.2 TABLE 2-74). The
 * standard reserves this message for exclusive use by surface surveillance systems and states
 * "there is no provision in these MOPS to transmit or receive" it (ED-102B §2.2.3.2.7.4.3); the
 * class name reflects the field-observed use of TC=24/ST=1 by multilateration (MLAT) systems
 * rather than any ED-102B terminology for the message. (ED-102B §2.2.19 "Traffic Uplink
 * Management Message" is a distinct DF=18/CF=4 ground-uplink advisory service and does not cover
 * this message; it is not TYPE=28 in any subtype.)
 */
public class MLATSystemStatusMsg extends ExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = 4597102504845213202L;

    private byte[] systemStatus;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected MLATSystemStatusMsg() {
    }

    /**
     * @param rawMessage the MLAT system status message in hex representation
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public MLATSystemStatusMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage the MLAT system status message as byte array
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public MLATSystemStatusMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this MLAT/Surface System Status msg
     * @throws BadFormatException if message has the wrong typecode
     */
    public MLATSystemStatusMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 24)
            throw new BadFormatException("MLAT system status messages must have typecode of 24.");

        byte[] msg = getMessage();
        BitReader b = BitReader.forBigEndian(msg);

        int messageSubtype = b.readByte(6, 8);
        if (messageSubtype != 1)
            throw new BadFormatException("Surface system status messages have subtype 1.");

        systemStatus = Arrays.copyOfRange(msg, 1, msg.length);
    }

    @Override
    public String toString() {
        return "MLATSystemStatusMsg{" + super.toString() +
                ", systemStatus=" + Tools.toHexString(systemStatus) +
                '}';
    }

}
