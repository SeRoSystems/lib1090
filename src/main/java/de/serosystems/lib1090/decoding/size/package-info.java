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

/**
 * The "Aircraft/Vehicle Length and Width Code" of the surface operational status message, and the
 * dimensions each version's table reports for it.
 * <p>
 * The field is four bits, three for a length category and one for a width category within it, but the
 * two are not independent: which width a width bit stands for depends on the length. The tables are
 * therefore keyed on the whole code, and so is every enum here — one constant per code, carrying both
 * dimensions as an {@code Extent}.
 * <p>
 * The dimensions are bounds, not measurements. A transmitter is assigned the smallest code whose upper
 * bounds its actual length and width both fit (ED-102B §2.2.3.2.7.2.11), so a code states that the
 * aircraft or vehicle is at most so long and so wide. Only at the largest codes of version 3 does the
 * table run out of upper bounds, and there the rule is what says what a code guarantees.
 * <p>
 * Version 3 tabulates the two largest codes differently from versions 1 and 2, so each table is its
 * own enum and both implement {@code AircraftVehicleSize}.
 */
package de.serosystems.lib1090.decoding.size;
