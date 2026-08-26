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
import de.serosystems.lib1090.msgs.squitter.ADSRAirborneCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCodeV1V2;
import de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCodeV2V3;

/**
 * The Capability Class (CC) Code of an ADS-R version 2 airborne operational status message, ME 9–24.
 * <p>
 * Identical to {@link AirborneCapabilityClassCodeV2} but for ME bit 20, which ADS-R defines as NIC
 * supplement B where ADS-B reserves it.
 */
public class ADSRAirborneCapabilityClassCodeV2 extends AbstractMEField
        implements AirborneCapabilityClassCodeV1V2, AirborneCapabilityClassCodeV2V3, ADSRAirborneCapabilityClassCode {

    private static final long serialVersionUID = 8815330472200615534L;

    /** ME bit range of this field. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 24;

    /** The only format selector value defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ADSRAirborneCapabilityClassCodeV2() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 9–24 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public ADSRAirborneCapabilityClassCodeV2(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Capability class code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }
}
