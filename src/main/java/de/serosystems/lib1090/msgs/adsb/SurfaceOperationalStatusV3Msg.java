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
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.CapabilityClassCodes;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.OperationalModeCodes;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;

import java.io.Serializable;

/**
 * Decoder for the ADS-B operational status message, as defined in ED-102B (ADS-B version 3), with subtype 1 (surface).
 */
public class SurfaceOperationalStatusV3Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, OperationalStatusV2Msg, ADSBMsg {

    private static final long serialVersionUID = -6412897503618274091L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte airplaneLenWidth; // length / width code
    private byte mopsVersion;
    private boolean nicSupplementA; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private boolean trackHeading; // heading/ground track
    private boolean horizontalReferenceDirection; // heading is based on true north (0) or magnetic north (1)
    private boolean silSupplement;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalStatusV3Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceOperationalStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceOperationalStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-B version or is not a surface
     *                            operational status message or the capability class code or operational mode
     *                            code is invalid.
     */
    public SurfaceOperationalStatusV3Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE_CODE)
            throw new BadFormatException("Not surface operational status message");

        capabilityClassCode = b.readInt(9, 20);
        airplaneLenWidth = b.readByte(21, 24);
        operationalModeCode = b.readInt(25, 40);
        mopsVersion = b.readByte(41, 43);

        if (mopsVersion < 3)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        sil = b.readByte(51, 52);
        trackHeading = b.readBoolean(53);
        horizontalReferenceDirection = b.readBoolean(54);

        silSupplement = b.readBoolean(55);
    }

    /**
     * @return the subtype code is 0 for airborne operational status msgs
     * and 1 for surface operational status msgs; all other codes
     * are "reserved"
     */
    @Override
    public byte getSubtypeCode() {
        return SUBTYPE_CODE;
    }

    @Override
    public byte getAircraftVehicleLengthAndWidthEncoded() {
        return airplaneLenWidth;
    }

    /**
     * @return the raw encoded ADS-B Version Number (MOPS version); see ED-102B §2.2.3.2.7.2.5 TABLE 2-66
     */
    @Override
    public byte getMOPSVersion() {
        return mopsVersion;
    }

    /**
     * @return NIC supplement A (ME bit 44); see ED-102B §2.2.3.2.7.2.6
     */
    @Override
    public boolean getNICSupplementA() {
        return nicSupplementA;
    }

    /**
     * @return the raw encoded Navigation Accuracy Category for Position (NACP), ED-102B §2.2.3.2.7.2.7 TABLE 2-68
     */
    @Override
    public byte getNACpEncoded() {
        return nacP;
    }

    @Override
    public double getPositionUncertainty() {
        return SurfaceOperationalStatusMsg.super.getPositionUncertainty();
    }

    /**
     * @return the raw encoded Source Integrity Level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
    @Override
    public byte getSILEncoded() {
        return sil;
    }

    /**
     * @return true if the horizontal reference direction is magnetic north, false if true north
     */
    @Override
    public boolean isHeadingReferencedToMagneticNorth() {
        return horizontalReferenceDirection;
    }

    /**
     * @return the Track Angle/Heading allows correct interpretation of the data
     * contained in the Heading/Ground Track subfield of ADS-B Surface
     * Position Messages.
     */
    @Override
    public boolean hasTrackHeading() {
        return trackHeading;
    }

    /**
     * ED-102B §2.2.3.2.7.2.14
     *
     * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
     * it's based on "per hour".
     */
    @Override
    public boolean getSILSupplement() {
        return silSupplement;
    }

    @Override
    public String toString() {
        return "SurfaceOperationalStatusV3Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", airplaneLenWidth=" + airplaneLenWidth +
                ", version=" + mopsVersion +
                ", nicSupplementA=" + nicSupplementA +
                ", nacP=" + nacP +
                ", sil=" + sil +
                ", trackHeading=" + trackHeading +
                ", horizontalReferenceDirection=" + horizontalReferenceDirection +
                ", silSupplement=" + silSupplement +
                '}';
    }

    @Override
    public int getCapabilityClassCodeEncoded() {
        return capabilityClassCode;
    }

    @Override
    public CapabilityClassCode getCapabilityClass() {
        return CapabilityClassCodes.surfaceV3(capabilityClassCode);
    }

    @Override
    public int getOperationalModeCodeEncoded() {
        return operationalModeCode;
    }

    @Override
    public OperationalModeCode getOperationalMode() {
        return OperationalModeCodes.surfaceV3(operationalModeCode);
    }
}
