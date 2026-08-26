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
 * An Operational Mode (OM) Code whose format selector selects a layout this library does not model.
 * <p>
 * The counterpart of {@link UnknownCapabilityClassCode}, and the more likely of the two to be met in
 * practice: ADS-B version 3 is the first standard to define a second layout for this field, so further
 * selector values are the obvious place for the next revision to extend it.
 * <p>
 * Only {@link #getEncoded()} and {@link #getFormatSelector()} are meaningful; the rest of the message
 * decodes normally.
 */
public class UnknownOperationalModeCode extends AbstractMEField implements OperationalModeCode {

    private static final long serialVersionUID = 1290746633870512508L;

    /** ME bit range of the Operational Mode Code, the same in every version. */
    public static final int FIRST_ME_BIT = 25;
    public static final int LAST_ME_BIT = 40;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected UnknownOperationalModeCode() {
    }

    /**
     * @param encoded the field as transmitted, i.e. ME bits 25–40 right-aligned
     */
    public UnknownOperationalModeCode(int encoded) {
        super(encoded, FIRST_ME_BIT, LAST_ME_BIT);
    }
}
