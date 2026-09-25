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

import de.serosystems.lib1090.decoding.Interval;

/**
 * A "Difference From Barometric Altitude" subfield together with its sign, as the coding of the
 * transmitting version defines it.
 */
public interface DiffBaroAlt {

    /**
     * What a code reports.
     */
    enum Status {
        /** A difference is reported. */
        AVAILABLE,
        /** The table's "Invalid / Unknown": no difference is reported. */
        UNKNOWN,
        /** The table's "N/A": a coding the table leaves undefined. */
        INVALID
    }

    /**
     * @return the encoded magnitude, without the sign, as the coding numbers it
     */
    int getEncoded();

    /**
     * @return the sign bit: true if geometric altitude is below barometric altitude
     */
    boolean isNegative();

    /**
     * @return what the code reports
     */
    Status getStatus();

    /**
     * @return whether a difference is reported, i.e. {@link #getStatus()} is {@link Status#AVAILABLE}
     */
    default boolean hasDifference() {
        return getStatus() == Status.AVAILABLE;
    }

    /**
     * The difference the code reports, geometric minus barometric altitude, in feet. The highest code
     * has no upper end on the positive side and no lower end on the negative one.
     *
     * @return the difference interval, or null if none is reported
     */
    Interval getDifference();

    /**
     * The value the code stands for, in feet: the midpoint of its interval, except where the interval
     * is cut off by the sign at zero, whose value is 0, and for the highest code, which only bounds the
     * difference and whose value is that bound.
     *
     * @return the value of the code, or null if none is reported
     */
    Double getValue();
}
