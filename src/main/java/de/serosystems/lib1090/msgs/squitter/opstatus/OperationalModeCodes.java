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

import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;

/**
 * Picks the Operational Mode (OM) Code layout a message's format selector selects — the counterpart of
 * {@link CapabilityClassCodes}, and the one that actually has a choice to make.
 * <p>
 * Every version before 3 defines a single layout, so those methods only distinguish "the layout" from
 * "not a layout I know". ADS-B version 3 surface messages are the first in the standard to define two,
 * and {@link #surfaceV3(int)} is where that dispatch lives.
 * <p>
 * ADS-B and ADS-R share every operational mode layout, so unlike the capability class factory none of
 * these methods is protocol-specific. Version 0 has no Operational Mode Code at all and so has no
 * method here.
 */
public final class OperationalModeCodes {

    private OperationalModeCodes() {
    }

    /** Version 1, both subtypes: the layouts are identical. */
    public static OperationalModeCode v1(int encoded) {
        return selector(encoded) == OperationalModeCodeV1.FORMAT_SELECTOR
                ? new OperationalModeCodeV1(encoded)
                : new UnknownOperationalModeCode(encoded);
    }

    /** Version 2 airborne. */
    public static OperationalModeCode airborneV2(int encoded) {
        return selector(encoded) == AirborneOperationalModeCodeV2.FORMAT_SELECTOR
                ? new AirborneOperationalModeCodeV2(encoded)
                : new UnknownOperationalModeCode(encoded);
    }

    /** Version 2 surface. */
    public static OperationalModeCode surfaceV2(int encoded) {
        return selector(encoded) == SurfaceOperationalModeCodeV2.FORMAT_SELECTOR
                ? new SurfaceOperationalModeCodeV2(encoded)
                : new UnknownOperationalModeCode(encoded);
    }

    /** Version 3 airborne. */
    public static OperationalModeCode airborneV3(int encoded) {
        return selector(encoded) == AirborneOperationalModeCodeV3.FORMAT_SELECTOR
                ? new AirborneOperationalModeCodeV3(encoded)
                : new UnknownOperationalModeCode(encoded);
    }

    /**
     * Version 3 surface — the only field in the standard with more than one layout to choose between.
     * <p>
     * Selector {@code 0} carries a GPS antenna offset over ME 33–40; selector {@code 1} reserves
     * ME 27–28 and carries a transponder antenna offset over ME 36–40 instead. Selectors {@code 2} and
     * {@code 3} are undefined and yield {@link UnknownOperationalModeCode}.
     */
    public static OperationalModeCode surfaceV3(int encoded) {
        int selector = selector(encoded);
        if (selector == SurfaceOperationalModeCodeV3.FORMAT_SELECTOR)
            return new SurfaceOperationalModeCodeV3(encoded);
        if (selector == SurfaceOperationalModeCodeV3Format1.FORMAT_SELECTOR)
            return new SurfaceOperationalModeCodeV3Format1(encoded);
        return new UnknownOperationalModeCode(encoded);
    }

    /** The 2-bit selector at ME 25–26; the operational mode field always spans ME 25–40. */
    private static int selector(int encoded) {
        return (encoded >>> 14) & 0x3;
    }
}
