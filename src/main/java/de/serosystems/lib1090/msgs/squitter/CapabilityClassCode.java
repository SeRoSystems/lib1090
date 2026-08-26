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
 * Common API for the Capability Class (CC) Code of an Aircraft Operational Status Message,
 * ED-102B §2.2.3.2.7.2.3.
 * <p>
 * The field spans ME 9–24 for the airborne subtype and ME 9–20 for the surface subtype; ADS-B version 0
 * has no CC Code at all and carries the equivalent two flags in its 4-bit "CC4" field at ME 9–12. All
 * three are addressed here by ME bit number, so a subfield sits at one bit regardless of field width.
 * <p>
 * The layout of everything after the format selector depends on that selector, so an instance of this
 * interface is only ever one particular layout. Test with {@code instanceof} and cast to reach the
 * subfields a layout defines.
 * <p>
 * Only the raw value and the selector live here, because they are the only things readable without
 * knowing the layout. That is what lets an undefined selector still yield a usable object rather than
 * discarding the message — see {@code opstatus.UnknownCapabilityClassCode}.
 */
public interface CapabilityClassCode extends MEField {

    /**
     * The 2-bit CC Code format selector, ED-102B §2.2.3.2.7.2.3. Only {@code 0} is defined for the
     * layouts modelled here.
     * <p>
     * ADS-B version 1 widens the selector: the standard reserves ME 13–14 as well, and since version 1
     * is no longer maintained those four bits are treated as one logical selector of which only
     * {@code 0} is defined. Version 1 layouts therefore override this method.
     *
     * @return the format selector value
     */
    default int getFormatSelector() {
        return getMEBits(9, 10);
    }
}
