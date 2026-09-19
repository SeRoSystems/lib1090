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
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV2;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R surface position messages (version 2), as defined in ED-102B §2.2.18.4.2 Figure 2-58.
 */
public class SurfacePositionV2Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 7058731502891153722L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private boolean imf;
    private CPREncodedPosition position;

    private Boolean nicSupplementA;
    private Boolean nicSupplementC;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-R surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV2Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-R surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV2Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV2Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
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
     * @return the NIC supplement A that was set before, or {@code null} if none was
     */
    public Boolean getNICSupplementA() {
        return nicSupplementA;
    }

    /**
     * @param nicSupplementA Navigation Integrity Category (NIC) supplement A from the operational
     *                       status message. Until it is set, the worst case the format type code and
     *                       the other supplement allow is reported.
     */
    public void setNICSupplementA(boolean nicSupplementA) {
        this.nicSupplementA = nicSupplementA;
    }

    /**
     * @return the NIC supplement C that was set before, or {@code null} if none was
     */
    public Boolean getNICSupplementC() {
        return nicSupplementC;
    }

    /**
     * @param nicSupplementC Navigation Integrity Category (NIC) supplement C, from the surface
     *                       capability class subfield of the operational status message.
     */
    public void setNICSupplementC(boolean nicSupplementC) {
        this.nicSupplementC = nicSupplementC;
    }

    /**
     * The horizontal containment radius limit together with the side of that value the true radius
     * lies on, ED-102A TABLE 2-14.
     *
     * @return the containment radius, worst case for whichever supplements have not been set
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
     * Everything the format type code and the NIC supplements say about this position, ED-102A
     * TABLE 2-14.
     * <p>
     * Both supplements travel in the operational status message, so both may be unset, and an unset
     * one reports the poorest row its knowledge allows rather than being read as clear.
     */
    private NavigationCharacteristicsV2 getNavigationCharacteristics() {
        return NavigationCharacteristicsV2.forSurfaceFormatTypeCode(
                getFormatTypeCode(), NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementC));
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
        return "SurfacePositionV2Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", imf=" + imf +
                ", position=" + position +
                ", nicSupplementA=" + nicSupplementA +
                ", nicSupplementC=" + nicSupplementC +
                '}';
    }

}
