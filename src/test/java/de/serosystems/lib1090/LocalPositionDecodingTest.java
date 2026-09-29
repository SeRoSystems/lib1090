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

package de.serosystems.lib1090;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalPositionDecodingTest {

    // a reference just west of the antimeridian, for a target just east of it at 10N 179.98W
    private static final Position REFERENCE = new Position(179.99, 10., 0.);

    /**
     * A local decode across the antimeridian returns a longitude within [-180, 180), as the global decode does,
     * and so passes the range check.
     */
    @Test
    public void airborneAcrossAntimeridian_isNormalized() {
        CPREncodedPosition cpr = CPREncodedPosition.ofAirborne(17, false, 87381, 65966, Instant.EPOCH);

        Position local = cpr.decodeLocal(REFERENCE);
        assertEquals(-179.98, local.getLongitude(), 1e-4);
        assertEquals(10., local.getLatitude(), 1e-4);

        Position decoded = cpr.decodePosition(null, REFERENCE);
        assertEquals(-179.98, decoded.getLongitude(), 1e-4);
        assertTrue(decoded.isReasonable());
    }

    /**
     * The same holds for surface positions, which span a quarter of the longitude range per zone.
     */
    @Test
    public void surfaceAcrossAntimeridian_isNormalized() {
        CPREncodedPosition cpr = CPREncodedPosition.ofSurface(17, false, false, 87381, 1718, Instant.EPOCH);

        Position local = cpr.decodeLocal(REFERENCE);
        assertEquals(-179.98, local.getLongitude(), 1e-4);
        assertEquals(10., local.getLatitude(), 1e-4);
        assertTrue(cpr.decodePosition(null, REFERENCE).isReasonable());
    }

}
