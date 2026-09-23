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
 * ADS-B messages, as an aircraft broadcasts them about itself, across all four versions of the
 * standard.
 * <p>
 * A class here decodes one message format at one version, since the versions disagree about what the
 * bits mean rather than merely adding to each other; what the versions share is declared in
 * {@link de.serosystems.lib1090.msgs.squitter} and implemented here. An interface in this package is
 * one that only ADS-B can implement, either because the version it belongs to has no counterpart
 * elsewhere or because the other protocols put a different field in the same place.
 */
package de.serosystems.lib1090.msgs.adsb;
