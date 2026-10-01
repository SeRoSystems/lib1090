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

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.bds.BDSCode;
import de.serosystems.lib1090.msgs.bds.DataLinkCapabilityReport;
import de.serosystems.lib1090.msgs.bds.DataLinkCapabilityReportV0V5;
import de.serosystems.lib1090.msgs.bds.DataLinkCapabilityReportV6;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CommBReplyTest {

    /**
     * A Comm-B altitude reply (DF=20) or identity reply (DF=21) with the given MB, all other bits zero.
     */
    private static CommBReply reply(int df, String mb) throws Exception {
        String hex = String.format("%02X", df << 3) + "000000" + mb + "000000";
        return df == 20 ? new CommBAltitudeReply(hex) : new CommBIdentifyReply(hex);
    }

    @Test
    void asDataLinkCapabilityReport_picksTheLayout() throws Exception {
        // subnetwork version 1: the DTE sub-address array, here sub-addresses 1 and 5
        DataLinkCapabilityReport v1 = reply(20, "10000000004400").asDataLinkCapabilityReport();
        assertInstanceOf(DataLinkCapabilityReportV0V5.class, v1);
        assertTrue(((DataLinkCapabilityReportV0V5) v1).getDTESubAddressSupport()[5]);

        // subnetwork version 6: the DO-181F and ED-73F layout, here basic dataflash
        DataLinkCapabilityReport v6 = reply(21, "10000C00004000").asDataLinkCapabilityReport();
        assertInstanceOf(DataLinkCapabilityReportV6.class, v6);
        assertTrue(((DataLinkCapabilityReportV6) v6).isBasicDataFlashCapability());
    }

    /**
     * Each method decodes MB as its register, on the reply's own array.
     */
    @Test
    void asRegister_decodesMB() throws Exception {
        CommBReply reply = reply(20, "20000000000000");

        assertEquals(new BDSCode(1, 7), reply.asCommonUsageGICBCapabilityReport().getBDSCode());
        assertEquals(new BDSCode(2, 0), reply.asAircraftIdentification().getBDSCode());
        assertEquals(new BDSCode(3, 0), reply(21, "30000000000000").asACASActiveResolutionAdvisoryReport()
                .getBDSCode());
        assertEquals(new BDSCode(4, 0), reply.asSelectedVerticalIntention().getBDSCode());
        assertEquals(new BDSCode(5, 0), reply.asTrackAndTurn().getBDSCode());
        assertEquals(new BDSCode(6, 0), reply.asHeadingAndSpeed().getBDSCode());

        assertSame(reply.getMessage(), reply.asAircraftIdentification().getMessage());
        assertArrayEquals(Tools.hexStringToByteArray("20000000000000"), reply.getMessage());
    }

    /**
     * BDS 1,0, 2,0 and 3,0 identify themselves in MB bits 1-8; MB that identifies itself otherwise is rejected with a
     * BadFormatException.
     */
    @Test
    void selfIdentifyingRegisters_rejectAnotherCode() throws Exception {
        CommBReply aircraftIdentification = reply(20, "20000000000000");
        assertThrows(BadFormatException.class, aircraftIdentification::asDataLinkCapabilityReport);
        assertThrows(BadFormatException.class, aircraftIdentification::asACASActiveResolutionAdvisoryReport);
        assertThrows(BadFormatException.class, () -> reply(21, "10000000000000").asAircraftIdentification());

        // the registers without a code decode whatever MB holds
        assertDoesNotThrow(aircraftIdentification::asHeadingAndSpeed);
    }

    /**
     * A Comm-B reply whose parity field leaves the given address once the CRC is removed: a reply with data parity
     * leaves the "Modified AA".
     */
    private static CommBReply replyWithAddress(int df, String mb, int address) throws Exception {
        byte[] raw = Tools.hexStringToByteArray(String.format("%02X", df << 3) + "000000" + mb + "000000");
        int parity = ModeSDownlinkMsg.calcParityInt(Arrays.copyOf(raw, 11)) ^ address;
        raw[11] = (byte) (parity >>> 16);
        raw[12] = (byte) (parity >>> 8);
        raw[13] = (byte) parity;
        return df == 20 ? new CommBAltitudeReply(raw) : new CommBIdentifyReply(raw);
    }

    /**
     * ICAO Annex 10 Volume IV §3.1.2.3.2.1.5: with data parity, address AAAAAA and register 5,F give the "Modified AA"
     * F5AAAA, which the reply reports as its address.
     */
    @Test
    void dataParity_annexExample() throws Exception {
        CommBReply reply = replyWithAddress(20, "00000000000000", 0xF5AAAA);

        assertEquals(0xF5AAAA, reply.getAddress().getAddress());
        assertEquals(0xAAAAAA, reply.getAddressAssumingDataParity(new BDSCode(5, 0xF)).getAddress());
        assertEquals(reply.getAddress().getType(),
                reply.getAddressAssumingDataParity(new BDSCode(5, 0xF)).getType());
    }

    /**
     * The register decoded from the reply gives its code: BDS 2,0 turns AAAAAA into 8AAAAA, and back.
     */
    @Test
    void dataParity_withTheDecodedRegister() throws Exception {
        CommBReply reply = replyWithAddress(21, "20000000000000", 0x8AAAAA);

        assertEquals(0xAAAAAA, reply.getAddressAssumingDataParity(reply.asAircraftIdentification()).getAddress());
    }
}
