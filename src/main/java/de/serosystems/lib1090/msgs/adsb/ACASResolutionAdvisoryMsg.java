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
import de.serosystems.lib1090.msgs.acas.RAMessageFormat;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisories;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisory;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for the 1090ES RA broadcast, the Extended Squitter Aircraft Status Message with subtype 2, as defined in
 * ED-102B §2.2.3.2.8.1.1 Figure 2-21 for version 3, and in ED-102B Appendix N §N.5.3 Figure N-22 for version 2.
 * ED-102B calls it the "TCAS RA Broadcast"; its ME bits 9-56 are the RA report (BDS 3,0) "as received from the
 * onboard CAS", which ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2 defines for TCAS and for ACAS X, see
 * {@link #getResolutionAdvisory()} and {@link de.serosystems.lib1090.msgs.acas}.
 * <br>
 * Note: This format only exists in ADS-B versions &gt;= 2
 */
public class ACASResolutionAdvisoryMsg extends TypeCodedExtendedSquitter implements Serializable, ADSBMsg {

    private static final long serialVersionUID = 2288992169091753527L;

    private static final byte SUBTYPE = 2;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ACASResolutionAdvisoryMsg() {
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as hex string
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ACASResolutionAdvisoryMsg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage raw ADS-B aircraft status message as byte array
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message format is not further specified
     */
    public ACASResolutionAdvisoryMsg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new TypeCodedExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this ACAS RA broadcast
     * @throws BadFormatException if message has wrong format
     */
    public ACASResolutionAdvisoryMsg(TypeCodedExtendedSquitter squitter) throws BadFormatException {
        super(squitter);

        if (getFormatTypeCode() != 28)
            throw new BadFormatException("ACAS RA broadcasts must have typecode 28");

        if (BitReader.forBigEndian(getMessage()).readByte(6, 8) != SUBTYPE)
            throw new BadFormatException("ACAS RA broadcasts have subtype 2");
    }

    /**
     * @return the subtype code of the aircraft status report (should always be 2)
     */
    public byte getSubtype() {
        return SUBTYPE;
    }

    /**
     * @return message bits 41-88 of the RA report (ME bits 9-56) as transmitted, right-aligned
     */
    public long getResolutionAdvisoryEncoded() {
        return BitReader.forBigEndian(getMessage()).readLong(9, 56);
    }

    /**
     * @return the collision avoidance system that generated the broadcast, and so its layout
     */
    public RAMessageFormat getRAMessageFormat() {
        return ResolutionAdvisories.messageFormat(getResolutionAdvisoryEncoded());
    }

    /**
     * @return the broadcast's content in the layout of its RA message format: a
     * {@link de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisoryReport}, an
     * {@link de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisoryReport} or a
     * {@link de.serosystems.lib1090.msgs.acas.TCAS6ResolutionAdvisory}, or null for a format without a defined layout
     * @see ResolutionAdvisories#report(long)
     */
    public ResolutionAdvisory getResolutionAdvisory() {
        return ResolutionAdvisories.report(getResolutionAdvisoryEncoded());
    }

    @Override
    public String toString() {
        return "ACASResolutionAdvisoryMsg{" + super.toString() +
                ", resolutionAdvisory=" + getResolutionAdvisory() +
                '}';
    }

}
