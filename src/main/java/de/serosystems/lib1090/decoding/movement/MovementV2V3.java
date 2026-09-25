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
 * The "Movement" subfield of ADS-B versions 2 and 3, ED-102B §2.2.3.2.4.2 TABLE 2-13, which version 3
 * took over from version 2 unchanged. TIS-B uses it as well: ED-102B §2.2.17.3.2.1 has the TIS-B
 * surface position message decode its movement as the ADS-B one does.
 * <p>
 * Every interval excludes its lower end and includes its upper, the reverse of versions 0 and 1: code
 * 13, for instance, is 2 kt &lt; GS &le; 2.5 kt, and 2 kt itself is code 12.
 * <p>
 * "Aircraft Stopped" (1) is a ground speed of exactly 0, code 2 anything above that up to 0.125 kt, and
 * codes 3 to 8 divide the rest up to 1 kt into six steps of 0.2700833 km/h, i.e. 7/48 kt. The highest
 * code (124) is a ground speed above 175 kt.
 */
public final class MovementV2V3 implements Movement {

    private static final Bound LOWER = Bound.MORE_THAN;
    private static final Bound UPPER = Bound.AT_MOST;

    private static Interval groundSpeed(int encoded) {
        if (encoded == 1) return Interval.of(Bound.AT_LEAST, 0, UPPER, 0);
        if (encoded == 2) return Interval.of(LOWER, 0, UPPER, 0.125);
        // steps of 7/48 kt, which no double represents exactly, so each end is computed from its
        // count of steps; code 8's upper end then comes out as exactly the 1 kt code 9 starts from
        if (encoded >= 3 && encoded <= 8)
            return Interval.of(LOWER, 0.125 + (encoded - 3) * 7.0 / 48, UPPER, 0.125 + (encoded - 2) * 7.0 / 48);
        if (encoded >= 9 && encoded <= 12) return band(encoded, 9, 1, 0.25);
        if (encoded >= 13 && encoded <= 38) return band(encoded, 13, 2, 0.5);
        if (encoded >= 39 && encoded <= 93) return band(encoded, 39, 15, 1);
        if (encoded >= 94 && encoded <= 108) return band(encoded, 94, 70, 2);
        if (encoded >= 109 && encoded <= 123) return band(encoded, 109, 100, 5);
        if (encoded == 124) return Interval.of(LOWER, 175, Bound.NONE, Double.NaN);
        return null;
    }

    /** The field is seven bits wide, so this is every value a message can carry. */
    private static final MovementV2V3[] VALUES = new MovementV2V3[128];

    static {
        for (int encoded = 0; encoded < VALUES.length; encoded++)
            VALUES[encoded] = new MovementV2V3((byte) encoded, groundSpeed(encoded));
    }

    private final byte encoded;
    private final Interval groundSpeed;

    private MovementV2V3(byte encoded, Interval groundSpeed) {
        this.encoded = encoded;
        this.groundSpeed = groundSpeed;
    }

    /**
     * @param encoded the encoded movement, 0 to 127
     * @return the movement for that code
     * @throws IllegalArgumentException if the value is outside the seven bits the field occupies
     */
    public static MovementV2V3 forEncoded(byte encoded) {
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
        return "MovementV2V3{encoded=" + encoded + ", groundSpeed=" + groundSpeed + '}';
    }
}
