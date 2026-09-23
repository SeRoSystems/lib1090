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
 * What the extended squitter formats have in common, as interfaces the message classes implement.
 * <p>
 * The same message format is broadcast by aircraft as ADS-B, rebroadcast by ground stations as ADS-R
 * and synthesised from other surveillance as TIS-B. Where a field means the same thing in more than
 * one of those, the accessor for it is declared here and the concrete classes in the protocol
 * packages implement it; where a field belongs to one protocol alone, its interface lives with that
 * protocol instead.
 * <p>
 * That rule is worth applying literally, because the same bit can be a different field. ME bit 9 of
 * the airborne velocity message is the intent change flag in ADS-B and the ICAO/Mode A flag in
 * ADS-R, so {@code IMFMsg} is here and {@code IntentChangeMsg} is in the ADS-B package — not because
 * ADS-R happens not to use the latter, but because for ADS-R it would be wrong.
 * <p>
 * Two kinds of interface live here. Most describe a whole message — an airborne position, an
 * operational status of some version. The rest name a single thing a message reports, such as a
 * navigation accuracy category or a source integrity level, and are mixed into whichever messages
 * carry it; those pair the transmitted number with the value it stands for, which
 * {@link de.serosystems.lib1090.decoding.quality} supplies.
 */
package de.serosystems.lib1090.msgs.squitter;
