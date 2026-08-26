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

import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;

/**
 * Picks the Capability Class (CC) Code layout a message's format selector selects.
 * <p>
 * This is the one place that has to be kept in step with the standard when a new layout appears, so it
 * is deliberately explicit: one method per (protocol, subtype, version), each naming the selector values
 * it knows. Message classes call the method matching what they are, since a message class already fixes
 * protocol, subtype and version at compile time — only the selector is a runtime choice.
 * <p>
 * <b>No method throws on an unrecognized selector.</b> Each returns an
 * {@link UnknownCapabilityClassCode} instead, so the rest of the message still decodes. The selector
 * governs only ME 9–24 (or 9–20 on the surface); everything the decoder needs from the message body
 * lies outside it.
 */
public final class CapabilityClassCodes {

    private CapabilityClassCodes() {
    }

    /** ADS-B version 0, the 4-bit "CC4" field at ME 9–12. */
    public static CapabilityClassCode adsbAirborneV0(int encoded) {
        return selector(encoded, 4) == AirborneCapabilityClassCodeV0.FORMAT_SELECTOR
                ? new AirborneCapabilityClassCodeV0(encoded)
                : new UnknownCapabilityClassCode(encoded, 4);
    }

    /** ADS-B version 1 airborne, ME 9–24, whose selector spans ME 9–10 and ME 13–14. */
    public static CapabilityClassCode adsbAirborneV1(int encoded) {
        return selectorV1(encoded, 16) == AirborneCapabilityClassCodeV1.FORMAT_SELECTOR
                ? new AirborneCapabilityClassCodeV1(encoded)
                : new UnknownCapabilityClassCodeV1(encoded, 16);
    }

    /** ADS-R version 1 airborne, ME 9–24. */
    public static CapabilityClassCode adsrAirborneV1(int encoded) {
        return selectorV1(encoded, 16) == ADSRAirborneCapabilityClassCodeV1.FORMAT_SELECTOR
                ? new ADSRAirborneCapabilityClassCodeV1(encoded)
                : new UnknownCapabilityClassCodeV1(encoded, 16);
    }

    /** Version 1 surface, ME 9–20; ADS-B and ADS-R share the layout. */
    public static CapabilityClassCode surfaceV1(int encoded) {
        return selectorV1(encoded, 12) == SurfaceCapabilityClassCodeV1.FORMAT_SELECTOR
                ? new SurfaceCapabilityClassCodeV1(encoded)
                : new UnknownCapabilityClassCodeV1(encoded, 12);
    }

    /** ADS-B version 2 airborne, ME 9–24. */
    public static CapabilityClassCode adsbAirborneV2(int encoded) {
        return selector(encoded, 16) == AirborneCapabilityClassCodeV2.FORMAT_SELECTOR
                ? new AirborneCapabilityClassCodeV2(encoded)
                : new UnknownCapabilityClassCode(encoded, 16);
    }

    /** ADS-R version 2 airborne, ME 9–24. */
    public static CapabilityClassCode adsrAirborneV2(int encoded) {
        return selector(encoded, 16) == ADSRAirborneCapabilityClassCodeV2.FORMAT_SELECTOR
                ? new ADSRAirborneCapabilityClassCodeV2(encoded)
                : new UnknownCapabilityClassCode(encoded, 16);
    }

    /** Version 2 surface, ME 9–20; ADS-B and ADS-R share the layout. */
    public static CapabilityClassCode surfaceV2(int encoded) {
        return selector(encoded, 12) == SurfaceCapabilityClassCodeV2.FORMAT_SELECTOR
                ? new SurfaceCapabilityClassCodeV2(encoded)
                : new UnknownCapabilityClassCode(encoded, 12);
    }

    /** ADS-B version 3 airborne, ME 9–24. */
    public static CapabilityClassCode adsbAirborneV3(int encoded) {
        return selector(encoded, 16) == AirborneCapabilityClassCodeV3.FORMAT_SELECTOR
                ? new AirborneCapabilityClassCodeV3(encoded)
                : new UnknownCapabilityClassCode(encoded, 16);
    }

    /** ADS-R version 3 airborne, ME 9–24. */
    public static CapabilityClassCode adsrAirborneV3(int encoded) {
        return selector(encoded, 16) == ADSRAirborneCapabilityClassCodeV3.FORMAT_SELECTOR
                ? new ADSRAirborneCapabilityClassCodeV3(encoded)
                : new UnknownCapabilityClassCode(encoded, 16);
    }

    /** Version 3 surface, ME 9–20; ADS-B and ADS-R share the layout. */
    public static CapabilityClassCode surfaceV3(int encoded) {
        return selector(encoded, 12) == SurfaceCapabilityClassCodeV3.FORMAT_SELECTOR
                ? new SurfaceCapabilityClassCodeV3(encoded)
                : new UnknownCapabilityClassCode(encoded, 12);
    }

    /**
     * The 2-bit selector at ME 9–10, which every layout starts with and which is therefore the top of
     * the field, whatever its width.
     *
     * @param width the width of the whole field in bits
     */
    private static int selector(int encoded, int width) {
        return (encoded >>> (width - 2)) & 0x3;
    }

    /**
     * The version 1 selector: ME 9–10 and ME 13–14, treated as one 4-bit value.
     * This is a shortcut to comply with the current design: V1 carries several classes which could,
     * in theory, be different. Since only 2 classes have been used and their selectors are always 0,
     * and we do not expect to have more classes in the future (V1 is deprecated), we just extract
     * all 4 bits of both selectors.
     * ME 13–14 are the fifth and sixth bits of the field, hence six from its bottom end.
     *
     * @param width the width of the whole field in bits
     */
    private static int selectorV1(int encoded, int width) {
        return selector(encoded, width) << 2 | (encoded >>> (width - 6)) & 0x3;
    }
}
