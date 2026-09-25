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

import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * Decoder for the Mode S surveillance altitude reply (DF=4), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.6.5.
 */
@SuppressWarnings("unused")
public class AltitudeReply extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 190338580932294046L;

    private byte flightStatusEncoded;
    private byte downlinkRequest;
    private byte utilityMsgEncoded;
    private short altitudeEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AltitudeReply() {
    }

    /**
     * @param rawMessage raw altitude reply as hex string
     * @throws BadFormatException     if message is not altitude reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AltitudeReply(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw altitude reply as byte array
     * @throws BadFormatException     if message is not altitude reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AltitudeReply(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this altitude reply
     * @throws BadFormatException if message is not altitude reply or
     *                            contains wrong values.
     */
    public AltitudeReply(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 4)
            throw new BadFormatException("Message is not an altitude reply");

        BitReader b = getBitReader();
        flightStatusEncoded = b.readByte(6, 8);
        downlinkRequest = b.readByte(9, 13);
        utilityMsgEncoded = b.readByte(14, 19);
        altitudeEncoded = b.readShort(20, 32);
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
     * Note that this is not the same identifier as the one contained in all-call replies.
     *
     * @return the 4-bit interrogator identifier subfield of the
     * utility message which reports the identifier of the
     * interrogator that is reserved for multisite communications.
     */
    public byte getInterrogatorIdentifier() {
        return (byte) ((utilityMsgEncoded >>> 2) & 0xF);
    }

    /**
     * Assigned coding is:<br>
     * 0 signifies no information<br>
     * 1 signifies IIS contains Comm-B II code<br>
     * 2 signifies IIS contains Comm-C II code<br>
     * 3 signifies IIS contains Comm-D II code<br>
     *
     * @return the 2-bit identifier designator subfield of the
     * utility message which reports the type of reservation made
     * by the interrogator identified in
     * {@link #getInterrogatorIdentifier() getInterrogatorIdentifier}.
     */
    public byte getIdentifierDesignator() {
        return (byte) (utilityMsgEncoded & 0x3);
    }

    /**
     * @return The 13 bits altitude code, see ICAO Annex 10 Volume IV §3.1.2.6.5.4
     */
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    /**
     * @return the decoded altitude in feet or null if not available
     */
    public Integer getAltitude() {
        return Altitude.decode13BitAltitude(altitudeEncoded);
    }

    /**
     * Decode Q bit for the altitude according to ICAO Annex 10 Volume IV §3.1.2.6.5.4
     *
     * @return value of the Q bit, false if altitude is not available or the M bit is set
     */
    public boolean hasQBit() {
        return Altitude.decode13BitQBit(altitudeEncoded);
    }

    @Override
    public String toString() {
        return "AltitudeReply{" + super.toString() +
                ", flightStatusEncoded=" + flightStatusEncoded +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsgEncoded=" + utilityMsgEncoded +
                ", altitudeEncoded=" + altitudeEncoded +
                '}';
    }

}
