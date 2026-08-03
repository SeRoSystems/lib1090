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

import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalStatusMsg;

import de.serosystems.lib1090.exceptions.BadFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared tests for the capability class code and operational mode code fields, which are encoded
 * identically (same bit offsets, same reserved-bit validation) across ADS-B versions 1 and 2 for
 * surface operational status messages. hasPositionOffsetApplied() is derived differently per
 * version (a capability class code bit in v1, the GPS antenna offset in v2) and is intentionally
 * NOT covered here; it stays in the version-specific subclasses.
 */
abstract class SurfaceOperationalStatusMsgTest {

    protected abstract byte[] baseMessage();

    protected abstract SurfaceOperationalStatusMsg create(byte[] msg) throws Exception;

    protected SurfaceOperationalStatusMsg withCapabilityClassCode(int capabilityClassCode) throws Exception {
        byte[] msg = baseMessage();
        msg[5] = (byte) (capabilityClassCode >>> 4);
        msg[6] = (byte) ((msg[6] & 0x0F) | ((capabilityClassCode & 0x0F) << 4));
        return create(msg);
    }

    protected SurfaceOperationalStatusMsg withOperationalModeCode(int operationalModeCode) throws Exception {
        byte[] msg = baseMessage();
        msg[7] = (byte) (operationalModeCode >>> 8);
        msg[8] = (byte) operationalModeCode;
        return create(msg);
    }

    @Test
    void testSubtypeCode() throws Exception {
        assertEquals(1, create(baseMessage()).getSubtypeCode());
    }

    @Test
    void testCapabilityClassCodeFlags() throws Exception {
        SurfaceOperationalStatusMsg esIn = withCapabilityClassCode(0x100);
        assertTrue(esIn.has1090ESIn());
        assertFalse(esIn.hasLowTxPower());

        SurfaceOperationalStatusMsg lowTxPower = withCapabilityClassCode(0x20);
        assertFalse(lowTxPower.has1090ESIn());
        assertTrue(lowTxPower.hasLowTxPower());
    }

    @Test
    void testCapabilityClassCodeReservedBits() throws Exception {
        assertThrows(BadFormatException.class, () -> withCapabilityClassCode(0x800));
        assertThrows(BadFormatException.class, () -> withCapabilityClassCode(0x400));

        withCapabilityClassCode(0x200);
    }

    @Test
    void testOperationalModeCodeFlags() throws Exception {
        SurfaceOperationalStatusMsg tcasResolutionAdvisory = withOperationalModeCode(0x2000);
        assertTrue(tcasResolutionAdvisory.hasTCASResolutionAdvisory());
        assertFalse(tcasResolutionAdvisory.hasActiveIDENTSwitch());
        assertFalse(tcasResolutionAdvisory.hasReceivingATCServices());

        SurfaceOperationalStatusMsg activeIdentSwitch = withOperationalModeCode(0x1000);
        assertFalse(activeIdentSwitch.hasTCASResolutionAdvisory());
        assertTrue(activeIdentSwitch.hasActiveIDENTSwitch());
        assertFalse(activeIdentSwitch.hasReceivingATCServices());

        SurfaceOperationalStatusMsg receivingAtcServices = withOperationalModeCode(0x0800);
        assertFalse(receivingAtcServices.hasTCASResolutionAdvisory());
        assertFalse(receivingAtcServices.hasActiveIDENTSwitch());
        assertTrue(receivingAtcServices.hasReceivingATCServices());
    }

    @Test
    void testOperationalModeCodeReservedBits() throws Exception {
        assertThrows(BadFormatException.class, () -> withOperationalModeCode(0x8000));
        assertThrows(BadFormatException.class, () -> withOperationalModeCode(0x4000));

        withOperationalModeCode(0x2000);
    }
}
