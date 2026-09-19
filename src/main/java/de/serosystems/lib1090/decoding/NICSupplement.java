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
 * What a receiver knows about one single-bit NIC supplement: that it is set, that it is clear, or
 * that it has not been seen.
 * <p>
 * The third state is not a formality. Most supplements travel in a different message from the
 * position they qualify — the operational status message, or for version 3 the airborne velocity
 * message — so a receiver routinely holds a position whose supplement it has never received. Reading
 * that as {@link #CLEAR} is what the tables make unsafe: which value is the pessimistic one changes
 * from type code to type code, so no constant substitute for "not seen" exists.
 */
public enum NICSupplement {

    /** The supplement was received and is set. */
    SET,

    /** The supplement was received and is clear. */
    CLEAR,

    /** The supplement has not been received, so its value is not known. */
    UNKNOWN;

    /**
     * @param nicSupplement the supplement as received, or {@code null} if it has not been
     * @return {@link #SET}, {@link #CLEAR} or {@link #UNKNOWN} accordingly
     */
    public static NICSupplement of(Boolean nicSupplement) {
        if (nicSupplement == null)
            return UNKNOWN;
        return nicSupplement ? SET : CLEAR;
    }
}
