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

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.msgs.bds.*;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Messages are {@link Serializable}; this holds for everything they carry, such as their
 * {@link QualifiedAddress} and a position message's {@link CPREncodedPosition}.
 */
public class SerializationTest {

    private static final String[] MESSAGES = {
            "5D485020994409", // all-call reply
            "A0001838CA3E51F0A8000047A1EA", // Comm-B altitude reply
            "8D406B902015A678D4D220AA4BDA", // identification
            "8d3461cf9908388930080f948ea1", // velocity over ground
            "8D000000F8000200494900000000", // airborne operational status, version 2
            "8D40621D58C382D690C8AC2863A7", // airborne position
            "8CA534363BFFF39B73400B6286F4", // surface position
            "9240621D58C386435CC412692AD6", // TIS-B fine airborne position
            "96000000F8000200494900000000", // ADS-R airborne operational status, version 2
    };

    @Test
    public void messages_surviveRoundTrip() throws Exception {
        for (String raw : MESSAGES) {
            ModeSDownlinkMsg msg = newDecoder().decode(raw, Instant.ofEpochSecond(1_600_000_000L));
            ModeSDownlinkMsg back = roundTrip(msg);

            assertEquals(msg.getClass(), back.getClass(), raw);
            assertEquals(msg, back, raw);
            assertEquals(msg.getAddress(), back.getAddress(), raw);

            if (msg instanceof PositionMsg) {
                CPREncodedPosition cpr = ((PositionMsg) msg).getCPREncodedPosition();
                CPREncodedPosition cprBack = ((PositionMsg) back).getCPREncodedPosition();
                assertEquals(cpr.getNBits(), cprBack.getNBits(), raw);
                assertEquals(cpr.isOddFormat(), cprBack.isOddFormat(), raw);
                assertEquals(cpr.isSurface(), cprBack.isSurface(), raw);
                assertEquals(cpr.yz(), cprBack.yz(), raw);
                assertEquals(cpr.xz(), cprBack.xz(), raw);
                assertEquals(cpr.getTimestamp(), cprBack.getTimestamp(), raw);
            }
        }
    }

    /**
     * BDS registers keep their message through a round trip: it is held by {@link BDSRegister}, which the decoded
     * fields of the subclasses do not replace, e.g. for the resolution advisories of BDS 3,0.
     */
    @Test
    public void bdsRegisters_surviveRoundTrip() throws Exception {
        BDSRegister[] registers = {
                new ACASActiveResolutionAdvisoryReport(Tools.hexStringToByteArray("300003FC000000")),
                new AircraftIdentification(Tools.hexStringToByteArray("202CC371C31DE0")),
                new CommonUsageGICBCapabilityReport(Tools.hexStringToByteArray("FA81C100000000")),
                new DataLinkCapabilityReport(Tools.hexStringToByteArray("10C003B3FD7260")),
                new HeadingAndSpeed(Tools.hexStringToByteArray("A74A072BFDEFC1")),
                new SelectedVerticalIntention(Tools.hexStringToByteArray("85E42F31300000")),
                new TrackAndTurn(Tools.hexStringToByteArray("81951536E024D4")),
        };
        for (BDSRegister register : registers) {
            BDSRegister back = roundTrip(register);
            String name = register.getClass().getSimpleName();

            assertEquals(register.getClass(), back.getClass(), name);
            assertArrayEquals(register.getMessage(), back.getMessage(), name);
            assertEquals(register.toString(), back.toString(), name);
        }

        ACASActiveResolutionAdvisoryReport ra = (ACASActiveResolutionAdvisoryReport) registers[0];
        ACASActiveResolutionAdvisoryReport raBack = roundTrip(ra);
        assertArrayEquals(ra.getActiveResolutionAdvisories(), raBack.getActiveResolutionAdvisories());
        assertArrayEquals(ra.getResolutionAdvisoriesComplementsRecord(),
                raBack.getResolutionAdvisoriesComplementsRecord());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T roundTrip(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }

    /**
     * The synthetic messages of these tests carry no valid parity, so the parity check is disabled.
     */
    private static StatefulModeSDecoder newDecoder() {
        return StatefulModeSDecoder.builder().checkParity(false).build();
    }

}
