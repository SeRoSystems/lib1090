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
import de.serosystems.lib1090.msgs.acas.RAMessageFormat;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisories;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisoryState;

import java.io.Serializable;

/**
 * Decoder for the Mode S long air-air ACAS reply (DF=16), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.8.3.
 */
public class LongACAS extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 1052613416840618986L;

    private boolean verticalStatus; // 0 = airborne, 1 = on the ground
    private byte sensitivityLevel;
    private byte replyInformationEncoded;
    private short altitudeEncoded;
    private byte vds; // V-definition subfield of MV, 0x30 for a coordination reply

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected LongACAS() {
    }

    /**
     * @param rawMessage raw long air-to-air ACAS reply as hex string
     * @throws BadFormatException     if message is not long air-to-air ACAS reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public LongACAS(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw long air-to-air ACAS reply as byte array
     * @throws BadFormatException     if message is not long air-to-air ACAS reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public LongACAS(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this long air-to-air ACAS reply
     * @throws BadFormatException if message is not long air-to-air ACAS reply or
     *                            contains wrong values.
     */
    public LongACAS(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 16)
            throw new BadFormatException("Message is not a long ACAS (air-air) message");

        BitReader b = getBitReader();
        verticalStatus = b.readBoolean(6);
        sensitivityLevel = b.readByte(9, 11);
        replyInformationEncoded = b.readByte(14, 17);
        altitudeEncoded = b.readShort(20, 32);

        // MV/air-air coordination info; see ICAO Annex 10 Volume IV §4.3.8.4.2.4
        vds = b.readByte(33, 40);
    }

    /**
     * @return true if MV carries a coordination reply (VDS = 0x30), ICAO Annex 10 Volume IV §4.3.8.4.2.4.2; check this
     * before using {@link #getResolutionAdvisory()}
     */
    public boolean hasValidRAC() {
        return vds == 0x30;
    }

    /**
     * @return message bits 41-88 of MV as transmitted, right-aligned
     */
    public long getResolutionAdvisoryEncoded() {
        return getBitReader().readLong(41, 88);
    }

    /**
     * @return the collision avoidance system that generated the coordination reply, and so its layout, bits 53-54;
     * only meaningful if {@link #hasValidRAC()}
     */
    public RAMessageFormat getRAMessageFormat() {
        return ResolutionAdvisories.messageFormat(getResolutionAdvisoryEncoded());
    }

    /**
     * The coordination reply, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.4.2: ARA, RAC, RAT and MTE, in the
     * TCAS layout, §4.3.8.4.2.4.2.1, or in the ACAS X layout, which adds the low-level descend inhibit,
     * §4.3.8.4.2.4.2.2.
     *
     * @return a {@link de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisory} or an
     * {@link de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisory}, or null if MV carries no coordination reply
     * (see {@link #hasValidRAC()}) or its RA message format has no defined layout
     * @see ResolutionAdvisories#coordinationReply(long)
     */
    public ResolutionAdvisoryState getResolutionAdvisory() {
        if (!hasValidRAC()) return null;
        return ResolutionAdvisories.coordinationReply(getResolutionAdvisoryEncoded());
    }

    /**
     * @return true if aircraft is airborne, false if it is on the ground
     */
    public boolean isAirborne() {
        return !verticalStatus;
    }

    /**
     * @return the sensitivity level at which ACAS is currently operating
     */
    public byte getSensitivityLevel() {
        return sensitivityLevel;
    }

    /**
     * This field is used to report the aircraft's maximum cruising
     * true airspeed capability and type of reply to interrogating aircraft
     *
     * @return the air-to-air reply information according to ICAO Annex 10 Volume IV §3.1.2.8.2.2
     * @see #getMaximumAirspeed()
     * @see #hasOperatingACAS()
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
     * @return the maximum airspeed in kt as specified in ICAO Annex 10 Volume IV §3.1.2.8.2.2<br>
     * null if unknown<br>Integer.MAX_VALUE if unbound
     */
    public Integer getMaximumAirspeed() {
        return ShortACAS.decodeMaximumAirspeed(getReplyInformationEncoded());
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
        return "LongACAS{" + super.toString() +
                ", verticalStatus=" + verticalStatus +
                ", sensitivityLevel=" + sensitivityLevel +
                ", replyInformationEncoded=" + replyInformationEncoded +
                ", altitudeEncoded=" + altitudeEncoded +
                ", vds=" + vds +
                ", resolutionAdvisory=" + getResolutionAdvisory() +
                '}';
    }

}
