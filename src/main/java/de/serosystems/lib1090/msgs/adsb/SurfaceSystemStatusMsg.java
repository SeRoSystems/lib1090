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
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for Surface System Status messages, as defined in ED-102B §2.2.3.2.7.4: TYPE Code=24, Subtype=1
 * ("Surface System Status (Allocated for national use)", ED-102B §2.2.3.2.7.4.2 TABLE 2-74). Version 2
 * named the same subtype "Multilateration System Status (Allocated for national use)", ED-102A Table 2-77.
 * The standard reserves this message for exclusive use by surface surveillance systems and states "there
 * is no provision in these MOPS to transmit or receive" it (ED-102B §2.2.3.2.7.4.3); its 48-bit content,
 * ME bits 9-56, "may be defined by the system equipment manufacturer" (ED-102B §2.2.3.2.7.4.3.1), so it
 * is available only raw, as {@link #getSurfaceSystemStatusEncoded()}. (ED-102B §2.2.19 "Traffic Uplink
 * Management Message" is a distinct DF=18/CF=4 ground-uplink advisory service and does not cover this
 * message; it is not TYPE=28 in any subtype.)
 */
public class SurfaceSystemStatusMsg extends TypeCodedExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = 4597102504845213202L;

    private long surfaceSystemStatus;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceSystemStatusMsg() {
    }

    /**
     * @param rawMessage the surface system status message in hex representation
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceSystemStatusMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage the surface system status message as byte array
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceSystemStatusMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this surface system status message
     * @throws BadFormatException if message has the wrong typecode
     */
    public SurfaceSystemStatusMsg(TypeCodedExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 24)
            throw new BadFormatException("Surface System Status messages must have typecode of 24");

        BitReader b = BitReader.forBigEndian(getMessage());

        int messageSubtype = b.readByte(6, 8);
        if (messageSubtype != 1)
            throw new BadFormatException("Surface System Status messages must have subtype 1");

        surfaceSystemStatus = b.readLong(9, 56);
    }

    /**
     * @return the "Surface System Status" subfield, ME bits 9-56, as the lower 48 bits; its content is defined by
     * the system equipment manufacturer, ED-102B §2.2.3.2.7.4.3.1
     */
    public long getSurfaceSystemStatusEncoded() {
        return surfaceSystemStatus;
    }

    @Override
    public String toString() {
        return "SurfaceSystemStatusMsg{" + super.toString() +
                ", surfaceSystemStatus=" + String.format("%012x", surfaceSystemStatus) +
                '}';
    }

}
