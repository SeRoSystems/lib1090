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

/**
 * Base class for BDS (Comm-B Data Selector) register decoders, as defined in ICAO Doc 9871
 * (First Edition, AN/464) §A.2.1 Register Allocation, for the register-numbering scheme
 * (register TABLE A-2-X where X is the decimal equivalent of the BDS1,BDS2 code pair).
 */
@SuppressWarnings("unused")
public abstract class BDSRegister {

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
     * @return the 7-byte comm-b message (BDS register)
     */
    public byte[] getMessage() {
        return message;
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
