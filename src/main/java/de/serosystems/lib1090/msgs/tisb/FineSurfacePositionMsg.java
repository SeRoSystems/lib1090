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
import de.serosystems.lib1090.decoding.quality.ContainmentRadius;
import de.serosystems.lib1090.decoding.quality.NICSupplements;
import de.serosystems.lib1090.decoding.quality.NavigationCharacteristics;
import de.serosystems.lib1090.decoding.quality.NavigationCharacteristicsV0;
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
     * {@inheritDoc}
     * <p>
     * Version 0 defines no NIC supplements, so the supplements given are not read: the format type
     * code settles everything. The parameter is kept because every position message answers this
     * question the same way — reading the supplements its table is keyed on, here none of them.
     */
    @Override
    public NavigationCharacteristics getNavigationCharacteristics(NICSupplements nicSupplements) {
        return NavigationCharacteristicsV0.forFormatTypeCode(getFormatTypeCode());
    }

    @Override
    public byte getNICEncoded() {
        return getNavigationCharacteristics(getKnownSupplements()).getNICEncoded();
    }

    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics(getKnownSupplements()).getContainmentRadius();
    }

    /**
     * What this message knows of its target's NIC supplements on its own, which is what the
     * no-argument accessors report with. A supplement it does not carry stays unknown, and the
     * tables answer that with the poorest row it allows.
     *
     * @return the supplements this message knows
     */
    protected NICSupplements getKnownSupplements() {
        return NICSupplements.none();
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
