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
 * ADS-R messages: ADS-B rebroadcast by a ground station on behalf of an aircraft transmitting on the
 * other link.
 * <p>
 * The formats mirror their ADS-B counterparts closely enough that these classes implement the same
 * interfaces from {@link de.serosystems.lib1090.msgs.squitter}, and differ where the rebroadcast
 * needs something the original did not. The clearest case is ME bit 9 of the airborne velocity
 * message, which carries the intent change flag in ADS-B and the ICAO/Mode A flag here, saying
 * whether the address is an ICAO one — a ground station relaying a target it did not itself
 * interrogate cannot leave that implicit.
 */
package de.serosystems.lib1090.msgs.adsr;
