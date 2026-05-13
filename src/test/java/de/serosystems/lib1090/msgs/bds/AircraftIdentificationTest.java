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

import de.serosystems.lib1090.decoding.Identification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AircraftIdentificationTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00100000, (byte) 0b00101100, (byte) 0b11000011, (byte) 0b01110001, (byte) 0b11000011,
                (byte) 0b00011101, (byte) 0b11100000
        };

    }

    @Test
    public void bdsCode() {

        short bds = AircraftIdentification.extractBdsCode(msg);
        assertEquals(20, bds);

    }


    @Test
    public void aircraftIdentification() {

        byte[] identityByteArray = Identification.decodeAircraftIdentification(msg);
        char[] identityCharArray = Identification.mapChar(identityByteArray);

        assertEquals("KLM1017 ", String.valueOf(identityCharArray));

    }
    
}
