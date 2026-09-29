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
import de.serosystems.lib1090.decoding.EmergencyOrPriorityStatus;
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.decoding.emergency.EmergencyStateV3;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.EmergencyOrPriorityStatusMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.ModeACodeMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-R emergency and priority status messages (version 3), as defined in ED-102B §2.2.18.4.5
 * Figure 2-60.
 */
public class EmergencyOrPriorityStatusV3Msg extends TypeCodedExtendedSquitter implements Serializable, EmergencyOrPriorityStatusMsg, ModeACodeMsg, IMFMsg, ADSRMsg {

    private static final long serialVersionUID = 3583128001201045981L;

    private static final byte SUBTYPE = 1;

    private byte emergencyState;
    private short modeACode;
    private boolean unmanned;
    private byte meanEdrEncoded;
    private byte peakEdrEncoded;
    private byte peakEdrOffsetEncoded;
    private short waterVaporEncoded;
    private boolean imf;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected EmergencyOrPriorityStatusV3Msg() {
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public EmergencyOrPriorityStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public EmergencyOrPriorityStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this emergency or priority status msg
     * @throws BadFormatException if message has wrong format
     */
    public EmergencyOrPriorityStatusV3Msg(TypeCodedExtendedSquitter squitter) throws BadFormatException {
        super(squitter);
        ADSRMsg.checkADSR(this);

        if (getFormatTypeCode() != 28)
            throw new BadFormatException("Emergency and Priority Status messages must have typecode 28");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("Emergency And Priority Status messages must have subtype 1");

        emergencyState = b.readByte(9, 11);
        modeACode = b.readShort(12, 24);

        unmanned = b.readBoolean(25);
        meanEdrEncoded = b.readByte(26, 32);
        peakEdrEncoded = b.readByte(33, 39);
        peakEdrOffsetEncoded = b.readByte(40, 42);
        waterVaporEncoded = b.readShort(43, 54);

        // ME bit 56 is redefined as the IMF flag for ADS-R
        imf = b.readBoolean(56);
    }

    @Override
    public byte getSubtype() {
        return SUBTYPE;
    }

    @Override
    public byte getEmergencyStateEncoded() {
        return emergencyState;
    }

    @Override
    public EmergencyStateV3 getEmergencyState() {
        return EmergencyStateV3.forEncoded(emergencyState);
    }

    /**
     * @return the four-digit Mode A (4096) code
     */
    @Override
    public short getModeACode() {
        return modeACode;
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    /**
     * @return true if the aircraft is operating unmanned, false if manned
     */
    public boolean isUnmanned() {
        return unmanned;
    }

    /**
     * @return the mean eddy dissipation rate (EDR) encoded value
     */
    public byte getMeanEdrEncoded() {
        return meanEdrEncoded;
    }

    /**
     * @return false if the mean EDR encoded value is 0, i.e. no mean EDR is available
     */
    public boolean hasMeanEdr() {
        return meanEdrEncoded != 0;
    }

    /**
     * @return the interval of the mean eddy dissipation rate (EDR) in m^(2/3)/s its code stands for, (0.850, ∞)
     * for the top code, see {@link EmergencyOrPriorityStatus#edr(int)}; or null if no mean EDR is available (see
     * {@link #hasMeanEdr()})
     */
    public Interval getMeanEdr() {
        return EmergencyOrPriorityStatus.edr(meanEdrEncoded);
    }

    /**
     * @return the peak eddy dissipation rate (EDR) encoded value
     */
    public byte getPeakEdrEncoded() {
        return peakEdrEncoded;
    }

    /**
     * @return false if the peak EDR encoded value is 0, i.e. no peak EDR is available
     */
    public boolean hasPeakEdr() {
        return peakEdrEncoded != 0;
    }

    /**
     * @return the interval of the peak eddy dissipation rate (EDR) in m^(2/3)/s its code stands for, (0.850, ∞)
     * for the top code, see {@link EmergencyOrPriorityStatus#edr(int)}; or null if no peak EDR is available (see
     * {@link #hasPeakEdr()})
     */
    public Interval getPeakEdr() {
        return EmergencyOrPriorityStatus.edr(peakEdrEncoded);
    }

    /**
     * @return the peak EDR offset encoded value
     */
    public byte getPeakEdrOffsetEncoded() {
        return peakEdrOffsetEncoded;
    }

    /**
     * @return the interval of the peak EDR offset in seconds, i.e. how long before this message the peak EDR
     * occurred, see {@link EmergencyOrPriorityStatus#peakEdrOffset(int)}; or null if no peak EDR is available
     * (see {@link #hasPeakEdr()}). Code 0 is also transmitted when only the offset is not available.
     */
    public Interval getPeakEdrOffset() {
        return hasPeakEdr() ? EmergencyOrPriorityStatus.peakEdrOffset(peakEdrOffsetEncoded) : null;
    }

    /**
     * @return the encoded water vapor value
     */
    public short getWaterVaporEncoded() {
        return waterVaporEncoded;
    }

    /**
     * @return false if the water vapor encoded value is 0, i.e. no water vapor value is available
     */
    public boolean hasWaterVapor() {
        return waterVaporEncoded != 0;
    }

    /**
     * @return the interval of the water vapor in kg/kg its code stands for, [0.04094, ∞) for the top code, see
     * {@link EmergencyOrPriorityStatus#waterVapor(int)}; or null if no water vapor value is available (see
     * {@link #hasWaterVapor()})
     */
    public Interval getWaterVapor() {
        return EmergencyOrPriorityStatus.waterVapor(waterVaporEncoded);
    }

    @Override
    public String toString() {
        return "EmergencyOrPriorityStatusV3Msg{" + super.toString() +
                ", emergencyState=" + emergencyState +
                ", modeACode=" + modeACode +
                ", unmanned=" + unmanned +
                ", meanEdrEncoded=" + meanEdrEncoded +
                ", peakEdrEncoded=" + peakEdrEncoded +
                ", peakEdrOffsetEncoded=" + peakEdrOffsetEncoded +
                ", waterVaporEncoded=" + waterVaporEncoded +
                ", imf=" + imf +
                '}';
    }

}
