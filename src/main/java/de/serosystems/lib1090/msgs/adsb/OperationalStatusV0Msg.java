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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-B operational status message as specified in DO-260 (ADS-B version 0).
 */
public class OperationalStatusV0Msg extends ExtendedSquitter implements Serializable, OperationalStatusMsg {

    private static final long serialVersionUID = -8925123066831152922L;

    private byte enrouteCapabilities;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected OperationalStatusV0Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public OperationalStatusV0Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public OperationalStatusV0Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version or enroute capabilities
     *                                are invalid
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public OperationalStatusV0Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);
        setType(subtype.ADSB_STATUS_V0);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(41, 43) != 0)
            throw new BadFormatException("Not a DO-260/version 0 status message.");

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 0) // all others are reserved
            throw new UnspecifiedFormatError("Operational status message subtype " + subtypeCode + " reserved.");

        enrouteCapabilities = b.readByte(9, 16);
        if (b.readByte(9, 10) != 0)
            throw new BadFormatException("Unknown enroute capabilities code!");
        // All other capability fields are "TBD" in standard
    }

    /**
     * DO-260 2.2.3.2.7.3.3.1
     *
     * @return true if TCAS is operational or unknown, false if TCAS is not operational.
     */
    public boolean hasOperationalTCAS() {
        return (enrouteCapabilities & 0x20) == 0;
    }

    /**
     * DO-260 2.2.3.2.7.3.3.1
     *
     * @return true if CDTI is operational or unknown, false if CDTI is not operational.
     */
    public boolean hasOperationalCDTI() {
        return (enrouteCapabilities & 0x10) != 0;
    }

    /**
     * @return whether 1090ES IN is available
     * @see #hasOperationalCDTI() alias: the field has been renamed in V1
     */
    @Override
    public boolean has1090ESIn() {
        return hasOperationalCDTI();
    }

    @Override
    public byte getVersion() {
        return 0;
    }

    @Override
    public String toString() {
        return "OperationalStatusV0Msg{" + super.toString() +
                ", enrouteCapabilities=" + enrouteCapabilities +
                '}';
    }
}
