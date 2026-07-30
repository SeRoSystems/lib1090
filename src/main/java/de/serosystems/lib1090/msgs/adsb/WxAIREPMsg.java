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

package de.serosystems.lib1090.msgs.adsb;

/**
 * Common API for ADS-B Wx AIREP messages, introduced in ADS-B version 3.
 * <p>
 * {@link WxAIREPAircraftStateMsg}, {@link WxAIREPWeatherStateMsg} and
 * {@link WxAIREPAlternateWeatherStateMsg} share no fields at this level, so this interface
 * currently serves only to mark all three subtypes as members of the Wx AIREP message family.
 */
public interface WxAIREPMsg {

    /**
     * @return the raw encoded message subtype (0 for {@link WxAIREPAircraftStateMsg}, 1 for
     * {@link WxAIREPWeatherStateMsg}, 2 for {@link WxAIREPAlternateWeatherStateMsg})
     */
    byte getMessageSubtype();
}
