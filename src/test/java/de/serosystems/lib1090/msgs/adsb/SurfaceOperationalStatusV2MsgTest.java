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

import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.squitter.SingleAntennaMsg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusV2MsgTest extends SurfaceOperationalStatusMsgTest {

    // Surface operational status message with ADS-B version 2
    public static final String S_OPSTAT_V2 = "8D000000F9000000004000000000";

    @Override
    protected byte[] baseMessage() {
        return Tools.hexStringToByteArray(S_OPSTAT_V2);
    }

    @Override
    protected SurfaceOperationalStatusV2Msg create(byte[] msg) throws Exception {
        return new SurfaceOperationalStatusV2Msg(msg);
    }

    @Test
    public void testDecodeSurfaceOpstat() throws Exception {
        final SurfaceOperationalStatusV2Msg opstat = new SurfaceOperationalStatusV2Msg(S_OPSTAT_V2);
        assertInstanceOf(OperationalStatusV2Msg.class, opstat);
        assertInstanceOf(SingleAntennaMsg.class, opstat);

        assertEquals(2, opstat.getMOPSVersion());
        assertFalse(opstat.has1090ESIn());
        assertFalse(opstat.hasUATIn());
        assertFalse(opstat.hasSingleAntenna());
        assertEquals(0, opstat.getSDAEncoded());
        assertFalse(opstat.hasSILSupplement());
    }

    @Test
    public void testRejectVersion1AsV2() throws Exception {
        byte[] msg = Tools.hexStringToByteArray(S_OPSTAT_V2);
        msg[9] = 0x20;

        assertThrows(BadFormatException.class, () -> new SurfaceOperationalStatusV2Msg(msg));
    }

    @Test
    public void testCapabilityClassCodeVersion2Fields() throws Exception {
        SurfaceOperationalStatusV2Msg uatIn = statusWithCapabilityClassCode(0x10);
        assertTrue(uatIn.hasUATIn());
        assertEquals(0, uatIn.getNACv());
        assertFalse(uatIn.hasNICSupplementC());

        SurfaceOperationalStatusV2Msg nacv = statusWithCapabilityClassCode(0x0A);
        assertFalse(nacv.hasUATIn());
        assertEquals(5, nacv.getNACv());
        assertFalse(nacv.hasNICSupplementC());

        SurfaceOperationalStatusV2Msg nicSupplementC = statusWithCapabilityClassCode(0x01);
        assertFalse(nicSupplementC.hasUATIn());
        assertEquals(0, nicSupplementC.getNACv());
        assertTrue(nicSupplementC.hasNICSupplementC());
    }

    @Test
    public void testOperationalModeCodeVersion2Fields() throws Exception {
        SurfaceOperationalStatusV2Msg singleAntenna = statusWithOperationalModeCode(0x0400);
        assertTrue(singleAntenna.hasSingleAntenna());
        assertEquals(0, singleAntenna.getSDAEncoded());
        assertEquals(0, singleAntenna.getGPSAntennaOffsetEncoded());

        SurfaceOperationalStatusV2Msg systemDesignAssurance = statusWithOperationalModeCode(0x0300);
        assertFalse(systemDesignAssurance.hasSingleAntenna());
        assertEquals(3, systemDesignAssurance.getSDAEncoded());
        assertEquals(0, systemDesignAssurance.getGPSAntennaOffsetEncoded());

        SurfaceOperationalStatusV2Msg gpsAntennaOffset = statusWithOperationalModeCode(0x005A);
        assertFalse(gpsAntennaOffset.hasSingleAntenna());
        assertEquals(0, gpsAntennaOffset.getSDAEncoded());
        assertEquals(0x5A, gpsAntennaOffset.getGPSAntennaOffsetEncoded());
    }

    @Test
    void testHasPositionOffsetApplied() throws Exception {
        assertTrue(statusWithOperationalModeCode(0x0001).hasPositionOffsetApplied());
        assertFalse(statusWithOperationalModeCode(0x0002).hasPositionOffsetApplied());
    }

    @Test
    public void testSILSupplement() throws Exception {
        assertFalse(create(baseMessage()).hasSILSupplement());

        byte[] msg = baseMessage();
        msg[10] |= 0x02;
        assertTrue(new SurfaceOperationalStatusV2Msg(msg).hasSILSupplement());
    }

    private SurfaceOperationalStatusV2Msg statusWithCapabilityClassCode(int capabilityClassCode) throws Exception {
        byte[] msg = baseMessage();
        msg[5] = (byte) (capabilityClassCode >>> 4);
        msg[6] = (byte) ((msg[6] & 0x0F) | ((capabilityClassCode & 0x0F) << 4));
        return new SurfaceOperationalStatusV2Msg(msg);
    }

    private SurfaceOperationalStatusV2Msg statusWithOperationalModeCode(int operationalModeCode) throws Exception {
        byte[] msg = baseMessage();
        msg[7] = (byte) (operationalModeCode >>> 8);
        msg[8] = (byte) operationalModeCode;
        return new SurfaceOperationalStatusV2Msg(msg);
    }
}
