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
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusMsg;
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV2V3Msg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2V3Msg;

import java.io.Serializable;

/**
 * Decoder for the ADS-B operational status message, as defined in ED-102B (ADS-B version 3), with subtype 0 (airborne).
 */
public class AirborneOperationalStatusV3Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusMsg, AirborneOperationalStatusV2V3Msg, OperationalStatusV2V3Msg, ADSBMsg {

    private static final long serialVersionUID = 8236451097734510298L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte mopsVersion;
    private boolean nicSupplementA; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private byte gva; // bit 49 and 50
    private boolean silSupplement;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV3Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-B version or is not an airborne
     *                            operational status message.
     */
    public AirborneOperationalStatusV3Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational Status messages must have typecode 31");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE_CODE)
            throw new BadFormatException("Not an Airborne Operational Status message");

        capabilityClassCode = b.readInt(9, 24);
        operationalModeCode = b.readInt(25, 40);

        mopsVersion = b.readByte(41, 43);
        if (mopsVersion < 3)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        gva = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        // bits 53 and 54 reserved

        silSupplement = b.readBoolean(55);
    }

    @Override
    public byte getSubtypeCode() {
        return AirborneOperationalStatusV2V3Msg.super.getSubtypeCode();
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
        return AirborneOperationalStatusV2V3Msg.super.getPositionUncertainty();
    }

    /**
     * @return the raw encoded Source Integrity Level (SIL), ED-102B §2.2.3.2.7.2.9 TABLE 2-70
     */
    @Override
    public byte getSILEncoded() {
        return sil;
    }

    /**
     * @return the raw encoded Geometric Vertical Accuracy (GVA); see ED-102B §2.2.3.2.7.2.8 TABLE 2-69
     */
    @Override
    public byte getGVAEncoded() {
        return gva;
    }

    @Override
    public boolean getSILSupplement() {
        return silSupplement;
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
                ", geometricVerticalAccuracy=" + gva +
                ", silSupplement=" + silSupplement +
                '}';
    }

    @Override
    public int getCapabilityClassCodeEncoded() {
        return capabilityClassCode;
    }

    @Override
    public CapabilityClassCode getCapabilityClass() {
        return CapabilityClassCodes.adsbAirborneV3(capabilityClassCode);
    }

    @Override
    public int getOperationalModeCodeEncoded() {
        return operationalModeCode;
    }

    @Override
    public OperationalModeCode getOperationalMode() {
        return OperationalModeCodes.airborneV3(operationalModeCode);
    }
}
