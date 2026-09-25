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

/**
 * The "Aircraft/Vehicle Length and Width Code" of ADS-B versions 1 and 2, ED-102B §N.3.3.3 TABLE N-18
 * for version 1 and ED-102A §2.2.3.2.7.2.11 TABLE 2-74 for version 2.
 * <p>
 * Every code but 0 states an upper bound for both dimensions; unlike version 3, the two largest codes
 * do too, at 85 m in length and 80 m or 90 m in width.
 * <p>
 * ED-102B gives no table for version 2: §N.4.3 leaves every Mode Status parameter it does not address
 * to the version 3 decoding, and does not address this one. ED-102A, which defines version 2,
 * tabulates the field exactly as version 1 does, so version 2 shares this table.
 * <p>
 * DO-260A defined code 0 as 15 m long and 11.5 m wide, but ED-102B has a version 1 code 0 reported as
 * "No Data or Unknown", since transmitters following the ICAO SARPs send it with that meaning.
 */
public enum AircraftVehicleSizeV1V2 implements AircraftVehicleSize {

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
    LENGTH_85_WIDTH_80(14, atMost(85), atMost(80)),
    LENGTH_85_WIDTH_90(15, atMost(85), atMost(90));

    /** In declaration order, which is the order of the encoded values. */
    private static final AircraftVehicleSizeV1V2[] VALUES = values();

    private final byte encoded;
    private final Extent length;
    private final Extent width;

    AircraftVehicleSizeV1V2(int encoded, Extent length, Extent width) {
        this.encoded = (byte) encoded;
        this.length = length;
        this.width = width;
    }

    /**
     * @param encoded the encoded length and width code, 0 to 15
     * @return the constant for that code
     * @throws IllegalArgumentException if the value is outside the four bits the field occupies
     */
    public static AircraftVehicleSizeV1V2 forEncoded(byte encoded) {
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
