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
import de.serosystems.lib1090.msgs.SingleAntennaMsg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirborneOperationalStatusV2MsgTest extends AirborneOperationalStatusMsgTest {

    // Airborne operational status message observed in the wild
    public static final String A_OPSTAT_V2 = "8d" +
            // ICAO 24 bit address
            "4d0131" +
            // 11111 => typecode (31)
            //   000 => subtype (1)
            "f8" +
            // 10000100000000 => capability class codes
            "2100" +
            // 00001000000000 => operational mode codes
            "0200" +
            //  010 => MOPS version
            //    0 => NIC suppl A
            // 1001 => NACp
            "49" +
            //        10 => GVA
            //        11 => SIL
            //         1 => NICbaro
            //         0 => HRD
            //         0 => SILsuppl
            //         0 => Reserved
            "b8" +
            // parity (correct, not test here)
            "209514";

    // ME = F8 00 02 00 49 49 00 (subtype 0/airborne, version 2, nacP=9)
    private static final String BASE_MESSAGE = "8D000000F8000200494900000000";

    @Override
    protected byte[] baseMessage() {
        return Tools.hexStringToByteArray(BASE_MESSAGE);
    }

    @Override
    protected AirborneOperationalStatusV2Msg create(byte[] msg) throws Exception {
        return new AirborneOperationalStatusV2Msg(msg);
    }

    @Test
    public void testDecodeAirborneOpstat() throws Exception {
        final AirborneOperationalStatusV2Msg opstat = new AirborneOperationalStatusV2Msg(A_OPSTAT_V2);
        assertInstanceOf(OperationalStatusV2Msg.class, opstat);
        assertInstanceOf(SingleAntennaMsg.class, opstat);

        assertEquals("4d0131", opstat.getAddress().getHexAddress());
        assertEquals(31, opstat.getFormatTypeCode());
        assertFalse(opstat.hasSingleAntenna());

        assertEquals(2, opstat.getVersion());
        assertFalse(opstat.has1090ESIn());
        assertFalse(opstat.hasNICSupplementA());
        assertEquals(9, opstat.getNACpEncoded());

        assertEquals(2, opstat.getGVAEncoded());
        assertEquals(3, opstat.getSILEncoded());
        assertTrue(opstat.getBarometricAltitudeIntegrityCode());
        assertFalse(opstat.getHorizontalReferenceDirection());
    }

    @Test
    public void testRejectVersion1AsV2() throws Exception {
        byte[] msg = Tools.hexStringToByteArray(A_OPSTAT_V2);
        msg[9] = 0x29;

        assertThrows(BadFormatException.class, () -> new AirborneOperationalStatusV2Msg(msg));
    }

    @Test
    void testHasOperationalTCAS() throws Exception {
        // In version 2, the bit means "TCAS operational" directly -- the opposite polarity of version 1.
        assertFalse(create(baseMessage()).hasOperationalTCAS());
        assertTrue(withCapabilityClassCode(0x2000).hasOperationalTCAS());
    }
}
