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
 * What a position report says about its own integrity: a navigation integrity category and the
 * containment radius that belongs to it, both keyed on the format type code they came from.
 * <p>
 * Two quite different things answer this. The version tables —
 * {@link NavigationCharacteristicsV0} through {@link NavigationCharacteristicsV3} — are the rows the
 * standard tabulates, one per format type code and supplement combination. The position messages are
 * the reports themselves, and each answers by selecting the row its type code and known supplements
 * point at. A caller that only wants to know how good a position is need not care which it is holding.
 * <p>
 * <b>NIC and the containment radius are two columns of one row, not one derived from the other.</b>
 * Neither direction is a function: NIC 6 appears at four different radii across the versions, and
 * NIC 0 covers "nothing reported" as well as four different lower bounds. They are reported together
 * here for that reason.
 */
public interface NavigationCharacteristics {

    /**
     * @return the format type code these characteristics were derived from
     */
    byte getFormatTypeCode();

    /**
     * @return the navigation integrity category. A NIC of 0 means "unknown" or "not upper bounded".
     */
    byte getNIC();

    /**
     * The horizontal containment radius limit, with the side of the reported value the true radius
     * lies on — the distinction a bare number cannot carry.
     *
     * @return the containment radius
     */
    ContainmentRadius getContainmentRadius();

    /**
     * The containment radius as a single number, for callers that want one: the value itself where an
     * upper bound is reported, {@link Double#POSITIVE_INFINITY} where only a lower bound is, and
     * {@link Double#NaN} where nothing is reported.
     * <p>
     * The horizontal containment radius limit is also known as "horizontal protection level".
     * <p>
     * See {@link ContainmentRadius#getGuaranteedUpperBound()} for why those two special values are
     * the right ones, and for the one question this number cannot answer safely.
     *
     * @return the guaranteed upper bound on the containment radius in meters
     */
    default double getHorizontalContainmentRadiusLimit() {
        return getContainmentRadius().getGuaranteedUpperBound();
    }
}
