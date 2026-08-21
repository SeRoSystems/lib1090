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
 * Common API for messages that expose the Navigation Uncertainty Category for velocity (NUCr).
 * NUCr is not defined in current ED-102B, where it is superseded by NACV, ED-102B
 * §2.2.3.2.6.1.5 TABLE 2-18. It is retained for ADS-B Version Zero (0) backward compatibility
 * per ED-102B §N.2.3.8, which attributes the field's original definition to the
 * former standard DO-260/ED-102 §2.2.3.2.6.1.5.
 */
public interface NUCrMsg {

    /**
     * @return the raw encoded Navigation Uncertainty Category for velocity
     */
    byte getNUCr();
}
