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

import de.serosystems.lib1090.msgs.squitter.*;
import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The layout dispatch. The behavior that matters here is the one the refactor exists for: an
 * unrecognized selector must yield a usable object rather than an exception, because the selector
 * governs one field and the rest of the message is still decodable.
 */
class FactoryTest {

    @Test
    public void definedSelectorsYieldTheirLayouts() {
        assertInstanceOf(AirborneCapabilityClassCodeV0.class, CapabilityClassCodes.adsbAirborneV0(0));
        assertInstanceOf(AirborneCapabilityClassCodeV1.class, CapabilityClassCodes.adsbAirborneV1(0));
        assertInstanceOf(ADSRAirborneCapabilityClassCodeV1.class, CapabilityClassCodes.adsrAirborneV1(0));
        assertInstanceOf(SurfaceCapabilityClassCodeV1.class, CapabilityClassCodes.surfaceV1(0));
        assertInstanceOf(AirborneCapabilityClassCodeV2.class, CapabilityClassCodes.adsbAirborneV2(0));
        assertInstanceOf(ADSRAirborneCapabilityClassCodeV2.class, CapabilityClassCodes.adsrAirborneV2(0));
        assertInstanceOf(SurfaceCapabilityClassCodeV2.class, CapabilityClassCodes.surfaceV2(0));
        assertInstanceOf(AirborneCapabilityClassCodeV3.class, CapabilityClassCodes.adsbAirborneV3(0));
        assertInstanceOf(ADSRAirborneCapabilityClassCodeV3.class, CapabilityClassCodes.adsrAirborneV3(0));
        assertInstanceOf(SurfaceCapabilityClassCodeV3.class, CapabilityClassCodes.surfaceV3(0));

        assertInstanceOf(OperationalModeCodeV1.class, OperationalModeCodes.v1(0));
        assertInstanceOf(AirborneOperationalModeCodeV2.class, OperationalModeCodes.airborneV2(0));
        assertInstanceOf(SurfaceOperationalModeCodeV2.class, OperationalModeCodes.surfaceV2(0));
        assertInstanceOf(AirborneOperationalModeCodeV3.class, OperationalModeCodes.airborneV3(0));
        assertInstanceOf(SurfaceOperationalModeCodeV3.class, OperationalModeCodes.surfaceV3(0));
    }

    /** The v3 surface field is the only one with a genuine choice to make. */
    @Test
    public void v3SurfaceDispatchesBetweenItsTwoLayouts() {
        assertInstanceOf(SurfaceOperationalModeCodeV3.class, OperationalModeCodes.surfaceV3(0x0000));
        assertInstanceOf(SurfaceOperationalModeCodeV3Format1.class, OperationalModeCodes.surfaceV3(0x4000));
        assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.surfaceV3(0x8000));
        assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.surfaceV3(0xC000));

        // and the two known layouts read the same bits differently, which is the point
        assertEquals(0xFF, ((SurfaceOperationalModeCodeV3) OperationalModeCodes.surfaceV3(0xFF))
                .getGPSAntennaOffsetEncoded());
        assertEquals(0x1F, ((SurfaceOperationalModeCodeV3Format1) OperationalModeCodes.surfaceV3(0x401F))
                .getTransponderAntennaOffsetEncoded());
    }

    /** No factory method may throw: an unknown layout is data, not a malformed message. */
    @Test
    public void unknownSelectorsFallBackInsteadOfThrowing() {
        IntFunction<?>[] cc = {
                CapabilityClassCodes::adsbAirborneV2, CapabilityClassCodes::adsrAirborneV2,
                CapabilityClassCodes::adsbAirborneV3, CapabilityClassCodes::adsrAirborneV3,
        };
        for (int sel = 1; sel <= 3; sel++)
            for (IntFunction<?> f : cc) {
                Object o = f.apply(sel << 14);
                assertInstanceOf(UnknownCapabilityClassCode.class, o, "selector " + sel);
                assertEquals(sel, ((CapabilityClassCode) o).getFormatSelector());
            }

        for (int sel = 1; sel <= 3; sel++) {
            assertInstanceOf(UnknownCapabilityClassCode.class, CapabilityClassCodes.surfaceV2(sel << 10));
            assertInstanceOf(UnknownCapabilityClassCode.class, CapabilityClassCodes.surfaceV3(sel << 10));
            assertInstanceOf(UnknownCapabilityClassCode.class, CapabilityClassCodes.adsbAirborneV0(sel << 2));
            assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.v1(sel << 14));
            assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.airborneV2(sel << 14));
            assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.surfaceV2(sel << 14));
            assertInstanceOf(UnknownOperationalModeCode.class, OperationalModeCodes.airborneV3(sel << 14));
        }
    }

    /**
     * Version 1 rejects on all four selector bits, so a non-zero ME 13–14 lands in the fallback too —
     * and the fallback must report the full 4-bit value, or the reason would be invisible.
     */
    @Test
    public void v1FallsBackOnEitherHalfOfItsSplitSelector() {
        Object bySecondHalf = CapabilityClassCodes.adsbAirborneV1(0x800);       // ME 13 set
        assertInstanceOf(UnknownCapabilityClassCodeV1.class, bySecondHalf);
        assertEquals(0b0010, ((CapabilityClassCode) bySecondHalf).getFormatSelector());

        Object byFirstHalf = CapabilityClassCodes.adsbAirborneV1(0x8000);       // ME 9 set
        assertInstanceOf(UnknownCapabilityClassCodeV1.class, byFirstHalf);
        assertEquals(0b1000, ((CapabilityClassCode) byFirstHalf).getFormatSelector());

        assertInstanceOf(UnknownCapabilityClassCodeV1.class, CapabilityClassCodes.surfaceV1(0xC0));
        assertEquals(0b0011, ((CapabilityClassCode) CapabilityClassCodes.surfaceV1(0xC0)).getFormatSelector());
        assertInstanceOf(UnknownCapabilityClassCodeV1.class, CapabilityClassCodes.adsrAirborneV1(0x800));
    }

    /** A fallback carries the raw field and its extent, and offers no subfield accessor. */
    @Test
    public void fallbackExposesOnlyRawValueAndSelector() {
        CapabilityClassCode cc = CapabilityClassCodes.adsbAirborneV3(0x8123);
        assertEquals(0x8123, cc.getEncoded());
        assertEquals(9, cc.getFirstMEBit());
        assertEquals(24, cc.getLastMEBit());
        assertEquals(2, cc.getFormatSelector());
        // no layout interface applies, so instanceof finds nothing to cast to
        assertFalse(AirborneCapabilityClassCode.class.isInstance(cc));
        assertFalse(SurfaceCapabilityClassCode.class.isInstance(cc));
        assertFalse(CapabilityClassCodeV2V3.class.isInstance(cc));

        OperationalModeCode om = OperationalModeCodes.airborneV3(0x8000);
        assertEquals(2, om.getFormatSelector());
        assertFalse(OperationalModeCodeV3.class.isInstance(om));
        assertFalse(OperationalModeCodeV1V2.class.isInstance(om));
    }

    /** Surface fallbacks stop at ME 20, so they cannot be read as if they were airborne. */
    @Test
    public void fallbackKeepsItsFieldExtent() {
        CapabilityClassCode surface = CapabilityClassCodes.surfaceV3(0x800);
        assertEquals(20, surface.getLastMEBit());
        assertThrows(IllegalArgumentException.class, () -> surface.getMEBit(21));

        CapabilityClassCode v0 = CapabilityClassCodes.adsbAirborneV0(0x8);
        assertEquals(12, v0.getLastMEBit());
        assertEquals(2, v0.getFormatSelector());
    }
}
