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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

/**
 * Marker interface implemented by every ADS-R message class, so that callers can determine
 * whether a decoded message originates from the ADS-R decoding path with a single
 * {@code instanceof} check, regardless of message type or version.
 */
public interface ADSRMsg {

    /**
     * Checks that a message is one to be processed as ADS-R: DF=18 with CF=6, ED-102B §2.2.18.3. The ADS-R
     * message classes call it on construction, as the TIS-B classes check their own DF and CF.
     *
     * @param msg the message
     * @throws BadFormatException if the message is not DF=18 with CF=6
     */
    static void checkADSR(ModeSDownlinkMsg msg) throws BadFormatException {
        if (msg.getDownlinkFormat() != 18 || msg.getFirstField() != 6)
            throw new BadFormatException("ADS-R messages must have downlink format 18 and CF value 6");
    }
}
