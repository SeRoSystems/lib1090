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

package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftIdentificationTest {

    private static final byte[] MSG = {
            (byte) 0b00100000, (byte) 0b00101100, (byte) 0b11000011, (byte) 0b01110001, (byte) 0b11000011,
            (byte) 0b00011101, (byte) 0b11100000
    };

    @Test
    public void aircraftIdentification() {
        AircraftIdentification id = new AircraftIdentification(MSG);

        assertEquals(0x2CC371C31DE0L, id.getAircraftIdentificationEncoded());
        assertArrayEquals(new byte[]{11, 12, 13, 49, 48, 49, 55, 32}, id.getAircraftIdentificationDigits());
        assertEquals("KLM1017 ", String.valueOf(id.getAircraftIdentification()));
    }

    /** Every register reports its raw message through the base class, as the ADS-B messages do. */
    @Test
    public void toStringIncludesTheMessage() {
        assertTrue(new AircraftIdentification(MSG).toString()
                .startsWith("AircraftIdentification{BDSRegister{bdsCode=2,0, message=202cc371c31de0}"));
    }

}
