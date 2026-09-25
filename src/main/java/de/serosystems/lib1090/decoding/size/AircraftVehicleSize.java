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

/**
 * The dimensions an "Aircraft/Vehicle Length and Width Code" reports, as the table of the
 * transmitting ADS-B version defines them.
 * <p>
 * Codes 0 to 13 mean the same in every version that transmits the field; codes 14 and 15 do not.
 * Version 0 has no such field.
 */
public interface AircraftVehicleSize {

    /**
     * @return the encoded length and width code, 0 to 15
     */
    byte getEncoded();

    /**
     * @return the length the code reports
     */
    Extent getLength();

    /**
     * @return the width the code reports
     */
    Extent getWidth();
}
