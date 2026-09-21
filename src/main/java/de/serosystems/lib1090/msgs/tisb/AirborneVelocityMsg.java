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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.decoding.HorizontalVelocityError;
import de.serosystems.lib1090.msgs.squitter.IMFMsg;

/**
 * Common API for TIS-B airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMsg extends IMFMsg {

    /**
     * @return the raw encoded Navigation Accuracy Category for velocity, or {@code null} if unavailable
     */
    Byte getNACvEncoded();

    /**
     * The Geo Flag determines whether the geometric minus barometric altitude difference (see
     * {@link de.serosystems.lib1090.msgs.squitter.AirborneVelocityMsg#hasDiffBaroAlt()}) or the
     * Navigation Accuracy Category for velocity and Source Integrity Level (see {@link #getNACvEncoded()})
     * are present in this message; the two are mutually exclusive.
     *
     * @return true if geometric minus barometric altitude difference data is present, false if
     * NACv/SIL data is present instead
     */
    boolean hasGeoFlag();

    /**
     * The 95% horizontal velocity error the reported category guarantees, ED-102B §2.2.3.2.6.1.5
     * TABLE 2-18.
     *
     * @return what the category guarantees, or {@code null} where this message carries no category
     * at all — which is not the same as a category guaranteeing nothing
     */
    default HorizontalVelocityError getHorizontalVelocityError() {
        Byte nacv = getNACvEncoded();
        if (nacv == null)
            return null;
        return HorizontalVelocityError.forNACv(nacv);
    }

}
