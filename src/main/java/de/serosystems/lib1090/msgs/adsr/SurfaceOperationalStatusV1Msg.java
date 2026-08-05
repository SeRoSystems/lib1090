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

import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV1Msg;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status message as specified in DO-260A (ADS-R version 1) with
 * subtype 1 (surface)
 */
public class SurfaceOperationalStatusV1Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, OperationalStatusV1Msg {

    private static final long serialVersionUID = 1146349460486137986L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte airplaneLenWidth; // length / width code
    private boolean nicSupplement; // may be passed to position messages
    private byte nacPos; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private boolean trackHeadingInfo; // heading/ground track info
    private boolean hrd; // heading info is based on true north (0) or magnetic north (1)
    private boolean uatIn;
    private byte nacv;
    private boolean nicSupplementC;
    private boolean nicSupplementB;
    private boolean imf; // bit 56 -- ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalStatusV1Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not a surface
     *                                operational status message or the capability class code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
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

        byte version = b.readByte(41, 43);

        if ((capabilityClassCode & 0xC00) != 0)
            throw new BadFormatException("Unknown capability class code!");
        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplement = b.readByte(44, 44) == 1;
        nacPos = b.readByte(45, 48);
        // bits 49 and 50 reserved
        sil = b.readByte(51, 52);
        trackHeadingInfo = b.readByte(53, 53) == 1;
        hrd = b.readByte(54, 54) == 1;
        imf = b.readByte(56, 56) == 1;

        uatIn = b.readByte(12, 12) == 1;
        nacv = b.readByte(13, 15);
        nicSupplementC = b.readByte(16, 16) == 1;
        nicSupplementB = b.readByte(20, 20) == 1;
    }

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
        return (capabilityClassCode & 0x200) != 0;
    }

    @Override
    public boolean hasTCASResolutionAdvisory() {
        return (operationalModeCode & 0x2000) != 0;
    }

    @Override
    public boolean hasActiveIDENTSwitch() {
        return (operationalModeCode & 0x1000) != 0;
    }

    @Override
    public boolean hasReceivingATCServices() {
        return (operationalModeCode & 0x800) != 0;
    }

    @Override
    public byte getAircraftVehicleLengthAndWidthEncoded() {
        return airplaneLenWidth;
    }

    @Override
    public byte getMOPSVersion() {
        return 1;
    }

    @Override
    public boolean hasNICSupplementA() {
        return nicSupplement;
    }

    @Override
    public byte getNACpEncoded() {
        return nacPos;
    }

    @Override
    public double getPositionUncertainty() {
        return SurfaceOperationalStatusMsg.super.getPositionUncertainty();
    }

    @Override
    public byte getSILEncoded() {
        return sil;
    }

    @Override
    public boolean hasTrackHeading() {
        return trackHeadingInfo;
    }

    @Override
    public boolean getHorizontalReferenceDirection() {
        return hrd;
    }

    /**
     * @return whether aircraft has an UAT receiver
     */
    public boolean hasUATIn() {
        return uatIn;
    }

    /**
     * @return navigation accuracy category for velocity
     */
    public byte getNACv() {
        return nacv;
    }

    /**
     * @return NIC supplement C for use on the surface
     */
    public boolean getNICSupplementC() {
        return nicSupplementC;
    }

    /**
     * @return NIC supplement B for use on the surface
     */
    public boolean getNICSupplementB() {
        return nicSupplementB;
    }

    /**
     * @return the ICAO Mode A Flag (for address type determination)
     */
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "SurfaceOperationalStatusV1Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", airplaneLenWidth=" + airplaneLenWidth +
                ", nicSupplement=" + nicSupplement +
                ", nacPos=" + nacPos +
                ", sil=" + sil +
                ", trackHeadingInfo=" + trackHeadingInfo +
                ", hrd=" + hrd +
                ", uatIn=" + uatIn +
                ", nacv=" + nacv +
                ", nicSupplementC=" + nicSupplementC +
                ", nicSupplementB=" + nicSupplementB +
                ", imf=" + imf +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_SURFACE_STATUS_V1;
    }
}
