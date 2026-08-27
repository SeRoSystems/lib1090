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
import de.serosystems.lib1090.msgs.squitter.AirborneCapabilityClassCodeV1V2;
import de.serosystems.lib1090.msgs.squitter.AirborneOperationalStatusV1V2Msg;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCodeV1V2;
import de.serosystems.lib1090.msgs.squitter.CapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.KnownCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.KnownOperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.OperationalModeCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.UnknownCapabilityClassCode;
import de.serosystems.lib1090.msgs.squitter.opstatus.UnknownOperationalModeCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared tests for the capability class code and operational mode code fields, which are encoded
 * identically (same bit offsets, same reserved-bit validation) across ADS-B versions 1 and 2 for
 * airborne operational status messages. isCollisionAvoidanceOperational()'s polarity differs between versions
 * and is intentionally NOT covered here; it stays in the version-specific subclasses.
 */
abstract class AirborneOperationalStatusMsgTest {

    protected abstract byte[] baseMessage();

    protected abstract AirborneOperationalStatusV1V2Msg create(byte[] msg) throws Exception;


    /**
     * The capability class and operational mode subfields live on the decoded field objects now, not on
     * the message, so these helpers hand back what the message's format selector selected.
     */
    protected AirborneCapabilityClassCodeV1V2 withCapabilityClassCode(int capabilityClassCode) throws Exception {
        byte[] msg = baseMessage();
        msg[5] = (byte) (capabilityClassCode >>> 8);
        msg[6] = (byte) capabilityClassCode;
        return (AirborneCapabilityClassCodeV1V2) create(msg).getCapabilityClass();
    }

    protected OperationalModeCodeV1V2 withOperationalModeCode(int operationalModeCode) throws Exception {
        byte[] msg = baseMessage();
        msg[7] = (byte) (operationalModeCode >>> 8);
        msg[8] = (byte) operationalModeCode;
        return (OperationalModeCodeV1V2) create(msg).getOperationalMode();
    }


    /** The undecoded field, for the layouts this library does not model. */
    protected CapabilityClassCode rawCapabilityClass(int capabilityClassCode) throws Exception {
        byte[] msg = baseMessage();
        msg[5] = (byte) (capabilityClassCode >>> 8);
        msg[6] = (byte) capabilityClassCode;
        return create(msg).getCapabilityClass();
    }

    protected OperationalModeCode rawOperationalMode(int operationalModeCode) throws Exception {
        byte[] msg = baseMessage();
        msg[7] = (byte) (operationalModeCode >>> 8);
        msg[8] = (byte) operationalModeCode;
        return create(msg).getOperationalMode();
    }

    @Test
    void testSubtypeCode() throws Exception {
        assertEquals(0, create(baseMessage()).getSubtypeCode());
    }

    @Test
    void testCapabilityClassCodeFlags() throws Exception {
        AirborneCapabilityClassCodeV1V2 es1090In = withCapabilityClassCode(0x1000);
        assertTrue(es1090In.has1090ESIn());
        assertFalse(es1090In.supportsARVReport());
        assertFalse(es1090In.supportsTSReport());

        AirborneCapabilityClassCodeV1V2 airReferencedVelocity = withCapabilityClassCode(0x0200);
        assertFalse(airReferencedVelocity.has1090ESIn());
        assertTrue(airReferencedVelocity.supportsARVReport());
        assertFalse(airReferencedVelocity.supportsTSReport());

        AirborneCapabilityClassCodeV1V2 targetStateReport = withCapabilityClassCode(0x0100);
        assertFalse(targetStateReport.has1090ESIn());
        assertFalse(targetStateReport.supportsARVReport());
        assertTrue(targetStateReport.supportsTSReport());
    }

    @Test
    void testTargetChangeReportCapability() throws Exception {
        assertEquals(0, withCapabilityClassCode(0x00).getTCReportCapabilityLevelEncoded());
        assertEquals(1, withCapabilityClassCode(0x40).getTCReportCapabilityLevelEncoded());
        assertEquals(2, withCapabilityClassCode(0x80).getTCReportCapabilityLevelEncoded());
        assertEquals(3, withCapabilityClassCode(0xC0).getTCReportCapabilityLevelEncoded());
    }

    /**
     * A selector this library does not model no longer costs the message. It used to throw
     * {@link BadFormatException}, discarding a message whose remaining subfields — MOPS version, NIC
     * supplement A, NACp, SIL — sit outside the field entirely and decode perfectly well.
     */
    @Test
    void unknownCapabilityClassSelectorFallsBackInsteadOfThrowing() throws Exception {
        for (int selector : new int[]{0x8000, 0x4000}) {
            CapabilityClassCode cc = rawCapabilityClass(selector);
            assertInstanceOf(UnknownCapabilityClassCode.class, cc);
            assertFalse(KnownCapabilityClassCode.class.isInstance(cc));
        }
        assertInstanceOf(KnownCapabilityClassCode.class, rawCapabilityClass(0x0000));
    }

    @Test
    void unknownOperationalModeSelectorFallsBackInsteadOfThrowing() throws Exception {
        for (int selector : new int[]{0x8000, 0x4000}) {
            OperationalModeCode om = rawOperationalMode(selector);
            assertInstanceOf(UnknownOperationalModeCode.class, om);
            assertFalse(KnownOperationalModeCode.class.isInstance(om));
        }
        assertInstanceOf(KnownOperationalModeCode.class, rawOperationalMode(0x0000));
    }

    @Test
    void testOperationalModeCodeFlags() throws Exception {
        OperationalModeCodeV1V2 tcasResolutionAdvisory = withOperationalModeCode(0x2000);
        assertTrue(tcasResolutionAdvisory.isCollisionAvoidanceResolutionAdvisoryActive());
        assertFalse(tcasResolutionAdvisory.isIDENTSwitchActive());
        assertFalse(tcasResolutionAdvisory.isReceivingATCServices());

        OperationalModeCodeV1V2 activeIdentSwitch = withOperationalModeCode(0x1000);
        assertFalse(activeIdentSwitch.isCollisionAvoidanceResolutionAdvisoryActive());
        assertTrue(activeIdentSwitch.isIDENTSwitchActive());
        assertFalse(activeIdentSwitch.isReceivingATCServices());

        OperationalModeCodeV1V2 receivingAtcServices = withOperationalModeCode(0x0800);
        assertFalse(receivingAtcServices.isCollisionAvoidanceResolutionAdvisoryActive());
        assertFalse(receivingAtcServices.isIDENTSwitchActive());
        assertTrue(receivingAtcServices.isReceivingATCServices());
    }

}
