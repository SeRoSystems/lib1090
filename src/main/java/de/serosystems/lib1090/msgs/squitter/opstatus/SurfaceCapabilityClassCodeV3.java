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
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCodeV3;
import de.serosystems.lib1090.msgs.squitter.SurfaceCapabilityClassCodeV2V3;

/**
 * The Capability Class (CC) Code of a version 3 surface operational status message, ME 9–20.
 * <p>
 * Shared by ADS-B and ADS-R. The class carries no accessors of its own.
 * <p>
 * Subfields: 1090ES IN (ME 12), ADS-B Receiver Version (13–14, new in version 3), B2 Low (15), UAT IN
 * (16), NACv (17–19), NIC supplement C (20). ME 11 is reserved.
 */
public class SurfaceCapabilityClassCodeV3 extends AbstractMEField
        implements SurfaceCapabilityClassCodeV2V3, CapabilityClassCodeV3 {

    private static final long serialVersionUID = -2286601495031507948L;

    /** ME bit range of this field. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 20;

    /** The only format selector value defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceCapabilityClassCodeV3() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 9–20 right-aligned
     * @throws IllegalArgumentException if the value does not fit 12 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public SurfaceCapabilityClassCodeV3(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Capability class code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }
}
