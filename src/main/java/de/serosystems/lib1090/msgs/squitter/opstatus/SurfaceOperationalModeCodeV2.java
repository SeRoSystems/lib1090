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
import de.serosystems.lib1090.msgs.squitter.OperationalModeCodeV1V2;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalModeCodeV2V3;

/**
 * The Operational Mode (OM) Code of an ADS-B or ADS-R version 2 surface operational status message,
 * ME 25–40. The class carries no accessors of its own.
 * <p>
 * Subfields: resolution advisory (ME 27), IDENT switch (28), Receiving ATC Services (29), single
 * antenna (30), SDA (31–32), GPS antenna offset (33–40, from which the lateral and longitudinal
 * offsets and the Position Offset Applied indication are derived).
 */
public class SurfaceOperationalModeCodeV2 extends AbstractMEField
        implements OperationalModeCodeV1V2, SurfaceOperationalModeCodeV2V3 {

    private static final long serialVersionUID = 4471928365501930847L;

    /** ME bit range of the Operational Mode Code. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /** The format selector value that selects this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalModeCodeV2() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 25–40 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public SurfaceOperationalModeCodeV2(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Operational mode code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }
}
