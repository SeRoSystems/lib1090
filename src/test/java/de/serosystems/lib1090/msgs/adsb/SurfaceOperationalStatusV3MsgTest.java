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

import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.squitter.OperationalStatusV2V3Msg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurfaceOperationalStatusV3MsgTest {

    /**
     * A version 3 surface operational status message is no version 2 message, as the version 3 airborne one is
     * not, but shares the API of versions 2 and 3 and reports its horizontal reference direction as every surface
     * message does; the same holds for ADS-R.
     */
    @Test
    void isNoVersion2Message() throws Exception {
        SurfaceOperationalStatusV3Msg msg = new SurfaceOperationalStatusV3Msg("8D4B1A2CF9175F4F02693E7322B2");
        assertFalse(msg instanceof OperationalStatusV2Msg);
        assertInstanceOf(OperationalStatusV2V3Msg.class, msg);
        assertInstanceOf(SurfaceOperationalStatusMsg.class, msg);
        msg.isHeadingReferencedToMagneticNorth();

        assertFalse(OperationalStatusV2Msg.class.isAssignableFrom(
                de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV3Msg.class));
        assertTrue(OperationalStatusV2V3Msg.class.isAssignableFrom(
                de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV3Msg.class));
    }

}
