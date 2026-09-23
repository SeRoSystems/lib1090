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

import de.serosystems.lib1090.decoding.quality.HorizontalVelocityError;
import de.serosystems.lib1090.decoding.quality.SourceIntegrityLevel;
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
     * Navigation Accuracy Category for velocity and Source Integrity Level (see
     * {@link #getNACvEncoded()} and {@link #getSILEncoded()}) are present in this message; the two
     * are mutually exclusive.
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

    /**
     * Source/Surveillance Integrity Level (SIL) according to ED-102B §N.2.3.9 TABLE N-10.
     * <p>
     * The concept of SIL was introduced in ADS-B version 1; for version 0 transmitters a mapping
     * exists, which is what this reports. The values are comparable to those an aircraft supporting
     * ADS-B version 1 or 2 transmits in its operational status message.
     * <p>
     * It shares its bits with the barometric altitude difference, as {@link #hasGeoFlag()} describes,
     * so a message carrying that difference carries no level at all.
     *
     * @return the raw encoded source integrity level, which indicates the probability of exceeding
     * the NIC containment radius, or {@code null} if unavailable
     */
    Byte getSILEncoded();

    /**
     * The probability of exceeding the NIC containment radius that the reported level guarantees,
     * ED-102B §2.2.3.2.7.2.9 TABLE 2-70.
     *
     * @return what the level guarantees, or {@code null} where this message carries no level at all
     * — which is not the same as a level guaranteeing nothing
     */
    default SourceIntegrityLevel getSourceIntegrityLevel() {
        Byte sil = getSILEncoded();
        if (sil == null)
            return null;
        return SourceIntegrityLevel.forSIL(sil);
    }

}
