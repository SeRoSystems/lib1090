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

import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV1V2Msg;
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV2V3Msg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status message as specified in DO-260B (ADS-R version 2) with
 * subtype 0 (airborne)
 */
public class AirborneOperationalStatusV2Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusV1V2Msg, AirborneOperationalStatusV2V3Msg, OperationalStatusV2Msg {

    private static final long serialVersionUID = 1248921149454196589L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte version;
    private boolean nicSupplement; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte gvaEncoded; // bits 49 and 50
    private byte sil; // surveillance integrity level
    private boolean nicBaro;
    private boolean hrd; // heading info is based on true north (0) or magnetic north (1)
    private boolean silSupplement;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not an airborne
     *                                operational status message or the capability code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 31) {
            throw new BadFormatException("Operational status messages must have typecode 31.");
        }

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 1) { // currently only 0 and 1 specified, 2-7 are reserved
            throw new UnspecifiedFormatError("Operational status message subtype " + subtypeCode + " reserved.");
        } else if (subtypeCode != SUBTYPE_CODE) {
            throw new BadFormatException("Not an airborne operational status message");
        }

        capabilityClassCode = b.readInt(9, 24);
        operationalModeCode = b.readInt(25, 40);
        version = b.readByte(41, 43);
        if (version != 2)
            throw new BadFormatException("Not a DO-260B/version 2 status message.");

        if ((capabilityClassCode & 0xC000) != 0)
            throw new BadFormatException("Unknown capability class code!");

        nicSupplement = b.readByte(44, 44) == 1;
        nacP = b.readByte(45, 48);
        gvaEncoded = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        nicBaro = b.readByte(53, 53) == 1;
        hrd = b.readByte(54, 54) == 1;
        silSupplement = b.readByte(55, 55) == 1;
    }

    @Override
    public byte getSubtypeCode() {
        return AirborneOperationalStatusV1V2Msg.super.getSubtypeCode();
    }

    @Override
    public boolean hasOperationalTCAS() {
        return (capabilityClassCode & 0x2000) != 0;
    }

    @Override
    public boolean has1090ESIn() {
        return (capabilityClassCode & 0x1000) != 0;
    }

    @Override
    public boolean hasAirReferencedVelocity() {
        return (capabilityClassCode & 0x200) != 0;
    }

    @Override
    public boolean hasTargetStateReport() {
        return (capabilityClassCode & 0x100) != 0;
    }

    @Override
    public byte getTargetChangeReportCapabilityEncoded() {
        return (byte) ((capabilityClassCode & 0xC0) >>> 6);
    }

    @Override
    public boolean hasUATIn() {
        return (capabilityClassCode & 0x20) != 0;
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
    public boolean hasSingleAntenna() {
        return (operationalModeCode & 0x400) != 0;
    }

    @Override
    public byte getSDAEncoded() {
        return (byte) ((operationalModeCode & 0x300) >>> 8);
    }

    @Override
    public byte getMOPSVersion() {
        return version;
    }

    @Override
    public boolean hasNICSupplementA() {
        return nicSupplement;
    }

    @Override
    public byte getNACpEncoded() {
        return nacP;
    }

    @Override
    public double getPositionUncertainty() {
        return AirborneOperationalStatusV1V2Msg.super.getPositionUncertainty();
    }

    @Override
    public byte getGVAEncoded() {
        return gvaEncoded;
    }

    @Override
    public byte getSILEncoded() {
        return sil;
    }

    @Override
    public boolean getBarometricAltitudeIntegrityCode() {
        return nicBaro;
    }

    @Override
    public boolean getHorizontalReferenceDirection() {
        return hrd;
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
    public String toString() {
        return "AirborneOperationalStatusV2Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", version=" + version +
                ", nicSupplement=" + nicSupplement +
                ", nacP=" + nacP +
                ", gvaEncoded=" + gvaEncoded +
                ", sil=" + sil +
                ", nicBaro=" + nicBaro +
                ", hrd=" + hrd +
                ", silSupplement=" + silSupplement +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_AIRBORN_STATUS_V2;
    }
}
