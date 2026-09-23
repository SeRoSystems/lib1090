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
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.CapabilityClassCodes;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.OperationalModeCodes;
import de.serosystems.lib1090.msgs.squitter.*;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status messages (version 2), subtype 0 (airborne), as
 * defined in ED-102B §2.2.18.4.7, which gives the differences from the ADS-B format of
 * ED-102B Appendix N §N.5.3 Figure N-24.
 */
public class AirborneOperationalStatusV2Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusV1V2Msg, AirborneOperationalStatusV2V3Msg, OperationalStatusV2Msg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 2841937650192837465L;

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
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public AirborneOperationalStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-R version or is not an airborne
     *                            operational status message.
     */
    public AirborneOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31");

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode != SUBTYPE_CODE)
            throw new BadFormatException("Not an airborne operational status message");

        capabilityClassCode = b.readInt(9, 24);
        operationalModeCode = b.readInt(25, 40);

        mopsVersion = b.readByte(41, 43);
        if (mopsVersion < 2)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        nicSupplementA = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        gva = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        nicBaro = b.readBoolean(53);
        hrd = b.readBoolean(54);

        silSupplement = b.readBoolean(55);
        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
    }

    @Override
    public byte getSubtypeCode() {
        return AirborneOperationalStatusV1V2Msg.super.getSubtypeCode();
    }

    @Override
    public byte getMOPSVersion() {
        return mopsVersion;
    }

    @Override
    public boolean getNICSupplementA() {
        return nicSupplementA;
    }

    @Override
    public byte getNACpEncoded() {
        return nacP;
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
    public boolean isHeadingReferencedToMagneticNorth() {
        return hrd;
    }

    @Override
    public byte getGVAEncoded() {
        return gva;
    }

    @Override
    public boolean getSILSupplement() {
        return silSupplement;
    }

    @Override
    public boolean getIMF() {
        return imf;
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
                ", imf=" + imf +
                '}';
    }

    @Override
    public int getCapabilityClassCodeEncoded() {
        return capabilityClassCode;
    }

    @Override
    public CapabilityClassCode getCapabilityClass() {
        return CapabilityClassCodes.adsrAirborneV2(capabilityClassCode);
    }

    @Override
    public int getOperationalModeCodeEncoded() {
        return operationalModeCode;
    }

    @Override
    public OperationalModeCode getOperationalMode() {
        return OperationalModeCodes.airborneV2(operationalModeCode);
    }
}
