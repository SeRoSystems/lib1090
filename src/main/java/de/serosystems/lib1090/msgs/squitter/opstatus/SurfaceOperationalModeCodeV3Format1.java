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
 * The Operational Mode (OM) Code of an ADS-B or ADS-R version 3 surface operational status message,
 * format selector {@code 1}, ME 25–40 — the second layout defined for this field, and the first case
 * in the standard of one field having two.
 * <p>
 * It differs from {@link SurfaceOperationalModeCodeV3} in reserving ME 27–28 and in carrying a
 * transponder antenna offset over ME 36–40 where format {@code 0} carries a GPS antenna offset over
 * ME 33–40. Because the standard only transmits this format while neither a resolution advisory nor
 * the IDENT switch is active, both of those accessors report a constant {@code false} here.
 */
public class SurfaceOperationalModeCodeV3Format1 extends AbstractMEField
        implements OperationalModeCodeV3 {

    private static final long serialVersionUID = -6293885570741220118L;

    /** ME bit range of the Operational Mode Code. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /** The format selector value that selects this layout. */
    public static final int FORMAT_SELECTOR = 1;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfaceOperationalModeCodeV3Format1() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 25–40 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public SurfaceOperationalModeCodeV3Format1(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Operational mode code format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }

    /**
     * The encoded transponder antenna offset, ME 36–40 — the subfield that distinguishes this layout
     * from format {@code 0}, which carries the GPS antenna offset over ME 33–40 instead.
     *
     * @return the encoded transponder antenna offset, ME bits 36–40
     */
    public byte getTransponderAntennaOffsetEncoded() {
        return (byte) getMEBits(36, 40);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This layout reserves ME 27–28 and is only transmitted while no resolution advisory is active, so
     * {@code false} here states what the standard guarantees about the format rather than reporting a
     * bit. The distinction matters: the value is knowable, unlike a redefined bit whose old meaning is
     * simply unavailable.
     *
     * @return always false
     */
    @Override
    public boolean isTCASResolutionAdvisoryActive() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always false, for the same reason as {@link #isTCASResolutionAdvisoryActive()}.
     *
     * @return always false
     */
    @Override
    public boolean isIDENTSwitchActive() {
        return false;
    }
}
