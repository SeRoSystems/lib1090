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
import de.serosystems.lib1090.decoding.quality.ContainmentRadius;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NIC supplement A traveling from a TIS-B velocity message to the position messages that follow it,
 * through the decoder's per-target state.
 * <p>
 * TIS-B has no operational status message, so the supplement that ADS-B carries there arrives at ME
 * bit 47 of the velocity message instead. ED-102B has the ground station choose a fine position
 * message's TYPE Code in line with §N.3.2.2 TABLE N-16, the version 1 mapping, and that table is
 * keyed on the type code together with this supplement.
 * <p>
 * Type code 11 is where the supplement is visible: set, it reports NIC 9 within 75 m; clear, NIC 8
 * within 185.2 m. The assertions read that off a decoded position rather than inspecting decoder
 * internals.
 */
class TISBNICSupplementStateTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    /** DF=18 with CF=2, which is what routes a message down the TIS-B path. */
    private static byte[] frame(byte[] me) {
        StringBuilder hex = new StringBuilder("92485020");
        for (byte b : me) hex.append(String.format("%02X", b));
        return Tools.hexStringToByteArray(hex + "000000");
    }

    /** TIS-B velocity over ground, carrying NIC supplement A at ME bit 47. */
    private static byte[] velocity(boolean nicSupplementA) {
        byte[] me = new byte[7];
        me[0] = (byte) 0x99;                            // format type code 19, subtype 1
        me[5] = (byte) (nicSupplementA ? 0x02 : 0x00);  // ME 47, the second-lowest bit of this byte
        return frame(me);
    }

    /** TIS-B fine airborne position, type code 11, which TABLE N-16 grades by supplement A. */
    private static byte[] position() {
        byte[] me = new byte[7];
        me[0] = (byte) (11 << 3);
        return frame(me);
    }

    private static FineAirbornePositionMsg positionAfter(StatefulModeSDecoder decoder) throws Exception {
        return (FineAirbornePositionMsg) decoder.decode(position(), T);
    }

    /** The supplement a velocity message carried reaches the position that follows it. */
    @Test
    void testSupplementAReachesTheFollowingPosition() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(velocity(true), T);

        FineAirbornePositionMsg position = positionAfter(decoder);
        assertEquals((byte) 9, position.getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_75, position.getContainmentRadius());
    }

    /** A clear supplement is a report in its own right, not the absence of one. */
    @Test
    void testAClearSupplementReachesItToo() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(velocity(false), T);

        FineAirbornePositionMsg position = positionAfter(decoder);
        assertEquals((byte) 8, position.getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_185_2, position.getContainmentRadius());
    }

    /**
     * Until a velocity message supplies it, the supplement is unknown and the poorest row the type
     * code allows is all that can be claimed — which for type code 11 is the clear row.
     */
    @Test
    void testWithoutAVelocityMessageTheWorstRowIsReported() throws Exception {
        FineAirbornePositionMsg position = positionAfter(new StatefulModeSDecoder());

        assertEquals((byte) 8, position.getNICEncoded());
        assertEquals(ContainmentRadius.BELOW_185_2, position.getContainmentRadius());
    }

    /**
     * The version 0 table this used to be read against grades type code 11 without any supplement,
     * so a set supplement went unseen; 75 m was unreachable for a TIS-B target however good its
     * source.
     */
    @Test
    void testTheSetSupplementIsNoLongerLostToTheVersionZeroTable() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(velocity(true), T);

        assertEquals(ContainmentRadius.BELOW_75, positionAfter(decoder).getContainmentRadius());
    }
}
