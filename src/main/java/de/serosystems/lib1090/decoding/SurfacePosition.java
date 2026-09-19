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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.exceptions.BadFormatException;

import java.time.Instant;
import java.util.Objects;

public final class SurfacePosition {

    private SurfacePosition() {
    }

    /**
     * Validate whether the given format type code denotes a surface position message, per the
     * ADS-B message type determination in ED-102B §2.2.3.2.2 TABLE 2-9: type code 0 or 5-8
     * identifies a Surface Position Message, defined in ED-102B §2.2.3.2.4.
     *
     * @throws BadFormatException if the format type code is not a surface position type
     */
    public static void validateSurfacePositionFormat(byte formatTypeCode) throws BadFormatException {
        if (!(formatTypeCode == 0 || (formatTypeCode >= 5 && formatTypeCode <= 8)))
            throw new BadFormatException("Wrong typecode for Surface Position message");
    }

    /**
     * Extract the CPR-encoded surface position from the message payload.
     *
     * @param br        bit reader positioned over the 7-byte extended squitter payload
     * @param movement  encoded movement field
     * @param timestamp timestamp for the position message
     * @return the encoded surface position
     */
    public static CPREncodedPosition extractCPREncodedPosition(BitReader br, byte movement, Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp");
        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);
        boolean highGroundSpeed = movement == 0 || movement > 49;
        return CPREncodedPosition.ofSurface(17, cprFormat, highGroundSpeed, cprEncodedLat, cprEncodedLon, timestamp);
    }

    /**
     * @return speed resolution (accuracy) in knots or null if ground speed is not available.
     */
    public static Double groundSpeedResolution(byte movement) {
        double resolution;

        if (movement >= 1 && movement <= 8)
            resolution = 0.125;
        else if (movement >= 9 && movement <= 12)
            resolution = 0.25;
        else if (movement >= 13 && movement <= 38)
            resolution = 0.5;
        else if (movement >= 39 && movement <= 93)
            resolution = 1;
        else if (movement >= 94 && movement <= 108)
            resolution = 2;
        else if (movement >= 109 && movement <= 123)
            resolution = 5;
        else if (movement == 124)
            resolution = 175;
        else
            return null;

        return resolution;
    }

    /**
     * @return speed in knots or null if ground speed is not available.
     */
    public static Double groundSpeed(byte movement) {
        double speed;

        if (movement == 1)
            speed = 0;
        else if (movement >= 2 && movement <= 8)
            speed = 0.125 + (movement - 2) * 0.125;
        else if (movement >= 9 && movement <= 12)
            speed = 1 + (movement - 9) * 0.25;
        else if (movement >= 13 && movement <= 38)
            speed = 2 + (movement - 13) * 0.5;
        else if (movement >= 39 && movement <= 93)
            speed = 15 + (movement - 39);
        else if (movement >= 94 && movement <= 108)
            speed = 70 + (movement - 94) * 2;
        else if (movement >= 109 && movement <= 123)
            speed = 100 + (movement - 109) * 5;
        else if (movement == 124)
            speed = 175;
        else
            return null;

        return speed;
    }

    /**
     * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see ED-102B §
     * §N.2.3.7 TABLE N-9 (Type Code to NACP Mapping), "Position Error (95%)" column.
     * <p>
     * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
     * is reflected by this method.
     * Values are comparable to what {@link OperationalStatus#nacPtoEPU(byte)} yields for the NACp an
     * aircraft supporting ADS-B version 1 or later transmits in its operational status message.
     * <ul>
     *     <li>Only upper bounds are reported - if the type code encodes a lower bound, this will return
     *         {@code Double.POSITIVE_INFINITY}.</li>
     *     <li>If the position error is unknown, {@code Double.NaN} is returned.</li>
     * </ul>
     *
     * @return the estimated position uncertainty according to the position NAC in meters
     */
    public static double decodeEPU(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 5:
                return 3;
            case 6:
                return 10;
            case 7:
                return 92.6;
            case 8:
                return Double.POSITIVE_INFINITY;
            default:
                return Double.NaN;
        }
    }

}
