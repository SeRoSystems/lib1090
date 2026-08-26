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

package de.serosystems.lib1090.msgs.squitter.opstatus;

import de.serosystems.lib1090.decoding.AbstractMEField;

/**
 * The Capability Class (CC) Code of an ADS-B version 3 airborne operational status message, ME 9–24,
 * format selector {@code 0} — the only selector value ED-102B defines for this field.
 * <p>
 * The class carries no accessors of its own: every subfield of this layout is inherited as a default
 * method from {@code msgs.squitter.AirborneCapabilityClassCodeV3} and its supertypes, keyed on ME bit
 * number. It exists to bind the encoded value to a field extent and to a set of interfaces, which
 * together say which subfields the layout defines.
 * <p>
 * The interface of the same simple name lives in {@code msgs.squitter} and is referenced here by its
 * fully qualified name: a compilation unit cannot import a type named like the class it declares.
 * <p>
 * Subfields, ED-102B §2.2.3.2.7.2.3 TABLE 2-49: CA Operational (ME 11), 1090ES IN (12), ADS-B Receiver
 * Version (13–14), Transponder Side Indication (15–16), Transmit Power (17–18), UAT IN (19),
 * Reduced Capability Equipment (21–22), Detect and Avoid (23–24). ME 20 is reserved; ADS-R defines it
 * as NIC supplement B.
 */
public class AirborneCapabilityClassCodeV3 extends AbstractMEField
        implements de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCodeV3 {

    private static final long serialVersionUID = 3298541730915663251L;

    /** ME bit range of the airborne Capability Class Code. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 24;

    /** The only format selector value defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneCapabilityClassCodeV3() {
    }

    /**
     * @param encoded the Capability Class Code as transmitted, i.e. ME bits 9–24 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR} — selecting the layout is the
     *                                  caller's job, so a mismatch here is a programming error rather
     *                                  than a malformed message
     */
    public AirborneCapabilityClassCodeV3(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Capability class code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }
}
