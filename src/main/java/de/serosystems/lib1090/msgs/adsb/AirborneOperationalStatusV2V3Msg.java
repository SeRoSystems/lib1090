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

/**
 * Common API for ADS-B airborne operational status version 2 and 3 messages.
 */
public interface AirborneOperationalStatusV2V3Msg extends AirborneOperationalStatusMsg {

    /**
     * @return the encoded geometric vertical accuracy (see DO-260B 2.2.3.2.7.2.8)
     */
    byte getGVAEncoded();

    /**
     * @return the geometric vertical accuracy in meters or -1 for "unknown or above 150m"
     */
    default int getGeometricVerticalAccuracy() {
        byte gva = getGVAEncoded();
        if (gva == 1)
            return 150;
        else if (gva == 2)
            return 45;
        else return -1;
    }
}
