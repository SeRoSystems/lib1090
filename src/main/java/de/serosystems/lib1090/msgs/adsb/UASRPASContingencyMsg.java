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
 * Decoder for 1090ES UAS/RPAS Contingency Messages (Extended Squitter Aircraft Status Message,
 * TYPE=28 Subtype=4, Optional), as defined in ED-102B §2.2.3.2.8.1.3 Figure 2-23.
 */
public class UASRPASContingencyMsg extends ExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = 4187522897452638491L;

    private static final byte SUBTYPE = 4;

    private byte contingencyPlan;
    private boolean currentOrNext;
    private short tcpAltitudeEncoded;
    private int tcpLatitudeEncoded;
    private int tcpLongitudeEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected UASRPASContingencyMsg() {
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public UASRPASContingencyMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public UASRPASContingencyMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this UAS/RPAS contingency msg
     * @throws BadFormatException if message has wrong format
     */
    public UASRPASContingencyMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 28)
            throw new BadFormatException("UAS/RPAS contingency reports must have typecode 28.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("UAS/RPAS contingency reports have subtype 4.");

        contingencyPlan = b.readByte(9, 12);
        currentOrNext = b.readBoolean(13);
        tcpAltitudeEncoded = b.readShort(14, 22);
        tcpLatitudeEncoded = b.readInt(23, 39);
        tcpLongitudeEncoded = b.readInt(40, 56);
    }

    /**
     * @return the subtype code of the aircraft status report (should always be 4)
     */
    public byte getSubtype() {
        return SUBTYPE;
    }

    /**
     * @return the contingency plan
     */
    public byte getContingencyPlan() {
        return contingencyPlan;
    }

    /**
     * @return true if the transmitted contingency location applies to the next contingency plan;
     * false if it applies to the current one
     */
    public boolean isCurrentOrNext() {
        return currentOrNext;
    }

    /**
     * @return the encoded UAS/RPAS Trajectory Change Point (TCP) altitude
     */
    public short getTcpAltitudeEncoded() {
        return tcpAltitudeEncoded;
    }

    /**
     * @return false if the TCP altitude encoded value is 0, i.e. no TCP altitude is available
     */
    public boolean hasTcpAltitude() {
        return tcpAltitudeEncoded != 0;
    }

    /**
     * @return the uncorrected Barometric Pressure Altitude of the UAS/RPAS Trajectory Change Point (TCP) in feet,
     * or null if no TCP altitude is available (see {@link #hasTcpAltitude()})
     */
    public Integer getTcpAltitude() {
        return hasTcpAltitude() ? (tcpAltitudeEncoded - 3) * 500 : null;
    }

    /**
     * @return the encoded UAS/RPAS Trajectory Change Point (TCP) latitude
     */
    public int getTcpLatitudeEncoded() {
        return tcpLatitudeEncoded;
    }

    /**
     * @return the latitude of the UAS/RPAS Trajectory Change Point (TCP) in degrees, north positive
     */
    public double getTcpLatitude() {
        int signed = (tcpLatitudeEncoded << 15) >> 15;
        return signed * 360. / (1 << 17);
    }

    /**
     * @return the encoded UAS/RPAS Trajectory Change Point (TCP) longitude
     */
    public int getTcpLongitudeEncoded() {
        return tcpLongitudeEncoded;
    }

    /**
     * @return the longitude of the UAS/RPAS Trajectory Change Point (TCP) in degrees, east positive
     */
    public double getTcpLongitude() {
        int signed = (tcpLongitudeEncoded << 15) >> 15;
        return signed * 360. / (1 << 17);
    }

    @Override
    public String toString() {
        return "UASRPASContingencyMsg{" + super.toString() +
                ", contingencyPlan=" + contingencyPlan +
                ", currentOrNext=" + currentOrNext +
                ", tcpAltitudeEncoded=" + tcpAltitudeEncoded +
                ", tcpLatitudeEncoded=" + tcpLatitudeEncoded +
                ", tcpLongitudeEncoded=" + tcpLongitudeEncoded +
                '}';
    }

}
