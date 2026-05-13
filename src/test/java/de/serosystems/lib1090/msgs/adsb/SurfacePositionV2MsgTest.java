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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfacePositionV2MsgTest {

	// A surface position report observed in the wild, decomposed by single bits
	public static final String SURF_POS =
			"8c" +

			// ICAO 24 bit address
			"3c4dc6" +

			//             00111 => type code 7
			//           0000001 => Movement
			//                 1 => Heading/Ground Track Status
			//           1000000 => Heading/Ground Track
			//                 0 => Time
			//                 1 => CPR format
			// 11001100110001101 => CPR lat
			// 10000001010011110 => CPT lon
			"381c07331b029e" +

			// parity bits (valid, not tested here)
			"b308de";

	@Test
	void testDecodeSurfacePosition() throws BadFormatException, UnspecifiedFormatError {
		final SurfacePositionV2Msg sPos = new SurfacePositionV2Msg(SURF_POS, null);

		assertEquals(2, sPos.getSIL());
		assertTrue(sPos.hasGroundSpeed());
		assertEquals(0, sPos.getGroundSpeed());
		assertEquals(0.125, sPos.getGroundSpeedResolution());
		assertTrue(sPos.hasValidHeading());
		assertEquals(64*360D/128D, sPos.getHeading());
		assertFalse(sPos.hasTimeFlag());
		assertTrue(sPos.hasValidPosition());
		assertEquals(0, sPos.getAltitude());
		assertEquals(Position.AltitudeType.ABOVE_GROUND_LEVEL, sPos.getAltitudeType());
	}

	@Test
	void testGetNIC() throws BadFormatException, UnspecifiedFormatError {
		final SurfacePositionV2Msg sPos = new SurfacePositionV2Msg(SURF_POS, null);

		assertEquals(8, sPos.getNIC());
	}

	@Test
	void testGetNICWithSupplementA() throws BadFormatException, UnspecifiedFormatError {
		final SurfacePositionV2Msg sPos = new SurfacePositionV2Msg(SURF_POS, null);
		sPos.setNICSupplementA(true);

		assertEquals(9, sPos.getNIC());
	}

	@Test
	void testGetHorizontalContainmentRadiusLimit() throws BadFormatException, UnspecifiedFormatError {
		final SurfacePositionV2Msg sPos = new SurfacePositionV2Msg(SURF_POS, null);

		assertEquals(185.2, sPos.getHorizontalContainmentRadiusLimit());
	}

	@Test
	void testGetHorizontalContainmentRadiusLimitWithSupplementA() throws BadFormatException, UnspecifiedFormatError {
		final SurfacePositionV2Msg sPos = new SurfacePositionV2Msg(SURF_POS, null);
		sPos.setNICSupplementA(true);

		assertEquals(75, sPos.getHorizontalContainmentRadiusLimit());
	}

}