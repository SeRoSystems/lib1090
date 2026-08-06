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
import de.serosystems.lib1090.decoding.TCASResolutionAdvisory;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.bds.ThreatIdentityData;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for 1090ES TCAS Resolution Advisory Messages.<br>
 * Note: This format only exists in ADS-B versions &gt;= 2
 * <p>
 * See DO-260B 2.2.3.2.7.8.2
 */
public class TCASResolutionAdvisoryMsg extends ExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = 2288992169091753527L;

    private static final byte SUBTYPE = 2;

    private short activeRa;
    private byte racRecord;
    private boolean raTerminated;
    private boolean multiThreatEncounter;
    private byte threatType;
    private int threatIdentity;
    private ThreatIdentityData threatIdentityData;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TCASResolutionAdvisoryMsg() {
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public TCASResolutionAdvisoryMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public TCASResolutionAdvisoryMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this TCAS resolution advisory msg
     * @throws BadFormatException if message has wrong format
     */
    public TCASResolutionAdvisoryMsg(ExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 28)
            throw new BadFormatException("TCAS RA reports must have typecode 28.");

        BitReader b = BitReader.forBigEndian(getMessage());

        if (b.readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("TCAS RA reports have subtype 2.");

        activeRa = TCASResolutionAdvisory.decodeActiveRa(b);
        racRecord = TCASResolutionAdvisory.decodeRacRecord(b);
        raTerminated = TCASResolutionAdvisory.decodeRaTerminated(b);
        multiThreatEncounter = TCASResolutionAdvisory.decodeMultiThreatEncounter(b);
        threatType = TCASResolutionAdvisory.decodeThreatType(b);
        threatIdentity = TCASResolutionAdvisory.decodeThreatIdentity(b);

        threatIdentityData = TCASResolutionAdvisory.extractThreatIdentityData(threatType, b);
    }

    /**
     * Although TCAS 7 is mandated in European and US airspaces, we could still see aircraft using TCAS 6.
     * In this case, the active RA needs to be interpreted differently and the threat identity is not present.
     *
     * @return whether the message should be assumed to originate from a TSO-C119A compatible system
     * (version 6.04 Enhanced).
     */
    public boolean isTCAS6() {
        // bits 59-88 == 0
        return threatIdentity == 0
                && threatType == 0
                && !multiThreatEncounter
                && !raTerminated;
    }

    /**
     * @return the subtype code of the aircraft status report (should always be 2)
     */
    public byte getSubtype() {
        return SUBTYPE;
    }

    /**
     * The active RA field must be interpreted according to ED-143 V1 2.2.3.9.3.2.3.1.2 for TCAS 6.
     *
     * @return 14 bits which indicate the characteristics of the resolution advisory
     * (Annex 10 V4, 4.3.8.4.2.2.1.1)
     */
    public short getActiveRA() {
        return activeRa;
    }

    /**
     * @return 4 bits which indicate all currently active RACs (Annex 10 V4, 4.3.8.4.2.2.1.2)
     */
    public byte getRACRecord() {
        return racRecord;
    }

    /**
     * @return whether RA previously generated by ACAS has ceased being generated
     * (Annex 10 V4, 4.3.8.4.2.2.1.3); not present for TCAS 6 systems
     */
    public Boolean hasRATerminated() {
        if (isTCAS6()) return null;
        return raTerminated;
    }

    /**
     * @return whether two or more simultaneous threats are currently being processed
     * (Annex 10 V4, 4.3.8.4.2.2.1.4); not present for TCAS 6 systems
     */
    public Boolean hasMultiThreatEncounter() {
        if (isTCAS6()) return null;
        return multiThreatEncounter;
    }

    /**
     * Threat type indicator according to Annex 10 V4, 4.3.8.4.2.2.1.5.
     * <ul>
     *     <li>0: no identity data in TID</li>
     *     <li>1: TID contains Mode S transponder address</li>
     *     <li>2: TID contains altitude, range, bearing</li>
     *     <li>3: not assigned</li>
     * </ul>
     *
     * @return the threat type indicator; not present for TCAS 6 systems
     */
    public Byte getThreatType() {
        if (isTCAS6()) return null;
        return threatType;
    }

    /**
     * @return the threat's identity. Check getThreatType() before.
     * (Annex 10 V4, 4.3.8.4.2.2.1.6); not present for TCAS 6 systems
     */
    public Integer getThreatIdentity() {
        if (isTCAS6()) return null;
        return threatIdentity;
    }

    /**
     * A convenient representation of the bit array provided by {@link #getActiveRA()}.
     * <p>
     * Further interpretation of the bits are subject to the caller which needs to handle differences between
     * TCAS 6 and 7 systems (see {@link #isTCAS6()}.
     * <p>
     * A value set to true indicates that the condition is active.
     *
     * @return the currently active resolution advisories (if any) generated by own ACAS unit against one or more threat
     * aircraft.
     */
    public boolean[] getActiveResolutionAdvisories() {
        return TCASResolutionAdvisory.extractActiveResolutionAdvisories(BitReader.forBigEndian(getMessage()));
    }

    /**
     * A convenient representation of the bit array provided by {@link #getRACRecord()}.
     * <p>
     * The active RA complement bits have the following meaning:
     * <ul>
     *     <li>index 0: Do not pass below</li>
     *     <li>index 1: Do not pass above</li>
     *     <li>index 2: Do not turn left</li>
     *     <li>index 3: Do not turn right</li>
     * </ul>
     * The value set to true indicates that the condition is active.
     *
     * @return the currently active resolution advisory complements (if any) received from other ACAS aircraft equipped
     * with on-board resolution capability.
     */
    public boolean[] getResolutionAdvisoriesComplementsRecord() {
        return TCASResolutionAdvisory.extractResolutionAdvisoriesComplementsRecord(BitReader.forBigEndian(getMessage()));
    }

    /**
     * @return the ICAO 24-bit aircraft address of the threat or the altitude, range, and bearing if the threat is not
     * Mode S equipped; not present for TCAS 6 systems
     */
    public ThreatIdentityData getThreatIdentityData() {
        if (isTCAS6()) return null;

        return threatIdentityData;
    }

    @Override
    public String toString() {
        return "TCASResolutionAdvisoryMsg{" + super.toString() +
                ", activeRa=" + activeRa +
                ", racRecord=" + racRecord +
                ", raTerminated=" + raTerminated +
                ", multiThreatEncounter=" + multiThreatEncounter +
                ", threatType=" + threatType +
                ", threatIdentity=" + threatIdentity +
                '}';
    }

}
