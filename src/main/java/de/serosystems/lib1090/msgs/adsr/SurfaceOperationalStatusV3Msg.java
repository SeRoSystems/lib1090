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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.*;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status message as specified in DO-260C (ADS-R version 3) with
 * subtype 1 (surface)
 */
public class SurfaceOperationalStatusV3Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, SurfaceOperationalStatusV2V3Msg, OperationalStatusV2Msg, ADSBReceiverVersionMsg, IMFMsg {

    private static final long serialVersionUID = -2039485716203948571L;

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
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalStatusV3Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not a surface
     *                                operational status message or the capability class code or operational mode
     *                                code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV3Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
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

        if (mopsVersion < 3)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        if ((capabilityClassCode & 0xC00) != 0)
            throw new BadFormatException("Unknown capability class code!");
        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplementA = b.readByte(44, 44) == 1;
        nacP = b.readByte(45, 48);
        sil = b.readByte(51, 52);
        trackHeading = b.readByte(53, 53) == 1;
        horizontalReferenceDirection = b.readByte(54, 54) == 1;

        silSupplement = b.readByte(55, 55) == 1;
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readByte(56, 56) == 1;
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
        // Note: using definition of ED-129B, which is a bit more explicit than DO-260B
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
     * @return whether ADS-B Transmitting Subsystem< is receiving ATC services.
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

    @Override
    public byte getNACpEncoded() {
        return nacP;
    }

    @Override
    public double getPositionUncertainty() {
        return SurfaceOperationalStatusV2V3Msg.super.getPositionUncertainty();
    }

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

    @Override
    public byte getADSBReceiverVersionEncoded() {
        return (byte) ((capabilityClassCode & 0xC0) >>> 6);
    }

    /**
     * @return whether aircraft uses a single antenna or two
     */
    @Override
    public boolean hasSingleAntenna() {
        return (operationalModeCode & 0x400) != 0;
    }

    /**
     * For interpretation see Table 2-65 in DO-260B
     *
     * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B)
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
     * DO-260B 2.2.3.2.7.2.14
     *
     * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
     * it's based on "per hour".
     */
    @Override
    public boolean hasSILSupplement() {
        return silSupplement;
    }

    @Override
    public boolean getIMF() {
        return imf;
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
                ", imf=" + imf +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_SURFACE_STATUS_V3;
    }
}
