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

import java.util.Objects;

/**
 * An interval a coded value reports: its two ends, each with the {@link Bound} that says whether the end
 * itself is included, or absent where the interval is unbounded on that side.
 * <p>
 * Tables that quantize a quantity state an interval per code rather than a number, and they differ in
 * which end they include. This keeps both ends and both bounds, so that nothing the table says is lost.
 * The unit is the one of the quantity the interval belongs to and is stated where it is reported.
 */
public final class Interval {

    private final Bound lowerBound;
    private final double lower;
    private final Bound upperBound;
    private final double upper;

    private Interval(Bound lowerBound, double lower, Bound upperBound, double upper) {
        this.lowerBound = lowerBound;
        this.lower = lowerBound == Bound.NONE ? Double.NaN : lower;
        this.upperBound = upperBound;
        this.upper = upperBound == Bound.NONE ? Double.NaN : upper;
    }

    /**
     * @param lowerBound {@link Bound#AT_LEAST}, {@link Bound#MORE_THAN}, or {@link Bound#NONE} for no lower end
     * @param lower      the lower end, ignored if there is none
     * @param upperBound {@link Bound#AT_MOST}, {@link Bound#BELOW}, or {@link Bound#NONE} for no upper end
     * @param upper      the upper end, ignored if there is none
     * @return the interval
     * @throws IllegalArgumentException if a bound does not belong to its end
     */
    public static Interval of(Bound lowerBound, double lower, Bound upperBound, double upper) {
        if (lowerBound != Bound.NONE && !lowerBound.isLower())
            throw new IllegalArgumentException(lowerBound + " is no bound for a lower end");
        if (upperBound != Bound.NONE && !upperBound.isUpper())
            throw new IllegalArgumentException(upperBound + " is no bound for an upper end");

        return new Interval(lowerBound, lower, upperBound, upper);
    }

    /**
     * @return whether {@link #getLower()} itself is included ({@link Bound#AT_LEAST}) or not
     * ({@link Bound#MORE_THAN}), or {@link Bound#NONE} if the interval has no lower end
     */
    public Bound getLowerBound() {
        return lowerBound;
    }

    /**
     * @return the lower end of the interval, or {@code NaN} if it has none
     */
    public double getLower() {
        return lower;
    }

    /**
     * @return whether {@link #getUpper()} itself is included ({@link Bound#AT_MOST}) or not
     * ({@link Bound#BELOW}), or {@link Bound#NONE} if the interval has no upper end
     */
    public Bound getUpperBound() {
        return upperBound;
    }

    /**
     * @return the upper end of the interval, or {@code NaN} if it has none
     */
    public double getUpper() {
        return upper;
    }

    /**
     * The largest value this interval guarantees the true value does not exceed, as a single number:
     * {@link #getUpper()}, or {@link Double#POSITIVE_INFINITY} if there is no upper end, so that
     * {@code getGuaranteedUpperBound() <= limit} is false wherever nothing guarantees it.
     *
     * @return the guaranteed upper bound, or {@code POSITIVE_INFINITY}
     */
    public double getGuaranteedUpperBound() {
        return upperBound == Bound.NONE ? Double.POSITIVE_INFINITY : upper;
    }

    /**
     * The counterpart of {@link #getGuaranteedUpperBound()}: {@link #getLower()}, or
     * {@link Double#NEGATIVE_INFINITY} if there is no lower end.
     *
     * @return the guaranteed lower bound, or {@code NEGATIVE_INFINITY}
     */
    public double getGuaranteedLowerBound() {
        return lowerBound == Bound.NONE ? Double.NEGATIVE_INFINITY : lower;
    }

    /**
     * @return the width of the interval, i.e. the step of the code, or {@link Double#POSITIVE_INFINITY}
     * if it is unbounded on either side
     */
    public double getWidth() {
        return getGuaranteedUpperBound() - getGuaranteedLowerBound();
    }

    /**
     * @return the interval mirrored at zero: each end negated, the ends swapped, and each bound turned to
     * face the other way, so that (2, 3] becomes [-3, -2)
     */
    public Interval negated() {
        return new Interval(mirror(upperBound), 0 - upper, mirror(lowerBound), 0 - lower);
    }

    private static Bound mirror(Bound bound) {
        switch (bound) {
            case BELOW:
                return Bound.MORE_THAN;
            case AT_MOST:
                return Bound.AT_LEAST;
            case AT_LEAST:
                return Bound.AT_MOST;
            case MORE_THAN:
                return Bound.BELOW;
            default:
                return Bound.NONE;
        }
    }

    /**
     * @param first the interval the result starts with
     * @param last  the interval the result ends with, adjoining or above {@code first}
     * @return the interval from {@code first}'s lower end to {@code last}'s upper end
     */
    public static Interval spanning(Interval first, Interval last) {
        return new Interval(first.lowerBound, first.lower, last.upperBound, last.upper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval)) return false;
        Interval other = (Interval) o;
        return lowerBound == other.lowerBound && Double.compare(lower, other.lower) == 0
                && upperBound == other.upperBound && Double.compare(upper, other.upper) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, lower, upperBound, upper);
    }

    /**
     * @return the interval in the usual notation, e.g. "[2.0, 2.5)", "(2.0, 2.5]" or "(-∞, -4550.0)"
     */
    @Override
    public String toString() {
        String from = lowerBound == Bound.NONE ? "(-∞" : (lowerBound == Bound.MORE_THAN ? "(" : "[") + lower;
        String to = upperBound == Bound.NONE ? "∞)" : upper + (upperBound == Bound.AT_MOST ? "]" : ")");
        return from + ", " + to;
    }
}
