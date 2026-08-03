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
 * Common API for ADS-B/ADS-R Wx AIREP messages, introduced in ADS-B version 3.
 * <p>
 * The aircraft state, weather state and alternate weather state subtypes share no fields at this
 * level, so this interface currently serves only to mark all three subtypes as members of the
 * Wx AIREP message family.
 */
public interface WxAIREPMsg {

    /**
     * @return the raw encoded message subtype (0 for the aircraft state subtype, 1 for the
     * weather state subtype, 2 for the alternate weather state subtype)
     */
    byte getMessageSubtype();
}
