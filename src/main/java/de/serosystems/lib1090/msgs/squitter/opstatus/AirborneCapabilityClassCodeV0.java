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
import de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCode;

/**
 * The capability class field of an ADS-B version 0 operational status message: the 4-bit field the
 * standard calls <b>CC4</b>, ME 9–12, format selector {@code 0}.
 * <p>
 * Version 0 does not have a Capability Class Code in the later sense. It splits ME 9–40 into eight
 * 4-bit fields — CC4 (ME 9–12), CC3 (13–16), CC2 (17–20), CC1 (21–24) and four "En Route Operational
 * Capability Status" fields (OM4 at ME 25–28 through OM1 at 37–40) — of which only CC4 is ever
 * defined. The other seven are reserved, and since version 0 is no longer maintained, permanently so.
 * CC4 leaves room for exactly the two flags below after its format selector.
 * <p>
 * This is the poorest of the capability class layouts and the only one narrower than 12 bits, which is
 * why the subfields are addressed by ME bit rather than by mask: {@code has1090ESIn()} is ME bit 12
 * here as everywhere else, even though the mask that reaches it is {@code 0x1} rather than the
 * {@code 0x1000} of an airborne version 3 message.
 * <p>
 * Both flags carry the names version 0 used for them, with the modern names delegating:
 * "Not-TCAS" ({@link #getNotTCAS()}) and "CDTI Traffic Display" ({@link #hasOperationalCDTI()}).
 */
public class AirborneCapabilityClassCodeV0 extends AbstractMEField
        implements AirborneCapabilityClassCode {

    private static final long serialVersionUID = -5471030982155841177L;

    /** ME bit range of the version 0 "CC4" field. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 12;

    /** The only format selector value ever defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AirborneCapabilityClassCodeV0() {
    }

    /**
     * @param encoded the CC4 field as transmitted, i.e. ME bits 9–12 right-aligned
     * @throws IllegalArgumentException if the value does not fit 4 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public AirborneCapabilityClassCodeV0(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
        if (getFormatSelector() != FORMAT_SELECTOR)
            throw new IllegalArgumentException(
                    "Enroute capabilities format selector " + getFormatSelector()
                            + " is not layout " + FORMAT_SELECTOR);
    }

    /**
     * The bit as transmitted, under the name version 0 gives it: <b>"Not-TCAS"</b>. Set means the
     * collision avoidance system is <i>not</i> operational; clear means operational or unknown.
     * <p>
     * This is the more honest of the field's two names — {@link #isCollisionAvoidanceOperational()},
     * which negates it, cannot distinguish "operational" from "unknown".
     *
     * @return the Not-TCAS bit as transmitted, ME bit 11
     */
    public boolean getNotTCAS() {
        return getMEBit(11);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Version 0 transmits the negation of this ("Not-TCAS"), so the bit is inverted here.
     */
    @Override
    public boolean isCollisionAvoidanceOperational() {
        return !getNotTCAS();
    }

    /**
     * Whether the Cockpit Display of Traffic Information is operational, under the name version 0 gives
     * the subfield: <b>"CDTI Traffic Display"</b>. Renamed to "1090ES IN" from version 1 onwards, which
     * is what {@link #has1090ESIn()} reports.
     *
     * @return true if CDTI is operational or its state is unknown, ME bit 12
     */
    public boolean hasOperationalCDTI() {
        return getMEBit(12);
    }

    /**
     * {@inheritDoc}
     *
     * @see #hasOperationalCDTI() the name version 0 uses for this subfield
     */
    @Override
    public boolean has1090ESIn() {
        return hasOperationalCDTI();
    }
}
