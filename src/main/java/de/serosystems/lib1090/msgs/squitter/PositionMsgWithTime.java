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
 * Common API for ADS-B position messages that carry a "TIME" (T) UTC-synchronization flag. This
 * flag only exists in ADS-B version 0 and version 1 Airborne and Surface Position Messages,
 * ED-102B §N.2.2.4 (version 0) resp. §N.3.2.4 (version 1); it has been removed from
 * ADS-B version 2/3 messages, where the Report Time of Applicability is instead always taken to
 * be the time of message receipt.
 */
public interface PositionMsgWithTime extends PositionMsg {

    /**
     * @return flag which will indicate whether the Time of Applicability of the message
     * is synchronized with UTC time. False will denote that the time is not synchronized
     * to UTC. True will denote that Time of Applicability is synchronized to UTC time.
     * ED-102B §N.2.2.4 (version 0) resp. §N.3.2.4 (version 1)
     */
    boolean hasTimeFlag();

}
