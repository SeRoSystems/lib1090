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
import de.serosystems.lib1090.msgs.squitter.OperationalModeCodeV3;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalModeCodeV2V3;

/**
 * The Operational Mode (OM) Code of an ADS-B or ADS-R version 3 surface operational status message,
 * format selector {@code 0}, ME 25–40. The class carries no accessors of its own.
 * <p>
 * This is the first field in the standard with more than one layout: see
 * {@link SurfaceOperationalModeCodeV3Format1} for the alternative, selected by format {@code 1}.
 * <p>
 * Subfields: resolution advisory (ME 27), IDENT switch (28), Mode S reply rate limiting (29), single
 * antenna (30), SDA (31–32), GPS antenna offset (33–40).
 */
public class SurfaceOperationalModeCodeV3 extends AbstractMEField
        implements OperationalModeCodeV3, SurfaceOperationalModeCodeV2V3 {

    private static final long serialVersionUID = 9020617143388452216L;

    /** ME bit range of the Operational Mode Code. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /** The format selector value that selects this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalModeCodeV3() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 25–40 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public SurfaceOperationalModeCodeV3(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Operational mode code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }
}
