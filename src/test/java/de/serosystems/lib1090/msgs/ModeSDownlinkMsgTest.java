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

package de.serosystems.lib1090.msgs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModeSDownlinkMsgTest {

    /**
     * A DF=17 whose parity field happens to equal its address is not the valid message: extended squitters carry the
     * address in the payload, so only a parity with or without the CRC subtracted makes two of them equal.
     */
    @Test
    void extendedSquitterWithAddressAsParity_isNotEqual() throws Exception {
        ModeSDownlinkMsg valid = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0576098");
        ModeSDownlinkMsg addressAsParity = new ModeSDownlinkMsg("8D4840D6202CC371C32CE04840D6");
        assertNotEquals(valid, addressAsParity);
        assertNotEquals(addressAsParity, valid);

        // the same message with the CRC subtracted, flagged or not, still is
        ModeSDownlinkMsg subtracted = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0000000", true);
        ModeSDownlinkMsg subtractedUnflagged = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0000000");
        assertEquals(valid, subtracted);
        assertEquals(valid.hashCode(), subtracted.hashCode());
        assertEquals(valid, subtractedUnflagged);
        assertEquals(valid.hashCode(), subtractedUnflagged.hashCode());
    }

    /**
     * Equal messages hash equally, also where the parity is overlaid with the address and one receiver reports it
     * with the CRC subtracted: here a DF=20 reply as received, and with its address as the parity field.
     */
    @Test
    void addressParityWithAndWithoutCRC_hashesEqually() throws Exception {
        ModeSDownlinkMsg received = new ModeSDownlinkMsg("A0001838CA3E51F0A8000047A1EA");
        ModeSDownlinkMsg subtracted = new ModeSDownlinkMsg("A0001838CA3E51F0A80000EF63CD", true);
        ModeSDownlinkMsg subtractedUnflagged = new ModeSDownlinkMsg("A0001838CA3E51F0A80000EF63CD");

        for (ModeSDownlinkMsg other : new ModeSDownlinkMsg[]{subtracted, subtractedUnflagged}) {
            assertEquals(received, other);
            assertEquals(other, received);
            assertEquals(received.hashCode(), other.hashCode());
        }
    }

}
