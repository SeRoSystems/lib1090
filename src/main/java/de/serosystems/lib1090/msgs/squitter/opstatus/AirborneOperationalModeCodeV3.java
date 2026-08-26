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

/**
 * The Operational Mode (OM) Code of an ADS-B or ADS-R version 3 airborne operational status message,
 * ME 25–40.
 * <p>
 * ME 29 is redefined here: versions 1 and 2 report Receiving ATC Services at that bit, version 3
 * reports Mode S reply rate limiting status. The two are not alternative names for one indication, so
 * this layout offers no ATC-services accessor at all rather than one returning a fabricated value.
 * <p>
 * Subfields: resolution advisory (ME 27), IDENT switch (28), Mode S reply rate limiting (29), single
 * antenna (30), SDA (31–32), CCCB (33–39), remain well clear (40).
 */
public class AirborneOperationalModeCodeV3 extends AbstractMEField
        implements OperationalModeCodeV3 {

    private static final long serialVersionUID = -3355014728106613365L;

    /** ME bit range of the Operational Mode Code. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /** The format selector value that selects this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneOperationalModeCodeV3() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 25–40 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public AirborneOperationalModeCodeV3(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Operational mode code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }

    /**
     * The encoded Collision Avoidance Coordination Capability Bits (CCCB), new in ADS-B version 3.
     * <p>
     * Declared on this layout rather than on a shared interface: the airborne version 3 operational
     * mode code is the only layout that defines ME 33–39.
     *
     * @return the encoded CCCB, ME bits 33–39
     */
    public byte getCollisionAvoidanceCoordinationCapabilityBitsEncoded() {
        return (byte) getMEBits(33, 39);
    }

    /**
     * Whether the Detect and Avoid (DAA) system has commanded the aircraft to remain well clear — the
     * "RWC Active" flag, new in ADS-B version 3.
     *
     * @return true if remain-well-clear is active, ME bit 40
     */
    public boolean hasRemainWellClearActive() {
        return getMEBit(40);
    }
}
