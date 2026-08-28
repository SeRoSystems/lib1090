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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.msgs.AirborneOperationalStatusMsgTest;
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV1V2Msg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the shared airborne operational status assertions against the ADS-R version 1 message.
 * <p>
 * The ADS-B and ADS-R layouts of this field are identical, so the whole assertion set applies unchanged;
 * only the frame differs, ADS-R arriving as DF=18 with CF=6 rather than DF=17. That is the point of
 * running it here: the two protocols keep separate message classes, and this is what stops them drifting
 * apart the way they had before.
 */
class AirborneOperationalStatusV1MsgTest extends AirborneOperationalStatusMsgTest {

    /** DF=18, CF=6, otherwise the ADS-B version 1 base frame bit for bit. */
    private static final String BASE_MESSAGE = "96000000F8000200492900000000";

    @Override
    protected byte[] baseMessage() {
        return Tools.hexStringToByteArray(BASE_MESSAGE);
    }

    @Override
    protected AirborneOperationalStatusV1V2Msg create(byte[] msg) throws Exception {
        return new AirborneOperationalStatusV1Msg(msg);
    }

    @Test
    void isTheADSRMessageAtTheExpectedVersion() throws Exception {
        AirborneOperationalStatusV1V2Msg msg = create(baseMessage());
        assertInstanceOf(AirborneOperationalStatusV1Msg.class, msg);
        assertInstanceOf(ADSRMsg.class, msg);
        assertEquals(1, msg.getMOPSVersion());
    }
}
