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
import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;

/**
 * The ME 25–40 field of a message whose ADS-B version defines <b>no Operational Mode Code at all</b> —
 * which is version 0, and only version 0.
 * <p>
 * Distinct from {@link UnknownOperationalModeCode}, and the distinction is worth a class. That one
 * means "there is an Operational Mode Code here, but its format selector selects a layout I do not
 * model"; this one means "there is no Operational Mode Code here at all". Version 0 divides ME 25–40
 * into four 4-bit "En Route Operational Capability Status" fields, every one of them reserved, and
 * since version 0 is no longer maintained that is final.
 * <p>
 * Consequently there is no format selector to report and {@link #getFormatSelector()} returns
 * {@link #NO_FORMAT_SELECTOR} rather than the two bits that would sit at ME 25–26 in a later version.
 * Reporting those would claim a selector value the standard never assigned, and {@code 0} in
 * particular would read as "the defined layout" — the one thing this field certainly is not.
 * <p>
 * {@link #getEncoded()} still gives the raw bits, which a conformant version 0 transmitter sends as
 * zero; a non-zero value indicates a transmitter putting something in a reserved field.
 */
public class UndefinedOperationalModeCode extends AbstractMEField implements OperationalModeCode {

    private static final long serialVersionUID = -4614523090133617418L;

    /** ME bit range of the field, the same as an Operational Mode Code would occupy. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /**
     * Returned by {@link #getFormatSelector()}: no selector exists for this field. Cannot collide with
     * a real selector, which is two bits and so always in 0–3.
     */
    public static final int NO_FORMAT_SELECTOR = -1;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected UndefinedOperationalModeCode() {
    }

    /**
     * @param encoded the reserved field as transmitted, i.e. ME bits 25–40 right-aligned
     */
    public UndefinedOperationalModeCode(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@link #NO_FORMAT_SELECTOR}, this version defining no such field
     */
    @Override
    public int getFormatSelector() {
        return NO_FORMAT_SELECTOR;
    }
}
