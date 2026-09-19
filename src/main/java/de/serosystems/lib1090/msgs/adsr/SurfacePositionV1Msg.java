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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV1;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R surface position messages (version 1), as defined in ED-102B §2.2.18.4.2 Figure 2-58.
 */
public class SurfacePositionV1Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 5508826457167641894L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private boolean imf;
    private CPREncodedPosition position;

    private Boolean nicSupplementA;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-R surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV1Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-R surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV1Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV1Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        SurfacePosition.validateSurfacePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        movement = br.readByte(6, 12);
        headingStatus = br.readBoolean(13);
        groundTrack = br.readByte(14, 20);
        imf = br.readBoolean(21);
        position = SurfacePosition.extractCPREncodedPosition(br, movement, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * @param nicSupplementA Navigation Integrity Category (NIC) supplement from the operational status
     *                       message. Until it is set, the worst case the format type code allows is
     *                       reported for the containment radius limit and NIC.
     */
    public void setNICSupplementA(boolean nicSupplementA) {
        this.nicSupplementA = nicSupplementA;
    }

    /**
     * @return the NIC supplement that was set before, or {@code null} if none was — the supplement is
     * transmitted in the operational status message, so a receiver that has seen none does not know it
     */
    public Boolean getNICSupplementA() {
        return nicSupplementA;
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on, ED-102B §N.5.4 TABLE N-16.
     *
     * @return the containment radius, worst case until {@link #setNICSupplementA(boolean)} is called
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return getNavigationCharacteristics().getContainmentRadius();
    }

    /**
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    @Override
    public byte getNIC() {
        return getNavigationCharacteristics().getNIC();
    }

    /**
     * Everything the format type code and the NIC supplement say about this position, ED-102B §N.5.4
     * TABLE N-16.
     * <p>
     * Until the supplement is set this reports the worst row the type code allows, which is not the
     * same as assuming the supplement is clear: at type code 13 a clear supplement is the better of
     * the two rows.
     */
    private NavigationCharacteristicsV1 getNavigationCharacteristics() {
        return NavigationCharacteristicsV1.forFormatTypeCode(
                getFormatTypeCode(), NICSupplement.of(nicSupplementA));
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
        return horizontalPositionAvailable;
    }

    @Override
    public String toString() {
        return "SurfacePositionV1Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", imf=" + imf +
                ", position=" + position +
                ", nicSupplementA=" + nicSupplementA +
                '}';
    }

}
