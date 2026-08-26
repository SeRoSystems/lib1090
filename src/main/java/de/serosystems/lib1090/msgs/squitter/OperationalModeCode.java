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

package de.serosystems.lib1090.msgs.squitter;

import de.serosystems.lib1090.decoding.MEField;

/**
 * Common API for the Operational Mode (OM) Code of an Aircraft Operational Status Message, ED-102B
 * §2.2.3.2.7.2.4. The field always spans ME 25–40.
 * <p>
 * The layout of everything after the format selector depends on that selector, so an instance of this
 * interface is only ever one particular layout. ADS-B version 3 surface messages are the first to
 * define more than one: selector {@code 0} and selector {@code 1} carry different subfields. Test with
 * {@code instanceof} and cast to reach the subfields a layout defines.
 * <p>
 * ADS-B version 0 has no Operational Mode Code; its four "En Route Operational Capability Status"
 * fields are reserved in full.
 * <p>
 * Only the raw value and the selector live here, being the only things readable without knowing the
 * layout, so an undefined selector still yields a usable object — see
 * {@code opstatus.UnknownOperationalModeCode}.
 */
public interface OperationalModeCode extends MEField {

    /**
     * The 2-bit OM Code format selector, ED-102B §2.2.3.2.7.2.4.
     *
     * @return the format selector value, ME bits 25–26
     */
    default int getFormatSelector() {
        return getMEBits(25, 26);
    }

}
