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
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusV2V3Msg;

import java.io.Serializable;

/**
 * Decoder for ADS-B operational status message as specified in DO-260B (ADS-B version 2) with subtype 1 (airborne)
 */
public class SurfaceOperationalStatusV2Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, SurfaceOperationalStatusV2V3Msg, OperationalStatusV2Msg, ADSBMsg {

    private static final long serialVersionUID = 353104982635210544L;

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
    protected SurfaceOperationalStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public SurfaceOperationalStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public SurfaceOperationalStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version or is not a surface
     *                                operational status message or the capability class code or operational mode
     *                                code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public SurfaceOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 31) {
            throw new BadFormatException("Operational status messages must have typecode 31.");
        }

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 1) { // currently only 0 and 1 specified, 2-7 are reserved
            throw new UnspecifiedFormatError("Operational status message subtype " + subtypeCode + " reserved.");
        } else if (subtypeCode != SUBTYPE_CODE) {
            throw new BadFormatException("Not surface operational status message");
        }

        capabilityClassCode = b.readInt(9, 20);
        airplaneLenWidth = b.readByte(21, 24);
        operationalModeCode = b.readInt(25, 40);
        mopsVersion = b.readByte(41, 43);

        if (mopsVersion < 2)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        if ((capabilityClassCode & 0xC00) != 0)
            throw new BadFormatException("Unknown capability class code!");
        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        // bits 49 and 50 reserved
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
    public boolean has1090ESIn() {
        return (capabilityClassCode & 0x100) != 0;
    }

    @Override
    public boolean hasLowTxPower() {
        return (capabilityClassCode & 0x20) != 0;
    }

    @Override
    public boolean hasPositionOffsetApplied() {
        // Position offset applied, per ED-129B; see ED-102B §2.2.3.2.7.2.4.7 for the encoded offset field
        return getGPSAntennaOffsetEncoded() == 0x1;
    }

    @Override
    public byte getAircraftVehicleLengthAndWidthEncoded() {
        return airplaneLenWidth;
    }

    /**
     * @return whether TCAS Resolution Advisory (RA) is active
     */
    @Override
    public boolean hasTCASResolutionAdvisory() {
        return (operationalModeCode & 0x2000) != 0;
    }

    /**
     * @return whether the IDENT switch is active
     */
    @Override
    public boolean hasActiveIDENTSwitch() {
        return (operationalModeCode & 0x1000) != 0;
    }

    /**
     * @return whether ADS-B Transmitting Subsystem is receiving ATC services.
     */
    @Override
    public boolean hasReceivingATCServices() {
        return (operationalModeCode & 0x800) != 0;
    }

    @Override
    public byte getMOPSVersion() {
        return mopsVersion;
    }

    @Override
    public boolean hasNICSupplementA() {
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
        return SurfaceOperationalStatusV2V3Msg.super.getPositionUncertainty();
    }

    /**
     * @return the raw encoded Source Integrity Level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
    @Override
    public byte getSILEncoded() {
        return sil;
    }

    /**
     * @return 0 if horizontal reference direction is the true north, 1 if magnetic north
     */
    @Override
    public boolean getHorizontalReferenceDirection() {
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
     * @return whether aircraft has an UAT receiver
     */
    @Override
    public boolean hasUATIn() {
        return (capabilityClassCode & 0x10) != 0;
    }

    @Override
    public byte getNACv() {
        return (byte) ((capabilityClassCode & 0xE) >>> 1);
    }

    @Override
    public boolean hasNICSupplementC() {
        return (capabilityClassCode & 0x1) != 0;
    }

    /**
     * @return whether aircraft uses a single antenna or two
     */
    @Override
    public boolean hasSingleAntenna() {
        return (operationalModeCode & 0x400) != 0;
    }

    /**
     * For interpretation see ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58
     *
     * @return system design assurance; see ED-102B §A.1.4.10.14
     */
    @Override
    public byte getSDAEncoded() {
        return (byte) ((operationalModeCode & 0x300) >>> 8);
    }

    @Override
    public byte getGPSAntennaOffsetEncoded() {
        return (byte) (operationalModeCode & 0xFF);
    }

    /**
     * ED-102B §2.2.3.2.7.2.14
     *
     * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
     * it's based on "per hour".
     */
    @Override
    public boolean hasSILSupplement() {
        return silSupplement;
    }

    @Override
    public String toString() {
        return "SurfaceOperationalStatusV2Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", airplaneLenWidth=" + airplaneLenWidth +
                ", mopsVersion=" + mopsVersion +
                ", nicSupplementA=" + nicSupplementA +
                ", nacP=" + nacP +
                ", sil=" + sil +
                ", trackHeading=" + trackHeading +
                ", horizontalReferenceDirection=" + horizontalReferenceDirection +
                ", silSupplement=" + silSupplement +
                '}';
    }

}
