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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.quality.ContainmentRadius;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NIC supplement D traveling from an airborne velocity message to the position messages that follow
 * it, through the decoder's per-target state.
 * <p>
 * Supplement D is the one supplement carried by neither the position message nor the operational
 * status message: ED-102B puts it in ME bits 47-48 of the airborne velocity message, and only when
 * that message reports no difference from barometric altitude in their place. A receiver therefore
 * learns it from a third message or not at all.
 * <p>
 * It is observable through a subsequent airborne position at format type code 20, which version 3
 * grades by supplement D alone: NIC runs 8, 9, 10, 11 as D runs 0 to 3. The assertions read that off
 * a real decoded position rather than inspecting decoder internals.
 */
class NICSupplementDStateTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    private static byte[] frame(byte[] me) {
        StringBuilder hex = new StringBuilder("8D485020");
        for (byte b : me) hex.append(String.format("%02X", b));
        return Tools.hexStringToByteArray(hex + "000000");
    }

    /** Airborne operational status, version 3, so that the decoder reads what follows as version 3. */
    private static byte[] operationalStatus() {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3);
        me[5] = 0x60;                                   // version 3 in ME 41-43
        return frame(me);
    }

    /**
     * Airborne velocity, version 3 layout, with the difference from barometric altitude reported as
     * unavailable so that ME 47-48 carry NIC supplement D.
     */
    private static byte[] velocityWithSupplementD(int nicSupplementD) {
        byte[] me = new byte[7];
        me[0] = (byte) 0x99;                            // format type code 19, subtype 1
        me[5] = (byte) nicSupplementD;                  // ME 47-48, the low two bits of this byte
        return frame(me);
    }

    /** Airborne velocity carrying a barometric altitude difference, so ME 47-48 are not supplement D. */
    private static byte[] velocityWithoutSupplementD() {
        byte[] me = new byte[7];
        me[0] = (byte) 0x99;
        me[6] = 0x02;                                   // a non-zero difference in ME 50-56
        return frame(me);
    }

    /** Airborne position, format type code 20, which version 3 grades by supplement D alone. */
    private static byte[] position() {
        byte[] me = new byte[7];
        me[0] = (byte) (20 << 3);
        return frame(me);
    }

    private static AirbornePositionV3Msg positionAfter(StatefulModeSDecoder decoder) throws Exception {
        return (AirbornePositionV3Msg) decoder.decode(position(), T);
    }

    private static StatefulModeSDecoder decoderAtVersion3() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(operationalStatus(), T);
        return decoder;
    }

    /**
     * Each value of the supplement reaches the position that follows it. Without the velocity message
     * this would be the type code's worst row every time.
     */
    @Test
    void testSupplementDReachesTheFollowingPosition() throws Exception {
        byte[] expectedNIC = {8, 9, 10, 11};
        ContainmentRadius[] expectedRadius = {ContainmentRadius.BELOW_185_2, ContainmentRadius.BELOW_75,
                ContainmentRadius.BELOW_25, ContainmentRadius.BELOW_7_5};

        for (int supplement = 0; supplement <= 3; supplement++) {
            StatefulModeSDecoder decoder = decoderAtVersion3();
            decoder.decode(velocityWithSupplementD(supplement), T);

            AirbornePositionV3Msg position = positionAfter(decoder);
            assertEquals(expectedNIC[supplement], position.getNICEncoded(), "supplement D " + supplement);
            assertEquals(expectedRadius[supplement], position.getContainmentRadius(),
                    "supplement D " + supplement);
        }
    }

    /** Until a velocity message supplies it, the type code's poorest row is all that can be claimed. */
    @Test
    void testWithoutAVelocityMessageTheWorstRowIsReported() throws Exception {
        AirbornePositionV3Msg position = positionAfter(decoderAtVersion3());

        assertEquals((byte) 8, position.getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_185_2, position.getContainmentRadius());
    }

    /**
     * A velocity message that reports a barometric altitude difference uses ME 47-48 for that instead,
     * so it says nothing about the supplement and what was already known of it survives.
     */
    @Test
    void testAVelocityMessageWithoutTheSupplementLeavesItAlone() throws Exception {
        StatefulModeSDecoder decoder = decoderAtVersion3();
        decoder.decode(velocityWithSupplementD(3), T);
        decoder.decode(velocityWithoutSupplementD(), T);

        assertEquals((byte) 11, positionAfter(decoder).getNICEncoded());
    }
}
