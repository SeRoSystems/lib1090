package de.serosystems.lib1090.msgs.modes;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ACASActiveResolutionAdvisorReportTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00110000, (byte) 0b00000000, (byte) 0b00000011, (byte) 0b11111100, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };

        for (byte b : msg)
            System.out.print(StringUtils.leftPad(Integer.toBinaryString(b & 0xFF), 8, '0') + " ");

    }

    @Test
    public void bdsCode() {

        short bdsCode = ACASActiveResolutionAdvisorReport.extractBdsCode(msg);
        Assert.assertEquals(30, bdsCode);

    }

    @Test
    public void activeResolutionAdvisories() {

        boolean[] activeResolutionAdvisories = ACASActiveResolutionAdvisorReport.extractActiveResolutionAdvisories(msg);
        Assert.assertFalse(activeResolutionAdvisories[0]);
        Assert.assertFalse(activeResolutionAdvisories[1]);
        Assert.assertFalse(activeResolutionAdvisories[2]);
        Assert.assertFalse(activeResolutionAdvisories[3]);
        Assert.assertFalse(activeResolutionAdvisories[4]);
        Assert.assertFalse(activeResolutionAdvisories[5]);
        Assert.assertFalse(activeResolutionAdvisories[6]);
        Assert.assertFalse(activeResolutionAdvisories[7]);
        Assert.assertFalse(activeResolutionAdvisories[8]);
        Assert.assertFalse(activeResolutionAdvisories[9]);
        Assert.assertFalse(activeResolutionAdvisories[10]);
        Assert.assertFalse(activeResolutionAdvisories[11]);
        Assert.assertFalse(activeResolutionAdvisories[12]);
        Assert.assertFalse(activeResolutionAdvisories[13]);

        boolean[] result = ACASActiveResolutionAdvisorReport.computeActiveResolutionAdvisories(activeResolutionAdvisories, ACASActiveResolutionAdvisorReport.extractMultipleThreatEncounter(msg));
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.length);

    }

    @Test
    public void resolutionAdvisoriesComponentsRecord() {

        boolean[] resolutionAdvisoriesComponentsRecord = ACASActiveResolutionAdvisorReport.extractResolutionAdvisoriesComponentsRecord(msg);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[0]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[1]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[2]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[3]);

    }

    @Test
    public void resolutionAdvisoryTerminated() {

        boolean resolutionAdvisoryTerminated = ACASActiveResolutionAdvisorReport.extractResolutionAdvisoryTerminated(msg);
        Assert.assertTrue(resolutionAdvisoryTerminated);

    }

    @Test
    public void multipleThreatEncounter() {

        boolean multipleThreatEncounter = ACASActiveResolutionAdvisorReport.extractMultipleThreatEncounter(msg);
        Assert.assertTrue(multipleThreatEncounter);

    }

    @Test
    public void threatTypeIndicator() {

        short threatTypeIndicator = ACASActiveResolutionAdvisorReport.extractThreatTypeIndicator(msg);
        Assert.assertEquals(3, threatTypeIndicator);

    }

    @Test
    public void threatIdentityData() {

        ThreatIdentityData threatIdentityData0 = ACASActiveResolutionAdvisorReport.extractThreatIdentityData((short) 0, msg);
        Assert.assertNull(threatIdentityData0);

        ThreatIdentityData threatIdentityData1 = ACASActiveResolutionAdvisorReport.extractThreatIdentityData((short) 1, msg);
        Assert.assertNotNull(threatIdentityData1);
        Assert.assertEquals(0L, threatIdentityData1.getIcao24().longValue());
        Assert.assertNull(threatIdentityData1.getAltitudeCode());
        Assert.assertNull(threatIdentityData1.getThreatIdentityDataRange());
        Assert.assertNull(threatIdentityData1.getThreatIdentityDataBearing());

        ThreatIdentityData threatIdentityData2 = ACASActiveResolutionAdvisorReport.extractThreatIdentityData((short) 2, msg);
        Assert.assertNotNull(threatIdentityData2);
        Assert.assertNull(threatIdentityData2.getIcao24());
        Assert.assertEquals(0, threatIdentityData2.getAltitudeCode().shortValue());
        Assert.assertEquals(0, threatIdentityData2.getThreatIdentityDataRange().shortValue());
        Assert.assertEquals(0, threatIdentityData2.getThreatIdentityDataBearing().shortValue());

        ThreatIdentityData threatIdentityData3 = ACASActiveResolutionAdvisorReport.extractThreatIdentityData((short) 3, msg);
        Assert.assertNull(threatIdentityData3);

    }

}
