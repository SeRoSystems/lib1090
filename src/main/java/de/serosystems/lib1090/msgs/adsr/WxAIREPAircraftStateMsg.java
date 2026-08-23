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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.InternationalAlphabet5;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.WxAIREPMsg;

import java.io.Serializable;

/**
 * Decoder for the aircraft state subtype (0) of the ADS-R Wx AIREP message, as defined in
 * ED-102B §2.2.18.4.8 Figure 2-64 (introduced in ADS-B version 3).
 */
public class WxAIREPAircraftStateMsg extends ExtendedSquitter implements Serializable, WxAIREPMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 528590727258097923L;

    private byte aircraftConfigurationEncoded; // raw encoded aircraft configuration field
    private int aircraftTypeEncoded; // raw encoded aircraft type characters #1-#4 (6 bits each)
    private short grossWeightEncoded; // raw encoded gross weight field
    private short wingspanEncoded; // raw encoded wingspan field
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected WxAIREPAircraftStateMsg() {
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.8 Figure 2-64
     */
    public WxAIREPAircraftStateMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R Wx AIREP message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.8 Figure 2-64
     */
    public WxAIREPAircraftStateMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this Wx AIREP aircraft state message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has a subtype other than 0 (aircraft state); see ED-102B §2.2.18.4.8 Figure 2-64
     */
    public WxAIREPAircraftStateMsg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 26)
            throw new BadFormatException("Wx AIREP messages must have typecode 26.");

        BitReader br = BitReader.forBigEndian(getMessage());

        byte messageSubtype = br.readByte(6, 7);
        if (messageSubtype != 0)
            throw new UnspecifiedFormatError("Wx AIREP aircraft state message must have subtype 0, got " + messageSubtype + ".");

        aircraftConfigurationEncoded = br.readByte(8, 11);
        aircraftTypeEncoded = br.readInt(12, 35);
        grossWeightEncoded = br.readShort(36, 47);
        wingspanEncoded = br.readShort(48, 55);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = br.readBoolean(56);
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
     * @return the aircraft type as a 4 character array, or {@code null} if unavailable
     */
    public char[] getAircraftType() {
        if (!hasAircraftType()) return null;
        return InternationalAlphabet5.mapChar(InternationalAlphabet5.toDigits(aircraftTypeEncoded, 4));
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
        if (!hasGrossWeight()) return null;

        if (grossWeightEncoded == 1) return 0.;
        if (grossWeightEncoded <= 386) return 55. + (grossWeightEncoded - 2) * 40.;
        if (grossWeightEncoded <= 1150) return 15455. + (grossWeightEncoded - 387) * 80.;
        if (grossWeightEncoded <= 2546) return 76575. + (grossWeightEncoded - 1151) * 160.;
        if (grossWeightEncoded <= 3536) return 299935. + (grossWeightEncoded - 2547) * 480.;
        if (grossWeightEncoded <= 4005) return 775135. + (grossWeightEncoded - 3537) * 1120.;
        if (grossWeightEncoded <= 4094) return 1300415. + (grossWeightEncoded - 4006) * 2400.;
        return 1514015.;
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
     * Decode the wingspan field.
     *
     * @return the wingspan in feet, {@code 387.018} meaning "equal to or greater than 387.018 ft"
     * (encoded field {@code 0xFF}), or {@code null} if unavailable
     */
    public Double getWingspan() {
        if (!hasWingspan()) return null;

        if (wingspanEncoded == 255) return 387.018;

        double t = 1 + (wingspanEncoded - 2) * 0.004;
        return (t * t - 0.952) / 0.008;
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "WxAIREPAircraftStateMsg{" + super.toString() +
                ", messageSubtype=" + getMessageSubtype() +
                ", aircraftConfigurationEncoded=" + aircraftConfigurationEncoded +
                ", aircraftTypeEncoded=" + aircraftTypeEncoded +
                ", grossWeightEncoded=" + grossWeightEncoded +
                ", wingspanEncoded=" + wingspanEncoded +
                ", imf=" + imf +
                '}';
    }

}
