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

package de.serosystems.lib1090.decoding.size;

import static de.serosystems.lib1090.decoding.size.Extent.atMost;
import static de.serosystems.lib1090.decoding.size.Extent.moreThan;

/**
 * The "Aircraft/Vehicle Length and Width Code" of ADS-B version 3, ED-102B §2.2.3.2.7.2.11 TABLE 2-71.
 * <p>
 * Codes 1 to 13 state an upper bound for both dimensions. The two largest codes run out of upper
 * bounds, and there the assignment rule — the smallest code whose bounds the actual dimensions fit —
 * decides what they guarantee:
 * <ul>
 *     <li>{@link #LENGTH_ABOVE_75_WIDTH_80}, code 14, is more than 75 m long and at most 80 m wide.
 *     Anything up to 75 m long that is at most 80 m wide would have fit a smaller code.</li>
 *     <li>{@link #WIDTH_ABOVE_80}, code 15, is more than 80 m wide, and <b>nothing is guaranteed about
 *     its length</b>, although TABLE 2-71 prints "&gt; 75" there. Code 14 accepts every length, so
 *     code 15 is reached by width alone: an aircraft 73 m long with a 117 m wingspan transmits it.</li>
 * </ul>
 */
public enum AircraftVehicleSizeV3 implements AircraftVehicleSize {

    NO_DATA_OR_UNKNOWN(0, Extent.UNKNOWN, Extent.UNKNOWN),
    LENGTH_15_WIDTH_23(1, atMost(15), atMost(23)),
    LENGTH_25_WIDTH_28_5(2, atMost(25), atMost(28.5)),
    LENGTH_25_WIDTH_34(3, atMost(25), atMost(34)),
    LENGTH_35_WIDTH_33(4, atMost(35), atMost(33)),
    LENGTH_35_WIDTH_38(5, atMost(35), atMost(38)),
    LENGTH_45_WIDTH_39_5(6, atMost(45), atMost(39.5)),
    LENGTH_45_WIDTH_45(7, atMost(45), atMost(45)),
    LENGTH_55_WIDTH_45(8, atMost(55), atMost(45)),
    LENGTH_55_WIDTH_52(9, atMost(55), atMost(52)),
    LENGTH_65_WIDTH_59_5(10, atMost(65), atMost(59.5)),
    LENGTH_65_WIDTH_67(11, atMost(65), atMost(67)),
    LENGTH_75_WIDTH_72_5(12, atMost(75), atMost(72.5)),
    LENGTH_75_WIDTH_80(13, atMost(75), atMost(80)),
    LENGTH_ABOVE_75_WIDTH_80(14, moreThan(75), atMost(80)),
    WIDTH_ABOVE_80(15, Extent.UNKNOWN, moreThan(80));

    /** In declaration order, which is the order of the encoded values. */
    private static final AircraftVehicleSizeV3[] VALUES = values();

    private final byte encoded;
    private final Extent length;
    private final Extent width;

    AircraftVehicleSizeV3(int encoded, Extent length, Extent width) {
        this.encoded = (byte) encoded;
        this.length = length;
        this.width = width;
    }

    /**
     * @param encoded the encoded length and width code, 0 to 15
     * @return the constant for that code
     * @throws IllegalArgumentException if the value is outside the four bits the field occupies
     */
    public static AircraftVehicleSizeV3 forEncoded(byte encoded) {
        if (encoded < 0 || encoded >= VALUES.length)
            throw new IllegalArgumentException(
                    "Length and width code " + encoded + " does not fit the four bits it occupies");

        return VALUES[encoded];
    }

    @Override
    public byte getEncoded() {
        return encoded;
    }

    @Override
    public Extent getLength() {
        return length;
    }

    @Override
    public Extent getWidth() {
        return width;
    }
}
