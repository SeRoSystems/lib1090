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

import de.serosystems.lib1090.msgs.squitter.ModeACodeMsg;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-R emergency and priority status messages
 */
public class EmergencyOrPriorityStatusMsg extends ExtendedSquitter implements Serializable, de.serosystems.lib1090.msgs.squitter.EmergencyOrPriorityStatusMsg, ModeACodeMsg {

    private static final long serialVersionUID = 2611795026824285668L;

    private static final byte SUBTYPE = 1;

    private byte emergencyState;
    private short modeACode;
    private boolean imf;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected EmergencyOrPriorityStatusMsg() {
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public EmergencyOrPriorityStatusMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public EmergencyOrPriorityStatusMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this emergency or priority status msg
     * @throws BadFormatException if message has wrong format
     */
    public EmergencyOrPriorityStatusMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 28) {
            throw new BadFormatException("Emergency and Priority Status messages must have typecode 28.");
        }

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE) {
            throw new BadFormatException("Emergency and priority status reports have subtype 1.");
        }

        emergencyState = b.readByte(9, 11);
        modeACode = b.readShort(12, 24);
        imf = b.readByte(56, 56) == 1;
    }

    @Override
    public byte getSubtype() {
        return SUBTYPE;
    }

    @Override
    public byte getEmergencyStateCode() {
        return emergencyState;
    }

    /**
     * @return the four-digit Mode A (4096) code (only ADS-R version 2)
     */
    @Override
    public short getModeACode() {
        return modeACode;
    }

    /**
     * @return the ICAO Mode A Flag (for address type determination)
     */
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "EmergencyOrPriorityStatusMsg{" + super.toString() +
                ", emergencyState=" + emergencyState +
                ", modeACode=" + modeACode +
                ", imf=" + imf +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_EMERGENCY;
    }
}
