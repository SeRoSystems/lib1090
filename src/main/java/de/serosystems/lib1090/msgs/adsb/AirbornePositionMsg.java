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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.msgs.PositionMsgWithTime;

public interface AirbornePositionMsg extends PositionMsgWithTime {

    /**
     * @return the message's format type code.
     */
    byte getFormatTypeCode();

    /**
     * Navigation accuracy category.
     * <p>
     * In ADS-B version 0 this is derived from the format type code. For version 1+ aircraft,
     * it is typically provided by the corresponding operational status message.
     *
     * @return navigation accuracy category according to DO-260B
     */
    byte getNACp();

    /**
     * Estimated horizontal accuracy derived from NACp.
     *
     * @return the estimated position uncertainty in meters, or -1 for unknown
     */
    double getPositionUncertainty();

    /**
     * Navigation integrity category. The default implementation assumes no supplements,
     * which may not be accurate for ADS-B version 1 and 2 aircraft that set them.
     *
     * @return navigation integrity category according to DO-260B
     */
    byte getNIC();

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. The default
     * implementation assumes no supplements, which may not be accurate for ADS-B version
     * 1 and 2 aircraft that set them.
     *
     * @return horizontal containment radius limit in meters
     */
    double getHorizontalContainmentRadiusLimit();

    /**
     * Source integrity level.
     *
     * @return the source integrity level
     */
    default byte getSIL() {
        byte formatTypeCode = getFormatTypeCode();
        return (byte) (formatTypeCode == 0 || formatTypeCode == 18 || formatTypeCode == 22 ? 0 : 2);
    }

    /**
     * 12-bit altitude field as encoded in the airborne position message.
     *
     * @return encoded altitude field
     */
    short getAltitudeEncoded();

    /**
     * Surveillance status encoded in the airborne position message.
     *
     * @return the surveillance status
     */
    byte getSurveillanceStatusEncoded();

    /**
     * Human-readable description of the surveillance status.
     *
     * @return a human-readable description of the surveillance status
     */
    default String getSurveillanceStatusDescription() {
        String[] desc = {
                "No condition information",
                "Permanent alert (emergency condition)",
                "Temporary alert (change in Mode A identity code other than emergency condition)",
                "SPI condition"
        };

        return desc[getSurveillanceStatusEncoded()];
    }

    /**
     * Reference system used for altitude.
     *
     * @return reference system used for altitude
     */
    @Override
    default Position.AltitudeType getAltitudeType() {
        byte formatTypeCode = getFormatTypeCode();
        if (formatTypeCode == 0 || (formatTypeCode >= 9 && formatTypeCode <= 18))
            return Position.AltitudeType.BAROMETRIC_ALTITUDE;
        else if (formatTypeCode >= 20 && formatTypeCode <= 22)
            return Position.AltitudeType.ABOVE_WGS84_ELLIPSOID;
        else return Position.AltitudeType.UNKNOWN;
    }

    /**
     * Decode the altitude from the raw encoded altitude field.
     *
     * @return decoded altitude in feet, or null if altitude is not available
     */
    @Override
    default Integer getAltitude() {
        if (!hasValidAltitude()) return null;
        return Altitude.decode12BitAltitude(getAltitudeEncoded());
    }

    /**
     * Decode the Q bit from the raw encoded altitude field.
     *
     * @return decoded Q bit, or null if altitude is not available
     */
    default Boolean hasQBit() {
        if (!hasValidAltitude()) return null;
        return Altitude.decode12BitQBit(getAltitudeEncoded());
    }

    /**
     * Whether the message indicates an alert condition.
     *
     * @return true if the surveillance status indicates alert
     */
    default boolean hasAlert() {
        byte surveillanceStatus = getSurveillanceStatusEncoded();
        return surveillanceStatus == 1 || surveillanceStatus == 2;
    }

    /**
     * Whether the message indicates SPI.
     *
     * @return true if the surveillance status indicates SPI
     */
    default boolean hasSPI() {
        return getSurveillanceStatusEncoded() == 3;
    }
}
