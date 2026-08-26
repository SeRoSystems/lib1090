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
     * magnitude, ED-102B §2.2.3.2.7.2.4.7 TABLE 2-59.
     * <ul>
     *     <li>measured from the longitudinal centre line (roll axis) of the aircraft</li>
     *     <li>in metres, at a resolution of 2 m, denoting an upper bound</li>
     *     <li>positive towards the left wing tip, negative towards the right</li>
     *     <li>capped at 6 m, i.e. 6 means "or above"</li>
     * </ul>
     *
     * @return the lateral offset in metres, or {@code null} for "no data"
     * @see #hasPositionOffsetApplied() if the aircraft already corrects for the offset, this is not
     * meaningful
     */
    default Integer getLateralAxisGPSAntennaOffset() {
        boolean right = getMEBit(33);
        int magnitude = getMEBits(34, 35);
        if (!right && magnitude == 0)
            return null;
        return 2 * (right ? -magnitude : magnitude);
    }

    /**
     * Longitudinal GPS antenna offset, ME 36–40, ED-102B §2.2.3.2.7.2.4.7 TABLE 2-60.
     * <ul>
     *     <li>measured from the nose of the aircraft</li>
     *     <li>in metres, at a resolution of 2 m, denoting an upper bound</li>
     *     <li>capped at 60 m, i.e. 60 means "or above"</li>
     * </ul>
     *
     * @return the longitudinal offset in metres, or {@code null} for "no data"
     * @see #hasPositionOffsetApplied() if the aircraft already corrects for the offset, this is not
     * meaningful
     */
    default Integer getLongitudinalAxisGPSAntennaOffset() {
        int offset = getMEBits(36, 40);
        return offset == 0 ? null : 2 * (offset - 1);
    }

    /**
     * Whether the reported position has already been corrected for the GPS antenna offset — the "POA"
     * bit of ED-129B, encoded from version 2 onwards as the reserved all-but-one value of the offset
     * field itself. ADS-B version 1 carries this in the Capability Class Code instead.
     *
     * @return true if the position offset has been applied, i.e. ME 33–40 read exactly 1
     */
    default boolean hasPositionOffsetApplied() {
        return getMEBits(33, 40) == 1;
    }
}
