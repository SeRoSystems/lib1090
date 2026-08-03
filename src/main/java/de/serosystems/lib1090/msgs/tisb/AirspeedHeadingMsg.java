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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for TIS-B airspeed+heading message (DO-260B, 2.2.17.3.4).
 */
public class AirspeedHeadingMsg extends ExtendedSquitter implements Serializable, AirborneVelocityMsg {

    private static final long serialVersionUID = 944130622021621845L;

    private byte messageSubtype;
    private boolean imf;
    private byte nacp;

    private boolean verticalRateDown; // 0 = up, 1 = down
    private short verticalRateEncoded; // in ft/min
    private boolean verticalRateInfoAvailable;

    private boolean headingStatusBit;
    private double heading; // in degrees
    private boolean trueAirspeed; // 0 = indicated AS, 1 = true AS
    private short airspeedEncoded; // in knots
    private boolean airspeedAvailable;

    private Integer diffBaroAlt; // in ft

    private Byte nacv;
    private Byte sil;
    private Boolean magneticHeading;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirspeedHeadingMsg() {
    }

    /**
     * @param rawMessage raw TIS-B velocity message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirspeedHeadingMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw TIS-B velocity message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public AirspeedHeadingMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter containing the velocity message
     * @throws BadFormatException if message has wrong format
     */
    public AirspeedHeadingMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getDownlinkFormat() != 18)
            throw new BadFormatException("TIS-B messages must have downlink format 18.");

        if (this.getFormatTypeCode() != 19)
            throw new BadFormatException("Velocity messages must have typecode 19.");

        // Table 2-13
        if (getFirstField() != 2 && getFirstField() != 5)
            throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

        BitReader br = BitReader.forBigEndian(getMessage());

        messageSubtype = br.readByte(6, 8);
        if (messageSubtype != 3 && messageSubtype != 4) {
            throw new BadFormatException("Ground speed messages have subtype 1 or 2.");
        }

        imf = br.readByte(9, 9) == 1;
        nacp = br.readByte(10, 13);

        // heading available in ADS-B version 1+, indicates true/magnetic north for version 0
        headingStatusBit = br.readByte(14, 14) == 1;
        heading = br.readShort(15, 24) * 360. / 1024.;

        trueAirspeed = br.readByte(25, 25) == 1;
        airspeedEncoded = (short) (br.readShort(26, 35) - 1);
        if (airspeedEncoded != -1) {
            airspeedAvailable = true;
            if (messageSubtype == 4) airspeedEncoded <<= 2;
        }

        // 0 = no geo data available, 1 = geo data available
        boolean geoFlag = br.readByte(36, 36) == 1;

        verticalRateDown = br.readByte(37, 37) == 1;
        verticalRateEncoded = (short) ((br.readShort(38, 46) - 1) << 6);

        if (geoFlag) {
            int diffBaroAltEncoded = br.readByte(50, 56);
            diffBaroAlt = (diffBaroAltEncoded - 1) * 25;
            if (br.readByte(49, 49) == 1) diffBaroAlt *= -1;

            nacv = null;
            sil = null;
            magneticHeading = null;
        } else {
            diffBaroAlt = null;
            nacv = br.readByte(48, 50);
            sil = br.readByte(51, 52);
            magneticHeading = br.readByte(55, 55) == 1;
        }
    }

    @Override
    public boolean getIMF() {
        return imf;
    }

    @Override
    public boolean hasVerticalRateInfo() {
        return verticalRateInfoAvailable;
    }

    @Override
    public boolean hasGeoMinusBaroInfo() {
        return diffBaroAlt != null;
    }

    /**
     * @return heading in decimal degrees ([0, 360]). 0° = geographic north or null if no information is available.
     */
    public Double getHeading() {
        if (!headingStatusBit) return null;
        return heading;
    }

    /**
     * @return airspeed in knots or null if information is not available.
     */
    public Integer getAirspeed() {
        if (!airspeedAvailable) return null;
        return (int) airspeedEncoded;
    }

    /**
     * @return true if airspeed is true airspeed, false if airspeed is indicated airspeed
     */
    public boolean isTrueAirspeed() {
        return trueAirspeed;
    }

    /**
     * @return If supersonic, velocity has only 4 kts accuracy, otherwise 1 kt
     */
    public boolean isSupersonic() {
        return messageSubtype == 4;
    }

    @Override
    public Byte getNACv() {
        return nacv;
    }

    @Override
    public Integer getVerticalRate() {
        if (!verticalRateInfoAvailable) return null;
        return (verticalRateDown ? -verticalRateEncoded : verticalRateEncoded);
    }

    @Override
    public Integer getGeoMinusBaro() {
        return diffBaroAlt;
    }

    /**
     * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
     * in the operational status message. For version 0 it is derived from the format type code.
     *
     * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
     * {@link AirborneOperationalStatusV1Msg}. Returns null if not available.
     */
    public byte getNACp() {
        return nacp;
    }

    /**
     * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
     * <p>
     * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
     * {@link AirborneOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
     * version 1 and 2.
     *
     * @return the source integrity level (SIL) which indicates the probability of exceeding
     * the NIC containment radius. Returns null if not available.
     */
    public Byte getSIL() {
        return sil;
    }

    /**
     * According to DO-260B, 2.2.3.2.7.2.13
     *
     * @return true if horizontal reference direction is magnet north; false if true north; null if info not available
     */
    public Boolean isMagneticHeading() {
        return magneticHeading;
    }

    @Override
    public String toString() {
        return "AirspeedHeadingMsg{" + super.toString() +
                ", messageSubtype=" + messageSubtype +
                ", imf=" + imf +
                ", nacp=" + nacp +
                ", verticalRateDown=" + verticalRateDown +
                ", verticalRateEncoded=" + verticalRateEncoded +
                ", verticalRateInfoAvailable=" + verticalRateInfoAvailable +
                ", headingStatusBit=" + headingStatusBit +
                ", heading=" + heading +
                ", trueAirspeed=" + trueAirspeed +
                ", airspeedEncoded=" + airspeedEncoded +
                ", airspeedAvailable=" + airspeedAvailable +
                ", diffBaroAlt=" + diffBaroAlt +
                ", nacv=" + nacv +
                ", sil=" + sil +
                ", magneticHeading=" + magneticHeading +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.TISB_VELOCITY;
    }
}
