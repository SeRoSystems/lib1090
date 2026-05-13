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

package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.cpr.CPREncodedPosition;

public interface PositionMsg {

    /**
     * @return true if this message has a valid CPR encoded position
     */
    boolean hasValidPosition();

    /**
     * @return the CPR encoded position that was announced in this message.
     * The method may return an invalid position if #hasValidPosition() is false.
     */
    CPREncodedPosition getCPREncodedPosition();

    /**
     * @return whether altitude information is available
     */
    boolean hasValidAltitude();

    /**
     * @return the decoded altitude in feet or null if altitude is not available. The latter can be checked with
     * {@link #hasValidAltitude()}. Also see {@link #getAltitudeType()}.
     */
    Integer getAltitude();

    /**
     * @return reference system used for altitude
     */
    Position.AltitudeType getAltitudeType();

}
