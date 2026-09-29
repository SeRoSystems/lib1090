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

package de.serosystems.lib1090.msgs.squitter;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;

/**
 * Common API for the surface Operational Mode Code of ADS-B versions 2 and 3, format {@code 0}.
 */
public interface SurfaceOperationalModeCodeV2V3 extends OperationalModeCodeV2V3 {

    /**
     * The encoded longitudinal and lateral distance of the GPS (Global Positioning System) antenna from
     * the nose of the aircraft,
     * ED-102B §2.2.3.2.7.2.4.7 TABLE 2-59 and TABLE 2-60.
     *
     * <b>Returned as an {@code int}, not a {@code byte}</b>: the subfield is a full 8 bits, so a signed
     * byte cannot represent it — an all-ones field would read as {@code -1} rather than {@code 255},
     * and any shift applied to it would sign-extend. Narrower encoded subfields keep {@code byte}.
     *
     * @return the encoded GPS antenna offset, ME bits 33–40, in the range 0–255
     */
    default int getGPSAntennaOffsetEncoded() {
        return getMEBits(33, 40);
    }

    /**
     * Lateral GPS antenna offset, derived from ME 33–35: ME 33 gives the direction and ME 34–35 the
     * magnitude, ED-102B §2.2.3.2.7.2.4.7 TABLE 2-59 for version 3 and ED-102A §2.2.3.2.7.2.4.7
     * Table 2-66 for version 2.
     * <ul>
     *     <li>measured from the longitudinal center line (roll axis) of the aircraft</li>
     *     <li>in meters, in steps of 2 m</li>
     *     <li>positive toward the left wing tip, negative toward the right</li>
     * </ul>
     * The two versions mean the same: version 3 states the top magnitude as more than 4 m, and version 2,
     * although it tabulates 6 m, encodes every larger offset with it too (NOTE 3 of Table 2-66). So the
     * top magnitude has no end, e.g. (4, ∞) to the left and (−∞, −4) to the right. Zero magnitude to the
     * right is an offset of exactly 0.
     *
     * @return the interval of the lateral offset in meters, or {@code null} for "no data"
     * @see #isPositionOffsetApplied() if the aircraft already corrects for the offset, this is not
     * meaningful
     */
    default Interval getLateralAxisGPSAntennaOffset() {
        boolean right = getMEBit(33);
        int magnitude = getMEBits(34, 35);
        if (magnitude == 0)
            return right ? Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 0) : null;
        Interval left = magnitude == 3
                ? Interval.of(Bound.MORE_THAN, 4, Bound.NONE, Double.NaN)
                : Interval.of(Bound.MORE_THAN, 2 * (magnitude - 1), Bound.AT_MOST, 2 * magnitude);
        return right ? left.negated() : left;
    }

    /**
     * Longitudinal GPS antenna offset, ME 36–40, ED-102B §2.2.3.2.7.2.4.7 TABLE 2-60 for version 3 and
     * ED-102A §2.2.3.2.7.2.4.7 Table 2-67 for version 2.
     * <ul>
     *     <li>measured aft from the nose of the aircraft</li>
     *     <li>in meters, in steps of 2 m: [0, 2] for code 2, then (2, 4] for code 3 and so on</li>
     * </ul>
     * As for the lateral offset, the two versions mean the same: version 3 states the top code as more
     * than 58 m, and version 2, although it tabulates 60 m, encodes every larger offset with it too
     * (NOTE 1 of Table 2-67). So code 31 is (58, ∞).
     *
     * @return the interval of the longitudinal offset in meters, or {@code null} for "no data" and for
     * code 1, which reports no offset but that the position offset has been applied, see
     * {@link #isPositionOffsetApplied()}
     * @see #isPositionOffsetApplied() if the aircraft already corrects for the offset, this is not
     * meaningful
     */
    default Interval getLongitudinalAxisGPSAntennaOffset() {
        int offset = getMEBits(36, 40);
        if (offset <= 1)
            return null;
        if (offset == 2)
            return Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 2);
        if (offset == 31)
            return Interval.of(Bound.MORE_THAN, 58, Bound.NONE, Double.NaN);
        return Interval.of(Bound.MORE_THAN, 2 * (offset - 2), Bound.AT_MOST, 2 * (offset - 1));
    }

    /**
     * Whether the reported position has already been corrected for the GPS antenna offset —
     * encoded from version 2 onwards as the reserved all-but-one value of the longitudinal offset
     * field itself. ADS-B version 1 carries this in the Capability Class Code instead.
     *
     * @return true if the position offset has been applied, i.e. ME 36–40 read exactly 1
     */
    default boolean isPositionOffsetApplied() {
        return getMEBits(36, 40) == 1;
    }
}
