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
 * The 10-bit "Extended Difference From Barometric Altitude" of ADS-R version 3, ED-102B §2.2.18.4.4.1
 * TABLE 2-186: ME 10, 47-48 and 50-56, without the least significant ME bit 9, which ADS-R uses for the
 * IMF. Each code stands for the two version 3 codes that share its bits, so the resolution is 25 ft
 * where version 3 has 12.5 ft and the highest code is a difference above 4450 ft.
 * <p>
 * Code 1 covers [0, 12.5] ft, exactly 0 included, as the two version 3 codes it stands for do; TABLE
 * 2-186 prints "0 &lt; DFBA &le; 12.5", leaving a difference of exactly 0 without a code.
 */
public final class ADSRDiffBaroAltV3 extends AbstractDiffBaroAlt {

    private ADSRDiffBaroAltV3(int encoded, boolean negative) {
        super(encoded, negative,
                DiffBaroAltV3.combinedStatus(encoded << 1, encoded << 1 | 1),
                DiffBaroAltV3.spanning(encoded << 1, encoded << 1 | 1));
    }

    /**
     * @param negative the sign bit
     * @param encoded  the 10-bit code, 0 to 1023
     * @return the difference the code reports
     * @throws IllegalArgumentException if the code does not fit ten bits
     */
    public static ADSRDiffBaroAltV3 of(boolean negative, int encoded) {
        if (encoded < 0 || encoded > 0x3FF)
            throw new IllegalArgumentException("ADS-R extended difference " + encoded + " does not fit ten bits");

        return new ADSRDiffBaroAltV3(encoded, negative);
    }
}
