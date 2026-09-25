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
 * Decoder for the Mode S short air-air ACAS reply (DF=0), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.8.2.
 */
public class ShortACAS extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = -8867923868755627826L;

    private boolean verticalStatus; // 0 = airborne, 1 = on the ground
    private boolean crossLinkCapability;
    private byte sensitivityLevel;
    private byte replyInformationEncoded;
    private short altitudeEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ShortACAS() {
    }

    /**
     * @param rawMessage raw short air-air acas reply as hex string
     * @throws BadFormatException     if message is not altitude reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ShortACAS(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw short air-air acas reply as byte array
     * @throws BadFormatException     if message is not altitude reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ShortACAS(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this short air-air acas reply
     * @throws BadFormatException if message is not short air-air acas reply or
     *                            contains wrong values.
     */
    public ShortACAS(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 0)
            throw new BadFormatException("Message is not a short ACAS (air-air) message");

        BitReader b = getBitReader();
        verticalStatus = b.readBoolean(6);
        crossLinkCapability = b.readBoolean(7);
        sensitivityLevel = b.readByte(9, 11);
        replyInformationEncoded = b.readByte(14, 17);
        altitudeEncoded = b.readShort(20, 32);
    }

    /**
     * @return true if aircraft is airborne, false if it is on the ground
     */
    public boolean isAirborne() {
        return !verticalStatus;
    }

    /**
     * Note: cross-link cabability is the ability to support decoding the contents
     * of the DS field in an interrogation with UF equals
     * 0 and respond with the contents of the specified GICB register in the
     * corresponding reply with DF equals 16.
     *
     * @return true if aircraft has the cross-link capability
     *
     */
    public boolean hasCrossLinkCapability() {
        return crossLinkCapability;
    }

    /**
     * @return the sensitivity level at which ACAS is currently operating
     */
    public byte getSensitivityLevel() {
        return sensitivityLevel;
    }

    /**
     * This field is used to report the aircraft's maximum cruising
     * true airspeed capability and TCAS capabilities. Capabilities are, as ICAO Annex 10 Volume IV
     * §4.3.8.4.1.2 defines them:<br>
     * <ul>
     *     <li>code 2: On-board TCAS with resolution capability inhibited</li>
     *     <li>code 3: On-board TCAS with vertical-only resolution capability</li>
     *     <li>code 4: On-board TCAS with vertical and horizontal resolution capability</li>
     * </ul>
     *
     * @return the air-to-air reply information according to ICAO Annex 10 Volume IV §3.1.2.8.2.2
     * @see #getMaximumAirspeed()
     * @see #hasOperatingACAS()
     * @see #hasHorizontalResolutionCapability()
     * @see #hasVerticalResolutionCapability()
     */
    public byte getReplyInformationEncoded() {
        return replyInformationEncoded;
    }

    /**
     * @return whether a/c has operating ACAS (derived from reply information)
     * @see #getReplyInformationEncoded()
     */
    public boolean hasOperatingACAS() {
        return getReplyInformationEncoded() != 0;
    }

    /**
     * @return the maximum airspeed in kn as specified in ICAO Annex 10 Volume IV §3.1.2.8.2.2<br>
     * null if unknown<br>Integer.MAX_VALUE if unbound
     */
    public Integer getMaximumAirspeed() {
        return decodeMaximumAirspeed(getReplyInformationEncoded());
    }

    static Integer decodeMaximumAirspeed(byte replyInformationEncoded) {
        switch (replyInformationEncoded) {
            case 9:
                return 75;
            case 10:
                return 150;
            case 11:
                return 300;
            case 12:
                return 600;
            case 13:
                return 1200;
            case 14:
                return Integer.MAX_VALUE;
            default:
                return null;
        }
    }

    /**
     * Derived from the reply information codes 0–7, ICAO Annex 10 Volume IV §4.3.8.4.1.2.
     *
     * @return true if vertical resolution capability announced; false if explicitly not available; null if information
     * not provided in this reply
     */
    public Boolean hasVerticalResolutionCapability() {
        switch (replyInformationEncoded) {
            case 0:
            case 1:
            case 2:
                return false;
            case 3:
            case 4:
                return true;
            default:
                return null;
        }
    }

    /**
     * Derived from the reply information codes 0–7, ICAO Annex 10 Volume IV §4.3.8.4.1.2.
     *
     * @return true if horizontal resolution capability announced; false if explicitly not available; null if
     * information not provided in this reply
     */
    public Boolean hasHorizontalResolutionCapability() {
        switch (replyInformationEncoded) {
            case 0:
            case 1:
            case 2:
            case 3:
                return false;
            case 4:
                return true;
            default:
                return null;
        }
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
        return "ShortACAS{" + super.toString() +
                ", verticalStatus=" + verticalStatus +
                ", crossLinkCapability=" + crossLinkCapability +
                ", sensitivityLevel=" + sensitivityLevel +
                ", replyInformationEncoded=" + replyInformationEncoded +
                ", altitudeEncoded=" + altitudeEncoded +
                '}';
    }

}
