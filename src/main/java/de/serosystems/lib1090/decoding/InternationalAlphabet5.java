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

package de.serosystems.lib1090.decoding;

/**
 * Decoding helpers for fields encoded as consecutive 6-bit International Alphabet No. 5 (IA5)
 * digits, as used e.g. by ADS-B identification and aircraft type fields. The digit set and
 * 6-bit encoding are per ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8; ED-102B §2.2.3.2.5.3
 * defines the ADS-B "Ident Character" subfield by reference to that same section and table
 * rather than defining its own table.
 */
public final class InternationalAlphabet5 {

    private InternationalAlphabet5() {
    }

    /**
     * Splits a raw field of consecutive 6-bit digits into individual encoded digits, most
     * significant first.
     *
     * @param encoded   the raw field, exactly {@code numDigits * 6} bits wide
     * @param numDigits the number of 6-bit digits to extract, in range 1-8
     * @return the encoded (6-bit) digits, in order
     * @throws IllegalArgumentException if numDigits is not in range 1-8
     */
    public static byte[] toDigits(long encoded, int numDigits) {
        if (numDigits < 1 || numDigits > 8)
            throw new IllegalArgumentException("numDigits must be in range 1-8, got " + numDigits);

        byte[] digits = new byte[numDigits];

        for (int i = 0; i < digits.length; i++)
            digits[i] = (byte) ((encoded >>> (6 * (numDigits - 1 - i))) & 0x3F);

        return digits;
    }

    /**
     * Whether a 6-bit code is one TABLE 3-8 defines: A to Z (1 to 26), space (32) and 0 to 9 (48 to 57).
     *
     * @param digit encoded digit
     * @return true if the code is defined
     */
    public static boolean isDefined(byte digit) {
        return digit > 0 && digit < 27 || digit == 32 || digit > 47 && digit < 58;
    }

    /**
     * @param digits array of encoded digits
     * @return true if every code is one TABLE 3-8 defines, see {@link #isDefined(byte)}
     */
    public static boolean isDefined(byte[] digits) {
        for (byte digit : digits)
            if (!isDefined(digit)) return false;
        return true;
    }

    /**
     * Maps an IA5 encoded digit to a readable character. A code TABLE 3-8 does not define is mapped to a
     * space, like the space itself; use {@link #isDefined(byte)} to tell them apart.
     *
     * @param digit encoded digit
     * @return readable character
     */
    public static char mapChar(byte digit) {
        if (digit > 0 && digit < 27) return (char) ('A' + digit - 1);
        else if (digit > 47 && digit < 58) return (char) ('0' + digit - 48);
        else return ' ';
    }

    /**
     * Maps IA5 encoded digits to readable characters.
     *
     * @param digits array of encoded digits
     * @return array of decoded characters
     */
    public static char[] mapChar(byte[] digits) {
        char[] result = new char[digits.length];

        for (int i = 0; i < digits.length; i++)
            result[i] = mapChar(digits[i]);

        return result;
    }
}
