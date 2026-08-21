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
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV1V2Msg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.NICSupplementBMsg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV1Msg;

import java.io.Serializable;

/**
 * Decoder for ADS-R operational status message as specified in DO-260A (ADS-R version 1) with subtype 0 (airborne)
 */
public class AirborneOperationalStatusV1Msg extends ExtendedSquitter implements Serializable, AirborneOperationalStatusV1V2Msg, OperationalStatusV1Msg, IMFMsg, NICSupplementBMsg, ADSRMsg {

    private static final long serialVersionUID = -4917283654012938471L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private boolean nicSupplement; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private byte baq;
    private boolean nicBaro;
    private boolean hrd; // heading is based on true north (0) or magnetic north (1)
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalStatusV1Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version or is not an airborne
     *                                operational status message or the capability class code or operational mode
     *                                code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype; see ED-102B §2.2.3.2.7.2.2 TABLE 2-46
     */
    public AirborneOperationalStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31.");

        BitReader b = BitReader.forBigEndian(this.getMessage());

        byte subtypeCode = b.readByte(6, 8);
        if (subtypeCode > 1) { // currently only 0 and 1 specified, 2-7 are reserved
            throw new UnspecifiedFormatError("Operational status message subtype " + subtypeCode + " reserved.");
        } else if (subtypeCode != SUBTYPE_CODE) {
            throw new BadFormatException("Not an airborne operational status message");
        }

        capabilityClassCode = b.readInt(9, 24);
        operationalModeCode = b.readInt(25, 40);

        int mopsVersion = b.readByte(41, 43);
        if (mopsVersion != 1)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);

        if ((capabilityClassCode & 0xC000) != 0)
            throw new BadFormatException("Unknown capability class code!");
        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplement = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        baq = b.readByte(49, 50);
        sil = b.readByte(51, 52);
        nicBaro = b.readBoolean(53);

        hrd = b.readBoolean(54);

        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
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
        return 1;
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
    public byte getSILEncoded() {
        return sil;
    }

    /**
     * @return the barometric altitude quality BAQ bit.
     */
    public byte getBAQ() {
        return baq;
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
    public boolean getIMF() {
        return imf;
    }

    @Override
    public String toString() {
        return "AirborneOperationalStatusV1Msg{" + super.toString() +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                ", nicSupplement=" + nicSupplement +
                ", nacPos=" + nacP +
                ", sil=" + sil +
                ", baq=" + baq +
                ", nicBaro=" + nicBaro +
                ", hrd=" + hrd +
                ", imf=" + imf +
                '}';
    }

}
