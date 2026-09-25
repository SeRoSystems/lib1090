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

package de.serosystems.lib1090.decoding.diffbaroalt;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;

/**
 * The 11-bit "Extended Difference From Barometric Altitude" of ADS-B version 3, ED-102B
 * §2.2.3.2.6.1.15 TABLE 2-27 as revised by Change 1. Its bits are ME 10, 47-48, 50-56 and 9, most
 * significant first; ME 50-56 are the 7-bit field of earlier versions.
 * <ul>
 *     <li>Codes whose ME 50-56 are zero report nothing: {@code 0xx 0000 0000} is "Invalid / Unknown",
 *     ME 47-48 then carrying NIC supplement D, and every other such code is undefined. The table's N/A
 *     ranges {@code 001 0000 0000 - 001 1111 1101} and so on start with codes that first row already
 *     claims; those are unknown here, their ME 47-48 being NIC supplement D whatever their value.</li>
 *     <li>2 is a difference of exactly 0.</li>
 *     <li>3 to {@code 000 1111 1110} step through (0, 3150] ft in 12.5 ft, and {@code 000 1111 1111} is
 *     (3150, 3250].</li>
 *     <li>Above that only the codes whose ME 50-56 are all ones are defined, stepping through
 *     (3250, 4550] ft in 100 ft; the highest, all ones, is a difference above 4550 ft. TABLE 2-27 gives
 *     its midpoint as "&ge; 4550", its interval as "&gt; 4550"; the interval is what this reports.</li>
 * </ul>
 */
public final class DiffBaroAltV3 extends AbstractDiffBaroAlt {

    private DiffBaroAltV3(int encoded, boolean negative) {
        super(encoded, negative, status(encoded), magnitude(encoded));
    }

    /**
     * @param negative the sign bit
     * @param encoded  the 11-bit code, 0 to 2047
     * @return the difference the code reports
     * @throws IllegalArgumentException if the code does not fit eleven bits
     */
    public static DiffBaroAltV3 of(boolean negative, int encoded) {
        if (encoded < 0 || encoded > 0x7FF)
            throw new IllegalArgumentException("Extended difference " + encoded + " does not fit eleven bits");

        return new DiffBaroAltV3(encoded, negative);
    }

    static Status status(int encoded) {
        int legacy = (encoded >>> 1) & 0x7F;
        int top = encoded >>> 8;

        // 0xx 0000 0000: ME 47-48 carry NIC supplement D, so only ME 10 and 9 must be clear
        if (legacy == 0)
            return (encoded & 0x401) == 0 ? Status.UNKNOWN : Status.INVALID;
        if (top == 0)
            return Status.AVAILABLE;
        return legacy == 0x7F ? Status.AVAILABLE : Status.INVALID;
    }

    /**
     * @return the magnitude interval of a code whose {@link #status(int)} is available, in feet
     */
    static Interval magnitude(int encoded) {
        if (status(encoded) != Status.AVAILABLE) return null;

        int top = encoded >>> 8;
        if (top == 0) {
            if (encoded == 2) return Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 0);
            if (encoded == 0xFF) return Interval.of(Bound.MORE_THAN, 3150, Bound.AT_MOST, 3250);
            return Interval.of(Bound.MORE_THAN, (encoded - 3) * 12.5, Bound.AT_MOST, (encoded - 2) * 12.5);
        }

        // the 100 ft steps above 3250 ft, two per value of the top three bits
        int step = 2 * (top - 1) + (encoded & 1);
        if (step == 13) return Interval.of(Bound.MORE_THAN, 4550, Bound.NONE, Double.NaN);
        return Interval.of(Bound.MORE_THAN, 3250 + 100 * step, Bound.AT_MOST, 3350 + 100 * step);
    }

    /**
     * The status of a coarser code standing for the given version 3 codes: unknown if any of them is,
     * undefined if any of them is, available otherwise. Used by the codings that drop low bits of this
     * one.
     */
    static Status combinedStatus(int... codes) {
        Status combined = Status.AVAILABLE;
        for (int code : codes) {
            Status status = status(code);
            if (status == Status.UNKNOWN) return Status.UNKNOWN;
            if (status == Status.INVALID) combined = Status.INVALID;
        }
        return combined;
    }

    /**
     * @return the interval from the lower end of code {@code first} to the upper end of code
     * {@code last}, or null unless both are available
     */
    static Interval spanning(int first, int last) {
        if (status(first) != Status.AVAILABLE || status(last) != Status.AVAILABLE) return null;
        return Interval.spanning(magnitude(first), magnitude(last));
    }
}
