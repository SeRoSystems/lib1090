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

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

/**
 * Decoder for the Mode S all-call reply (DF=11), as defined in
 * ICAO Annex 10 Volume IV §3.1.2.5.2.2.
 */
@SuppressWarnings("unused")
public class AllCallReply extends ModeSDownlinkMsg implements Serializable {

    private static final long serialVersionUID = 2459589933570219472L;

    private byte capabilities;
    private int parity_interrogator;
    private byte code_label;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AllCallReply() {
    }

    /**
     * @param rawMessage raw all-call reply as hex string
     * @throws BadFormatException     if message is not all-call reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — the all-call
     *                                reply (DF=11) format is defined in ICAO Annex 10
     *                                Volume IV §3.1.2.5.2.2
     */
    public AllCallReply(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param rawMessage raw all-call reply as byte array
     * @throws BadFormatException     if message is not all-call reply or
     *                                contains wrong values.
     * @throws UnspecifiedFormatError if message has a format that is not further specified — the all-call
     *                                reply (DF=11) format is defined in ICAO Annex 10
     *                                Volume IV §3.1.2.5.2.2
     */
    public AllCallReply(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ModeSDownlinkMsg(rawMessage));
    }

    /**
     * @param reply Mode S reply containing this all-call reply
     * @throws BadFormatException if message is not all-call reply or
     *                            contains wrong values.
     */
    public AllCallReply(ModeSDownlinkMsg reply) throws BadFormatException {
        super(reply);

        if (getDownlinkFormat() != 11) {
            throw new BadFormatException("Message is not an all-call reply!");
        }

        capabilities = getFirstField();

        // extract interrogator ID
        this.parity_interrogator = calcParityInt() ^ getParity();

        code_label = (byte) ((parity_interrogator >> 4) & 0x7);
    }

    /**
     * @return The emitter's capabilities, see ICAO Annex 10 Volume IV §3.1.2.5.2.2.1
     */
    public byte getCapabilities() {
        return capabilities;
    }

    /**
     * Whether capabilities indicate that aircraft is airborne.
     *
     * @return true if airborne, false if on ground or null if ground status is unknown
     */
    public Boolean isAirborne() {
        if (capabilities == 5) {
            return true;
        } else if (capabilities == 4) {
            return false;
        }
        return null;
    }

    /**
     * Some receivers already subtract the crc checksum
     * from the parity field right after reception.
     * In that case, use {@link #getParity()} to get the interrogator ID.<br><br>
     * Note: Use {@link #hasValidInterrogatorCode()} to check the validity of this field.
     *
     * @return the interrogator code which can either be the interrogator id or the surveillance id.
     * Check {@link #isSurveillanceID()} for interpretation of the result.
     */
    public byte getInterrogatorCode() {
        switch (code_label) {
            case 0:
            case 1:
                return (byte) (parity_interrogator & 0xF);
            case 2:
                return (byte) ((parity_interrogator & 0xF) + 16);
            case 3:
                return (byte) ((parity_interrogator & 0xF) + 32);
            default: // 4 and >= 4 (illegal)
                return (byte) ((parity_interrogator & 0xF) + 48);
        }

    }

    /**
     * If true, {@link #getInterrogatorCode()} returns the SI-Code, otherwise it returns the II-Code.
     *
     * @return true if the interrogator has a surveillance identifier, false if it has an interrogator identifier.
     */
    public boolean isSurveillanceID() {
        return code_label > 0;
    }

    /**
     * Coding of the 3 bit code label (DL) is:<br>
     * <ul>
     * <li>000 - signifies that the IC field contains the II code</li>
     * <li>001 - signifies that the IC field contains SI codes 1 to 15</li>
     * <li>010 - signifies that the IC field contains SI codes 16 to 31</li>
     * <li>011 - signifies that the IC field contains SI codes 32 to 47</li>
     * <li>100 - signifies that the IC field contains SI codes 48 to 63</li>
     * </ul>
     *
     * @return code label
     */
    public byte getCodeLabel() {
        return code_label;
    }

    /**
     * Note: this can be used as an accurate check whether the all call reply
     * has been received correctly without knowing the interrogator in advance.
     *
     * @return true if the interrogator ID is conformant with ICAO Annex 10 Volume IV
     * §3.1.2.3.3.2 and §3.1.2.5.2.1.3
     */
    public boolean hasValidInterrogatorCode() {
        // ICAO Annex 10 Volume IV §3.1.2.3.3.2
        // the first 17 bits have to be zero
        if (parity_interrogator > 127) return false;

        // Note: seems to be used by ACAS
        //int ii = interrogator[2] & 0xF;
        // ICAO Annex 10 Volume IV §3.1.2.5.2.1.2.4
        // surveillance identifier of 0 shall never be used
        //if (cl > 0 && ii == 0) return false;

        // ICAO Annex 10 Volume IV §3.1.2.5.2.1.3
        // code label is only defined for 0-4
        return code_label <= 4;
    }

    @Override
    public String toString() {
        return "AllCallReply{" +
                "capabilities=" + capabilities +
                ", interrogator_id=" + getInterrogatorCode() + (isSurveillanceID() ? "/SI" : "/II") +
                ", code_label=" + code_label +
                '}';
    }

}
