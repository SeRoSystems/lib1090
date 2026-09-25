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
 * Decoder for the Mode S long air-air ACAS reply (DF=16), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.8.3.
 */
public class LongACAS extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 1052613416840618986L;

    private boolean verticalStatus; // 0 = airborne, 1 = on the ground
    private byte sensitivityLevel;
    private byte replyInformationEncoded;
    private short altitudeEncoded;
    private byte vds; // V-definition subfield of MV, 0x30 for a resolution advisory report
    private short activeResolutionAdvisories;
    private byte racRecordEncoded; // RAC = resolution advisory complement
    private boolean raTerminated;
    private boolean multipleThreatEncounter;

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
        activeResolutionAdvisories = b.readShort(41, 54);
        racRecordEncoded = b.readByte(55, 58);
        raTerminated = b.readBoolean(59);
        multipleThreatEncounter = b.readBoolean(60);
    }

    /**
     * Important note: check this before using any of
     * {@link #getActiveResolutionAdvisories()},
     * {@link #noPassBelow()}, {@link #noPassAbove()},
     * {@link #noTurnLeft()}, {@link #noTurnRight()},
     * {@link #hasTerminated()}, {@link #hasMultipleThreats()}
     *
     * @return true if resolution advisory complement is valid
     */
    public boolean hasValidRAC() {
        return vds == 0x30;
    }

    /**
     * @return the binary encoded information about active
     * resolution advisories, see ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.1
     */
    public short getActiveResolutionAdvisories() {
        return activeResolutionAdvisories;
    }

    /**
     * @return the binary encoded resolution advisory complement
     * @see #noPassBelow()
     * @see #noPassAbove()
     * @see #noTurnLeft()
     * @see #noTurnRight()
     */
    public byte getResolutionAdvisoryComplementEncoded() {
        return racRecordEncoded;
    }

    /**
     * @return true iff do not pass below advisory is active
     */
    public boolean noPassBelow() {
        return (racRecordEncoded & 8) == 8;
    }

    /**
     * @return true iff do not pass above advisory is active
     */
    public boolean noPassAbove() {
        return (racRecordEncoded & 4) == 4;
    }

    /**
     * @return true iff do not turn left advisory is active
     */
    public boolean noTurnLeft() {
        return (racRecordEncoded & 2) == 2;
    }

    /**
     * @return true iff do not turn right advisory is active
     */
    public boolean noTurnRight() {
        return (racRecordEncoded & 1) == 1;
    }

    /**
     * @return true if aircraft is airborne, false if it is on the ground
     */
    public boolean isAirborne() {
        return !verticalStatus;
    }

    /**
     * @return true iff the RA from {@link #getActiveResolutionAdvisories()} has been terminated
     */
    public boolean hasTerminated() {
        return raTerminated;
    }

    /**
     * @return true iff two or more threats are being processed
     */
    public boolean hasMultipleThreats() {
        return multipleThreatEncounter;
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
                ", activeResolutionAdvisories=" + activeResolutionAdvisories +
                ", racRecordEncoded=" + racRecordEncoded +
                ", raTerminated=" + raTerminated +
                ", multipleThreatEncounter=" + multipleThreatEncounter +
                '}';
    }

}
