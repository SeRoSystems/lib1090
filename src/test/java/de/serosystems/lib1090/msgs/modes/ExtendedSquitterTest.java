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
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.tisb.CoarsePositionMsg;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedSquitterTest {

    /**
     * DF=18 with CF=3, the TIS-B coarse position, whose ME field has no format type code.
     */
    private static final String COARSE_POSITION = "93485020" + "80000000000000" + "000000";

    @Test
    void testOnlyDownlinkFormats17To19() throws Exception {
        new ExtendedSquitter("8D485020994409940838175B284F");
        new ExtendedSquitter(COARSE_POSITION);
        assertThrows(BadFormatException.class, () -> new ExtendedSquitter("5D485020994409"));
        assertThrows(BadFormatException.class, () -> new ExtendedSquitter("A0001838CA3E51F0A8000047A1EA"));
    }

    @Test
    void testMessage() throws Exception {
        assertArrayEquals(new byte[]{(byte) 0x99, 0x44, 0x09, (byte) 0x94, 0x08, 0x38, 0x17},
                new ExtendedSquitter("8D485020994409940838175B284F").getMessage());
    }

    /**
     * Only messages whose ME field starts with a format type code are type coded.
     */
    @Test
    void testTypeCodedRejectsCoarsePosition() {
        assertThrows(BadFormatException.class, () -> new TypeCodedExtendedSquitter(COARSE_POSITION));
    }

    @Test
    void testCoarsePositionIsNotTypeCoded() throws Exception {
        ModeSDownlinkMsg compatible = StatefulModeSDecoder.builder().tisbV2CompatibilityMode(true).build()
                .decode(COARSE_POSITION, Instant.EPOCH);
        assertInstanceOf(CoarsePositionMsg.class, compatible);
        assertFalse(compatible instanceof TypeCodedExtendedSquitter);

        ModeSDownlinkMsg strict = StatefulModeSDecoder.builder().tisbV2CompatibilityMode(false).build()
                .decode(COARSE_POSITION, Instant.EPOCH);
        assertEquals(ExtendedSquitter.class, strict.getClass());
    }
}
