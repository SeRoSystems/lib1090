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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisoryReport;
import de.serosystems.lib1090.msgs.acas.RAMessageFormat;
import de.serosystems.lib1090.msgs.acas.ThreatIdentityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ACASResolutionAdvisoryMsgTest {

    /**
     * The broadcast carries the RA report in ME bits 9-56, here the ACAS X report of BDS 3,0 30800408371528.
     */
    @Test
    void acasX_isDecodedAsTheRAReport() throws BadFormatException, UnspecifiedFormatError {
        ACASResolutionAdvisoryMsg msg = new ACASResolutionAdvisoryMsg("8d4840d6e2800408371528000000");

        assertEquals(2, msg.getSubtype());
        assertEquals(RAMessageFormat.ACAS_X, msg.getRAMessageFormat());
        assertInstanceOf(ACASXResolutionAdvisoryReport.class, msg.getResolutionAdvisory());
        ACASXResolutionAdvisoryReport x = (ACASXResolutionAdvisoryReport) msg.getResolutionAdvisory();
        assertEquals(ThreatIdentityType.ALTITUDE_RANGE_BEARING, x.getThreatIdentityType());
        assertEquals(9900, (int) x.getThreatIdentityData().getAltitude());
    }

    @Test
    void otherSubtype_isRejected() {
        assertThrows(BadFormatException.class, () -> new ACASResolutionAdvisoryMsg("8d4840d6e1800408371528000000"));
    }
}
