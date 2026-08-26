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
 * Common API for ADS-B operational status messages.
 */
public interface OperationalStatusMsg {

    /**
     * The version number of the formats and protocols in use on the aircraft installation.
     * <ul>
     *     <li>0: Conformant to ED-102 and DO-242</li>
     *     <li>1: Conformant to DO-260A and DO-242A</li>
     *     <li>2: Conformant to ED-102A and DO-242B</li>
     *     <li>3-7: reserved</li>
     * </ul>
     *
     * @return the version number
     */
    byte getMOPSVersion();

    /**
     * @return the Capability Class (CC) Code as transmitted, right-aligned — ME 9–24 for the airborne
     * subtype, ME 9–20 for the surface subtype, and the 4-bit "CC4" field at ME 9–12 in version 0
     */
    int getCapabilityClassCodeEncoded();

    /**
     * The Capability Class Code, decoded according to the layout its format selector selects.
     * <p>
     * Test the result with {@code instanceof} and cast to reach the subfields a layout defines. A
     * selector this library does not model yields an {@code UnknownCapabilityClassCode}, which exposes
     * the raw value and the selector but no subfield — the rest of this message decodes either way.
     *
     * @return the decoded Capability Class Code, never null
     */
    CapabilityClassCode getCapabilityClass();

    /**
     * @return the Operational Mode (OM) Code as transmitted, i.e. ME bits 25–40 right-aligned. ADS-B
     * version 0 reserves the whole field, so there the value is expected to be zero.
     */
    int getOperationalModeCodeEncoded();

    /**
     * The Operational Mode Code, decoded according to the layout its format selector selects.
     * <p>
     * Test the result with {@code instanceof} and cast to reach the subfields a layout defines. Two
     * cases carry no subfields: a selector this library does not model yields an
     * {@code UnknownOperationalModeCode}, and ADS-B version 0 — which defines no Operational Mode Code
     * at all — yields an {@code UndefinedOperationalModeCode}.
     *
     * @return the decoded Operational Mode Code, never null
     */
    OperationalModeCode getOperationalMode();
}
