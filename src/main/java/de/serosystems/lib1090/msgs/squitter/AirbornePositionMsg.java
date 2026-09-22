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

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.decoding.NICSupplements;
import de.serosystems.lib1090.decoding.NavigationCharacteristics;

/**
 * Common API for ADS-B airborne position messages.
 */
public interface AirbornePositionMsg extends PositionMsg, NavigationCharacteristics {

    /**
     * The characteristics this message reports for what is known of its target's NIC supplements.
     * <p>
     * Each ADS-B version reads the supplements its table is keyed on and ignores the rest — version 0
     * reads none of them, the format type code settling everything there — so a caller passes what it
     * knows without needing to know which version will read what. Where the message carries a
     * supplement itself, that value wins over the one supplied.
     * <p>
     * The no-argument accessors inherited from {@link NavigationCharacteristics} answer with what the
     * message knew when it was decoded. This is how a caller re-reads it against newer knowledge,
     * which is a deliberate act rather than something the message does on its own.
     *
     * @param nicSupplements what is known of the target's NIC supplements
     * @return the navigation characteristics that knowledge selects
     */
    NavigationCharacteristics getNavigationCharacteristics(NICSupplements nicSupplements);

    @Override
    byte getNICEncoded();

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
     * @return decoded altitude in feet, or null if altitude is not available or has an invalid encoding
     */
    @Override
    default Integer getAltitude() {
        if (!hasValidAltitude()) return null;
        return Altitude.decode12BitAltitude(getAltitudeEncoded());
    }

    @Override
    default boolean hasValidAltitude() {
        return Altitude.valid12BitAltitude(getAltitudeEncoded());
    }

    /**
     * Decode the Q bit from the raw encoded altitude field.
     *
     * @return decoded Q bit, or null if altitude is not available
     */
    default Boolean hasQBit() {
        if (getAltitudeEncoded() == 0) return null;
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
