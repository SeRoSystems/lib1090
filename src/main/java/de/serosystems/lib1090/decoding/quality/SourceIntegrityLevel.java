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
 * The probability of the true position falling outside the containment radius, as a Source Integrity
 * Level reports it, ED-102B §2.2.3.2.7.2.9 TABLE 2-70.
 * <p>
 * This is the one quantity in this family that is not a distance. It grades how often the position
 * may lie outside the radius {@link ContainmentRadius} bounds, so the two are read together: the
 * radius says how large the containment is, this says how much to trust it.
 * <p>
 * <b>The figure has no meaning without its basis.</b> ED-102B states it "per flight hour or per
 * sample", and which of the two applies is carried by a separate bit, the SIL supplement — not to be
 * confused with this field, and not part of this type. A receiver that has the supplement should read
 * it alongside; one that does not knows the probability but not what it is per.
 * <p>
 * As in {@link GeometricVerticalAccuracy} the bounds are inclusive, and as there, category 0 is two
 * answers in one: "unknown or more than 10&#94;-3" combines a poor probability with no probability at
 * all, which a receiver cannot separate, so it guarantees nothing.
 *
 * @see ContainmentRadius for the containment this grades the trustworthiness of
 */
public enum SourceIntegrityLevel {

    /**
     * SIL 0, which ED-102B defines as "unknown or more than 10&#94;-3" — one category for both, so no
     * bound is guaranteed.
     */
    UNKNOWN_OR_ABOVE_1E_MINUS_3(Double.NaN),

    /** SIL 1: the probability is 10&#94;-3 or less. */
    AT_MOST_1E_MINUS_3(1e-3),

    /** SIL 2: the probability is 10&#94;-5 or less. */
    AT_MOST_1E_MINUS_5(1e-5),

    /** SIL 3: the probability is 10&#94;-7 or less. */
    AT_MOST_1E_MINUS_7(1e-7);

    /** The field is two bits wide, so this is every value a message can carry. */
    private static final int MAX_SIL = 3;

    private final double probability;

    SourceIntegrityLevel(double probability) {
        this.probability = probability;
    }

    /**
     * The probability a Source Integrity Level reports, ED-102B §2.2.3.2.7.2.9 TABLE 2-70.
     *
     * @param sil the encoded source integrity level, 0 to 3
     * @return what that level guarantees, {@link #UNKNOWN_OR_ABOVE_1E_MINUS_3} for level 0
     * @throws IllegalArgumentException if the value is outside the two bits the field occupies
     */
    public static SourceIntegrityLevel forSIL(byte sil) {
        if (sil < 0 || sil > MAX_SIL)
            throw new IllegalArgumentException(
                    "Source integrity level " + sil + " does not fit the two bits it occupies");

        switch (sil) {
            case 1:
                return AT_MOST_1E_MINUS_3;
            case 2:
                return AT_MOST_1E_MINUS_5;
            case 3:
                return AT_MOST_1E_MINUS_7;
            default:
                return UNKNOWN_OR_ABOVE_1E_MINUS_3;
        }
    }

    /**
     * The largest probability this level guarantees the true one stays at or below, per flight hour or
     * per sample as the SIL supplement says, or {@link Double#NaN} where it guarantees none.
     * <p>
     * As with {@link ContainmentRadius#getGuaranteedUpperBound()}, the encoding is chosen so that
     * {@code getGuaranteedUpperBound() < limit} is <b>false</b> wherever nothing is guaranteed, so a
     * caller asking "is this position known to be more trustworthy than {@code limit}?" gets the
     * conservative answer without a special case. {@code NaN} rather than an infinity, because
     * {@link #UNKNOWN_OR_ABOVE_1E_MINUS_3} may also mean the transmitter simply does not know — the
     * reverse question cannot be answered either.
     * <p>
     * The bound is inclusive: {@link #AT_MOST_1E_MINUS_3} guarantees 10&#94;-3 or less.
     *
     * @return the guaranteed probability, or {@code NaN}
     */
    public double getGuaranteedUpperBound() {
        return probability;
    }

    /**
     * @return true where a probability is guaranteed, i.e. for every level but
     * {@link #UNKNOWN_OR_ABOVE_1E_MINUS_3}
     */
    public boolean isKnown() {
        return !Double.isNaN(probability);
    }
}
