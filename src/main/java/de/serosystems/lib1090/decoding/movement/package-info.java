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
 * The "Movement" subfield of the surface position message and the ground speed each version's table
 * reports for it.
 * <p>
 * No code states a speed. Each states an interval, and the tables differ in which end of it is
 * included: versions 0 and 1 include the lower end and exclude the upper, versions 2 and 3 the
 * reverse, so that the same code covers a slightly different interval in each. Version 2 and 3 also
 * redefine code 2 and quantize codes 3 to 8 in different steps. Each table is therefore its own
 * implementation of {@code Movement}, and the speed is reported as an {@code Interval} in knots with
 * both of its ends and their bounds.
 */
package de.serosystems.lib1090.decoding.movement;
