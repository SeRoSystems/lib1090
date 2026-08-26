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
import de.serosystems.lib1090.msgs.squitter.*;
import de.serosystems.lib1090.msgs.squitter.opstatus.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The behavior change the operational status refactor exists for, exercised through the public
 * decoder: a capability class or operational mode selector this library does not model no longer
 * costs the whole message.
 */
class OperationalStatusFallbackTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    /** Version 2 airborne with ME 9 set, i.e. capability class selector 2, which is undefined. */
    private static final String UNKNOWN_CC = "8D000000F8800000004900000000";

    @Test
    void decoderKeepsTheMessageWhenTheCapabilityClassSelectorIsUnknown() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        Object msg = decoder.decode(Tools.hexStringToByteArray(UNKNOWN_CC), T);

        // before the refactor this threw BadFormatException and the message was lost entirely
        assertInstanceOf(AirborneOperationalStatusV2Msg.class, msg);
        AirborneOperationalStatusV2Msg opstat = (AirborneOperationalStatusV2Msg) msg;

        // the field itself is undecodable, and says so
        CapabilityClassCode cc = opstat.getCapabilityClass();
        assertInstanceOf(UnknownCapabilityClassCode.class, cc);
        assertEquals(2, cc.getFormatSelector());
        assertEquals(0x8000, cc.getEncoded());
        assertFalse(KnownCapabilityClassCode.class.isInstance(cc));

        // everything outside the field decodes exactly as before: it is positionally fixed
        assertEquals(2, opstat.getMOPSVersion());
        assertEquals(9, opstat.getNACpEncoded());
        assertFalse(opstat.getNICSupplementA());
        assertInstanceOf(KnownOperationalModeCode.class, opstat.getOperationalMode());
    }

    @Test
    void rawFieldStaysAvailableWhicheverLayoutApplies() throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        AirborneOperationalStatusV2Msg opstat = (AirborneOperationalStatusV2Msg)
                decoder.decode(Tools.hexStringToByteArray(UNKNOWN_CC), T);
        assertEquals(0x8000, opstat.getCapabilityClassCodeEncoded());
        assertEquals(opstat.getCapabilityClassCodeEncoded(), opstat.getCapabilityClass().getEncoded());
        assertEquals(opstat.getOperationalModeCodeEncoded(), opstat.getOperationalMode().getEncoded());
    }
}
