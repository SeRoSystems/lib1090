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
 * A horizontal containment radius limit (Rc) as the standard reports it: one of the values the
 * format type code and NIC supplement tables tabulate, together with which side of that value the
 * true radius lies on.
 * <p>
 * The standard never states a radius outright. It states a bound, and it states it in one of three
 * ways — the radius is <b>below</b> a value, the radius is that value <b>or above</b>, or nothing is
 * reported at all. A bare {@code double} cannot hold the third of those, and cannot tell the first
 * two apart at a shared value: 25 m, 185.2 m, 1111.2 m, 3704 m and 37040 m each appear as an upper
 * bound for one format type code and as a lower bound for another. Naming the value alone therefore
 * loses the only part that says how good the position is.
 * <p>
 * Values are tabulated once per ADS-B version, and the constants here are the union of everything
 * those tables name: ED-102B §2.4.8.1.16 TABLE 2-304 with §N.2.2.2 TABLE N-4 for version 0,
 * ED-102B §N.3.2.2 TABLE N-16 for version 1, ED-102A TABLE 2-14 for version 2, and
 * ED-102B §2.2.3.2.3.1 TABLE 2-11 for version 3. Which of them a given message reports is a
 * question for {@link NavigationCharacteristicsV0} through {@link NavigationCharacteristicsV3},
 * not for this type.
 * <p>
 * <b>Declaration order carries no meaning.</b> Ordinals do not rank positions by quality and
 * {@link #compareTo} must not be used to do so — {@link #AT_LEAST_25} describes a worse position than
 * {@link #BELOW_37040} despite the smaller number. NIC is the ordered scale; this is the unit-bearing
 * detail hanging off it.
 */
public enum ContainmentRadius {

    /** No containment radius is reported. The true radius may be anything. */
    UNKNOWN(Bound.NONE, Double.NaN),

    BELOW_7_5(Bound.UPPER, 7.5),
    BELOW_25(Bound.UPPER, 25),
    BELOW_75(Bound.UPPER, 75),
    BELOW_185_2(Bound.UPPER, 185.2),
    BELOW_370_4(Bound.UPPER, 370.4),
    BELOW_555_6(Bound.UPPER, 555.6),
    BELOW_926(Bound.UPPER, 926),
    BELOW_1111_2(Bound.UPPER, 1111.2),
    BELOW_1852(Bound.UPPER, 1852),
    BELOW_3704(Bound.UPPER, 3704),
    BELOW_7408(Bound.UPPER, 7408),
    BELOW_14816(Bound.UPPER, 14816),
    BELOW_18520(Bound.UPPER, 18520),
    BELOW_37040(Bound.UPPER, 37040),

    AT_LEAST_25(Bound.LOWER, 25),
    AT_LEAST_185_2(Bound.LOWER, 185.2),
    AT_LEAST_1111_2(Bound.LOWER, 1111.2),
    AT_LEAST_3704(Bound.LOWER, 3704),
    AT_LEAST_37040(Bound.LOWER, 37040);

    /**
     * Which side of {@link #getMeters()} the true containment radius lies on.
     */
    public enum Bound {

        /** Nothing is reported; {@link ContainmentRadius#getMeters()} is {@code NaN}. */
        NONE,

        /** The true radius is below the value. This is the useful case: it guarantees a quality. */
        UPPER,

        /** The true radius is the value or above. It bounds the quality from the wrong side. */
        LOWER
    }

    private final Bound bound;
    private final double meters;

    ContainmentRadius(Bound bound, double meters) {
        this.bound = bound;
        this.meters = meters;
    }

    /**
     * @return which side of {@link #getMeters()} the true radius lies on
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
     * The smallest radius this report guarantees the true radius stays below, as a single number:
     * the value itself for an upper bound, {@link Double#POSITIVE_INFINITY} for a lower bound since
     * such a report guarantees no upper bound at all, and {@link Double#NaN} when nothing is
     * reported.
     * <p>
     * This encoding is chosen so that {@code getGuaranteedUpperBound() < limit} is <b>false</b> for
     * both degenerate cases. A caller asking "is this position known to be better than {@code limit}?"
     * therefore gets the conservative answer in all three states without special-casing anything.
     * <p>
     * <b>The reverse question is not safe.</b> {@code getGuaranteedUpperBound() > limit} overstates a
     * lower bound: {@link #AT_LEAST_1111_2} answers true for a limit of 5000 m, although the true
     * radius may well be under 5000 m. Ask that question of the constant itself, not of this number.
     *
     * @return the guaranteed upper bound in meters, {@code POSITIVE_INFINITY}, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return bound == Bound.LOWER ? Double.POSITIVE_INFINITY : meters;
    }

    /**
     * @return true if nothing is reported, i.e. this is {@link #UNKNOWN}
     */
    public boolean isUnknown() {
        return bound == Bound.NONE;
    }

    /**
     * The one of two reports that describes the poorer position, for picking the worst possibility
     * when it is not known which of several the transmitter means.
     * <p>
     * A larger value is worse than a smaller one; at the same value a lower bound is worse than an
     * upper bound, since it rules nothing out above it; and {@link #UNKNOWN} is worse than anything,
     * having ruled nothing out at all.
     * <p>
     * This is deliberately not {@link Comparable}: the enum's declaration order is not a quality
     * ranking and must not become one, so the comparison lives here where it can be named.
     *
     * @param first  one report
     * @param second the other report
     * @return whichever of the two describes the poorer position
     */
    public static ContainmentRadius worseOf(ContainmentRadius first, ContainmentRadius second) {
        if (first == second)
            return first;
        if (first.isUnknown() || second.isUnknown())
            return UNKNOWN;
        if (first.meters != second.meters)
            return first.meters > second.meters ? first : second;
        return first.bound == Bound.LOWER ? first : second;
    }
}
