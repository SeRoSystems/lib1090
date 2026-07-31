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

import java.io.Serializable;

/**
 * Decoder for ADS-B operational status message as specified in DO-260B (ADS-B version 2) with
 * subtype 1 (airborne)
 */
public class SurfaceOperationalStatusV2Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, OperationalStatusV2Msg {

    private static final long serialVersionUID = 5774750859726557576L;

    private int capabilityClassCode; // actually 16 bit unsigned
    private int operationalModeCode; // actually 16 bit unsigned
    private byte airplaneLenWidth; // length / width code
    private byte version;
    private boolean nicSupplement; // may be passed to position messages
    private byte nacPos; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private boolean trackHeadingInfo; // heading/ground track info
    private boolean horizontalReferenceDirection; // heading info is based on true north (0) or magnetic north (1)
    private boolean silSupplement;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version or is not a surface
     *                                operational status message or the capability class code or operational mode
     *                                code is invalid.
     * @throws UnspecifiedFormatError if message has the wrong subtype
     */
    public SurfaceOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);
        setType(subtype.ADSB_SURFACE_STATUS_V2);

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
        version = b.readByte(41, 43);

        if (version < 2)
            throw new BadFormatException("Unsupported operational status version " + version);

        if ((capabilityClassCode & 0xC00) != 0)
            throw new BadFormatException("Unknown capability class code!");
        if ((operationalModeCode & 0xC000) != 0)
            throw new BadFormatException("Unknown operational mode code!");

        nicSupplement = b.readByte(44, 44) == 1;
        nacPos = b.readByte(45, 48);
        // bits 49 and 50 reserved
        sil = b.readByte(51, 52);
        trackHeadingInfo = b.readByte(53, 53) == 1;
        horizontalReferenceDirection = b.readByte(54, 54) == 1;

        silSupplement = b.readByte(55, 55) == 1;
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
    public byte getVersion() {
        return version;
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
    public boolean hasTrackHeadingInfo() {
        return trackHeadingInfo;
    }

    /**
     * @return whether aircraft has an UAT receiver
     */
    @Override
    public boolean hasUATIn() {
        return (capabilityClassCode & 0x10) != 0;
    }

    /**
     * @return navigation accuracy category for velocity
     */
    public byte getNACv() {
        return (byte) ((capabilityClassCode & 0xE) >>> 1);
    }

    /**
     * @return NIC supplement C for use on the surface
     */
    public boolean getNICSupplementC() {
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
     * For interpretation see Table 2-65 in DO-260B
     *
     * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B)
     */
    @Override
    public byte getSDAEncoded() {
        return (byte) ((operationalModeCode & 0x300) >>> 8);
    }

    /**
     * @return encoded longitudinal and lateral distance of the GPS Antenna from the NOSE of the aircraft
     * (see Table 2-66 and 2-67, RTCA DO-260B)
     */
    public byte getGPSAntennaOffsetEncoded() {
        return (byte) (operationalModeCode & 0xFF);
    }

    /**
     * Get lateral axis GPS antenna offset.
     * <ul>
     *     <li>values are measured from the longitudinal center line (=roll axis) of the aircraft</li>
     *     <li>values are given in meters</li>
     *     <li>values denote an upper bound</li>
     *     <li>positive values mean "toward left wing tip"</li>
     *     <li>negative values mean "toward right wind tip"</li>
     *     <li>values have a resolution of 2m</li>
     *     <li>values are capped at 6m, i.e. 6 means "or above"</li>
     *     <li>{@code null} means "no data"</li>
     * </ul>
     *
     * @return lateral axis GPS Antenna offset in meters
     * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
     */
    public Integer getLateralAxisGPSAntennaOffset() {
        int offset3 = getGPSAntennaOffsetEncoded() >>> 5;
        int offset = offset3 & 0x3;
        boolean right = (offset3 & 0x4) != 0;
        return !right && offset == 0 ? null :
                2 * (right ? -offset : offset);
    }

    /**
     * Get longitudinal axis GPS antenna offset.
     * <ul>
     *     <li>values are measured from the nose of the aircraft</li>
     *     <li>values are given in meters</li>
     *     <li>values denote an upper bound</li>
     *     <li>values have a resolution of 2m</li>
     *     <li>values are capped at 60m, i.e. 60 means "or above"</li>
     *     <li>{@code null} means "no data"</li>
     * </ul>
     *
     * @return longitudinal axis GPS Antenna offset in meters
     * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
     */
    public Integer getLongitudinalAxisGPSAntennaOffset() {
        int offset = getGPSAntennaOffsetEncoded() & 0x1f;
        return offset == 0 ? null : 2 * (offset - 1);
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
        return "SurfaceOperationalStatusV2Msg{" + super.toString() +
                ", silSupplement=" + silSupplement +
                ", capabilityClassCode=" + capabilityClassCode +
                ", operationalModeCode=" + operationalModeCode +
                '}';
    }
}
