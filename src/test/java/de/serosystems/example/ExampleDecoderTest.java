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

package de.serosystems.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExampleDecoderTest {

    /**
     * The example prints the IFR capability of a velocity message only if the message carries it: a TIS-B velocity
     * of an address whose DF=17 reported version 1 does not, and is printed without it instead of throwing
     * ClassCastException; the DF=17 version 1 velocity does.
     */
    @Test
    void velocityIFRCapability_isPrintedOnlyWhereCarried() {
        String out = decode(
                "8DABCDEFF8000000002000F20627", // DF=17 operational status, version 1
                "92ABCDEF9948640C802AB0B27FB0", // TIS-B velocity, same address
                "8DABCDEF9948640C802AB07F91B5"); // DF=17 velocity, version 1

        assertEquals(1, out.split("Has IFR capability", -1).length - 1, out);
    }

    /**
     * A version 3 target state and status message is printed as such, not as an unknown extended squitter.
     */
    @Test
    void version3TargetState_isPrinted() {
        String out = decode(
                "8D4840D6F80000000060009B04F1", // operational status, version 3
                "8D4840D6EA7FE0000000005C03BC"); // target state and status, selected altitude 65440 ft

        assertTrue(out.contains("Target State and Status reported"), out);
        assertTrue(out.contains("Selected altitude: 65440 ft"), out);
        assertFalse(out.contains("Unknown extended squitter"), out);
    }

    private static String decode(String... messages) {
        ExampleDecoder decoder = new ExampleDecoder();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(bytes, true));
        try {
            for (String raw : messages)
                assertDoesNotThrow(() -> decoder.decodeMsg(Instant.EPOCH, raw, null), raw);
        } finally {
            System.setOut(stdout);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

}
