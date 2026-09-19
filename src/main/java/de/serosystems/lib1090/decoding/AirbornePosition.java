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

public final class AirbornePosition {

    private AirbornePosition() {
    }

    /**
     * Validate whether the given format type code denotes an airborne position message, per the
     * ADS-B message type determination in ED-102B §2.2.3.2.2 TABLE 2-9: type code 0, 9-18 or
     * 20-22 identifies an Airborne Position Message, defined in ED-102B §2.2.3.2.3.
     *
     * @throws BadFormatException if the format type code is not an airborne position type
     */
    public static void validateAirbornePositionFormat(byte formatTypeCode) throws BadFormatException {
        if (!(formatTypeCode == 0 ||
                (formatTypeCode >= 9 && formatTypeCode <= 18) ||
                (formatTypeCode >= 20 && formatTypeCode <= 22))) {
            throw new BadFormatException("Wrong typecode for Airborne Position message");
        }
    }

    /**
     * Extract the CPR-encoded airborne position from the message payload.
     *
     * @param br        bit reader positioned over the 7-byte extended squitter payload
     * @param timestamp timestamp for the position message
     * @return the encoded airborne position
     */
    public static CPREncodedPosition extractCPREncodedPosition(BitReader br, Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp");
        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);
        return CPREncodedPosition.ofAirborne(17, cprFormat, cprEncodedLat, cprEncodedLon, timestamp);
    }

}
