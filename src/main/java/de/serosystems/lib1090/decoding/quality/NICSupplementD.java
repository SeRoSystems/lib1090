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

package de.serosystems.lib1090.decoding.quality;

/**
 * What a receiver knows about NIC supplement D: which of its four values was received, or that none
 * was.
 * <p>
 * Supplement D is new in ADS-B version 3 and is the one supplement that is not a single bit. It
 * travels in the airborne velocity message, so like supplements A and C it belongs to a different
 * message from the position it qualifies and a receiver routinely holds a position without it.
 * <p>
 * Unlike the single-bit supplements it is monotone wherever it applies: a higher value always reports
 * a smaller containment radius, so {@link #ZERO} is the poorest of the four and stands in for
 * {@link #UNKNOWN} in the tables. That equivalence is a property of the tables, not a license to
 * substitute one for the other — see {@link NavigationCharacteristicsV3}.
 *
 * @see NICSupplement for the single-bit supplements A, B and C
 */
public enum NICSupplementD {

    /** The supplement was received and is 0. */
    ZERO,

    /** The supplement was received and is 1. */
    ONE,

    /** The supplement was received and is 2. */
    TWO,

    /** The supplement was received and is 3. */
    THREE,

    /** The supplement has not been received, so its value is not known. */
    UNKNOWN;

    /**
     * @param nicSupplementD the supplement as received, 0 to 3, or {@code null} if it has not been
     * @return the matching constant, or {@link #UNKNOWN} for {@code null}
     * @throws IllegalArgumentException if the value is outside the two bits the field occupies
     */
    public static NICSupplementD of(Byte nicSupplementD) {
        if (nicSupplementD == null)
            return UNKNOWN;

        switch (nicSupplementD) {
            case 0:
                return ZERO;
            case 1:
                return ONE;
            case 2:
                return TWO;
            case 3:
                return THREE;
            default:
                throw new IllegalArgumentException(
                        "NIC supplement D " + nicSupplementD + " does not fit the two bits it occupies");
        }
    }
}
