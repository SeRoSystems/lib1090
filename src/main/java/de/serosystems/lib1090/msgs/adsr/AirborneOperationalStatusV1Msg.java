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
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV1Msg;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status message as specified in DO-260A (ADS-R version 1) with
 * subtype 0 (airborne)
 */
public class AirborneOperationalStatusV1Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusV1V2Msg, OperationalStatusV1Msg {

    private static final long serialVersionUID = 1284570176022959367L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte version;
    private boolean nicSupplement; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte gvaEncoded; // bits 49 and 50
    private byte sil; // surveillance integrity level
    private boolean nicBaro; // NIC baro for airborne status, heading/ground track info else
    private boolean hrd; // heading info is based on true north (0) or magnetic north (1)
    private boolean imf;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV1Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not an airborne
     *                                operational status message or the capability code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public AirborneOperationalStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
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

        if ((capabilityClassCode & 0xC000) != 0)
            throw new BadFormatException("Unknown capability class code!");

        nicSupplement = b.readByte(44, 44) == 1;
        nacP = b.readByte(45, 48);
        gvaEncoded = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        nicBaro = b.readByte(53, 53) == 1;
        hrd = b.readByte(54, 54) == 1;
        imf = b.readByte(56, 56) == 1;
    }

    @Override
    public byte getSubtypeCode() {
        return SUBTYPE_CODE;
    }

    @Override
    public boolean hasOperationalTCAS() {
        return (capabilityClassCode & 0x2000) == 0;
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

    /**
     * @return whether aircraft has an UAT receiver
     */
    public boolean hasUATIn() {
        return (capabilityClassCode & 0x20) != 0;
    }

    /**
     * @return NIC supplement B
     */
    public boolean getNICSupplementB() {
        return (capabilityClassCode & 0x10) != 0;
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

    /**
     * @return the encoded geometric vertical accuracy (see DO-260B 2.2.3.2.7.2.8)
     */
    public byte getGVAEncoded() {
        return gvaEncoded;
    }

    /**
     * @return the geometric vertical accuracy in meters or -1 for unknown
     */
    public int getGeometricVerticalAccuracy() {
        if (gvaEncoded == 1)
            return 150;
        else if (gvaEncoded == 2)
            return 45;
        else return -1;
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
     * @return the ICAO Mode A Flag (for address type determination)
     */
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "AirborneOperationalStatusV1Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", version=" + version +
                ", nicSupplement=" + nicSupplement +
                ", nacP=" + nacP +
                ", gvaEncoded=" + gvaEncoded +
                ", sil=" + sil +
                ", nicBaro=" + nicBaro +
                ", hrd=" + hrd +
                ", imf=" + imf +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_AIRBORN_STATUS_V1;
    }
}
