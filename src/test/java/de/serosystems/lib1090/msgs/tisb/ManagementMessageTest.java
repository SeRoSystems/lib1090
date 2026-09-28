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

package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ManagementMessageTest {

    /**
     * DF=18 with CF=4 and the given Management Message Bit Field in bits 9-13, the rest clear.
     */
    private static byte[] management(int bitField) {
        return Tools.hexStringToByteArray("94" + String.format("%02X", bitField << 3) + "0000000000000000000000000000".substring(0, 24));
    }

    /**
     * The decoder reaches ManagementMessage, where the downlink message used to throw "TIS-B/ADS-R
     * management frames not implemented" first.
     */
    @Test
    void testDecodes() throws Exception {
        ModeSDownlinkMsg msg = new StatefulModeSDecoder().decode(management(5), Instant.EPOCH);

        assertInstanceOf(ManagementMessage.class, msg);
        assertInstanceOf(ExtendedSquitter.class, msg);
        assertEquals(QualifiedAddress.Type.TISB_MANAGEMENT_INFO, msg.getAddress().getType());
    }

    /**
     * ED-102B TABLE 2-187: each service has a bit of its own.
     */
    @Test
    void testServices() throws Exception {
        ManagementMessage none = new ManagementMessage(management(0));
        assertEquals(0, none.getManagementMessageBitFieldEncoded());
        assertFalse(none.isTISBServiceActive());
        assertFalse(none.isADSRServiceActive());
        assertFalse(none.isADSSLRServiceActive());

        ManagementMessage tisbAndSlr = new ManagementMessage(management(5));
        assertEquals(5, tisbAndSlr.getManagementMessageBitFieldEncoded());
        assertTrue(tisbAndSlr.isTISBServiceActive());
        assertFalse(tisbAndSlr.isADSRServiceActive());
        assertTrue(tisbAndSlr.isADSSLRServiceActive());

        assertTrue(new ManagementMessage(management(2)).isADSRServiceActive());
        assertEquals(31, new ManagementMessage(management(31)).getManagementMessageBitFieldEncoded());
    }

    /**
     * The message carries no ME field, so it has no format type code.
     */
    @Test
    void testTypeCodedRejectsManagementMessages() throws Exception {
        assertThrows(BadFormatException.class, () -> new TypeCodedExtendedSquitter(management(1)));
        new ExtendedSquitter(management(1));
        assertThrows(BadFormatException.class, () -> new ManagementMessage("8DABCDEF00000000000000000000"));
    }

    @Test
    void testToString() throws Exception {
        String s = new ManagementMessage(management(3)).toString();
        assertTrue(s.startsWith("ManagementMessage{ExtendedSquitter{ModeSReply{"), s);
        assertTrue(s.endsWith("}, managementMessageBitFieldEncoded=3}"), s);
    }
}
