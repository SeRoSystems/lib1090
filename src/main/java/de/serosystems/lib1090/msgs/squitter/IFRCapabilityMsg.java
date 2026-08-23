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
 * standards DO-260 and DO-260A respectively. In current ED-102B, this bit position is a removed,
 * reserved subfield in the main-body message format, ED-102B §2.2.3.2.6.1.4. The flag is retained
 * for backward compatibility in ED-102B Appendix N, where it appears at the same bit position in
 * both Airborne Velocity Message subtype pairs: the Version Zero encoding is ED-102B §N.5.1
 * Figures N-4 (Subtypes 1 &amp; 2) and N-5 (Subtypes 3 &amp; 4), and the Version One encoding is
 * ED-102B §N.5.2 Figures N-11 (Subtypes 1 &amp; 2) and N-12 (Subtypes 3 &amp; 4).
 */
public interface IFRCapabilityMsg {

    /**
     * @return true if the aircraft reports IFR capability
     */
    boolean hasIFRCapability();
}
