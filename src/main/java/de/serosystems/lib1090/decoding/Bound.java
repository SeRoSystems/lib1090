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
 * Which side of a tabulated value the true value lies on, and whether the value itself is included.
 * <p>
 * The standards rarely state a quantity outright. They state a bound, and the tables differ in how:
 * the containment radius is <i>below</i> a value or <i>at least</i> one, an aircraft's length is
 * <i>at most</i> a value or <i>more than</i> one. A bare number holds none of that, and cannot tell an
 * upper bound from a lower one at a value both use. Each constant names the relation exactly as its
 * table states it, so the inclusivity is part of the API rather than of a comment.
 */
public enum Bound {

    /** Nothing is reported. The true value may be anything. */
    NONE,

    /** The true value is less than the tabulated one. */
    BELOW,

    /** The true value is the tabulated one or less. */
    AT_MOST,

    /** The true value is the tabulated one or more. */
    AT_LEAST,

    /** The true value is more than the tabulated one. */
    MORE_THAN;

    /**
     * @return true for {@link #BELOW} and {@link #AT_MOST}, the bounds that guarantee something about
     * how large the true value can be
     */
    public boolean isUpper() {
        return this == BELOW || this == AT_MOST;
    }

    /**
     * @return true for {@link #AT_LEAST} and {@link #MORE_THAN}, the bounds that only say how small the
     * true value cannot be
     */
    public boolean isLower() {
        return this == AT_LEAST || this == MORE_THAN;
    }

    /**
     * @return true for {@link #AT_MOST} and {@link #AT_LEAST}, whose tabulated value is itself possible
     */
    public boolean isInclusive() {
        return this == AT_MOST || this == AT_LEAST;
    }
}
