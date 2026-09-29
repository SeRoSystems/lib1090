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
import de.serosystems.lib1090.decoding.Identity;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Decoder for the Mode S surveillance identity reply with Comm-B message (DF=21),
 * as defined in ICAO Annex 10 Volume IV §3.1.2.6.8.
 */
@SuppressWarnings("unused")
public class CommBIdentifyReply extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = -1623942073259152603L;

    private byte flightStatusEncoded;
    private byte downlinkRequest;
    private byte utilityMsgEncoded;
    private short identityEncoded;
    private byte[] message;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected CommBIdentifyReply() {
    }

    /**
     * @param rawMessage raw comm-b identify reply as hex string
     * @throws BadFormatException     if message is not comm-b identify reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public CommBIdentifyReply(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw comm-b identify reply as byte array
     * @throws BadFormatException     if message is not comm-b identify reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public CommBIdentifyReply(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply which contains this comm-b identify reply
     * @throws BadFormatException if message is not comm-b identify reply or
     *                            contains wrong values.
     */
    public CommBIdentifyReply(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 21)
            throw new BadFormatException("Message is not a comm-b identify reply");

        BitReader b = getBitReader();
        flightStatusEncoded = b.readByte(6, 8);
        downlinkRequest = b.readByte(9, 13);
        utilityMsgEncoded = b.readByte(14, 19);
        identityEncoded = b.readShort(20, 32);

        // extract Comm-B message
        message = Arrays.copyOfRange(getPayload(), 3, 10);
    }

    /**
     * Indicates alerts, whether SPI is enabled, and if the plane is on ground.
     *
     * @return The 3 bits flight status. The coding is:<br>
     * <ul>
     * <li>0 signifies no alert and no SPI, aircraft is airborne</li>
     * <li>1 signifies no alert and no SPI, aircraft is on the ground</li>
     * <li>2 signifies alert, no SPI, aircraft is airborne</li>
     * <li>3 signifies alert, no SPI, aircraft is on the ground</li>
     * <li>4 signifies alert and SPI, aircraft is airborne or on the ground</li>
     * <li>5 signifies no alert and SPI, aircraft is airborne or on the ground</li>
     * <li>6 reserved</li>
     * <li>7 not assigned</li>
     * </ul>
     * @see #hasAlert()
     * @see #hasSPI()
     * @see #isAirborne()
     */
    public byte getFlightStatusEncoded() {
        return flightStatusEncoded;
    }

    /**
     * @return whether flight status indicates alert
     */
    public boolean hasAlert() {
        return flightStatusEncoded >= 2 && flightStatusEncoded <= 4;
    }

    /**
     * @return whether flight status indicates special purpose indicator
     */
    public boolean hasSPI() {
        return flightStatusEncoded == 4 || flightStatusEncoded == 5;
    }

    /**
     * Whether flight status indicates that the aircraft is airborne.
     *
     * @return true if airborne, false if on ground or null if ground status is unknown
     */
    public Boolean isAirborne() {
        if (flightStatusEncoded == 0 || flightStatusEncoded == 2) {
            return true;
        } else if (flightStatusEncoded == 1 || flightStatusEncoded == 3) {
            return false;
        }
        return null;
    }

    /**
     * indicator for downlink requests
     *
     * @return the 5 bits downlink request. The coding is:<br>
     * <ul>
     * <li>0 signifies no downlink request</li>
     * <li>1 signifies request to send Comm-B message</li>
     * <li>2 reserved for ACAS</li>
     * <li>3 reserved for ACAS</li>
     * <li>4 signifies Comm-B broadcast message 1 available</li>
     * <li>5 signifies Comm-B broadcast message 2 available</li>
     * <li>6 reserved for ACAS</li>
     * <li>7 reserved for ACAS</li>
     * <li>8-15 not assigned</li>
     * <li>16-31 see downlink ELM protocol, ICAO Annex 10 Volume IV §3.1.2.7.7.1</li>
     * </ul>
     */
    public byte getDownlinkRequest() {
        return downlinkRequest;
    }

    /**
     * @return The 6 bits utility message, see ICAO Annex 10 Volume IV §3.1.2.6.5.3
     */
    public byte getUtilityMsgEncoded() {
        return utilityMsgEncoded;
    }

    /**
     * Note: this is not the same identifier as the one contained in all-call replies.
     *
     * @return the 4-bit interrogator identifier subfield of the
     * utility message which reports the identifier of the
     * interrogator that is reserved for multisite communications.
     */
    public byte getInterrogatorIdentifier() {
        return (byte) ((utilityMsgEncoded >>> 2) & 0xF);
    }

    /**
     * @return the 2-bit identifier designator subfield of the
     * utility message which reports the type of reservation made
     * by the interrogator identified in
     * {@link #getInterrogatorIdentifier() getInterrogatorIdentifier}.
     * Assigned coding is:<br>
     * <ul>
     * <li>0 signifies no information</li>
     * <li>1 signifies IIS contains Comm-B II code</li>
     * <li>2 signifies IIS contains Comm-C II code</li>
     * <li>3 signifies IIS contains Comm-D II code</li>
     * </ul>
     */
    public byte getIdentifierDesignator() {
        return (byte) (utilityMsgEncoded & 0x3);
    }

    /**
     * @return The 13 bits identity code (Mode A code), see ICAO Annex 10 Volume IV §3.1.2.6.7.1
     */
    public short getIdentityEncoded() {
        return identityEncoded;
    }

    /**
     * The Comm-B message (BDS register; register numbering and content per ICAO Doc 9871 (First Edition,
     * AN/464) §A.2.1 Register Allocation — individual registers are decoded in package
     * de.serosystems.lib1090.msgs.bds).
     * <p>
     * <b>The array is not a copy and must not be modified:</b> it is this message's own, shared with every
     * message decoded or copied from it, so a change would alter all of them. It is not copied for
     * performance.
     *
     * @return the 7-byte Comm-B message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * @return The identity/Mode A code, see ICAO Annex 10 Volume IV §3.1.2.6.7.1.
     * Special codes are<br>
     * <ul>
     * <li> 7700 indicates emergency<br>
     * <li> 7600 indicates radiocommunication failure</li>
     * <li> 7500 indicates unlawful interference</li>
     * <li> 2000 indicates that transponder is not yet operated</li>
     * </ul>
     */
    public String getIdentity() {
        return Identity.decodeIdentity(identityEncoded);
    }

    @Override
    public String toString() {
        return "CommBIdentifyReply{" + super.toString() +
                ", flightStatusEncoded=" + flightStatusEncoded +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsgEncoded=" + utilityMsgEncoded +
                ", identityEncoded=" + identityEncoded +
                ", message=" + Tools.toHexString(message) +
                '}';
    }

}
