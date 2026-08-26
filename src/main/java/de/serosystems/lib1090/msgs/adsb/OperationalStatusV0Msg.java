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
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.CapabilityClassCodes;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusMsg;
import de.serosystems.lib1090.msgs.squitter.opstatus.UndefinedOperationalModeCode;

import java.io.Serializable;

/**
 * Decoder for the ADS-B operational status message, as defined in ED-102 (ADS-B version 0).
 */
public class OperationalStatusV0Msg extends ExtendedSquitter implements Serializable, OperationalStatusMsg, ADSBMsg {

    private static final long serialVersionUID = 4280693148589377648L;

    private int capabilityClassCode; // "CC4", ME 9-12
    private int operationalModeCode; // reserved in full for version 0, ME 25-40

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected OperationalStatusV0Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public OperationalStatusV0Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public OperationalStatusV0Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-B version or enroute capabilities
     *                            are invalid
     */
    public OperationalStatusV0Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(41, 43) != 0)
            throw new BadFormatException("Not a DO-260/version 0 status message");

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 0) // all others are reserved
            throw new BadFormatException("Operational status message subtype " + subtypeCode + " reserved");

        capabilityClassCode = b.readInt(9, 12);
        operationalModeCode = b.readInt(25, 40);
    }

    @Override
    public byte getMOPSVersion() {
        return 0;
    }

    @Override
    public String toString() {
        return "OperationalStatusV0Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                '}';
    }

    @Override
    public int getCapabilityClassCodeEncoded() {
        return capabilityClassCode;
    }

    @Override
    public CapabilityClassCode getCapabilityClass() {
        return CapabilityClassCodes.adsbAirborneV0(capabilityClassCode);
    }

    @Override
    public int getOperationalModeCodeEncoded() {
        return operationalModeCode;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Version 0 defines no Operational Mode Code: ME 25–40 holds four reserved "En Route Operational
     * Capability Status" fields. There is therefore nothing to select a layout with, and no layout to
     * select.
     *
     * @return always an {@link UndefinedOperationalModeCode} over the reserved field
     */
    @Override
    public OperationalModeCode getOperationalMode() {
        return new UndefinedOperationalModeCode(operationalModeCode);
    }
}
