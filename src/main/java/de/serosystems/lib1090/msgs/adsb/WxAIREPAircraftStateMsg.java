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
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.decoding.WxAIREP;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.WxAIREPMsg;

import java.io.Serializable;

/**
 * Decoder for the aircraft state subtype (0) of the ADS-B Wx AIREP message, as defined in ED-102B §2.2.3.2.7.6.3 Figure 2-17, introduced in ADS-B version 3.
 */
public class WxAIREPAircraftStateMsg extends TypeCodedExtendedSquitter implements Serializable, WxAIREPMsg, ADSBMsg {

    private static final long serialVersionUID = -8264790351706421573L;

    private byte aircraftConfigurationEncoded; // raw encoded aircraft configuration field
    private int aircraftTypeEncoded; // raw encoded aircraft type characters #1-#4 (6 bits each)
    private short grossWeightEncoded; // raw encoded gross weight field
    private short wingspanEncoded; // raw encoded wingspan field

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected WxAIREPAircraftStateMsg() {
    }

    /**
     * @param rawMessage raw ADS-B Wx AIREP message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public WxAIREPAircraftStateMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B Wx AIREP message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public WxAIREPAircraftStateMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Wx AIREP aircraft state message
     * @throws BadFormatException if message has wrong format
     */
    public WxAIREPAircraftStateMsg(TypeCodedExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        BitReader br = BitReader.forBigEndian(getMessage());
        WxAIREP.validateFormat(getFormatTypeCode(), br, 0, "aircraft state");

        aircraftConfigurationEncoded = br.readByte(8, 11);
        aircraftTypeEncoded = br.readInt(12, 35);
        grossWeightEncoded = br.readShort(36, 47);
        wingspanEncoded = br.readShort(48, 55);
    }

    @Override
    public byte getMessageSubtype() {
        return 0;
    }

    /**
     * @return the raw encoded aircraft configuration field
     */
    public byte getAircraftConfigurationEncoded() {
        return aircraftConfigurationEncoded;
    }

    /**
     * @return the raw encoded aircraft type characters #1-#4 (6 bits each, 24 bits total)
     */
    public int getAircraftTypeCharEncoded() {
        return aircraftTypeEncoded;
    }

    /**
     * @return whether the aircraft type field is available
     */
    public boolean hasAircraftType() {
        return aircraftTypeEncoded != 0;
    }

    /**
     * Decode the aircraft type field, which is IA5 encoded as 4 consecutive 6-bit characters.
     *
     * @return the aircraft type as a 4 character array, or {@code null} if unavailable; a character whose code
     * is not defined is decoded as a space, see {@link #hasValidAircraftType()}
     */
    public char[] getAircraftType() {
        return WxAIREP.aircraftType(aircraftTypeEncoded);
    }

    /**
     * @return true if the aircraft type is available and every character has a code that ICAO Annex 10 Volume IV
     * §3.1.2.9.1.2 TABLE 3-8 defines
     */
    public boolean hasValidAircraftType() {
        return WxAIREP.isValidAircraftType(aircraftTypeEncoded);
    }

    /**
     * @return the raw encoded gross weight field
     */
    public short getGrossWeightEncoded() {
        return grossWeightEncoded;
    }

    /**
     * @return whether the gross weight field is available
     */
    public boolean hasGrossWeight() {
        return grossWeightEncoded != 0;
    }

    /**
     * Decode the gross weight field.
     *
     * @return a lower bound for the gross weight in lbs, {@code 1514015} meaning "equal to or
     * greater than 1514015 lbs", or {@code null} if unavailable
     */
    public Double getGrossWeight() {
        return WxAIREP.grossWeight(grossWeightEncoded);
    }

    /**
     * @return the raw encoded wingspan field
     */
    public short getWingspanEncoded() {
        return wingspanEncoded;
    }

    /**
     * @return whether the wingspan field is available
     */
    public boolean hasWingspan() {
        return wingspanEncoded != 0;
    }

    /**
     * The wingspan as the interval its code stands for, ED-102B §2.2.3.2.7.6.3.5: the encoding truncates, so
     * code N covers [W(N), W(N + 1)) with W(N) = ((1 + (N - 2) * 0.004)² - 0.952) / 0.008 ft; code 1 is any
     * wingspan less than 6 ft, and code 255 any of 387.018 ft or more.
     *
     * @return the interval of the wingspan in feet, or {@code null} if unavailable
     */
    public Interval getWingspan() {
        return WxAIREP.wingspan(wingspanEncoded);
    }

    @Override
    public String toString() {
        return "WxAIREPAircraftStateMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", aircraftConfigurationEncoded=" + aircraftConfigurationEncoded +
                ", aircraftTypeEncoded=" + aircraftTypeEncoded +
                ", grossWeightEncoded=" + grossWeightEncoded +
                ", wingspanEncoded=" + wingspanEncoded +
                '}';
    }

}
