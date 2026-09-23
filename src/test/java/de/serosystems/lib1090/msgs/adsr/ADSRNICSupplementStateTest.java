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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NIC supplement B traveling from an operational status message to the position messages that follow
 * it, through the decoder's per-target state.
 * <p>
 * Supplement B lives inside the capability class field, so which layout the format selector picks
 * decides whether a message supplies it at all — and only the ADS-R airborne layouts do. The decoder
 * therefore takes it only when the layout offers it and leaves its state alone otherwise, which is what
 * the pre-refactor code achieved by throwing away any message it could not fully parse.
 * <p>
 * The supplement is observable through the navigation integrity category of a subsequent airborne
 * position: at format type code 11, NIC is 9 when supplements A and B are both set and 8 otherwise
 * (ED-102B TABLE 2-67), so the assertions below read it off a real decoded position rather than
 * inspecting decoder internals.
 */
class ADSRNICSupplementStateTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    private static String hex(byte[] me) {
        StringBuilder s = new StringBuilder();
        for (byte b : me) s.append(String.format("%02X", b));
        return s.toString();
    }

    private static byte[] frame(byte[] me) {
        return Tools.hexStringToByteArray("96ABCDEF" + hex(me) + "000000");
    }

    /**
     * Airborne operational status, version 3, NIC supplement A at ME 44 always set so that supplement B
     * is the only variable, and the capability class carrying supplement B at ME 20 or not.
     */
    private static byte[] airborneStatus(boolean nicSupplementB) {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3);
        me[2] = (byte) (nicSupplementB ? 0x10 : 0x00);   // capability class ME 20
        me[5] = 0x70;                                    // version 3 (ME 41-43) + NIC supplement A (ME 44)
        return frame(me);
    }

    /** Surface operational status, version 3: carries NIC supplement C, never supplement B. */
    private static byte[] surfaceStatus() {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3 | 1);
        me[2] = 0x10;                                    // surface capability class ME 20 = supplement C
        me[5] = 0x70;
        return frame(me);
    }

    /** Airborne operational status whose capability class selector selects no layout we model. */
    private static byte[] airborneStatusWithUnknownSelector() {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3);
        me[1] = (byte) 0x80;                             // ME 9 set -> selector 2
        me[5] = 0x70;
        return frame(me);
    }

    /** Airborne position, format type code 11, where supplement B moves NIC between 8 and 9. */
    private static byte[] airbornePosition() {
        byte[] me = new byte[7];
        me[0] = (byte) (11 << 3);
        return frame(me);
    }

    private static byte nicOfFollowingPosition(StatefulModeSDecoder decoder) throws Exception {
        AirbornePositionV3Msg.WithNICSupplements position =
                (AirbornePositionV3Msg.WithNICSupplements) decoder.decode(airbornePosition(), T);
        return position.getNICEncoded();
    }

    @Test
    void supplementBReachesTheFollowingPosition() throws Exception {
        StatefulModeSDecoder withB = new StatefulModeSDecoder();
        withB.decode(airborneStatus(true), T);
        assertEquals(9, nicOfFollowingPosition(withB), "supplements A and B both set");

        StatefulModeSDecoder withoutB = new StatefulModeSDecoder();
        withoutB.decode(airborneStatus(false), T);
        assertEquals(8, nicOfFollowingPosition(withoutB), "supplement A only");
    }

    /** With no operational status seen at all, ADS-R decoding does not start: version 0 is unspecified. */
    @Test
    void withoutAnOperationalStatusThereIsNoPositionToRead() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        assertFalse(AirbornePositionV3Msg.class.isInstance(decoder.decode(airbornePosition(), T)));
    }

    /**
     * A layout that does not carry supplement B must leave the decoder's copy alone rather than
     * overwrite it with a default. The surface layouts carry supplement C instead, so a surface status
     * arriving between an airborne status and a position must not clear what the airborne one set.
     */
    @Test
    void aLayoutWithoutSupplementBLeavesItAlone() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(airborneStatus(true), T);
        decoder.decode(surfaceStatus(), T);
        assertEquals(9, nicOfFollowingPosition(decoder), "surface status must not clear supplement B");
    }

    /**
     * Same rule for a selector this library does not model: the field decodes to the fallback, which
     * offers no supplements, so the decoder keeps what it had. Before the refactor this case threw and
     * the state was never reached at all — the behavior is deliberately unchanged.
     */
    @Test
    void anUnknownSelectorLeavesSupplementBAlone() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(airborneStatus(true), T);
        decoder.decode(airborneStatusWithUnknownSelector(), T);
        assertEquals(9, nicOfFollowingPosition(decoder), "unknown selector must not clear supplement B");
    }

    /** And the supplement is per target, not global. */
    @Test
    void supplementsAreHeldPerTarget() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(airborneStatus(true), T);

        byte[] otherStatus = airborneStatus(false);
        byte[] otherPosition = airbornePosition();
        for (byte[] f : new byte[][]{otherStatus, otherPosition}) {
            f[1] = 0x11; f[2] = 0x22; f[3] = 0x33;       // a different ICAO address
        }
        decoder.decode(otherStatus, T);
        AirbornePositionV3Msg.WithNICSupplements other =
                (AirbornePositionV3Msg.WithNICSupplements) decoder.decode(otherPosition, T);
        assertEquals(8, other.getNICEncoded(), "the second target never reported supplement B");

        assertEquals(9, nicOfFollowingPosition(decoder), "the first target still has it");
    }
}
