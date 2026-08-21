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

import de.serosystems.lib1090.msgs.squitter.AirborneVelocityMsg;

/**
 * Marker interface for ADS-B version 2 airborne velocity messages. Implemented both by the
 * ground-speed variant (Subtypes 1 and 2, DO-260B §2.2.3.2.6.1/§2.2.3.2.6.2) and by the
 * airspeed-and-heading variant (Subtypes 3 and 4, DO-260B §2.2.3.2.6.3/§2.2.3.2.6.4); the latter
 * pair is reserved and no longer specified in current ED-102B (§2.2.3.2.6.5). Each subtype's
 * Navigation Accuracy Category for Velocity (NAC_V) subfield, DO-260B §2.2.3.2.6.1.5/§2.2.3.2.6.2.5
 * (ground speed) and §2.2.3.2.6.3.5/§2.2.3.2.6.4.5 (airspeed/heading), is grounded by {@link NACvMsg}.
 */
public interface AirborneVelocityV2Msg extends AirborneVelocityMsg, IntentChangeMsg, NACvMsg {
}
