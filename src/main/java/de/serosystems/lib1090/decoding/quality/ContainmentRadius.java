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

import de.serosystems.lib1090.decoding.Bound;

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

    BELOW_7_5(Bound.BELOW, 7.5),
    BELOW_25(Bound.BELOW, 25),
    BELOW_75(Bound.BELOW, 75),
    BELOW_185_2(Bound.BELOW, 185.2),
    BELOW_370_4(Bound.BELOW, 370.4),
    BELOW_555_6(Bound.BELOW, 555.6),
    BELOW_926(Bound.BELOW, 926),
    BELOW_1111_2(Bound.BELOW, 1111.2),
    BELOW_1852(Bound.BELOW, 1852),
    BELOW_3704(Bound.BELOW, 3704),
    BELOW_7408(Bound.BELOW, 7408),
    BELOW_14816(Bound.BELOW, 14816),
    BELOW_18520(Bound.BELOW, 18520),
    BELOW_37040(Bound.BELOW, 37040),

    AT_LEAST_25(Bound.AT_LEAST, 25),
    AT_LEAST_185_2(Bound.AT_LEAST, 185.2),
    AT_LEAST_1111_2(Bound.AT_LEAST, 1111.2),
    AT_LEAST_3704(Bound.AT_LEAST, 3704),
    AT_LEAST_37040(Bound.AT_LEAST, 37040);

    /** The position integrity category is four bits wide, so this is every value it can carry. */
    private static final int MAX_PIC = 15;

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
        return bound.isLower() ? Double.POSITIVE_INFINITY : meters;
    }

    /**
     * @return true if nothing is reported, i.e. this is {@link #UNKNOWN}
     */
    public boolean isUnknown() {
        return bound == Bound.NONE;
    }

    /**
     * The containment radius a Position Integrity Category (PIC) reports, ED-102B §2.2.3.2.7.5.4.8
     * TABLE 2-82.
     * <p>
     * PIC is the HVA Velocity Message's own way of reporting the horizontal containment region, in
     * place of the format type code and supplements a position message is read with. The field is
     * four bits and the table defines every value: fourteen of them bound the radius from above, and
     * the two at the ends report nothing — PIC 0 because the radius is unknown, PIC 15 because the
     * standard reserves it.
     * <p>
     * <b>This does not give the NIC.</b> ED-102B §2.2.3.2.7.2.6 TABLE 2-67 tabulates the same radii
     * against a NIC, and against the format type code and supplements a position message would have
     * needed to report them. That mapping is deliberately left out: a NIC obtained from a containment
     * radius is not the NIC a message transmitted, and the two are tabulated independently everywhere
     * else in this package precisely so that neither is computed from the other. Reading it off
     * TABLE 2-67 remains possible, and is then the caller's own claim rather than the library's.
     *
     * @param pic the encoded position integrity category, 0 to 15
     * @return the radius that category reports, {@link #UNKNOWN} for PIC 0 and for the reserved
     * PIC 15
     * @throws IllegalArgumentException if the value is outside the four bits the field occupies
     */
    public static ContainmentRadius forPIC(byte pic) {
        if (pic < 0 || pic > MAX_PIC)
            throw new IllegalArgumentException(
                    "Position integrity category " + pic + " does not fit the four bits it occupies");

        switch (pic) {
            case 1:
                return BELOW_37040;
            case 2:
                return BELOW_18520;
            case 3:
                return BELOW_14816;
            case 4:
                return BELOW_7408;
            case 5:
                return BELOW_3704;
            case 6:
                return BELOW_1852;
            case 7:
                return BELOW_1111_2;
            case 8:
                return BELOW_926;
            case 9:
                return BELOW_555_6;
            case 10:
                return BELOW_370_4;
            case 11:
                return BELOW_185_2;
            case 12:
                return BELOW_75;
            case 13:
                return BELOW_25;
            case 14:
                return BELOW_7_5;
            default:
                return UNKNOWN;
        }
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
        return first.bound.isLower() ? first : second;
    }
}
