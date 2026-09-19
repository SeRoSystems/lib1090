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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV0;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for TIS-B fine surface position, as defined in ED-102B §2.2.17.3.2.
 */
public class FineSurfacePositionMsg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, IMFMsg, TISBMsg {

    private static final long serialVersionUID = -8306226295512272609L;

    private byte movement;
    private boolean headingStatus; // is heading valid?
    private byte groundTrack;
    private boolean imf;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected FineSurfacePositionMsg() {
    }

    /**
     * @param rawMessage raw TIS-B fine surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public FineSurfacePositionMsg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw TIS-B fine surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public FineSurfacePositionMsg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the surface position msg in high resolution
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public FineSurfacePositionMsg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18");

        // ED-102B §2.2.17.2 TABLE 2-184
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("TIS-B messages must have CF value 2 or 5");

        if (getFormatTypeCode() < 5 || getFormatTypeCode() > 8)
            throw new BadFormatException("Wrong format type code for TIS-B Fine Surface Position message");

        BitReader br = BitReader.forBigEndian(getMessage());

        movement = br.readByte(6, 12);
        headingStatus = br.readBoolean(13);
        groundTrack = br.readByte(14, 20);

        imf = br.readBoolean(21);
        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);

        boolean highGroundSpeed = movement == 0 || movement > 49;
        position = CPREncodedPosition.ofSurface(17, cprFormat, highGroundSpeed, cprEncodedLat, cprEncodedLon,
                Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on, ED-102B §N.2.2.2 TABLE N-4.
     *
     * @return the containment radius the format type code reports
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return characteristics().getContainmentRadius();
    }

    /**
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    @Override
    public byte getNIC() {
        return characteristics().getNIC();
    }

    /**
     * Everything the format type code says about this position, as one row. TIS-B carries no ADS-B
     * version of its own and reads its type code the same way version 0 does. The type code is
     * validated in the constructor, so the lookup always finds a row.
     */
    private NavigationCharacteristicsV0 characteristics() {
        return NavigationCharacteristicsV0.forFormatTypeCode(getFormatTypeCode());
    }

    @Override
    public byte getMovementEncoded() {
        return movement;
    }

    @Override
    public byte getHeadingEncoded() {
        return groundTrack;
    }

    @Override
    public boolean hasValidHeading() {
        return headingStatus;
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public CPREncodedPosition getCPREncodedPosition() {
        return position;
    }

    @Override
    public boolean hasValidPosition() {
        return true;
    }

    @Override
    public String toString() {
        return "FineSurfacePositionMsg{" + super.toString() +
                "movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", imf=" + imf +
                ", position=" + position +
                '}';
    }

}
