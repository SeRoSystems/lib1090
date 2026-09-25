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

package de.serosystems.lib1090.decoding.size;

import de.serosystems.lib1090.decoding.Bound;

import java.util.Objects;

/**
 * One dimension of an aircraft or vehicle as a length and width code reports it: a value together
 * with which side of it the true dimension lies on.
 * <p>
 * The tables state a dimension in one of three ways — <b>at most</b> a value, <b>more than</b> a
 * value, or not at all. A bare {@code double} cannot hold the third and cannot tell the first two
 * apart, which is how earlier releases came to report the "more than 75 m" of version 3 as a
 * fabricated upper bound of 85 m.
 */
public final class Extent {

    /** No dimension is reported. The true dimension may be anything. */
    public static final Extent UNKNOWN = new Extent(Bound.NONE, Double.NaN);

    private final Bound bound;
    private final double meters;

    private Extent(Bound bound, double meters) {
        this.bound = bound;
        this.meters = meters;
    }

    /**
     * @param meters the upper bound
     * @return a dimension of at most {@code meters}
     */
    static Extent atMost(double meters) {
        return new Extent(Bound.AT_MOST, meters);
    }

    /**
     * @param meters the lower bound, itself excluded
     * @return a dimension of more than {@code meters}
     */
    static Extent moreThan(double meters) {
        return new Extent(Bound.MORE_THAN, meters);
    }

    /**
     * @return which side of {@link #getMeters()} the true dimension lies on
     */
    public Bound getBound() {
        return bound;
    }

    /**
     * The tabulated value itself, without the bound. Meaningless on its own — read it together with
     * {@link #getBound()}, or use {@link #getGuaranteedUpperBound()} if one number has to do.
     *
     * @return the value in meters, or {@code NaN} if nothing is reported
     */
    public double getMeters() {
        return meters;
    }

    /**
     * The largest value this report guarantees the true dimension does not exceed, as a single number:
     * the value itself for an upper bound, {@link Double#POSITIVE_INFINITY} for a lower bound, and
     * {@link Double#NaN} when nothing is reported.
     * <p>
     * As with the quality indicators, the encoding makes {@code getGuaranteedUpperBound() <= limit}
     * <b>false</b> in both degenerate cases, so a caller asking "is this aircraft known to fit
     * {@code limit}?" gets the conservative answer without special-casing anything. The reverse
     * question is not safe: ask a lower bound whether it exceeds a limit through {@link #getBound()}.
     *
     * @return the guaranteed upper bound in meters, {@code POSITIVE_INFINITY}, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return bound.isLower() ? Double.POSITIVE_INFINITY : meters;
    }

    /**
     * @return true if nothing is reported, i.e. this is {@link #UNKNOWN}
     */
    public boolean isUnknown() {
        return bound == Bound.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Extent)) return false;
        Extent other = (Extent) o;
        return bound == other.bound && Double.compare(meters, other.meters) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bound, meters);
    }

    @Override
    public String toString() {
        switch (bound) {
            case AT_MOST:
                return "at most " + meters + " m";
            case MORE_THAN:
                return "more than " + meters + " m";
            default:
                return "unknown";
        }
    }
}
