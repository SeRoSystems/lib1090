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
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.squitter.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What ADS-R actually decodes differently from ADS-B, asserted as an exhaustive claim rather than left
 * implicit.
 * <p>
 * After the operational status refactor the answer is: one bit. Every operational mode layout and every
 * surface capability class layout is a single class serving both protocols, so only the airborne
 * capability class differs, ADS-R defining ME 20 as NIC supplement B where ADS-B reserves it. The
 * message classes stay separate for the ICAO/Mode A Flag at ME 56, which ADS-B has no equivalent of.
 * <p>
 * The equality assertions below are the point of the file: they turn "ADS-R and ADS-B agree" from a
 * claim in a document into something that fails a build when it stops being true. Hand-copied
 * per-protocol layouts are what left ADS-R version 3 airborne missing three subfields before.
 */
class ADSRDeltaTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);

    private static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) s.append(String.format("%02X", x));
        return s.toString();
    }

    /** The same ME field carried by ADS-R (DF=18 CF=6) and by ADS-B (DF=17). */
    private static byte[] adsr(byte[] me) {
        return Tools.hexStringToByteArray("96ABCDEF" + hex(me) + "000000");
    }

    private static byte[] adsb(byte[] me) {
        return Tools.hexStringToByteArray("8DABCDEF" + hex(me) + "000000");
    }

    /** Operational status with the given subtype, version, capability class and operational mode. */
    private static byte[] me(int subtype, int version, int capabilityClass, int operationalMode) {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3 | subtype);
        if (subtype == 0) {                       // airborne: capability class spans ME 9-24
            me[1] = (byte) (capabilityClass >>> 8);
            me[2] = (byte) capabilityClass;
        } else {                                  // surface: ME 9-20, the low nibble sharing ME[2]
            me[1] = (byte) (capabilityClass >>> 4);
            me[2] = (byte) ((capabilityClass & 0xF) << 4);
        }
        me[3] = (byte) (operationalMode >>> 8);
        me[4] = (byte) operationalMode;
        me[5] = (byte) (version << 5);
        return me;
    }

    private static ModeSDownlinkMsg decode(byte[] raw) throws Exception {
        return new StatefulModeSDecoder().decode(raw, T);
    }

    // ------------------------------------------------------------------ the one difference

    /** ME 20 of the airborne capability class: NIC supplement B in ADS-R, reserved in ADS-B. */
    @Test
    void nicSupplementBIsTheOnlyCapabilityClassDifference() throws Exception {
        for (int version : new int[]{1, 2, 3}) {
            byte[] field = me(0, version, 0x0010, 0);      // ME 20 set

            CapabilityClassCode r = ((OperationalStatusMsg) decode(adsr(field))).getCapabilityClass();
            CapabilityClassCode b = ((OperationalStatusMsg) decode(adsb(field))).getCapabilityClass();

            assertInstanceOf(ADSRAirborneCapabilityClassCode.class, r, "version " + version);
            assertTrue(((ADSRAirborneCapabilityClassCode) r).getNICSupplementB(), "version " + version);

            assertFalse(ADSRAirborneCapabilityClassCode.class.isInstance(b),
                    "ADS-B airborne reserves ME 20, version " + version);
        }
    }

    /**
     * Every other airborne capability class subfield reads the same in both protocols. Sweeping the
     * whole field one bit at a time, the two disagree only where ME 20 is set.
     */
    @Test
    void airborneCapabilityClassAgreesExceptAtME20() throws Exception {
        for (int meBit = 11; meBit <= 24; meBit++) {
            int field = 1 << (24 - meBit);
            byte[] raw = me(0, 3, field, 0);

            CapabilityClassCode r = ((OperationalStatusMsg) decode(adsr(raw))).getCapabilityClass();
            CapabilityClassCode b = ((OperationalStatusMsg) decode(adsb(raw))).getCapabilityClass();

            assertEquals(b.getEncoded(), r.getEncoded(), "ME " + meBit);
            assertEquals(b.getFirstMEBit(), r.getFirstMEBit());
            assertEquals(b.getLastMEBit(), r.getLastMEBit());

            // identical readings for every subfield both protocols define
            AirborneCapabilityClassCodeV3 rv = (AirborneCapabilityClassCodeV3) r;
            AirborneCapabilityClassCodeV3 bv = (AirborneCapabilityClassCodeV3) b;
            assertEquals(bv.isCollisionAvoidanceOperational(), rv.isCollisionAvoidanceOperational(), "ME " + meBit);
            assertEquals(bv.has1090ESIn(), rv.has1090ESIn(), "ME " + meBit);
            assertEquals(bv.hasUATIn(), rv.hasUATIn(), "ME " + meBit);
            assertEquals(bv.getADSBReceiverVersionEncoded(), rv.getADSBReceiverVersionEncoded(), "ME " + meBit);
            assertEquals(bv.getTransponderSideIndicationEncoded(), rv.getTransponderSideIndicationEncoded(), "ME " + meBit);
            assertEquals(bv.getTxPowerEncoded(), rv.getTxPowerEncoded(), "ME " + meBit);
            assertEquals(bv.getReducedCapabilityEquipmentEncoded(), rv.getReducedCapabilityEquipmentEncoded(), "ME " + meBit);
            assertEquals(bv.getDetectAndAvoidEncoded(), rv.getDetectAndAvoidEncoded(), "ME " + meBit);
        }
    }

    // ------------------------------------------------------------------ what is shared outright

    /**
     * Surface capability class and operational mode layouts are not merely equivalent between the
     * protocols — they are the same class, so equality holds by construction.
     */
    @Test
    void surfaceCapabilityClassAndOperationalModeAreSharedClasses() throws Exception {
        for (int version : new int[]{1, 2, 3}) {
            byte[] surface = me(1, version, 0x0FF, 0x00FF);
            CapabilityClassCode rc = ((OperationalStatusMsg) decode(adsr(surface))).getCapabilityClass();
            CapabilityClassCode bc = ((OperationalStatusMsg) decode(adsb(surface))).getCapabilityClass();
            assertEquals(bc.getClass(), rc.getClass(), "surface capability class, version " + version);
            assertEquals(bc, rc, "surface capability class, version " + version);

            for (int subtype : new int[]{0, 1}) {
                byte[] field = me(subtype, version, 0, 0x1F00);
                OperationalModeCode ro = ((OperationalStatusMsg) decode(adsr(field))).getOperationalMode();
                OperationalModeCode bo = ((OperationalStatusMsg) decode(adsb(field))).getOperationalMode();
                assertEquals(bo.getClass(), ro.getClass(), "operational mode, v" + version + " subtype " + subtype);
                assertEquals(bo, ro, "operational mode, v" + version + " subtype " + subtype);
            }
        }
    }

    // ------------------------------------------------------------------ the ICAO/Mode A Flag

    /** ME 56 is the ICAO/Mode A Flag in ADS-R; ADS-B messages carry no such member. */
    @Test
    void imfIsExposedOnADSROnly() throws Exception {
        byte[] field = me(0, 3, 0, 0);
        field[6] = 0x01;                                   // ME 56

        ModeSDownlinkMsg r = decode(adsr(field));
        assertInstanceOf(IMFMsg.class, r);
        assertTrue(((IMFMsg) r).getIMF());
        assertFalse(IMFMsg.class.isInstance(decode(adsb(field))));

        field[6] = 0x00;
        assertFalse(((IMFMsg) decode(adsr(field))).getIMF());
    }

    /** The IMF flag also decides how the target's address is qualified. */
    @Test
    void imfSelectsTheAddressType() throws Exception {
        byte[] field = me(0, 3, 0, 0);
        assertEquals(de.serosystems.lib1090.msgs.QualifiedAddress.Type.ICAO24,
                decode(adsr(field)).getAddress().getType());

        field[6] = 0x01;
        assertEquals(de.serosystems.lib1090.msgs.QualifiedAddress.Type.ANONYMOUS,
                decode(adsr(field)).getAddress().getType());
    }

    /** Every ADS-R message carries the protocol marker, and no ADS-B message does. */
    @Test
    void protocolMarkers() throws Exception {
        byte[] field = me(0, 3, 0, 0);
        assertInstanceOf(ADSRMsg.class, decode(adsr(field)));
        assertFalse(ADSRMsg.class.isInstance(decode(adsb(field))));
    }
}
