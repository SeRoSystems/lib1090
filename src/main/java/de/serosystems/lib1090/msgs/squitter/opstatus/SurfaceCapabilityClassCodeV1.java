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
import de.serosystems.lib1090.msgs.squitter.SurfaceCapabilityClassCode;

/**
 * The Capability Class (CC) Code of a version 1 surface operational status message, ME 9–20.
 * <p>
 * Shared by ADS-B and ADS-R: the two carry byte-for-byte the same surface layout.
 * <p>
 * Subfields: Position Offset Applied (ME 11), CDTI Traffic Display (12), B2 Low (15). ME 16–20 are
 * reserved, and carry UAT IN, NACv and NIC supplement C from version 2 onwards.
 */
public class SurfaceCapabilityClassCodeV1 extends AbstractMEField
        implements SurfaceCapabilityClassCode {

    private static final long serialVersionUID = 2947158013369104337L;

    /** ME bit range of this field. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 20;

    /** The only format selector value defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceCapabilityClassCodeV1() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 9–20 right-aligned
     * @throws IllegalArgumentException if the value does not fit 12 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public SurfaceCapabilityClassCodeV1(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Capability class code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }

    /**
     * {@inheritDoc}
     * <p>
     * ADS-B version 1 reserves ME 13–14 in addition to the selector proper, and since version 1 is no
     * longer maintained no combination other than all-zero can ever acquire a meaning. The two halves
     * are therefore treated as one logical 4-bit selector, ME 9–10 above ME 13–14.
     *
     * @return the 4-bit format selector, ME 9–10 and ME 13–14
     */
    @Override
    public int getFormatSelector() {
        return getMEBits(9, 10) << 2 | getMEBits(13, 14);
    }

    /**
     * Whether the reported position has already been corrected for the GPS antenna offset — the "POA"
     * bit, ED-102B §2.2.3.2.7.2.4.7.
     * <p>
     * ADS-B version 1 is the only version carrying this in the Capability Class Code. From version 2
     * onwards it is derived from the GPS antenna offset subfield of the Operational Mode Code instead,
     * so it is declared on this layout rather than on a shared interface.
     *
     * @return true if the position offset has been applied, ME bit 11
     */
    public boolean hasPositionOffsetApplied() {
        return getMEBit(11);
    }

    /**
     * Whether the CDTI (Cockpit Display of Traffic Information) is operational, under the name ADS-B version 1
     * gives the subfield: <b>"CDTI Traffic Display"</b>. Renamed to "1090ES IN" from version 2 onwards,
     * which is what {@link #has1090ESIn()} reports.
     *
     * @return true if CDTI is operational or its state is unknown, ME bit 12
     */
    public boolean hasOperationalCDTI() {
        return getMEBit(12);
    }

    /**
     * {@inheritDoc}
     *
     * @see #hasOperationalCDTI() the name ADS-B version 1 uses for this subfield
     */
    @Override
    public boolean has1090ESIn() {
        return hasOperationalCDTI();
    }
}
