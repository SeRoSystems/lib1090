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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.EmergencyOrPriorityStatusMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-R version 1 emergency and priority status messages
 */
public class EmergencyOrPriorityStatusV1Msg extends ExtendedSquitter implements Serializable, EmergencyOrPriorityStatusMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 1154983892373289756L;

    private static final byte SUBTYPE = 1;

    private byte emergencyState;
    private boolean imf;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected EmergencyOrPriorityStatusV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public EmergencyOrPriorityStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public EmergencyOrPriorityStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this emergency or priority status msg
     * @throws BadFormatException if message has wrong format
     */
    public EmergencyOrPriorityStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 28)
            throw new BadFormatException("Emergency and Priority Status messages must have typecode 28.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("Emergency and priority status reports have subtype 1.");

        emergencyState = b.readByte(9, 11);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
    }

    @Override
    public byte getSubtype() {
        return SUBTYPE;
    }

    @Override
    public byte getEmergencyStateCode() {
        return emergencyState;
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "EmergencyOrPriorityStatusV1Msg{" + super.toString() +
                ", emergencyState=" + emergencyState +
                ", imf=" + imf +
                '}';
    }

}
