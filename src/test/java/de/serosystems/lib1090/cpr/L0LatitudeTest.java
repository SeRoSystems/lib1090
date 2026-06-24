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

package de.serosystems.lib1090.cpr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for {@link L0Latitude#NL()}, in particular the polar
 * transition latitude (87°) which lands exactly on a lattice point.
 */
public class L0LatitudeTest {

    /**
     * Per DO-260B / NASA reference: NL == 1 for |lat| >= 87°. Because 87.0°
     * is an exact lattice point and the topmost transition latitude, the top
     * boundary must be exclusive for the NL=2 zone. This is the regression
     * for the off-by-one that returned NL=2 exactly at 87°.
     */
    @Test
    public void nlAtPolarBoundaryIsOne() {
        assertEquals(1, L0Latitude.ofDegrees(87.0).NL(), "NL at exactly 87.0° must be 1");
        assertEquals(1, L0Latitude.ofDegrees(-87.0).NL(), "NL at exactly -87.0° must be 1");
        assertEquals(1, L0Latitude.ofDegrees(88.5).NL(), "NL above 87° must be 1");
        assertEquals(1, L0Latitude.ofDegrees(90.0).NL(), "NL at the pole must be 1");
    }

    /**
     * Just below the polar boundary NL must still be 2 (the boundary is
     * exclusive only at the top edge, the zone below is unaffected).
     */
    @Test
    public void nlJustBelowPolarBoundaryIsTwo() {
        assertEquals(2, L0Latitude.ofDegrees(86.999).NL(), "NL just below 87° must be 2");
        assertEquals(2, L0Latitude.ofDegrees(86.6).NL(), "NL inside the NL=2 zone must be 2");
    }

    /**
     * Anchor points of the NL step function at and near the equator.
     */
    @Test
    public void nlAtEquatorIs59() {
        assertEquals(59, L0Latitude.ofDegrees(0.0).NL(), "NL at the equator must be 59");
        assertEquals(59, L0Latitude.ofDegrees(10.0).NL(), "NL at 10° must be 59");
    }
}
