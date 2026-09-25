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

public final class Altitude {

    private Altitude() {
    }

    /**
     * This method converts a gray code encoded int to a standard decimal int
     *
     * @param gray gray code encoded integer
     * @return radix 2 encoded integer
     */
    public static int grayToBin(int gray) {
        int result = 0;
        while (gray != 0) {
            result ^= gray;
            gray >>>= 1;
        }
        return result;
    }

    /**
     * Decode altitude code according to ICAO Annex 10 Volume IV §3.1.2.6.5.4, Gillham pulse
     * assignment per ICAO Annex 10 Volume IV Appendix to Chapter 3 (SSR Automatic Pressure-Altitude
     * Transmission Code)
     *
     * @param altitudeCode as provided in most Mode S replies (13 bits)
     * @return altitude in feet or null if altitude is not available or has an invalid encoding, which
     * includes every code with the M bit set; see {@link #valid13BitAltitude(short)}
     */
    public static Integer decode13BitAltitude(short altitudeCode) {
        // altitude not available or not valid
        if (!valid13BitAltitude(altitudeCode)) return null;

        boolean Qbit = decode13BitQBit(altitudeCode);
        if (Qbit) { // altitude reported in 25ft increments
            int N = (altitudeCode & 0x0F) | ((altitudeCode & 0x20) >>> 1) | ((altitudeCode & 0x1F80) >>> 2);
            return 25 * N - 1000;
        } else { // altitude is above 50175ft, so we use 100ft increments
            // it's decoded using the Gillham code
            int C1 = (altitudeCode & 0x1000) >>> 12;
            int A1 = (altitudeCode & 0x0800) >>> 11;
            int C2 = (altitudeCode & 0x0400) >>> 10;
            int A2 = (altitudeCode & 0x0200) >>> 9;
            int C4 = (altitudeCode & 0x0100) >>> 8;
            int A4 = (altitudeCode & 0x0080) >>> 7;
            int B1 = (altitudeCode & 0x0020) >>> 5;
            int B2 = (altitudeCode & 0x0008) >>> 3;
            int D2 = (altitudeCode & 0x0004) >>> 2;
            int B4 = (altitudeCode & 0x0002) >>> 1;
            int D4 = (altitudeCode & 0x0001);

            // this is standard gray code
            int N500 = grayToBin(D2 << 7 | D4 << 6 | A1 << 5 | A2 << 4 | A4 << 3 | B1 << 2 | B2 << 1 | B4);

            // 100-ft steps must be converted
            int N100 = grayToBin(C1 << 2 | C2 << 1 | C4) - 1;
            if (N100 == 6) N100 = 4;
            if (N500 % 2 != 0) N100 = 4 - N100; // invert it

            return -1200 + N500 * 500 + N100 * 100;
        }
    }

    /**
     * Decode Q bit for altitude code according to ICAO Annex 10 Volume IV §3.1.2.6.5.4, Gillham
     * pulse assignment per ICAO Annex 10 Volume IV Appendix to Chapter 3 (SSR Automatic
     * Pressure-Altitude Transmission Code)
     *
     * @param altitudeCode as provided in most Mode S replies (13 bits)
     * @return value of the Q bit, false if the M bit is set
     */
    public static boolean decode13BitQBit(short altitudeCode) {
        boolean Mbit = (altitudeCode & 0x40) != 0;
        return !Mbit && ((altitudeCode & 0x10) != 0);
    }

    /**
     * Decode altitude according to ED-102B §2.2.3.2.3.4.3 Figure 2-5 <br>
     * Check if a given 13 bit altitude code is valid.
     * <ul>
     *     <li>a code of 0 is invalid</li>
     *     <li>a code with the M bit set is invalid: DO-181E §2.2.13.1.2 a.(2)(i) reserves M equals ONE "for
     *     possible future use to indicate that the altitude reporting is in metric units", so there is no
     *     metric coding to decode</li>
     *     <li>a code without the Q bit set is invalid for certain combinations that are unused</li>
     * </ul>
     *
     * @param altitudeCode as provided in most Mode S replies (13 bits)
     * @return true if altitude code is considered valid, false otherwise
     */
    public static boolean valid13BitAltitude(short altitudeCode) {
        if (altitudeCode == 0) // fast path for explicit "invalid value"
            return false;

        boolean Mbit = (altitudeCode & 0x40) != 0;
        if (Mbit) return false;

        if (decode13BitQBit(altitudeCode)) {
            return true;
        } else {
            boolean C1 = (altitudeCode & 0x1000) != 0;
            boolean C2 = (altitudeCode & 0x0400) != 0;
            boolean C4 = (altitudeCode & 0x0100) != 0;
            return (!C1 || !C4) && (C1 || C2 || C4);
        }
    }

    /**
     * Decode altitude according to ED-102B §2.2.3.2.3.4.3 Figure 2-5
     *
     * @param altitudeCode 12 bit encoded altitude
     * @return altitude in feet or null if altitude is not available or has an invalid encoding
     */
    public static Integer decode12BitAltitude(short altitudeCode) {
        // altitude not available or not valid
        if (!valid12BitAltitude(altitudeCode)) return null;

        // In contrast to the decodeAltitude method in {@link de.serosystems.lib1090.msgs.modes.AltitudeReply}, input
        // does not contain the MBit
        boolean Qbit = decode12BitQBit(altitudeCode);
        int N;
        if (Qbit) { // altitude reported in 25ft increments
            N = (altitudeCode & 0xF) | ((altitudeCode & 0xFE0) >>> 1);
            return 25 * N - 1000;
        } else { // altitude is above 50175ft, so we use 100ft increments
            // it's decoded using the Gillham code
            int C1 = (altitudeCode & 0x800) >>> 11;
            int A1 = (altitudeCode & 0x400) >>> 10;
            int C2 = (altitudeCode & 0x200) >>> 9;
            int A2 = (altitudeCode & 0x100) >>> 8;
            int C4 = (altitudeCode & 0x080) >>> 7;
            int A4 = (altitudeCode & 0x040) >>> 6;
            int B1 = (altitudeCode & 0x020) >>> 5;
            int B2 = (altitudeCode & 0x008) >>> 3;
            int D2 = (altitudeCode & 0x004) >>> 2;
            int B4 = (altitudeCode & 0x002) >>> 1;
            int D4 = (altitudeCode & 0x001);

            // this is standard gray code
            int N500 = grayToBin(D2 << 7 | D4 << 6 | A1 << 5 | A2 << 4 | A4 << 3 | B1 << 2 | B2 << 1 | B4);

            // 100-ft steps must be converted
            int N100 = grayToBin(C1 << 2 | C2 << 1 | C4) - 1;
            if (N100 == 6) N100 = 4;
            if (N500 % 2 != 0) N100 = 4 - N100; // invert it

            return -1200 + N500 * 500 + N100 * 100;
        }
    }

    /**
     * Decode the Q bit for an altitude provided according to ED-102B §2.2.3.2.3.4.3 Figure 2-5
     *
     * @param altitudeCode 12 bit encoded altitude
     * @return value of the Q bit
     */
    public static boolean decode12BitQBit(short altitudeCode) {
        return (altitudeCode & 0x10) != 0;
    }

    /**
     * Check if a given 12 bit altitude code is valid.
     * <ul>
     *     <li>a code of 0 is invalid</li>
     *     <li>a code without the Q bit set (see {@link #decode12BitQBit(short)}) is invalid for certain combinations that are unused</li>
     * </ul>
     *
     * @param altitudeCode 12 bit encoded altitude
     * @return true if altitude code is considered valid, false otherwise
     */
    public static boolean valid12BitAltitude(short altitudeCode) {
        if (altitudeCode == 0) // fast path for explicit "invalid value"
            return false;

        if (decode12BitQBit(altitudeCode)) {
            return true;
        } else {
            boolean C1 = (altitudeCode & 0x800) != 0;
            boolean C2 = (altitudeCode & 0x200) != 0;
            boolean C4 = (altitudeCode & 0x080) != 0;
            return (!C1 || !C4) && (C1 || C2 || C4);
        }
    }
}
