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
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for 1090ES CAS Operational Coordination Messages.
 */
public class CASOperationalCoordinationMsg extends ExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = -3269481672107864825L;

    private static final byte SUBTYPE = 3;

    private boolean multipleThreatBit;
    private byte cancelVerticalRaComplementEncoded;
    private byte verticalRaComplementEncoded;
    private byte cancelHorizontalRaComplementEncoded;
    private byte horizontalRaComplementEncoded;
    private byte horizontalSenseBitsEncoded;
    private byte verticalSenseBitsEncoded;
    private int threatIdentityAircraftAddress;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected CASOperationalCoordinationMsg() {
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public CASOperationalCoordinationMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260C
     */
    public CASOperationalCoordinationMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this CAS operational coordination msg
     * @throws BadFormatException if message has wrong format
     */
    public CASOperationalCoordinationMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 28)
            throw new BadFormatException("CAS operational coordination reports must have typecode 28.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("CAS operational coordination reports have subtype 3.");

        multipleThreatBit = b.readBoolean(10);
        cancelVerticalRaComplementEncoded = b.readByte(11, 12);
        verticalRaComplementEncoded = b.readByte(13, 14);
        cancelHorizontalRaComplementEncoded = b.readByte(15, 17);
        horizontalRaComplementEncoded = b.readByte(18, 20);
        horizontalSenseBitsEncoded = b.readByte(21, 25);
        verticalSenseBitsEncoded = b.readByte(26, 29);
        threatIdentityAircraftAddress = b.readInt(33, 56);
    }

    /**
     * @return the subtype code of the aircraft status report (should always be 3)
     */
    public byte getSubtype() {
        return SUBTYPE;
    }

    /**
     * @return true if own aircraft is engaged with multiple threats
     */
    public boolean isMultipleThreatBit() {
        return multipleThreatBit;
    }

    /**
     * @return the cancel vertical RA complement encoded bits
     */
    public byte getCancelVerticalRaComplementEncoded() {
        return cancelVerticalRaComplementEncoded;
    }

    /**
     * @return the vertical RA complement encoded bits
     */
    public byte getVerticalRaComplementEncoded() {
        return verticalRaComplementEncoded;
    }

    /**
     * @return the cancel horizontal RA complement encoded bits
     */
    public byte getCancelHorizontalRaComplementEncoded() {
        return cancelHorizontalRaComplementEncoded;
    }

    /**
     * @return the horizontal RA complement encoded bits
     */
    public byte getHorizontalRaComplementEncoded() {
        return horizontalRaComplementEncoded;
    }

    /**
     * @return the horizontal sense encoded bits
     */
    public byte getHorizontalSenseBitsEncoded() {
        return horizontalSenseBitsEncoded;
    }

    /**
     * @return the vertical sense encoded bits
     */
    public byte getVerticalSenseBitsEncoded() {
        return verticalSenseBitsEncoded;
    }

    /**
     * @return the ICAO 24-bit aircraft address of the threat aircraft
     */
    public int getThreatIdentityAircraftAddress() {
        return threatIdentityAircraftAddress;
    }

    @Override
    public String toString() {
        return "CASOperationalCoordinationMsg{" + super.toString() +
                ", multipleThreatBit=" + multipleThreatBit +
                ", cancelVerticalRaComplementEncoded=" + cancelVerticalRaComplementEncoded +
                ", verticalRaComplementEncoded=" + verticalRaComplementEncoded +
                ", cancelHorizontalRaComplementEncoded=" + cancelHorizontalRaComplementEncoded +
                ", horizontalRaComplementEncoded=" + horizontalRaComplementEncoded +
                ", horizontalSenseBitsEncoded=" + horizontalSenseBitsEncoded +
                ", verticalSenseBitsEncoded=" + verticalSenseBitsEncoded +
                ", threatIdentityAircraftAddress=" + threatIdentityAircraftAddress +
                '}';
    }

}
