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
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplement;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV2;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-R airborne position messages (version 2), as defined in ED-102A.
 */
public class AirbornePositionV2Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 6290709359481466246L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;
    private boolean imf;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    private Boolean nicSupplementA;
    private Boolean nicSupplementB;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV2Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV2Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV2Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV2Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        imf = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
    }

    /**
     * @param nicSupplementA Navigation Integrity Category (NIC) supplement A from the operational
     *                       status message. Until it is set, the worst case the format type code and
     *                       the other supplements allow is reported.
     */
    public void setNICSupplementA(boolean nicSupplementA) {
        this.nicSupplementA = nicSupplementA;
    }

    /**
     * @return the NIC supplement A that was set before, or {@code null} if none was
     */
    public Boolean getNICSupplementA() {
        return nicSupplementA;
    }

    /**
     * @param nicSupplementB Navigation Integrity Category (NIC) supplement B. ADS-R carries it in the
     *                       airborne capability class of the operational status message, ME bit 20,
     *                       rather than in the position message as ADS-B does.
     */
    public void setNICSupplementB(boolean nicSupplementB) {
        this.nicSupplementB = nicSupplementB;
    }

    /**
     * @return the NIC supplement B that was set before, or {@code null} if none was
     */
    public Boolean getNICSupplementB() {
        return nicSupplementB;
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
     * ADS-R carries both supplements in the operational status message, so both may be unset, and an
     * unset one reports the poorest row its knowledge allows rather than being read as clear.
     */
    private NavigationCharacteristicsV2 getNavigationCharacteristics() {
        return NavigationCharacteristicsV2.forAirborneFormatTypeCode(
                getFormatTypeCode(), NICSupplement.of(nicSupplementA), NICSupplement.of(nicSupplementB));
    }

    @Override
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    @Override
    public boolean getIMF() {
        return imf;
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
    public short getAltitudeEncoded() {
        return altitudeEncoded;
    }

    @Override
    public String toString() {
        return "AirbornePositionV2Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", imf=" + imf +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                ", nicSupplementA=" + nicSupplementA +
                ", nicSupplementB=" + nicSupplementB +
                '}';
    }

}
