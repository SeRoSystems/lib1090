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
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;

import java.io.Serializable;

/**
 * Decoder for the ADS-R surface operational status message (subtype 1), as defined in DO-260A (ADS-R version 1).
 */
public class SurfaceOperationalStatusV1Msg extends ExtendedSquitter implements Serializable, SurfaceOperationalStatusMsg, OperationalStatusV1Msg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = -3948572019384756123L;

    protected int capabilityClassCode; // actually 16 bit unsigned
    protected int operationalModeCode; // actually 16 bit unsigned
    private byte airplaneLenWidth; // length / width code
    private boolean nicSupplement; // may be passed to position messages
    private byte nacP; // navigational accuracy category - position
    private byte sil; // surveillance integrity level
    private boolean trackHeading; // heading/ground track
    private boolean horizontalReferenceDirection; // heading is based on true north (0) or magnetic north (1)
    private boolean imf; // ADS-R-specific, occupies an otherwise-spare/reserved bit

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalStatusV1Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceOperationalStatusV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-R version
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public SurfaceOperationalStatusV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException if message has the wrong typecode or ADS-R version or is not a surface
     *                            operational status message or the capability class code or operational mode
     *                            code is invalid.
     */
    public SurfaceOperationalStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 31)
            throw new BadFormatException("Operational status messages must have typecode 31");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE_CODE)
            throw new BadFormatException("Not surface operational status message");

        capabilityClassCode = b.readInt(9, 20);
        airplaneLenWidth = b.readByte(21, 24);
        operationalModeCode = b.readInt(25, 40);

        int mopsVersion = b.readByte(41, 43);
        if (mopsVersion != 1)
            throw new BadFormatException("Unsupported operational status version " + mopsVersion);


        nicSupplement = b.readBoolean(44);
        nacP = b.readByte(45, 48);
        // bits 49 and 50 reserved
        sil = b.readByte(51, 52);
        trackHeading = b.readBoolean(53);
        horizontalReferenceDirection = b.readBoolean(54);

        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
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

    /**
     * @return raw aircraft vehicle length and width code (4 bit)
     */
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
        return nacP;
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
     * @return the Track Angle/Heading allows correct interpretation of the data
     * contained in the Heading/Ground Track subfield of ADS-B Surface
     * Position Messages.
     */
    public boolean hasTrackHeading() {
        return trackHeading;
    }

    /**
     * @return 0 if horizontal reference direction is the true north, 1 if magnetic north
     */
    public boolean getHorizontalReferenceDirection() {
        return horizontalReferenceDirection;
    }

    @Override
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
                ", nacP=" + nacP +
                ", sil=" + sil +
                ", trackHeading=" + trackHeading +
                ", horizontalReferenceDirection=" + horizontalReferenceDirection +
                ", imf=" + imf +
                '}';
    }

    @Override
    public int getCapabilityClassCodeEncoded() {
        return capabilityClassCode;
    }

    @Override
    public CapabilityClassCode getCapabilityClass() {
        return CapabilityClassCodes.surfaceV1(capabilityClassCode);
    }

    @Override
    public int getOperationalModeCodeEncoded() {
        return operationalModeCode;
    }

    @Override
    public OperationalModeCode getOperationalMode() {
        return OperationalModeCodes.v1(operationalModeCode);
    }
}
