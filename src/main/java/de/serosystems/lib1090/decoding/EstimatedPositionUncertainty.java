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
 * The 95% horizontal accuracy bound a Navigation Accuracy Category for position reports, ED-102B
 * §2.2.3.2.7.2.7 TABLE 2-68.
 * <p>
 * The category is four bits and the standard defines twelve of the sixteen values: eleven bound the
 * estimated position uncertainty from above, and category 0 does not.
 * <p>
 * <b>Category 0 is two answers in one, and the standard makes it so.</b> The table gives it as 18.52
 * km or more, and annotates that row "Unknown accuracy" — a transmitter reporting it may be saying
 * its accuracy is poor, or that it has none to report, and a receiver cannot separate the two.
 * Nothing is guaranteed either way, which is why this reports no bound rather than the 18.52 km the
 * table names.
 * <p>
 * <b>This is accuracy, not integrity.</b> {@link ContainmentRadius} says how large a region the true
 * position is contained within and how much that containment can be trusted; this says how close the
 * reported position is expected to be. The two travel in the same messages and are tabulated
 * separately, so neither may be computed from the other.
 * <p>
 * The four values the standard reserves report {@link #RESERVED}, one constant for all of them. A
 * caller that needs to tell them apart still has the encoded category, which this never discards.
 */
public enum EstimatedPositionUncertainty {

    /**
     * NACp 0, which ED-102B gives as 18.52 km or more and annotates "Unknown accuracy" — one category
     * for both, so no bound is guaranteed.
     */
    UNKNOWN_OR_AT_LEAST_18520(Double.NaN),

    /** NACp 1: the uncertainty is below 18.52 km. */
    BELOW_18520(18520),

    /** NACp 2: the uncertainty is below 7.408 km. */
    BELOW_7408(7408),

    /** NACp 3: the uncertainty is below 3.704 km. */
    BELOW_3704(3704),

    /** NACp 4: the uncertainty is below 1852 m. */
    BELOW_1852(1852),

    /** NACp 5: the uncertainty is below 926 m. */
    BELOW_926(926),

    /** NACp 6: the uncertainty is below 555.6 m. */
    BELOW_555_6(555.6),

    /** NACp 7: the uncertainty is below 185.2 m. */
    BELOW_185_2(185.2),

    /** NACp 8: the uncertainty is below 92.6 m. */
    BELOW_92_6(92.6),

    /** NACp 9: the uncertainty is below 30 m. */
    BELOW_30(30),

    /** NACp 10: the uncertainty is below 10 m. */
    BELOW_10(10),

    /** NACp 11: the uncertainty is below 3 m. */
    BELOW_3(3),

    /** NACp 12 to 15, which the standard reserves: nothing is reported. */
    RESERVED(Double.NaN);

    /** The category is four bits wide, so this is every value a message can carry. */
    private static final int MAX_NACP = 15;

    /** The lowest reserved category; everything from here up reports {@link #RESERVED}. */
    private static final int FIRST_RESERVED_NACP = 12;


    private final double meters;

    EstimatedPositionUncertainty(double meters) {
        this.meters = meters;
    }

    /**
     * The accuracy bound a Navigation Accuracy Category for position reports, ED-102B
     * §2.2.3.2.7.2.7 TABLE 2-68.
     *
     * @param nacP the encoded navigation accuracy category for position, 0 to 15
     * @return what that category guarantees, nothing for category 0 and for the values the
     * standard reserves
     * @throws IllegalArgumentException if the value is outside the four bits the field occupies
     */
    public static EstimatedPositionUncertainty forNACp(byte nacP) {
        if (nacP < 0 || nacP > MAX_NACP)
            throw new IllegalArgumentException(
                    "Navigation accuracy category for position " + nacP
                            + " does not fit the four bits it occupies");

        if (nacP >= FIRST_RESERVED_NACP)
            return RESERVED;

        switch (nacP) {
            case 1:
                return BELOW_18520;
            case 2:
                return BELOW_7408;
            case 3:
                return BELOW_3704;
            case 4:
                return BELOW_1852;
            case 5:
                return BELOW_926;
            case 6:
                return BELOW_555_6;
            case 7:
                return BELOW_185_2;
            case 8:
                return BELOW_92_6;
            case 9:
                return BELOW_30;
            case 10:
                return BELOW_10;
            case 11:
                return BELOW_3;
            default:
                return UNKNOWN_OR_AT_LEAST_18520;
        }
    }



    /**
     * The largest uncertainty this category guarantees the true one stays below, in meters, or
     * {@link Double#NaN} where it guarantees none.
     * <p>
     * As with {@link ContainmentRadius#getGuaranteedUpperBound()}, the encoding is chosen so that
     * {@code getGuaranteedUpperBound() < limit} is <b>false</b> wherever nothing is guaranteed, so a
     * caller asking "is this position known to be more accurate than {@code limit}?" gets the
     * conservative answer without a special case. {@code NaN} rather than an infinity, because
     * {@link #UNKNOWN_OR_AT_LEAST_18520} may also mean the transmitter simply does not know — the
     * reverse question cannot be answered either.
     * <p>
     * The bound is strict: {@link #BELOW_30} guarantees less than 30 m.
     *
     * @return the guaranteed upper bound in meters, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return meters;
    }

    /**
     * @return true where an accuracy is guaranteed, i.e. for every category but
     * {@link #UNKNOWN_OR_AT_LEAST_18520} and {@link #RESERVED}
     */
    public boolean isKnown() {
        return !Double.isNaN(meters);
    }
}
