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

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusMsg;
import de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.KnownCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.AirborneCapabilityClassCodeV0;
import de.serosystems.lib1090.msgs.squitter.KnownOperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.UndefinedOperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.UnknownCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.UnknownOperationalModeCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationalStatusV0MsgTest {

    @Test
    public void testValidEnrouteCapabilities() throws Exception {
        byte[] msg = Tools.hexStringToByteArray("8D000000F8300000000000000000");
        OperationalStatusV0Msg status = new OperationalStatusV0Msg(msg);
        assertInstanceOf(OperationalStatusMsg.class, status);
        assertFalse(((AirborneCapabilityClassCode) status.getCapabilityClass()).isCollisionAvoidanceOperational());
        assertTrue(((AirborneCapabilityClassCodeV0) status.getCapabilityClass()).hasOperationalCDTI());
        assertTrue(((KnownCapabilityClassCode) status.getCapabilityClass()).has1090ESIn());
    }

    /**
     * ME 9-10 outside the one layout version 0 defines used to reject the whole message. It now yields
     * the fallback, leaving the MOPS version and the rest of the message readable.
     */
    @Test
    public void testInvalidEnrouteCapabilitiesHighBits() throws Exception {
        byte[] msg = Tools.hexStringToByteArray("8D000000F8800000000000000000");
        OperationalStatusV0Msg status = new OperationalStatusV0Msg(msg);
        assertEquals(0, status.getMOPSVersion());
        assertInstanceOf(UnknownCapabilityClassCode.class, status.getCapabilityClass());
        assertEquals(2, status.getCapabilityClass().getFormatSelector());
    }

    /**
     * Version 0 defines no Operational Mode Code, but the accessor is on every operational status
     * message: the absence is reported by the object rather than by a missing method.
     */
    @Test
    public void testUndefinedOperationalMode() throws Exception {
        byte[] msg = Tools.hexStringToByteArray("8D000000F8300000000000000000");
        OperationalStatusV0Msg status = new OperationalStatusV0Msg(msg);

        assertInstanceOf(UndefinedOperationalModeCode.class, status.getOperationalMode());
        // distinct from "a selector I do not model" - here there is no selector at all
        assertFalse(UnknownOperationalModeCode.class.isInstance(status.getOperationalMode()));
        assertFalse(KnownOperationalModeCode.class.isInstance(status.getOperationalMode()));
        assertEquals(UndefinedOperationalModeCode.NO_FORMAT_SELECTOR,
                status.getOperationalMode().getFormatSelector());

        // the reserved field is still readable, and zero from a conformant version 0 transmitter
        assertEquals(0, status.getOperationalModeCodeEncoded());
        assertEquals(0, status.getOperationalMode().getEncoded());
    }
}
