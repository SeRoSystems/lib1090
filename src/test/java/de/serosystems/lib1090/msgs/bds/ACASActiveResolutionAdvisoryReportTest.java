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

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.acas.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ACASActiveResolutionAdvisoryReportTest {

    private static ResolutionAdvisory ra(byte[] mb) throws BadFormatException {
        return new ACASActiveResolutionAdvisoryReport(mb).getResolutionAdvisory();
    }

    private static ResolutionAdvisory ra(String mb) throws BadFormatException {
        return ra(Tools.hexStringToByteArray(mb));
    }

    // ---------------------------------------------------------------- TCAS layout, Annex 10 Vol IV §4.3.8.4.2.2.1

    /**
     * No ARA, every RAC, RAT and MTE set, and the unassigned TTI 3.
     */
    @Test
    public void tcas_racRatMteAndUnassignedThreatType() throws BadFormatException {
        ResolutionAdvisory ra = ra(new byte[]{
                (byte) 0b00110000, (byte) 0b00000000, (byte) 0b00000011, (byte) 0b11111100, 0, 0, 0});

        assertInstanceOf(TCASResolutionAdvisoryReport.class, ra);
        TCASResolutionAdvisoryReport tcas = (TCASResolutionAdvisoryReport) ra;
        assertEquals(RAMessageFormat.TCAS_II, tcas.getRAMessageFormat());
        assertArrayEquals(new boolean[14], tcas.getActiveResolutionAdvisories());
        assertEquals(0b1111, tcas.getRACRecordEncoded());
        assertTrue(tcas.isDoNotPassBelowActive());
        assertTrue(tcas.isDoNotPassAboveActive());
        assertTrue(tcas.isDoNotTurnLeftActive());
        assertTrue(tcas.isDoNotTurnRightActive());
        assertTrue(tcas.isTerminated());
        assertTrue(tcas.isMultipleThreatEncounter());
        assertEquals(3, tcas.getThreatTypeEncoded());
        assertEquals(ThreatIdentityType.NOT_ASSIGNED, tcas.getThreatIdentityType());
        assertNull(tcas.getThreatIdentityData());
    }

    @Test
    public void tcas_threatAddress() throws BadFormatException {
        // icao24 set here is 0xabcdef = 0b 10101011 11001101 11101111
        TCASResolutionAdvisoryReport tcas = (TCASResolutionAdvisoryReport) ra(new byte[]{
                0b00110000, 0b01000000, 0b01000000, 0b01110110, (byte) 0b10101111, 0b00110111, (byte) 0b10111100});

        assertTrue(tcas.isTerminated());
        assertTrue(tcas.isMultipleThreatEncounter());
        assertEquals(0b01000000010000, tcas.getActiveRAEncoded());
        assertArrayEquals(new boolean[]{false, true, false, false, false, false, false, false, false, true, false,
                false, false, false}, tcas.getActiveResolutionAdvisories());
        assertEquals(1, tcas.getRACRecordEncoded());
        assertFalse(tcas.isDoNotTurnLeftActive());
        assertTrue(tcas.isDoNotTurnRightActive());
        assertEquals(ThreatIdentityType.ADDRESS, tcas.getThreatIdentityType());
        assertEquals(0b10101011110011011110111100, tcas.getThreatIdentityEncoded());
        assertEquals(0xabcdef, tcas.getThreatIdentityData().getIcao24().intValue());
    }

    /**
     * An unassigned threat bearing (code 61) no longer discards the report: every other field is decoded, and the
     * bearing reads as not available.
     */
    @Test
    public void tcas_unassignedBearing_keepsTheReport() throws BadFormatException {
        TCASResolutionAdvisoryReport tcas = (TCASResolutionAdvisoryReport) ra("3080000800017d");

        assertEquals(0x2000, tcas.getActiveRAEncoded());
        assertEquals(ThreatIdentityType.ALTITUDE_RANGE_BEARING, tcas.getThreatIdentityType());
        ThreatIdentityData tid = tcas.getThreatIdentityData();
        assertFalse(tid.isAltitudeBinary());
        assertNull(tid.getBearing());
        assertEquals((short) 61, tid.getEncodedBearing());
        assertEquals(0.4f, tid.getRange(), 1e-6f);
    }

    /**
     * The TCAS layout with bits 59-88 all zero is taken for a TCAS II version 6.04 report, DO-185B
     * §2.2.3.9.3.2.3.1.2: 14 dedicated ARA bits and RAC, nothing after.
     */
    @Test
    public void tcas6() throws BadFormatException {
        ResolutionAdvisory ra = ra("30020040000000"); // ARA bit 47 (don't climb), RAC bit 58 (don't turn right)

        assertInstanceOf(TCAS6ResolutionAdvisory.class, ra);
        assertFalse(ra instanceof ResolutionAdvisoryState);
        assertEquals(RAMessageFormat.TCAS_II, ra.getRAMessageFormat());
        assertTrue(ra.getActiveResolutionAdvisories()[47 - 41]);
        assertTrue(((TCAS6ResolutionAdvisory) ra).isDoNotTurnRightActive());
    }

    // ------------------------------------------------ ACAS X layout, Annex 10 Vol IV (6th ed.) §4.3.8.4.2.2.2

    /**
     * RMF = 1: ARA is 10 bits, TTI 1 bit with 0 for altitude, range and bearing, and TIDA a binary altitude in 100 ft
     * steps; TIDR and TIDB follow at bits 74-80 and 81-86.
     */
    @Test
    public void acasX_altitudeRangeBearing() throws BadFormatException {
        ResolutionAdvisory ra = ra("30800408371528");

        assertInstanceOf(ACASXResolutionAdvisoryReport.class, ra);
        ACASXResolutionAdvisoryReport x = (ACASXResolutionAdvisoryReport) ra;
        assertEquals(RAMessageFormat.ACAS_X, x.getRAMessageFormat());
        assertEquals(0x200, x.getActiveRAEncoded()); // bit 41 only
        assertEquals(10, x.getActiveResolutionAdvisories().length);
        assertTrue(x.isSameVerticalSense());
        assertFalse(x.isCrossing());
        assertFalse(x.isDownwardSense());
        assertEquals(0, x.getVerticalRAStrengthEncoded());
        assertEquals(0, x.getLowLevelDescendInhibitEncoded());
        assertTrue(x.hasFollowOnMessage());
        assertEquals(0, x.getThreatTypeEncoded());
        assertEquals(ThreatIdentityType.ALTITUDE_RANGE_BEARING, x.getThreatIdentityType());
        assertFalse(x.isThreatDesignated());
        assertFalse(x.getSuppressionIndicator());

        ThreatIdentityData tid = x.getThreatIdentityData();
        assertTrue(tid.isAltitudeBinary());
        assertEquals(110, (short) tid.getAltitudeCode());
        assertEquals(9900, (int) tid.getAltitude());
        assertEquals(Interval.of(Bound.AT_LEAST, 9850, Bound.BELOW, 9950), tid.getAltitudeInterval());
        assertEquals(21, (short) tid.getEncodedRange()); // 2.0 NM
        assertEquals(2.0f, tid.getRange(), 1e-6f);
        assertEquals(57f, tid.getBearing());
        assertEquals(Interval.of(Bound.AT_LEAST, 54, Bound.AT_MOST, 60), tid.getBearingInterval());
    }

    /**
     * RMF = 1 with TTI = 1: TID holds the 24-bit address in bits 63-86.
     */
    @Test
    public void acasX_address() throws BadFormatException {
        ACASXResolutionAdvisoryReport x = (ACASXResolutionAdvisoryReport) ra("3080040d210358");

        assertEquals(ThreatIdentityType.ADDRESS, x.getThreatIdentityType());
        assertEquals(0x4840D6, x.getThreatIdentityEncoded());
        assertEquals(0x4840D6, (int) x.getThreatIdentityData().getIcao24());
    }

    /**
     * RMF = 1 with DSI and SPI set: bits 87 and 88 are no longer read as part of the bearing, which in the TCAS
     * layout made this an unassigned code.
     */
    @Test
    public void acasX_designationAndSuppression() throws BadFormatException {
        ACASXResolutionAdvisoryReport x = (ACASXResolutionAdvisoryReport) ra("3080040837153f");

        assertTrue(x.isThreatDesignated());
        assertTrue(x.getSuppressionIndicator());
        assertEquals(87f, x.getThreatIdentityData().getBearing());
    }

    /**
     * RMF = 2 (reserved for ACAS III) has no defined layout: no content, but the format and the raw bits.
     */
    @Test
    public void reservedFormat_hasNoContent() throws BadFormatException {
        ACASActiveResolutionAdvisoryReport report =
                new ACASActiveResolutionAdvisoryReport(Tools.hexStringToByteArray("30800808371528"));

        assertEquals(RAMessageFormat.ACAS_III, report.getRAMessageFormat());
        assertNull(report.getResolutionAdvisory());
        assertEquals(0x800808371528L, report.getResolutionAdvisoryEncoded());
    }
}
