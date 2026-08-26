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
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;

/**
 * A Capability Class (CC) Code whose format selector selects a layout this library does not model.
 * <p>
 * This is what makes an unrecognized selector survivable. The selector governs only ME 9–24 (or 9–20
 * on the surface); the rest of the operational status message — MOPS version, NIC supplement A, NACp,
 * GVA, SIL and the rest — sits outside the field and is positionally fixed, so it stays perfectly
 * decodable. Rejecting the whole message, as the library used to, discarded all of that over bits it
 * merely did not recognize, and took the NIC supplements that later position messages depend on with
 * it.
 * <p>
 * Only {@link #getEncoded()} and {@link #getFormatSelector()} are meaningful here; no subfield accessor
 * is offered, because outside a known layout no bit has a known meaning.
 *
 * @see UnknownCapabilityClassCodeV1 for the version 1 selector, which is four bits wide
 */
public class UnknownCapabilityClassCode extends AbstractMEField implements CapabilityClassCode {

    private static final long serialVersionUID = -9067349120356173925L;

    /** The ME bit at which every capability class field starts, whatever its version or subtype. */
    public static final int FIRST_ME_BIT = 9;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected UnknownCapabilityClassCode() {
    }

    /**
     * @param encoded the field as transmitted, right-aligned
     * @param width   the width of the field in bits: 16 airborne, 12 surface, 4 for version 0
     */
    public UnknownCapabilityClassCode(int encoded, int width) {
        super(encoded, FIRST_ME_BIT, FIRST_ME_BIT + width - 1);
    }
}
