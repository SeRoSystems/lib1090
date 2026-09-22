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
import de.serosystems.lib1090.decoding.AirbornePosition;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.ContainmentRadius;
import de.serosystems.lib1090.decoding.NICSupplements;
import de.serosystems.lib1090.decoding.NavigationCharacteristics;
import de.serosystems.lib1090.decoding.NavigationCharacteristicsV0;
import de.serosystems.lib1090.decoding.SourceIntegrityLevel;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.AirbornePositionMsg;
import de.serosystems.lib1090.msgs.squitter.PositionMsgWithTime;
import de.serosystems.lib1090.msgs.squitter.SingleAntennaMsg;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Decoder for ADS-B airborne position messages (version 0), as defined in ED-102B §2.2.3.2.3.1 TABLE 2-11.
 */
@SuppressWarnings("unused")
public class AirbornePositionV0Msg extends ExtendedSquitter implements Serializable, AirbornePositionMsg, PositionMsgWithTime, SingleAntennaMsg, ADSBMsg {

    private static final long serialVersionUID = 8440428954946862126L;

    private boolean horizontalPositionAvailable;
    private byte surveillanceStatus;

    private boolean singleAntennaFlag;
    private short altitudeEncoded;
    private boolean timeFlag;
    private CPREncodedPosition position;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirbornePositionV0Msg() {
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as hex string
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV0Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-B airborne position message as byte array
     * @param timestamp  timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirbornePositionV0Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter containing the airborne position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public AirbornePositionV0Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter);

        byte formatTypeCode = getFormatTypeCode();
        AirbornePosition.validateAirbornePositionFormat(formatTypeCode);

        horizontalPositionAvailable = formatTypeCode != 0;
        BitReader br = BitReader.forBigEndian(getMessage());
        surveillanceStatus = br.readByte(6, 7);
        singleAntennaFlag = br.readBoolean(8);
        altitudeEncoded = br.readShort(9, 20);
        timeFlag = br.readBoolean(21);
        position = AirbornePosition.extractCPREncodedPosition(br, Objects.requireNonNull(timestamp, "timestamp"));
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
        return characteristics();
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

    /**
     * The version 0 row this message's format type code selects, typed so that the columns only
     * version 0 has — NUCp, NACp and SIL — can be read from it.
     */
    private NavigationCharacteristicsV0 characteristics() {
        return NavigationCharacteristicsV0.forFormatTypeCode(getFormatTypeCode());
    }

    /**
     * Navigation uncertainty category for position, the one of these ADS-B version 0 defines itself,
     * ED-102 §2.2.8.1.5.
     *
     * @return the navigation uncertainty category for position, never {@code null} for an airborne
     * type code other than 22
     */
    public Byte getNUCpEncoded() {
        return characteristics().getNUCpEncoded();
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
    public byte getNACpEncoded() {
        return characteristics().getNACpEncoded();
    }

    /**
     * Source integrity level, derived from the format type code.
     * <p>
     * SIL was introduced in ADS-B version 1, which transmits it in the operational status and target
     * state and status messages. Version 0 has no such field, and this mapping is the only way to
     * obtain the value — which is why no other position message offers it.
     *
     * @return the encoded source integrity level
     */
    public byte getSILEncoded() {
        return characteristics().getSILEncoded();
    }

    /**
     * What the source integrity level derived from the format type code guarantees, ED-102B
     * §2.2.3.2.7.2.9 TABLE 2-70.
     *
     * @return the probability of exceeding the containment radius the level guarantees
     */
    public SourceIntegrityLevel getSourceIntegrityLevel() {
        return characteristics().getSourceIntegrityLevel();
    }

    @Override
    public byte getSurveillanceStatusEncoded() {
        return surveillanceStatus;
    }

    @Override
    public boolean hasSingleAntenna() {
        return singleAntennaFlag;
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
        return "AirbornePositionV0Msg{" + super.toString() +
                ", horizontalPositionAvailable=" + horizontalPositionAvailable +
                ", surveillanceStatus=" + surveillanceStatus +
                ", singleAntennaFlag=" + singleAntennaFlag +
                ", altitudeEncoded=" + altitudeEncoded +
                ", timeFlag=" + timeFlag +
                ", position=" + position +
                '}';
    }

}
