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

/**
 * The 7-bit "Difference From Barometric Altitude" of ADS-B versions 0 to 2, ME bits 50-56, which ADS-R
 * versions 1 and 2 and TIS-B carry as well, and which version 3 still transmits in the same place.
 * <p>
 * A code L stands for every version 3 code whose ME 50-56 are L, so it is (L - 1) * 25 ft rounded to
 * 25 ft: the interval ((L - 1.5) * 25, (L - 0.5) * 25], with L = 1 covering [0, 12.5] and the highest,
 * 127, every difference above 3137.5 ft. 0 reports nothing.
 */
public final class DiffBaroAltV0V2 extends AbstractDiffBaroAlt {

    private DiffBaroAltV0V2(int encoded, boolean negative) {
        super(encoded, negative,
                DiffBaroAltV3.combinedStatus(encoded << 1, encoded << 1 | 1),
                // all ones also stands for every extended version 3 code, which all lie above it
                DiffBaroAltV3.spanning(encoded << 1, encoded == 0x7F ? 0x7FF : encoded << 1 | 1));
    }

    /**
     * @param negative the sign bit
     * @param encoded  the 7-bit code, 0 to 127
     * @return the difference the code reports
     * @throws IllegalArgumentException if the code does not fit seven bits
     */
    public static DiffBaroAltV0V2 of(boolean negative, int encoded) {
        if (encoded < 0 || encoded > 0x7F)
            throw new IllegalArgumentException("Difference " + encoded + " does not fit seven bits");

        return new DiffBaroAltV0V2(encoded, negative);
    }
}
