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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusV1MsgTest extends SurfaceOperationalStatusMsgTest {

    private static final String BASE_MESSAGE = "8D000000F9000000002000000000";

    @Override
    protected byte[] baseMessage() {
        return Tools.hexStringToByteArray(BASE_MESSAGE);
    }

    @Override
    protected SurfaceOperationalStatusV1Msg create(byte[] msg) throws Exception {
        return new SurfaceOperationalStatusV1Msg(msg);
    }

    @Test
    public void testValidVersion1Message() throws Exception {
        SurfaceOperationalStatusV1Msg status = create(baseMessage());
        assertEquals(1, status.getSubtypeCode());
        assertEquals(1, status.getVersion());
    }

    @Test
    public void testRejectVersion0Message() throws Exception {
        byte[] msg = baseMessage();
        msg[9] = 0x00;

        assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(msg));
    }

    @Test
    public void testOperationalModeCodeWithHighByte() throws Exception {
        byte[] msg = Tools.hexStringToByteArray("8D000000F9000080002000000000");
        assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV1Msg(msg));
    }

    @Test
    void testHasPositionOffsetApplied() throws Exception {
        SurfaceOperationalStatusMsg positionOffsetApplied = withCapabilityClassCode(0x200);
        assertTrue(positionOffsetApplied.hasPositionOffsetApplied());
        assertFalse(positionOffsetApplied.has1090ESIn());
        assertFalse(positionOffsetApplied.hasLowTxPower());
    }
}
