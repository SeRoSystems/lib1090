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
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCodeV2V3;
import de.serosystems.lib1090.msgs.squitter.KnownCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCodeV2V3;
import de.serosystems.lib1090.msgs.squitter.SurfaceCapabilityClassCodeV2V3;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalModeCodeV2V3;
import de.serosystems.lib1090.msgs.SurfaceOperationalStatusMsgTest;
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

        assertEquals(2, opstat.getMOPSVersion());
        assertFalse(((KnownCapabilityClassCode) opstat.getCapabilityClass()).has1090ESIn());
        assertFalse(((CapabilityClassCodeV2V3) opstat.getCapabilityClass()).hasUATIn());
        assertFalse(((OperationalModeCodeV2V3) opstat.getOperationalMode()).hasSingleAntenna());
        assertEquals(0, ((OperationalModeCodeV2V3) opstat.getOperationalMode()).getSDAEncoded());
        assertFalse(opstat.getSILSupplement());
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
        assertTrue(((CapabilityClassCodeV2V3) uatIn.getCapabilityClass()).hasUATIn());
        assertEquals(0, ((SurfaceCapabilityClassCodeV2V3) uatIn.getCapabilityClass()).getNACv());
        assertFalse(((SurfaceCapabilityClassCodeV2V3) uatIn.getCapabilityClass()).getNICSupplementC());

        SurfaceOperationalStatusV2Msg nacv = statusWithCapabilityClassCode(0x0A);
        assertFalse(((CapabilityClassCodeV2V3) nacv.getCapabilityClass()).hasUATIn());
        assertEquals(5, ((SurfaceCapabilityClassCodeV2V3) nacv.getCapabilityClass()).getNACv());
        assertFalse(((SurfaceCapabilityClassCodeV2V3) nacv.getCapabilityClass()).getNICSupplementC());

        SurfaceOperationalStatusV2Msg nicSupplementC = statusWithCapabilityClassCode(0x01);
        assertFalse(((CapabilityClassCodeV2V3) nicSupplementC.getCapabilityClass()).hasUATIn());
        assertEquals(0, ((SurfaceCapabilityClassCodeV2V3) nicSupplementC.getCapabilityClass()).getNACv());
        assertTrue(((SurfaceCapabilityClassCodeV2V3) nicSupplementC.getCapabilityClass()).getNICSupplementC());
    }

    @Test
    public void testOperationalModeCodeVersion2Fields() throws Exception {
        SurfaceOperationalStatusV2Msg singleAntenna = statusWithOperationalModeCode(0x0400);
        assertTrue(((OperationalModeCodeV2V3) singleAntenna.getOperationalMode()).hasSingleAntenna());
        assertEquals(0, ((OperationalModeCodeV2V3) singleAntenna.getOperationalMode()).getSDAEncoded());
        assertEquals(0, ((SurfaceOperationalModeCodeV2V3) singleAntenna.getOperationalMode()).getGPSAntennaOffsetEncoded());

        SurfaceOperationalStatusV2Msg systemDesignAssurance = statusWithOperationalModeCode(0x0300);
        assertFalse(((OperationalModeCodeV2V3) systemDesignAssurance.getOperationalMode()).hasSingleAntenna());
        assertEquals(3, ((OperationalModeCodeV2V3) systemDesignAssurance.getOperationalMode()).getSDAEncoded());
        assertEquals(0, ((SurfaceOperationalModeCodeV2V3) systemDesignAssurance.getOperationalMode()).getGPSAntennaOffsetEncoded());

        SurfaceOperationalStatusV2Msg gpsAntennaOffset = statusWithOperationalModeCode(0x005A);
        assertFalse(((OperationalModeCodeV2V3) gpsAntennaOffset.getOperationalMode()).hasSingleAntenna());
        assertEquals(0, ((OperationalModeCodeV2V3) gpsAntennaOffset.getOperationalMode()).getSDAEncoded());
        assertEquals(0x5A, ((SurfaceOperationalModeCodeV2V3) gpsAntennaOffset.getOperationalMode()).getGPSAntennaOffsetEncoded());
    }

    @Test
    void testHasPositionOffsetApplied() throws Exception {
        assertTrue(((SurfaceOperationalModeCodeV2V3) statusWithOperationalModeCode(0x0001).getOperationalMode()).isPositionOffsetApplied());
        assertFalse(((SurfaceOperationalModeCodeV2V3) statusWithOperationalModeCode(0x0002).getOperationalMode()).isPositionOffsetApplied());
    }

    @Test
    public void testSILSupplement() throws Exception {
        assertFalse(create(baseMessage()).getSILSupplement());

        byte[] msg = baseMessage();
        msg[10] |= 0x02;
        assertTrue(new SurfaceOperationalStatusV2Msg(msg).getSILSupplement());
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
