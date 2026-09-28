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
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for the TIS-B/ADS-R Traffic Uplink Management Message, DF=18 with CF=4, as defined in ED-102B
 * §2.2.19.2 Figure 2-67.
 * <p>
 * The message is counted as an extended squitter but has neither its address nor its ME field. Bits 9-13
 * are the Management Message Bit Field, which says which services are active, and bits 14-88 a payload
 * whose content depends on it, such as the TIS-B Service Status Message of §2.2.19.2.4.1. The payload
 * is not decoded. So the address this message reports is its bits 9-32, the bit field and the start of
 * the payload, and {@link #getMessage()} holds only bits 33-88.
 */
public class ManagementMessage extends ExtendedSquitter implements Serializable, TISBMsg {

    private static final long serialVersionUID = 6266047064873866100L;

    private byte managementMessageBitFieldEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ManagementMessage() {
    }

    /**
     * @param rawMessage raw TIS-B/ADS-R management message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ManagementMessage(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw TIS-B/ADS-R management message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ManagementMessage(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing the management message
     * @throws BadFormatException if message has wrong format
     */
    public ManagementMessage(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18");

        // ED-102B §2.2.17.2 TABLE 2-184
        if (getFirstField() != 4)
            throw new BadFormatException("TIS-B management messages must have CF value 4");

        managementMessageBitFieldEncoded = getBitReader().readByte(9, 13);
    }

    /**
     * @return the Management Message Bit Field, ED-102B §2.2.19.2.3 TABLE 2-187
     */
    public byte getManagementMessageBitFieldEncoded() {
        return managementMessageBitFieldEncoded;
    }

    /**
     * @return whether the TIS-B service is active, bit 0 of the Management Message Bit Field; the payload
     * then carries the TIS-B Service Status Message
     */
    public boolean isTISBServiceActive() {
        return (managementMessageBitFieldEncoded & 0x1) != 0;
    }

    /**
     * @return whether the ADS-R service is active, bit 1 of the Management Message Bit Field
     */
    public boolean isADSRServiceActive() {
        return (managementMessageBitFieldEncoded & 0x2) != 0;
    }

    /**
     * @return whether the ADS-SLR service is active, bit 2 of the Management Message Bit Field
     */
    public boolean isADSSLRServiceActive() {
        return (managementMessageBitFieldEncoded & 0x4) != 0;
    }

    @Override
    public String toString() {
        return "ManagementMessage{" + super.toString() +
                ", managementMessageBitFieldEncoded=" + managementMessageBitFieldEncoded +
                '}';
    }

}
