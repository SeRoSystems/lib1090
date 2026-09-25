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

package de.serosystems.lib1090.decoding.size;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AircraftVehicleSizeTest {

    /**
     * Every value the four-bit field can carry names a constant, and the one it names carries that
     * value — which is what lets {@code forEncoded} index the constants by declaration order. Anything
     * else is a caller's mistake.
     */
    @Test
    void testEveryValueOfTheFieldIsDefined() {
        for (byte encoded = 0; encoded <= 15; encoded++) {
            assertEquals(encoded, AircraftVehicleSizeV1V2.forEncoded(encoded).getEncoded(), "v1/v2 " + encoded);
            assertEquals(encoded, AircraftVehicleSizeV3.forEncoded(encoded).getEncoded(), "v3 " + encoded);
        }

        assertThrows(IllegalArgumentException.class, () -> AircraftVehicleSizeV1V2.forEncoded((byte) 16));
        assertThrows(IllegalArgumentException.class, () -> AircraftVehicleSizeV3.forEncoded((byte) -1));
    }

    /** ED-102B TABLE N-18 and TABLE 2-71, which agree on codes 1 to 13, as does ED-102A TABLE 2-74. */
    @Test
    void testUpperBounds() {
        double[] length = {15, 25, 25, 35, 35, 45, 45, 55, 55, 65, 65, 75, 75};
        double[] width = {23, 28.5, 34, 33, 38, 39.5, 45, 45, 52, 59.5, 67, 72.5, 80};

        for (byte encoded = 1; encoded <= 13; encoded++) {
            for (AircraftVehicleSize size : new AircraftVehicleSize[]{
                    AircraftVehicleSizeV1V2.forEncoded(encoded), AircraftVehicleSizeV3.forEncoded(encoded)}) {
                assertEquals(Extent.atMost(length[encoded - 1]), size.getLength(), size + " length");
                assertEquals(Extent.atMost(width[encoded - 1]), size.getWidth(), size + " width");
            }
        }
    }

    /** Code 0 is "No Data or Unknown" in both tables, including for version 1 despite DO-260A. */
    @Test
    void testCode0IsUnknown() {
        assertTrue(AircraftVehicleSizeV1V2.NO_DATA_OR_UNKNOWN.getLength().isUnknown());
        assertTrue(AircraftVehicleSizeV1V2.NO_DATA_OR_UNKNOWN.getWidth().isUnknown());
        assertTrue(AircraftVehicleSizeV3.NO_DATA_OR_UNKNOWN.getLength().isUnknown());
        assertTrue(AircraftVehicleSizeV3.NO_DATA_OR_UNKNOWN.getWidth().isUnknown());
    }

    /** Versions 1 and 2 bound their two largest codes from above, ED-102B TABLE N-18 and ED-102A TABLE 2-74. */
    @Test
    void testVersion1And2LargestCodes() {
        assertSame(AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_80, AircraftVehicleSizeV1V2.forEncoded((byte) 14));
        assertEquals(Extent.atMost(85), AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_80.getLength());
        assertEquals(Extent.atMost(80), AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_80.getWidth());

        assertSame(AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_90, AircraftVehicleSizeV1V2.forEncoded((byte) 15));
        assertEquals(Extent.atMost(85), AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_90.getLength());
        assertEquals(Extent.atMost(90), AircraftVehicleSizeV1V2.LENGTH_85_WIDTH_90.getWidth());
    }

    /**
     * Version 3 runs out of upper bounds at its two largest codes. Code 15 is reached by width
     * alone, since code 14 accepts any length, so it guarantees nothing about the length.
     */
    @Test
    void testVersion3LargestCodes() {
        assertSame(AircraftVehicleSizeV3.LENGTH_ABOVE_75_WIDTH_80, AircraftVehicleSizeV3.forEncoded((byte) 14));
        assertEquals(Extent.moreThan(75), AircraftVehicleSizeV3.LENGTH_ABOVE_75_WIDTH_80.getLength());
        assertEquals(Extent.atMost(80), AircraftVehicleSizeV3.LENGTH_ABOVE_75_WIDTH_80.getWidth());

        assertSame(AircraftVehicleSizeV3.WIDTH_ABOVE_80, AircraftVehicleSizeV3.forEncoded((byte) 15));
        assertTrue(AircraftVehicleSizeV3.WIDTH_ABOVE_80.getLength().isUnknown());
        assertEquals(Extent.moreThan(80), AircraftVehicleSizeV3.WIDTH_ABOVE_80.getWidth());
    }

    /**
     * "Is this aircraft known to fit the limit?" answers false wherever nothing guarantees it, without
     * the caller special-casing a lower bound or an unknown dimension.
     */
    @Test
    void testNothingIsKnownToFitALimitUnlessItIs() {
        double limit = 80;

        assertTrue(Extent.atMost(80).getGuaranteedUpperBound() <= limit);
        assertFalse(Extent.atMost(85).getGuaranteedUpperBound() <= limit);
        assertFalse(Extent.moreThan(75).getGuaranteedUpperBound() <= limit);
        assertFalse(Extent.UNKNOWN.getGuaranteedUpperBound() <= limit);
        assertFalse(Extent.UNKNOWN.getGuaranteedUpperBound() > limit);
    }
}
