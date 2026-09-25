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
import de.serosystems.lib1090.decoding.movement.Movement;
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
     * <p>
     * The position also records whether the ground speed may exceed 25 knots, which ED-102B
     * §2.2.10.3.2 uses to shorten the window in which an even and an odd surface position message may
     * be paired from 50 to 25 seconds: "unless the Ground Speed in either Surface Position Message is
     * greater than 25 knots, or is unknown". A speed that cannot be shown to be at most 25 knots counts
     * as greater, since the shorter window is the safe one. Versions 2 and 3 decide it exactly; the
     * version 0 and 1 code for [25, 26) knots cannot, and counts as greater.
     *
     * @param br        bit reader positioned over the 7-byte extended squitter payload
     * @param movement  the message's movement, as its version's table defines it
     * @param timestamp timestamp for the position message
     * @return the encoded surface position
     */
    public static CPREncodedPosition extractCPREncodedPosition(BitReader br, Movement movement, Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp");
        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);
        Interval groundSpeed = movement.getGroundSpeed();
        boolean highGroundSpeed = groundSpeed == null || groundSpeed.getGuaranteedUpperBound() > 25;
        return CPREncodedPosition.ofSurface(17, cprFormat, highGroundSpeed, cprEncodedLat, cprEncodedLon, timestamp);
    }
}
