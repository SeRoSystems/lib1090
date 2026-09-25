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

package de.serosystems.lib1090.decoding.movement;

import de.serosystems.lib1090.decoding.Interval;

/**
 * The "Movement" subfield of a surface position message, as the table of the transmitting version
 * defines it.
 */
public interface Movement {

    /**
     * @return the encoded movement, 0 to 127
     */
    byte getEncoded();

    /**
     * @return whether the code reports a ground speed: false for "No Movement Information Available"
     * (0) and for the reserved codes 125 to 127
     */
    default boolean hasGroundSpeed() {
        return getGroundSpeed() != null;
    }

    /**
     * @return the ground speed interval the code reports in knots, or null if it reports none
     */
    Interval getGroundSpeed();
}
