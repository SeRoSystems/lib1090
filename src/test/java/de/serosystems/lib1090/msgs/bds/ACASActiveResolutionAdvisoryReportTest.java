package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.adsb.TCASResolutionAdvisoryMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ACASActiveResolutionAdvisoryReportTest {

    private static byte[] msg;
    private static ACASActiveResolutionAdvisoryReport acasReport;

    @BeforeAll
    public static void setup() throws BadFormatException {

        msg = new byte[]{
                (byte) 0b00110000, (byte) 0b00000000, (byte) 0b00000011, (byte) 0b11111100, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };

        acasReport = new ACASActiveResolutionAdvisoryReport(msg);


    }

    @Test
    public void bdsCode() {
        assertEquals(BDSRegister.bdsCode.ACAS_ACTIVE_RESOLUTION_ADVISORY, acasReport.getBds());
    }

    @Test
    public void activeResolutionAdvisories() {

        boolean[] activeResolutionAdvisories = acasReport.getActiveResolutionAdvisories();
        assertFalse(activeResolutionAdvisories[0]);
        assertFalse(activeResolutionAdvisories[1]);
        assertFalse(activeResolutionAdvisories[2]);
        assertFalse(activeResolutionAdvisories[3]);
        assertFalse(activeResolutionAdvisories[4]);
        assertFalse(activeResolutionAdvisories[5]);
        assertFalse(activeResolutionAdvisories[6]);
        assertFalse(activeResolutionAdvisories[7]);
        assertFalse(activeResolutionAdvisories[8]);
        assertFalse(activeResolutionAdvisories[9]);
        assertFalse(activeResolutionAdvisories[10]);
        assertFalse(activeResolutionAdvisories[11]);
        assertFalse(activeResolutionAdvisories[12]);
        assertFalse(activeResolutionAdvisories[13]);

    }

    @Test
    public void resolutionAdvisoriesComponentsRecord() {

        boolean[] resolutionAdvisoriesComponentsRecord = acasReport.getResolutionAdvisoriesComplementsRecord();
        assertTrue(resolutionAdvisoriesComponentsRecord[0]);
        assertTrue(resolutionAdvisoriesComponentsRecord[1]);
        assertTrue(resolutionAdvisoriesComponentsRecord[2]);
        assertTrue(resolutionAdvisoriesComponentsRecord[3]);

    }

    @Test
    public void resolutionAdvisoryTerminated() {
        assertTrue(acasReport.hasRATerminated());
    }

    @Test
    public void multipleThreatEncounter() {
        assertTrue(acasReport.hasMultiThreatEncounter());
    }

    @Test
    public void threatTypeIndicator() {
        assertEquals(3, (int) acasReport.getThreatType());
    }

    @Test
    public void threatIdentityData() throws BadFormatException {

        ThreatIdentityData threatIdentityData0 = TCASResolutionAdvisoryMsg.extractThreatIdentityData((short) 0, msg);
        assertNull(threatIdentityData0);

        ThreatIdentityData threatIdentityData1 = TCASResolutionAdvisoryMsg.extractThreatIdentityData((short) 1, msg);
        assertNotNull(threatIdentityData1);
        assertEquals(0L, threatIdentityData1.getIcao24().longValue());
        assertNull(threatIdentityData1.getAltitudeCode());
        assertNull(threatIdentityData1.getEncodedRange());
        assertNull(threatIdentityData1.getEncodedBearing());

        ThreatIdentityData threatIdentityData2 = TCASResolutionAdvisoryMsg.extractThreatIdentityData((short) 2, msg);
        assertNotNull(threatIdentityData2);
        assertNull(threatIdentityData2.getIcao24());
        assertEquals(0, threatIdentityData2.getAltitudeCode().shortValue());
        assertEquals(0, threatIdentityData2.getEncodedRange().shortValue());
        assertEquals(0, threatIdentityData2.getEncodedBearing().shortValue());

        ThreatIdentityData threatIdentityData3 = TCASResolutionAdvisoryMsg.extractThreatIdentityData((short) 3, msg);
        assertNull(threatIdentityData3);

    }

    @Test
    public void threatIdentityDataWithIcao() throws BadFormatException {
        // icao24 set here is 0xabcdef = 0b 10101011 11001101 11101111
        byte[] msg = new byte[] {
                0b00110000, 0b01000000, 0b01000000, 0b01110110, (byte) 0b10101111, 0b00110111, (byte) 0b10111100
        };

        final ACASActiveResolutionAdvisoryReport acasReport = new ACASActiveResolutionAdvisoryReport(msg);

        assertTrue(acasReport.hasRATerminated());
        assertTrue(acasReport.hasMultiThreatEncounter());

        assertEquals(0b01000000010000, acasReport.getActiveRA());
        assertArrayEquals(new boolean[] {
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
        }, acasReport.getActiveResolutionAdvisories());

        assertEquals(1, acasReport.getRACRecord());
        assertArrayEquals(new boolean[] { false, false, false, true }, acasReport.getResolutionAdvisoriesComplementsRecord());

        assertEquals(0b10101011110011011110111100, acasReport.getThreatIdentity().intValue());
        assertEquals(0xabcdef, acasReport.getThreatIdentityData().getIcao24().intValue());
    }

}
