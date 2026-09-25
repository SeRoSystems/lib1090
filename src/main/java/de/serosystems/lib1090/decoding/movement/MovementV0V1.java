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

package de.serosystems.lib1090.decoding.movement;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;

/**
 * The "Movement" subfield of ADS-B versions 0 and 1, DO-260 and DO-260A.
 * <p>
 * Every interval includes its lower end and excludes its upper: code 13, for instance, is
 * 2 kt &le; GS &lt; 2.5 kt. The tables ED-102B repeats in Appendix N next to FIGURE N-2 and FIGURE N-9
 * confuse &lt; and &le; in places, so this follows the original standards, e.g. ED-102 TABLE 2-13.
 * <p>
 * "Aircraft Stopped" (1) is a ground speed below 0.125 kt, and the highest code (124) one of 175 kt or
 * more.
 */
public final class MovementV0V1 implements Movement {

    private static final Bound LOWER = Bound.AT_LEAST;
    private static final Bound UPPER = Bound.BELOW;

    private static Interval groundSpeed(int encoded) {
        if (encoded == 1) return Interval.of(LOWER, 0, UPPER, 0.125);
        if (encoded >= 2 && encoded <= 8) return band(encoded, 2, 0.125, 0.125);
        if (encoded >= 9 && encoded <= 12) return band(encoded, 9, 1, 0.25);
        if (encoded >= 13 && encoded <= 38) return band(encoded, 13, 2, 0.5);
        if (encoded >= 39 && encoded <= 93) return band(encoded, 39, 15, 1);
        if (encoded >= 94 && encoded <= 108) return band(encoded, 94, 70, 2);
        if (encoded >= 109 && encoded <= 123) return band(encoded, 109, 100, 5);
        if (encoded == 124) return Interval.of(LOWER, 175, Bound.NONE, Double.NaN);
        return null;
    }

    /** The field is seven bits wide, so this is every value a message can carry. */
    private static final MovementV0V1[] VALUES = new MovementV0V1[128];

    static {
        for (int encoded = 0; encoded < VALUES.length; encoded++)
            VALUES[encoded] = new MovementV0V1((byte) encoded, groundSpeed(encoded));
    }

    private final byte encoded;
    private final Interval groundSpeed;

    private MovementV0V1(byte encoded, Interval groundSpeed) {
        this.encoded = encoded;
        this.groundSpeed = groundSpeed;
    }

    /**
     * @param encoded the encoded movement, 0 to 127
     * @return the movement for that code
     * @throws IllegalArgumentException if the value is outside the seven bits the field occupies
     */
    public static MovementV0V1 forEncoded(byte encoded) {
        if (encoded < 0)
            throw new IllegalArgumentException("Movement " + encoded + " does not fit the seven bits it occupies");

        return VALUES[encoded];
    }

    /**
     * The code's interval: {@code step} knots wide and the {@code (encoded - first)}-th of its band,
     * which starts at {@code start} knots.
     */
    private static Interval band(int encoded, int first, double start, double step) {
        return Interval.of(LOWER, start + (encoded - first) * step, UPPER, start + (encoded - first + 1) * step);
    }

    @Override
    public byte getEncoded() {
        return encoded;
    }

    @Override
    public Interval getGroundSpeed() {
        return groundSpeed;
    }

    @Override
    public String toString() {
        return "MovementV0V1{encoded=" + encoded + ", groundSpeed=" + groundSpeed + '}';
    }
}
