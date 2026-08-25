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
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV1V2Msg;
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV2V3Msg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;

import java.io.Serializable;

/**
 * Decoder for the ADS-B operational status message, as defined in ED-102A (ADS-B version 2), with subtype 0 (airborne).
 */
public class AirborneOperationalStatusV2Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusV1V2Msg, AirborneOperationalStatusV2V3Msg, OperationalStatusV2Msg, ADSBMsg {

    private static final long serialVersionUID = 939103146345326372L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte mopsVersion;
    private boolean nicSupplementA; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private boolean nicBaro;
    private boolean hrd; // heading is based on true north (0) or magnetic north (1)
    private byte gva; // bit 49 and 50
    private boolean silSupplement;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-B version or is not an airborne
     *                            operational status message or the capability class code or operational mode
     *                            code is invalid.
     */
    public AirborneOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
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
        if (mopsVersion < 2)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        if ((capabilityClassCode & 0xC000) != 0)
            throw new BadFormatException("Unknown capability class code!");

        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        gva = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        nicBaro = b.readBoolean(53);
        hrd = b.readBoolean(54);

        silSupplement = b.readBoolean(55);
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
        return (capabilityClassCode & 0x0200) != 0;
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

    /**
     * @return the raw encoded Navigation Accuracy Category for Position (NACP), ED-102B §2.2.3.2.7.2.7 TABLE 2-68
     */
    @Override
    public byte getNACpEncoded() {
        return nacP;
    }

    @Override
    public double getPositionUncertainty() {
        return AirborneOperationalStatusV1V2Msg.super.getPositionUncertainty();
    }

    /**
     * @return the raw encoded Source Integrity Level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
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
    public String toString() {
        return "AirborneOperationalStatusV2Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", mopsVersion=" + mopsVersion +
                ", nicSupplementA=" + nicSupplementA +
                ", nacP=" + nacP +
                ", sil=" + sil +
                ", nicBaro=" + nicBaro +
                ", hrd=" + hrd +
                ", geometricVerticalAccuracy=" + gva +
                ", silSupplement=" + silSupplement +
                '}';
    }

}
