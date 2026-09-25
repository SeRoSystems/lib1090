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

package de.serosystems.lib1090.decoding.diffbaroalt;

import de.serosystems.lib1090.decoding.Interval;

/**
 * What the three codings have in common: the code, its sign, what it reports and the interval of its
 * magnitude, from which the signed difference and value follow.
 */
abstract class AbstractDiffBaroAlt implements DiffBaroAlt {

    private final int encoded;
    private final boolean negative;
    private final Status status;
    private final Interval magnitude;
    private final Double magnitudeValue;

    AbstractDiffBaroAlt(int encoded, boolean negative, Status status, Interval magnitude) {
        this.encoded = encoded;
        this.negative = negative;
        this.status = status;
        this.magnitude = status == Status.AVAILABLE ? magnitude : null;
        this.magnitudeValue = this.magnitude == null ? null : valueOf(this.magnitude);
    }

    /**
     * The value of a magnitude interval: its lower end if it has no upper end, 0 if it starts at 0
     * inclusive, the midpoint otherwise.
     */
    private static double valueOf(Interval magnitude) {
        if (Double.isInfinite(magnitude.getGuaranteedUpperBound())) return magnitude.getLower();
        if (magnitude.getLower() == 0 && magnitude.getLowerBound().isInclusive()) return 0;
        return (magnitude.getLower() + magnitude.getUpper()) / 2;
    }

    @Override
    public int getEncoded() {
        return encoded;
    }

    @Override
    public boolean isNegative() {
        return negative;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Interval getDifference() {
        if (magnitude == null) return null;
        return negative ? magnitude.negated() : magnitude;
    }

    @Override
    public Double getValue() {
        if (magnitudeValue == null) return null;
        return negative ? 0 - magnitudeValue : magnitudeValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{encoded=" + encoded + ", negative=" + negative
                + ", status=" + status + ", difference=" + getDifference() + '}';
    }
}
