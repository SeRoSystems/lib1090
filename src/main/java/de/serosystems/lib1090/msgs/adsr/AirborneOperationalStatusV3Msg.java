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
 * subtype 0 (airborne)
 */
public class AirborneOperationalStatusV3Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusMsg, AirborneOperationalStatusV2V3Msg, OperationalStatusV2V3Msg, ADSBReceiverVersionMsg, IMFMsg, NICSupplementBMsg, ADSRMsg {

    private static final long serialVersionUID = 6172938451029384756L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte mopsVersion;
    private boolean nicSupplementA; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private byte gva; // bit 49 and 50
    private boolean silSupplement;
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV3Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not an airborne
     *                                operational status message or the capability class code or operational mode
     *                                code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV3Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31.");

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 1) // currently only 0 and 1 specified, 2-7 are reserved
            throw new UnspecifiedFormatError("Operational status message subtype " + subtypeCode + " reserved.");
        else if (subtypeCode != SUBTYPE_CODE)
            throw new BadFormatException("Not an airborne operational status message");

        capabilityClassCode = b.readInt(9, 24);
        operationalModeCode = b.readInt(25, 40);

        mopsVersion = b.readByte(41, 43);
        if (mopsVersion < 3)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        if ((capabilityClassCode & 0xC000) != 0)
            throw new BadFormatException("Unknown capability class code!");

        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        gva = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        // bits 53 and 54 reserved

        silSupplement = b.readBoolean(55);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
    }

    @Override
    public byte getSubtypeCode() {
        return AirborneOperationalStatusV2V3Msg.super.getSubtypeCode();
    }

    @Override
    public boolean hasOperationalTCAS() {
        return (capabilityClassCode & 0x2000) != 0;
    }

    @Override
    public boolean has1090ESIn() {
        return (capabilityClassCode & 0x1000) != 0;
    }

    /**
     * capabilityClassCode covers ME bits 9-24, i.e. bit m of the ME is bit (24-m) of this field.
     *
     * @return the encoded transponder side indication (ME bits 15-16); see ED-102B §2.2.3.2.7.2.3.4 TABLE 2-50
     */
    public byte getTransponderSideIndicationEncoded() {
        return (byte) ((capabilityClassCode & 0x300) >>> 8);
    }

    /**
     * capabilityClassCode covers ME bits 9-24, i.e. bit m of the ME is bit (24-m) of this field.
     *
     * @return the encoded transmit power (ME bits 17-18); see ED-102B §2.2.3.2.7.2.3.6 TABLE 2-51
     */
    public byte getTxPowerEncoded() {
        return (byte) ((capabilityClassCode & 0xC0) >>> 6);
    }

    /**
     * capabilityClassCode covers ME bits 9-24, i.e. bit m of the ME is bit (24-m) of this field.
     *
     * @return the encoded Reduced Capability Equipment (RCE) capability (ME bits 21-22); see ED-102B §2.2.3.2.7.2.3.11 TABLE 2-53
     */
    public byte getReducedCapabilityEquipmentEncoded() {
        return (byte) ((capabilityClassCode & 0xC) >>> 2);
    }

    /**
     * capabilityClassCode covers ME bits 9-24, i.e. bit m of the ME is bit (24-m) of this field.
     *
     * @return the encoded Detect and Avoid (DAA) capability (ME bits 23-24); see ED-102B §2.2.3.2.7.2.3.12 TABLE 2-54
     */
    public byte getDetectAndAvoidEncoded() {
        return (byte) (capabilityClassCode & 0x3);
    }

    @Override
    public byte getADSBReceiverVersionEncoded() {
        return (byte) ((capabilityClassCode & 0xC00) >>> 10);
    }

    /**
     * capabilityClassCode covers ME bits 9-24, i.e. bit m of the ME is bit (24-m) of this field.
     *
     * @return NIC supplement B (ME bit 20)
     */
    @Override
    public boolean hasNICSupplementB() {
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
        return AirborneOperationalStatusV2V3Msg.super.getPositionUncertainty();
    }

    @Override
    public byte getSILEncoded() {
        return sil;
    }

    @Override
    public boolean hasUATIn() {
        return (capabilityClassCode & 0x20) != 0;
    }

    @Override
    public boolean hasSingleAntenna() {
        return (operationalModeCode & 0x400) != 0;
    }

    @Override
    public byte getGVAEncoded() {
        return gva;
    }

    @Override
    public byte getSDAEncoded() {
        return (byte) ((operationalModeCode & 0x300) >>> 8);
    }

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
        return "AirborneOperationalStatusV3Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", mopsVersion=" + mopsVersion +
                ", nicSupplement=" + nicSupplementA +
                ", nacP=" + nacP +
                ", sil=" + sil +
                ", transponderSideIndication=" + getTransponderSideIndicationEncoded() +
                ", txPower=" + getTxPowerEncoded() +
                ", rce=" + getReducedCapabilityEquipmentEncoded() +
                ", daa=" + getDetectAndAvoidEncoded() +
                ", geometricVerticalAccuracy=" + gva +
                ", silSupplement=" + silSupplement +
                ", imf=" + imf +
                '}';
    }

}
