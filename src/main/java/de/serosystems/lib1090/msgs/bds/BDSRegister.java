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

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;

/**
 * Base class for BDS (Comm-B Data Selector) register decoders, as defined in ICAO Doc 9871
 * (First Edition, AN/464) §A.2.1 Register Allocation, for the register-numbering scheme
 * (register TABLE A-2-X where X is the decimal equivalent of the BDS1,BDS2 code pair).
 */
@SuppressWarnings("unused")
public abstract class BDSRegister implements Serializable {

    private static final long serialVersionUID = 1089747986665440871L;

    private byte[] message;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected BDSRegister() {
    }

    /**
     * Copy constructor for subclasses
     *
     * @param bdsRegister instance of bdsRegister to copy from
     */
    public BDSRegister(BDSRegister bdsRegister) {
        message = bdsRegister.getMessage();
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public BDSRegister(byte[] message) {
        this.message = message;
    }

    /**
     * @return the BDS code of the register this message reports
     */
    public abstract BDSCode getBDSCode();

    /**
     * The Comm-B message (BDS register).
     * <p>
     * <b>The array is not a copy and must not be modified:</b> it is the one passed to the constructor, shared
     * with that caller and with every register copied from this one. It is not copied for performance.
     *
     * @return the 7-byte comm-b message (BDS register)
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * For a register that identifies itself in MB bits 1-8, BDS1 in bits 1-4 and BDS2 in bits 5-8: checks that the
     * message is this register.
     *
     * @throws BadFormatException if MB bits 1-8 hold another BDS code than {@link #getBDSCode()}
     */
    protected void requireBDSCode() throws BadFormatException {
        int bds1 = (message[0] >>> 4) & 0xF;
        int bds2 = message[0] & 0xF;
        if (bds1 != getBDSCode().getBDS1() || bds2 != getBDSCode().getBDS2())
            throw new BadFormatException(String.format("MB identifies itself as BDS %X,%X, not %X,%X",
                    bds1, bds2, getBDSCode().getBDS1(), getBDSCode().getBDS2()), Tools.toHexString(message));
    }

    /**
     * @param first the first MB bit, inclusive
     * @param last  the last MB bit, inclusive
     * @return whether these MB bits are all ZERO
     */
    protected boolean isZero(int first, int last) {
        BitReader b = BitReader.forBigEndian(message);
        for (int bit = first; bit <= last; bit++)
            if (b.readBoolean(bit)) return false;
        return true;
    }

    /**
     * ICAO Doc 9871 §A.2.1.1: if the data of a field are not available, "the status bit (if specified for that field)
     * shall indicate that the data in that field are invalid and the field shall be zeroed", where a status bit covers
     * "the data field(s) which follow, up to the next status bit" (§A.2.2.1).
     *
     * @param status the MB bit of the status bit
     * @param last   the last MB bit the status bit covers
     * @return whether the status bit is set or the bits it covers are all ZERO
     */
    protected boolean isZeroUnlessValid(int status, int last) {
        return BitReader.forBigEndian(message).readBoolean(status) || isZero(status + 1, last);
    }

    /**
     * The value of a signed field. ICAO Doc 9871 lists the sign and the value of every signed field
     * as separate subfields, but codes the two together in two's complement, so the sign bit counts
     * as {@code -2^valueBits}.
     *
     * @param sign      the sign bit
     * @param value     the value subfield without the sign bit, as transmitted
     * @param valueBits the width of the value subfield in bits
     * @return the signed value, from {@code -2^valueBits} to {@code 2^valueBits - 1}
     */
    protected static int twosComplement(boolean sign, int value, int valueBits) {
        return sign ? value - (1 << valueBits) : value;
    }

    @Override
    public String toString() {
        return "BDSRegister{" +
                "bdsCode=" + getBDSCode() +
                ", message=" + Tools.toHexString(message) +
                '}';
    }

}
