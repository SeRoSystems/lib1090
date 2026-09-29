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

import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModeSDownlinkMsgTest {

    /**
     * A DF=17 whose parity field happens to equal its address is not the valid message: extended squitters carry the
     * address in the payload, so only the same parity field once the CRC is removed makes two of them equal.
     */
    @Test
    void extendedSquitterWithAddressAsParity_isNotEqual() throws Exception {
        ModeSDownlinkMsg valid = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0576098");
        ModeSDownlinkMsg addressAsParity = new ModeSDownlinkMsg("8D4840D6202CC371C32CE04840D6");
        assertNotEquals(valid, addressAsParity);
        assertNotEquals(addressAsParity, valid);

        // the same message with the CRC removed is, if built with noCRC
        ModeSDownlinkMsg crcRemoved = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0000000", true);
        ModeSDownlinkMsg crcRemovedUnflagged = new ModeSDownlinkMsg("8D4840D6202CC371C32CE0000000");
        assertEquals(valid, crcRemoved);
        assertEquals(valid.hashCode(), crcRemoved.hashCode());
        assertNotEquals(valid, crcRemovedUnflagged);
    }

    /**
     * A malformed hex string is a bad format, as the constructors document, rather than an unchecked exception or a
     * message decoded from converted garbage.
     */
    @Test
    void malformedHex_isBadFormat() {
        assertThrows(BadFormatException.class, () -> new ModeSDownlinkMsg("8D4840D6202CC371C32CE057609"));
        assertThrows(BadFormatException.class, () -> new ModeSDownlinkMsg("8D4840D6202CC371C32CE05760ZZ"));
        assertThrows(BadFormatException.class, () -> new ModeSDownlinkMsg("8D4840D6202CC371C32CE05760ZZ", true));
        // Unicode digits other than ASCII hex digits, here fullwidth nine and eight
        assertThrows(BadFormatException.class, () -> new ModeSDownlinkMsg("8D4840D6202CC371C32CE05760\uFF19\uFF18"));
    }

    /**
     * An unknown downlink format is rejected with its number in the reason.
     */
    @Test
    void unknownDownlinkFormat_isBadFormat() {
        BadFormatException e = assertThrows(BadFormatException.class, () -> new ModeSDownlinkMsg("38000000000000"));
        assertTrue(e.getMessage().endsWith("Invalid downlink format 7 detected."), e.getMessage());
    }

    /**
     * A raw message of the wrong length for its downlink format is rejected with both lengths in the reason.
     */
    @Test
    void wrongLength_isBadFormat() {
        BadFormatException e = assertThrows(BadFormatException.class,
                () -> new ModeSDownlinkMsg("2000000000000000000000000000"));
        assertTrue(e.getMessage().endsWith("Downlink format 4 has length 7, but only 14 bytes provided"),
                e.getMessage());
    }

    /**
     * Equal messages hash equally, also where the parity is overlaid with the address and one receiver reports it
     * with the CRC removed: here a DF=20 reply as received, and with its address as the parity field, which is the
     * same reply only if built with noCRC.
     */
    @Test
    void addressParityWithAndWithoutCRC_hashesEqually() throws Exception {
        ModeSDownlinkMsg received = new ModeSDownlinkMsg("A0001838CA3E51F0A8000047A1EA");
        ModeSDownlinkMsg crcRemoved = new ModeSDownlinkMsg("A0001838CA3E51F0A80000EF63CD", true);
        ModeSDownlinkMsg crcRemovedUnflagged = new ModeSDownlinkMsg("A0001838CA3E51F0A80000EF63CD");

        assertEquals(received, crcRemoved);
        assertEquals(crcRemoved, received);
        assertEquals(received.hashCode(), crcRemoved.hashCode());
        assertNotEquals(received, crcRemovedUnflagged);
        assertNotEquals(crcRemovedUnflagged, received);
    }

    /**
     * Two raw replies with the same content from addresses whose XOR is the CRC of that content are different
     * replies: the parity is compared once the CRC is removed, as noCRC says, not in either form.
     */
    @Test
    void repliesFromAddressesDifferingByTheCRC_areNotEqual() throws Exception {
        ModeSDownlinkMsg a = new ModeSDownlinkMsg("2000183859C38D");
        ModeSDownlinkMsg b = new ModeSDownlinkMsg("200018384840D6");
        assertNotEquals(a.getAddress(), b.getAddress());
        assertNotEquals(a, b);
        assertNotEquals(b, a);
    }

}
