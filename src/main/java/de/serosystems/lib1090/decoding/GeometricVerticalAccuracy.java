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
 * The geometric vertical accuracy a Geometric Vertical Accuracy category reports, ED-102B
 * §2.2.3.2.7.2.8 TABLE 2-69.
 * <p>
 * The field is two bits and the standard defines all four values, so every category a message can
 * carry names something. Three of them bound the accuracy from above; the fourth does not.
 * <p>
 * <b>The bounds here are inclusive</b> — 150 m means "150 m or better" — where
 * {@link ContainmentRadius} and {@link HorizontalVelocityError} bound strictly. That is the
 * standard's own distinction between these tables rather than an inconsistency in how they are read,
 * so the constants are named for the bound they actually state.
 * <p>
 * <b>Category 3 is read the same way whatever version transmitted it.</b> ADS-B version 2 reserved
 * that value and version 3 gave it its meaning, so strictly a version 2 message carrying it says
 * nothing. One table serves both versions here rather than two differing in a single row, on the
 * grounds that the combination is not expected in practice — a deliberate choice, and the place to
 * revisit should such messages turn up.
 * <p>
 * <b>Category 0 is two answers in one, and the standard makes it so.</b> It reads "unknown or more
 * than 150 m", which are different facts a receiver cannot separate: the transmitter may be saying
 * its vertical accuracy is poor, or that it does not know. Nothing can be guaranteed either way.
 *
 * @see HorizontalVelocityError for the velocity counterpart, whose category 0 combines the same way
 */
public enum GeometricVerticalAccuracy {

    /**
     * GVA 0, which ED-102B defines as "unknown or more than 150 m" — one category for both, so no
     * upper bound is guaranteed.
     */
    UNKNOWN_OR_ABOVE_150(Double.NaN),

    /** GVA 1: the accuracy is 150 m or better. */
    AT_MOST_150(150),

    /** GVA 2: the accuracy is 45 m or better. */
    AT_MOST_45(45),

    /** GVA 3: the accuracy is 10 m or better. */
    AT_MOST_10(10);

    /** The field is two bits wide, so this is every value a message can carry. */
    private static final int MAX_GVA = 3;

    private final double meters;

    GeometricVerticalAccuracy(double meters) {
        this.meters = meters;
    }

    /**
     * The accuracy a Geometric Vertical Accuracy category reports, ED-102B §2.2.3.2.7.2.8
     * TABLE 2-69.
     *
     * @param gva the encoded geometric vertical accuracy, 0 to 3
     * @return what that category guarantees, {@link #UNKNOWN_OR_ABOVE_150} for category 0
     * @throws IllegalArgumentException if the value is outside the two bits the field occupies
     */
    public static GeometricVerticalAccuracy forGVA(byte gva) {
        if (gva < 0 || gva > MAX_GVA)
            throw new IllegalArgumentException(
                    "Geometric vertical accuracy " + gva + " does not fit the two bits it occupies");

        switch (gva) {
            case 1:
                return AT_MOST_150;
            case 2:
                return AT_MOST_45;
            case 3:
                return AT_MOST_10;
            default:
                return UNKNOWN_OR_ABOVE_150;
        }
    }

    /**
     * The largest accuracy this category guarantees, in meters, or {@link Double#NaN} where it
     * guarantees none.
     * <p>
     * As with {@link ContainmentRadius#getGuaranteedUpperBound()}, the encoding is chosen so that
     * {@code getGuaranteedUpperBound() < limit} is <b>false</b> wherever nothing is guaranteed, so a
     * caller asking "is this accuracy known to be better than {@code limit}?" gets the conservative
     * answer without a special case. {@code NaN} rather than an infinity, because
     * {@link #UNKNOWN_OR_ABOVE_150} may also mean the transmitter simply does not know — the reverse
     * question cannot be answered either.
     * <p>
     * The bound is inclusive: {@link #AT_MOST_150} guarantees 150 m or better, so a limit of exactly
     * 150 m is met by it even though the comparison above is strict.
     *
     * @return the guaranteed accuracy in meters, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return meters;
    }

    /**
     * @return true where an accuracy is guaranteed, i.e. for every category but
     * {@link #UNKNOWN_OR_ABOVE_150}
     */
    public boolean isKnown() {
        return !Double.isNaN(meters);
    }
}
