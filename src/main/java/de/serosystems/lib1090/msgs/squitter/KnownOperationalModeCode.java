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
 * An Operational Mode (OM) Code whose format selector selects a layout this library models.
 * <p>
 * {@code instanceof KnownOperationalModeCode} is the test for "was this field decodable?", the
 * counterpart of the {@code opstatus.UnknownOperationalModeCode} a selector outside the standard
 * yields. It carries the two indications every defined layout answers, so a caller who only wants
 * those need not know which version or subtype produced the message.
 */
public interface KnownOperationalModeCode extends OperationalModeCode {

    /**
     * Whether a TCAS/ACAS (Traffic Alert and Collision Avoidance System / Airborne Collision Avoidance
     * System) resolution advisory is active, ED-102B §2.2.3.2.7.2.4.2. Called "CA RA Active"
     * in ADS-B version 3.
     * <p>
     * Every layout but one transmits this at ME bit 27. The ADS-B version 3 surface format {@code 1}
     * layout reserves ME 27–28 and is only sent while neither this nor the IDENT switch is active, so
     * it reports {@code false} — a fact the standard guarantees about that format rather than a value
     * read off the wire.
     *
     * @return true if a resolution advisory is active, ME bit 27
     */
    default boolean isTCASResolutionAdvisoryActive() {
        return getMEBit(27);
    }

    /**
     * Whether the IDENT switch is currently active, ED-102B §2.2.3.2.7.2.4.3.
     *
     * @return true if the IDENT switch is active, ME bit 28
     * @see #isTCASResolutionAdvisoryActive() for the one layout that reports a constant instead
     */
    default boolean isIDENTSwitchActive() {
        return getMEBit(28);
    }
}
