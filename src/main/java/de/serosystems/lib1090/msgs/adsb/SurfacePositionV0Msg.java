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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV0;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;
import de.serosystems.lib1090.msgs.squitter.SurfacePositionMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B surface position messages (version 0), as defined in ED-102B §2.2.3.2.4 Figure 2-6.
 */
public class SurfacePositionV0Msg extends ExtendedSquitter implements Serializable, SurfacePositionMsg, PositionMsgWithTime, ADSBMsg {

    private static final long serialVersionUID = 7290522585963455918L;

    private boolean horizontalPositionAvailable;
    private byte movement;
    private boolean headingStatus;
    private byte groundTrack;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV0Msg() {
    }

    /**
     * @param rawMessage raw ADS-B surface position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV0Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B surface position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfacePositionV0Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV0Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        SurfacePosition.validateSurfacePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        movement = br.readByte(6, 12);
        headingStatus = br.readBoolean(13);
        groundTrack = br.readByte(14, 20);
        timeFlag = br.readBoolean(21);
        position = SurfacePosition.extractCPREncodedPosition(br, movement, Objects.requireNonNull(timestamp, "timestamp"));
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
     * Navigation uncertainty category for position, the one of these ADS-B version 0 defines itself,
     * ED-102 §2.2.8.1.5.
     *
     * @return the navigation uncertainty category for position, never {@code null} for a surface
     * type code
     */
    public Byte getNUCp() {
        return characteristics().getNUCp();
    }

    /**
     * Navigation accuracy category for position, derived from the format type code.
     * <p>
     * NACp was introduced in ADS-B version 1, which transmits it in the operational status and target
     * state and status messages. Version 0 has no such field, and this mapping is the only way to
     * obtain the value — which is why no other position message offers it.
     *
     * @return the navigation accuracy category for position
     * @see de.serosystems.lib1090.decoding.OperationalStatus#nacPtoEPU(byte) to turn it into an
     * estimated position uncertainty in meters
     */
    public byte getNACp() {
        return characteristics().getNACp();
    }

    @Override
    public byte getNIC() {
        return characteristics().getNIC();
    }

    /**
     * Source integrity level, derived from the format type code.
     * <p>
     * SIL was introduced in ADS-B version 1, which transmits it in the operational status and target
     * state and status messages. Version 0 has no such field, and this mapping is the only way to
     * obtain the value — which is why no other position message offers it.
     *
     * @return the source integrity level
     */
    public byte getSIL() {
        return characteristics().getSIL();
    }

    /**
     * Everything the format type code says about this position, as one row. The type code is
     * validated in the constructor, so the lookup always finds one.
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
    public boolean hasTimeFlag() {
        return timeFlag;
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
        return "SurfacePositionV0Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", movement=" + movement +
                ", headingStatus=" + headingStatus +
                ", groundTrack=" + groundTrack +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

}
