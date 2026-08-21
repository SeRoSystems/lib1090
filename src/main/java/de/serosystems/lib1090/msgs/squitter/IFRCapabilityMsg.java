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
 * Common API for messages that expose the IFR capability flag. This flag is "ME" bit 10 of the
 * Version Zero and Version One Airborne Velocity Message, originally defined in the former
 * standards DO-260 and DO-260A respectively. It is not defined in current ED-102B: ED-102B
 * §2.2.3.2.6.1.4 marks this bit position as a removed, reserved subfield in the current main-body
 * message format. The flag is retained for backward compatibility in ED-102B Appendix N: the
 * Version Zero encoding is ED-102B §N.5.1 Figure N-4, and the Version One encoding is ED-102B
 * §N.5.2 Figure N-12.
 */
public interface IFRCapabilityMsg {

    /**
     * @return true if the aircraft reports IFR capability
     */
    boolean hasIFRCapability();
}
