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

/**
 * Common API for ADS-B position messages that carry a "TIME" (T) UTC-synchronization flag, ME bit 21.
 * This flag exists in the Airborne and Surface Position Messages of ADS-B versions 0 to 2, ED-102B
 * §N.2.2.4 (version 0), §N.3.2.4 (version 1) and §N.4.2.3 (version 2), for version 2 defined in
 * ED-102A §2.2.3.2.3.5 and §2.2.3.2.4.5. It has been removed from ADS-B version 3, where ME bit 21 is
 * reserved (ED-102B FIGURE 2-4, NOTE 2) and the Report Time of Applicability is always taken to be the
 * time of message receipt.
 */
public interface PositionMsgWithTime extends PositionMsg {

    /**
     * @return flag which will indicate whether the Time of Applicability of the message
     * is synchronized with UTC time. False will denote that the time is not synchronized
     * to UTC. True will denote that Time of Applicability is synchronized to UTC time.
     * ED-102B §N.2.2.4 (version 0), §N.3.2.4 (version 1) and §N.4.2.3 (version 2)
     */
    boolean hasTimeFlag();

}
