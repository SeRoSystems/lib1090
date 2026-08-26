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

/**
 * The Capability Class (CC) Code of an ADS-R version 1 airborne operational status message, ME 9–24.
 * <p>
 * Identical to {@link AirborneCapabilityClassCodeV1} but for ME bit 20, which ADS-R defines as NIC
 * supplement B where ADS-B reserves it. That single bit is the only reason airborne capability class
 * layouts exist per protocol at all; every surface layout and every operational mode layout is shared.
 */
public class ADSRAirborneCapabilityClassCodeV1 extends AbstractMEField
        implements AirborneCapabilityClassCodeV1V2, ADSRAirborneCapabilityClassCode {

    private static final long serialVersionUID = -7134901526628357290L;

    /** ME bit range of this field. */
    public static final int FIRST_ME_BIT = 9;
    public static final int LAST_ME_BIT = 24;

    /** The only format selector value defined for this layout. */
    public static final int FORMAT_SELECTOR = 0;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ADSRAirborneCapabilityClassCodeV1() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 9–24 right-aligned
     * @throws IllegalArgumentException if the value does not fit 16 bits, or if its format selector is
     *                                  not {@link #FORMAT_SELECTOR}
     */
    public ADSRAirborneCapabilityClassCodeV1(int encoded) {
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
     * The bit as transmitted, under the name ADS-B version 1 gives it: <b>"Not-TCAS"</b>. Set means the
     * collision avoidance system is <i>not</i> operational; clear means operational or unknown.
     *
     * @return the Not-TCAS bit as transmitted, ME bit 11
     */
    public boolean getNotTCAS() {
        return getMEBit(11);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Version 1 transmits the negation of this ("Not-TCAS"), so the bit is inverted here.
     */
    @Override
    public boolean isCollisionAvoidanceOperational() {
        return !getNotTCAS();
    }

    /**
     * Whether the Cockpit Display of Traffic Information is operational, under the name ADS-B version 1
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
