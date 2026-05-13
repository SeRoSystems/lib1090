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

package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

public interface PositionDecoder {
	/**
	 * Decodes position with speed estimation-based reasonableness test.
	 *
	 * @param cpr      CPR encoded position
	 * @param receiver position of the receiver for surface decoding and to check if received position was more than 700km away;
	 *                 null disables checks and surface decoding
	 * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in feet. altitude might be null
	 * if unavailable. On error, the returned position is null. Check the .isReasonable() flag before using
	 * the position.
	 */
	Position decodePosition(CPREncodedPosition cpr, Position receiver);
}
