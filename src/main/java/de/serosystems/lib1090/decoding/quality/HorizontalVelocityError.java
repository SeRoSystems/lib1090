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
 * The 95% horizontal velocity error a Navigation Accuracy Category for velocity reports, ED-102B
 * §2.2.3.2.6.1.5 TABLE 2-18.
 * <p>
 * Like the containment radius, the standard states a bound rather than a value: each category says
 * the true error is <b>below</b> a figure, not what it is. Unlike the containment radius there is no
 * category reporting a lower bound, so one number says everything a category guarantees — see
 * {@link #getGuaranteedUpperBound()}.
 * <p>
 * <b>NACv 0 is two answers in one, and the standard makes it so.</b> It reads "unknown or 10 m/s or
 * more", which are different facts a receiver cannot separate: the transmitter may be saying its
 * velocity is poor, or that it does not know. Nothing can be guaranteed either way, so that category
 * guarantees no upper bound at all, and the categories the standard leaves reserved are reported the
 * same way for the same reason.
 *
 * @see ContainmentRadius for the position counterpart, where lower bounds do occur
 */
public enum HorizontalVelocityError {

    /**
     * NACv 0, which ED-102B defines as "unknown or 10 m/s or more" — one category for both, so no
     * upper bound is guaranteed. The categories the standard reserves report this too.
     */
    UNKNOWN_OR_AT_LEAST_10(Double.NaN),

    /** NACv 1: the true error is below 10 m/s. */
    BELOW_10(10),

    /** NACv 2: the true error is below 3 m/s. */
    BELOW_3(3),

    /** NACv 3: the true error is below 1 m/s. */
    BELOW_1(1),

    /** NACv 4: the true error is below 0.3 m/s. */
    BELOW_0_3(0.3);

    /** The field is three bits wide, so this is every value a message can carry. */
    private static final int MAX_NACV = 7;

    private final double metersPerSecond;

    HorizontalVelocityError(double metersPerSecond) {
        this.metersPerSecond = metersPerSecond;
    }

    /**
     * The error a Navigation Accuracy Category for velocity reports, ED-102B §2.2.3.2.6.1.5
     * TABLE 2-18.
     *
     * @param nacV the encoded navigation accuracy category for velocity, 0 to 7
     * @return what that category guarantees, {@link #UNKNOWN_OR_AT_LEAST_10} for category 0 and for
     * the categories the standard reserves
     * @throws IllegalArgumentException if the value is outside the three bits the field occupies
     */
    public static HorizontalVelocityError forNACv(byte nacV) {
        if (nacV < 0 || nacV > MAX_NACV)
            throw new IllegalArgumentException(
                    "Navigation accuracy category for velocity " + nacV
                            + " does not fit the three bits it occupies");

        switch (nacV) {
            case 1:
                return BELOW_10;
            case 2:
                return BELOW_3;
            case 3:
                return BELOW_1;
            case 4:
                return BELOW_0_3;
            default:
                return UNKNOWN_OR_AT_LEAST_10;
        }
    }

    /**
     * The error a version 0 Navigation Uncertainty Category for velocity reports.
     * <p>
     * Version 0 transmits NUCr where later versions transmit NACv, and ED-102B §N.2.3.8 maps the two
     * one for one, so this reads the same table. It exists so that a call site says which field it
     * holds; the answer is the same either way.
     *
     * @param nucR the encoded navigation uncertainty category for velocity, 0 to 7
     * @return what that category guarantees, {@link #UNKNOWN_OR_AT_LEAST_10} for category 0 and for
     * the categories the standard reserves
     * @throws IllegalArgumentException if the value is outside the three bits the field occupies
     */
    public static HorizontalVelocityError forNUCr(byte nucR) {
        return forNACv(nucR);
    }

    /**
     * The largest error this category guarantees the true error stays below, in meters per second, or
     * {@link Double#NaN} where it guarantees none.
     * <p>
     * As with {@link ContainmentRadius#getGuaranteedUpperBound()}, the encoding is chosen so that
     * {@code getGuaranteedUpperBound() < limit} is <b>false</b> wherever nothing is guaranteed, so a
     * caller asking "is this velocity known to be better than {@code limit}?" gets the conservative
     * answer without a special case. {@code NaN} rather than an infinity, because
     * {@link #UNKNOWN_OR_AT_LEAST_10} may also mean the transmitter simply does not know — the
     * reverse question cannot be answered either.
     *
     * @return the guaranteed upper bound in meters per second, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return metersPerSecond;
    }

    /**
     * @return true where an upper bound is guaranteed, i.e. for every category but
     * {@link #UNKNOWN_OR_AT_LEAST_10}
     */
    public boolean isKnown() {
        return !Double.isNaN(metersPerSecond);
    }
}
