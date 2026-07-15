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
 * Decoder for ADS-B target state and status message as specified in DO-260A (ADS-B version 1).
 */
public class TargetStateAndStatusV1Msg extends ExtendedSquitter implements Serializable, TargetStateAndStatusMsg {

    private static final long serialVersionUID = -3226687215928593692L;

    private byte verticalDataAvailableAndSourceIndicator;
    private boolean targetAltitudeType;
    private byte targetAltitudeCapability;
    private byte verticalModeIndicator;
    private int targetAltitude;
    private byte horizontalDataAvailableAndSourceIndicator;
    private short targetHeadingTrackAngle;
    private boolean targetHeadingTrackIndicator;
    private byte horizontalModeIndicator;
    private byte nacP;
    private boolean nicBaro;
    private byte sil;
    private boolean capabilityNotTcas;
    private boolean capabilityTcasRaActive;
    private byte emergencyPriorityStatus;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TargetStateAndStatusV1Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public TargetStateAndStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public TargetStateAndStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public TargetStateAndStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);
        setType(subtype.ADSB_TARGET_STATE_AND_STATUS_V1);

        if (getFormatTypeCode() != 29) {
            throw new BadFormatException("Target state and status messages must have typecode 29.");
        }

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 7);
        if (subtypeCode != 0)
            throw new UnspecifiedFormatError("Target state and status message subtype " + subtypeCode + " reserved.");

        verticalDataAvailableAndSourceIndicator = b.readByte(8, 9);
        targetAltitudeType = b.readByte(10, 10) == 1;
        targetAltitudeCapability = b.readByte(12, 13);
        verticalModeIndicator = b.readByte(14, 15);
        targetAltitude = b.readInt(16, 25);
        horizontalDataAvailableAndSourceIndicator = b.readByte(26, 27);
        targetHeadingTrackAngle = b.readShort(28, 36);
        targetHeadingTrackIndicator = b.readByte(37, 37) == 1;
        horizontalModeIndicator = b.readByte(38, 39);
        nacP = b.readByte(40, 43);
        nicBaro = b.readByte(44, 44) == 1;
        sil = b.readByte(45, 46);
        capabilityNotTcas = b.readByte(52, 52) == 1;
        capabilityTcasRaActive = b.readByte(53, 53) == 1;
        emergencyPriorityStatus = b.readByte(54, 56);
    }

    /**
     * @return the raw vertical data available and source indicator value
     */
    public byte getVerticalDataAvailableAndSourceIndicator() {
        return verticalDataAvailableAndSourceIndicator;
    }

    @Override
    public boolean hasSelectedAltitude() {
        return targetAltitudeCapability == 1 || targetAltitudeCapability == 2;
    }

    @Override
    public int getSelectedAltitudeEncoded() {
        return targetAltitude;
    }

    @Override
    public Integer getSelectedAltitude() {
        return hasSelectedAltitude() ? 100 * targetAltitude - 1000 : null;
    }

    @Override
    public boolean hasSelectedHeading() {
        return horizontalDataAvailableAndSourceIndicator != 0;
    }

    @Override
    public Float getSelectedHeading() {
        return hasSelectedHeading() ? targetHeadingTrackAngle * (360.f / 512) : null;
    }

    @Override
    public int getSelectedHeadingEncoded() {
        return targetHeadingTrackAngle;
    }

    @Override
    public byte getNACpEncoded() {
        return nacP;
    }

    @Override
    public boolean getBarometricAltitudeIntegrityCode() {
        return nicBaro;
    }

    @Override
    public byte getSILEncoded() {
        return sil;
    }

    @Override
    public boolean hasOperationalTCAS() {
        return !capabilityNotTcas;
    }

    /**
     * @return true if a TCAS resolution advisory is active
     */
    public boolean hasActiveTCASResolutionAdvisory() {
        return capabilityTcasRaActive;
    }

    /**
     * @return the raw emergency / priority status field value
     */
    public byte getEmergencyPriorityStatus() {
        return emergencyPriorityStatus;
    }

    @Override
    public String toString() {
        return "TargetStateAndStatusV1Msg{" + super.toString() +
                ", verticalDataAvailableAndSourceIndicator=" + verticalDataAvailableAndSourceIndicator +
                ", targetAltitudeType=" + targetAltitudeType +
                ", targetAltitudeCapability=" + targetAltitudeCapability +
                ", verticalModeIndicator=" + verticalModeIndicator +
                ", targetAltitude=" + targetAltitude +
                ", horizontalDataAvailableAndSourceIndicator=" + horizontalDataAvailableAndSourceIndicator +
                ", targetHeadingTrackAngle=" + targetHeadingTrackAngle +
                ", targetHeadingTrackIndicator=" + targetHeadingTrackIndicator +
                ", horizontalModeIndicator=" + horizontalModeIndicator +
                ", nacP=" + nacP +
                ", nicBaro=" + nicBaro +
                ", sil=" + sil +
                ", capabilityNotTcas=" + capabilityNotTcas +
                ", capabilityTcasRaActive=" + capabilityTcasRaActive +
                ", emergencyPriorityStatus=" + emergencyPriorityStatus +
                '}';
    }
}
