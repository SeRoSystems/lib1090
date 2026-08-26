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

/**
 * A version 1 Capability Class (CC) Code whose format selector selects a layout this library does not
 * model.
 * <p>
 * Version 1 needs its own fallback because its selector is four bits wide, ME 9–10 above ME 13–14,
 * rather than the two bits of every other version. Reporting only ME 9–10 would hide the reason a
 * message landed here whenever it was ME 13–14 that was non-zero.
 */
public class UnknownCapabilityClassCodeV1 extends UnknownCapabilityClassCode {

    private static final long serialVersionUID = 4408217205599473194L;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected UnknownCapabilityClassCodeV1() {
    }

    /**
     * @param encoded the field as transmitted, right-aligned
     * @param width   the width of the field in bits: 16 for the airborne subtype, 12 for the surface one
     */
    public UnknownCapabilityClassCodeV1(int encoded, int width) {
        super(encoded, width);
    }

    /**
     * {@inheritDoc}
     *
     * @return the 4-bit version 1 selector, ME 9–10 above ME 13–14
     */
    @Override
    public int getFormatSelector() {
        return getMEBits(9, 10) << 2 | getMEBits(13, 14);
    }
}
