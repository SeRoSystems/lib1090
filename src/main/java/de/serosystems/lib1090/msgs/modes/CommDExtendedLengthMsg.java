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
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * Decoder for the Mode S Comm-D Extended Length Message (DF=24), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.7.3.
 */
@SuppressWarnings("unused")
public class CommDExtendedLengthMsg extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 8539282448043078992L;

    private byte[] message;
    private boolean ack;
    private byte sequenceNumber;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected CommDExtendedLengthMsg() {
    }

    /**
     * @param rawMessage raw comm-d extended len msg as hex string
     * @throws BadFormatException     if message is not extended len msg or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public CommDExtendedLengthMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw comm-d extended len msg as byte array
     * @throws BadFormatException     if message is not extended len msg or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public CommDExtendedLengthMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply which contains this extended len msg
     * @throws BadFormatException if message is not extended len msg or
     *                            contains wrong values.
     */
    public CommDExtendedLengthMsg(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 24)
            throw new BadFormatException("Message is not an extended length message");

        BitReader b = getBitReader();
        ack = b.readBoolean(4);
        sequenceNumber = b.readByte(5, 8);

        // extract Comm-D extended length message
        message = getPayload();
    }

    /**
     * The Comm-D extended length message.
     * <p>
     * <b>The array is not a copy and must not be modified:</b> it is this message's own, shared with every
     * message decoded or copied from it, so a change would alter all of them. It is not copied for
     * performance.
     *
     * @return the 10-byte Comm-D extended length message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * @return true if this is an uplink ELM acknowledgment, i.e. the KE bit is set
     */
    public boolean isAck() {
        return ack;
    }

    /**
     * @return the number of the message segment returned by {@link #getMessage()}, i.e. the 4-bit ND
     * field, 0 to 15
     */
    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public String toString() {
        return "CommDExtendedLengthMsg{" + super.toString() +
                ", ack=" + ack +
                ", sequenceNumber=" + sequenceNumber +
                ", message=" + Tools.toHexString(message) +
                '}';
    }

}
