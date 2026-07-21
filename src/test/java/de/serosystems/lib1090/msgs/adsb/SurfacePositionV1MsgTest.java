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

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SurfacePositionV1MsgTest extends SurfacePositionMsgTest {

	@Override
	protected SurfacePositionMsg create(String hex) throws Exception {
		return new SurfacePositionV1Msg(hex, Instant.EPOCH);
	}

	@Test
	void testHasTimeFlag() throws Exception {
		final SurfacePositionV1Msg sPos = new SurfacePositionV1Msg(SURF_POS, Instant.EPOCH);

		assertFalse(sPos.hasTimeFlag());
	}

	@Test
	void testGetNICWithSupplementA() throws Exception {
		final SurfacePositionV1Msg sPos = new SurfacePositionV1Msg(SURF_POS, Instant.EPOCH);

		assertEquals(9, sPos.getNIC(true));
	}

	@Test
	void testGetHorizontalContainmentRadiusLimitWithSupplementA() throws Exception {
		final SurfacePositionV1Msg sPos = new SurfacePositionV1Msg(SURF_POS, Instant.EPOCH);

		assertEquals(75, sPos.getHorizontalContainmentRadiusLimit(true));
	}
}
