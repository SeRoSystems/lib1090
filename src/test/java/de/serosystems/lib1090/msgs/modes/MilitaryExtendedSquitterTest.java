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

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MilitaryExtendedSquitterTest {

    /**
     * DF=19 with the given application field, address ABCDEF and an empty ME field.
     */
    private static byte[] df19(int applicationField) {
        return Tools.hexStringToByteArray(String.format("%02X", 0x98 | applicationField) + "ABCDEF" + "00000000000000" + "000000");
    }

    /**
     * Every AF value decodes, where DF=19 with AF other than 0 used to throw "Military frame not
     * implemented" before reaching this class.
     */
    @Test
    void testEveryApplicationFieldDecodes() throws Exception {
        for (int af = 0; af <= 7; af++) {
            ModeSDownlinkMsg msg = new StatefulModeSDecoder().decode(df19(af), Instant.EPOCH);

            assertInstanceOf(MilitaryExtendedSquitter.class, msg, "AF=" + af);
            assertEquals(af, ((MilitaryExtendedSquitter) msg).getApplicationField(), "AF=" + af);
            assertEquals(0xABCDEF, msg.getAddress().getAddress(), "AF=" + af);
            assertEquals(af == 0 ? QualifiedAddress.Type.ICAO24 : QualifiedAddress.Type.RESERVED,
                    msg.getAddress().getType(), "AF=" + af);
        }
    }

    /**
     * Only DF=19 with AF=0 has a format type code.
     */
    @Test
    void testTypeCodedRequiresApplicationFieldZero() throws Exception {
        new TypeCodedExtendedSquitter(df19(0));
        assertThrows(BadFormatException.class, () -> new TypeCodedExtendedSquitter(df19(2)));
        new ExtendedSquitter(df19(2));
    }

    @Test
    void testMessageAndToString() throws Exception {
        byte[] raw = df19(2);
        raw[4] = 0x12;
        raw[10] = 0x34;
        MilitaryExtendedSquitter msg = new MilitaryExtendedSquitter(raw);

        assertArrayEquals(new byte[]{0x12, 0, 0, 0, 0, 0, 0x34}, msg.getMessage());
        assertInstanceOf(ExtendedSquitter.class, msg);
        assertTrue(msg.toString().startsWith("MilitaryExtendedSquitter{ExtendedSquitter{ModeSReply{"));
        assertTrue(msg.toString().endsWith(", message=12000000000034}}"));
    }

    @Test
    void testOnlyDownlinkFormat19() {
        assertThrows(BadFormatException.class,
                () -> new MilitaryExtendedSquitter("8DABCDEF00000000000000000000"));
    }
}
