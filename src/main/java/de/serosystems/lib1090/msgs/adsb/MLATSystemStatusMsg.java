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
 * Decoder for Surface System Status messages (2.2.3.2.7.4)
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
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public MLATSystemStatusMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage the MLAT system status message as byte array
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public MLATSystemStatusMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this identification msg
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
