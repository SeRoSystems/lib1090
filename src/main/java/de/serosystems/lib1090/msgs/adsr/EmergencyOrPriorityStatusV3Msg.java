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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.EmergencyOrPriorityStatusMsg;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;
import de.serosystems.lib1090.msgs.squitter.ModeACodeMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-R version 3 emergency and priority status messages, as defined in ED-102B §2.2.18.4.5 Figure 2-60.
 */
public class EmergencyOrPriorityStatusV3Msg extends ExtendedSquitter implements Serializable, EmergencyOrPriorityStatusMsg, ModeACodeMsg, IMFMsg, ADSRMsg {

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
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.5 Figure 2-60
     */
    public EmergencyOrPriorityStatusV3Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-R aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in ED-102B §2.2.18.4.5 Figure 2-60
     */
    public EmergencyOrPriorityStatusV3Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this emergency or priority status msg
     * @throws BadFormatException if message has wrong format
     */
    public EmergencyOrPriorityStatusV3Msg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (this.getFormatTypeCode() != 28)
            throw new BadFormatException("Emergency and Priority Status messages must have typecode 28.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("Emergency and priority status reports have subtype 1.");

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
    public byte getEmergencyStateCode() {
        return emergencyState;
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
     * @return the upper bound of the mean eddy dissipation rate (EDR) in m^(2/3)/s
     * (0.850 denotes 0.850 or larger), or null if no mean EDR is available (see {@link #hasMeanEdr()})
     */
    public Double getMeanEdr() {
        return hasMeanEdr() ? EmergencyOrPriorityStatus.decodeEdr(meanEdrEncoded) : null;
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
     * @return the upper bound of the peak eddy dissipation rate (EDR) in m^(2/3)/s
     * (0.850 denotes 0.850 or larger), or null if no peak EDR is available (see {@link #hasPeakEdr()})
     */
    public Double getPeakEdr() {
        return hasPeakEdr() ? EmergencyOrPriorityStatus.decodeEdr(peakEdrEncoded) : null;
    }

    /**
     * @return the peak EDR offset encoded value
     */
    public byte getPeakEdrOffsetEncoded() {
        return peakEdrOffsetEncoded;
    }

    /**
     * @return the upper bound of the peak EDR offset in seconds, indicating how long before the
     * current time the peak EDR value occurred
     */
    public double getPeakEdrOffset() {
        return peakEdrOffsetEncoded * -7.5;
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
     * @return the water vapor value in kg/kg, or null if no water vapor value is available
     * (see {@link #hasWaterVapor()})
     */
    public Double getWaterVapor() {
        return hasWaterVapor() ? (waterVaporEncoded - 1) / 1e5 : null;
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
