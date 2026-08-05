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
 * Common API for ADS-B surface operational status version 2 and 3 messages.
 */
public interface SurfaceOperationalStatusV2V3Msg extends SurfaceOperationalStatusMsg {

    /**
     * @return navigation accuracy category for velocity
     */
    byte getNACv();

    /**
     * @return NIC supplement C for use on the surface
     */
    boolean hasNICSupplementC();

    /**
     * @return encoded longitudinal and lateral distance of the GPS Antenna from the NOSE of the aircraft
     * (see Table 2-66 and 2-67, RTCA DO-260B)
     */
    byte getGPSAntennaOffsetEncoded();

    /**
     * Get lateral axis GPS antenna offset.
     * <ul>
     *     <li>values are measured from the longitudinal center line (=roll axis) of the aircraft</li>
     *     <li>values are given in meters</li>
     *     <li>values denote an upper bound</li>
     *     <li>positive values mean "toward left wing tip"</li>
     *     <li>negative values mean "toward right wind tip"</li>
     *     <li>values have a resolution of 2m</li>
     *     <li>values are capped at 6m, i.e. 6 means "or above"</li>
     *     <li>{@code null} means "no data"</li>
     * </ul>
     *
     * @return lateral axis GPS Antenna offset in meters
     * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
     */
    default Integer getLateralAxisGPSAntennaOffset() {
        int offset3 = getGPSAntennaOffsetEncoded() >>> 5;
        int offset = offset3 & 0x3;
        boolean right = (offset3 & 0x4) != 0;
        return !right && offset == 0 ? null :
                2 * (right ? -offset : offset);
    }

    /**
     * Get longitudinal axis GPS antenna offset.
     * <ul>
     *     <li>values are measured from the nose of the aircraft</li>
     *     <li>values are given in meters</li>
     *     <li>values denote an upper bound</li>
     *     <li>values have a resolution of 2m</li>
     *     <li>values are capped at 60m, i.e. 60 means "or above"</li>
     *     <li>{@code null} means "no data"</li>
     * </ul>
     *
     * @return longitudinal axis GPS Antenna offset in meters
     * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
     */
    default Integer getLongitudinalAxisGPSAntennaOffset() {
        int offset = getGPSAntennaOffsetEncoded() & 0x1f;
        return offset == 0 ? null : 2 * (offset - 1);
    }
}
