package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.bds.ACASActiveResolutionAdvisoryReport;
import de.serosystems.lib1090.msgs.bds.ThreatIdentityData;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ACASActiveResolutionAdvisoryReportTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b00110000, (byte) 0b00000000, (byte) 0b00000011, (byte) 0b11111100, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };

    }

    @Test
    public void bdsCode() {

        short bdsCode = ACASActiveResolutionAdvisoryReport.extractBdsCode(msg);
        Assert.assertEquals(30, bdsCode);

    }

    @Test
    public void activeResolutionAdvisories() {

        boolean[] activeResolutionAdvisories = ACASActiveResolutionAdvisoryReport.extractActiveResolutionAdvisories(msg);
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

        boolean[] result = ACASActiveResolutionAdvisoryReport.computeActiveResolutionAdvisories(activeResolutionAdvisories, ACASActiveResolutionAdvisoryReport.extractMultipleThreatEncounter(msg));
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.length);

    }

    @Test
    public void resolutionAdvisoriesComponentsRecord() {

        boolean[] resolutionAdvisoriesComponentsRecord = ACASActiveResolutionAdvisoryReport.extractResolutionAdvisoriesComponentsRecord(msg);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[0]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[1]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[2]);
        Assert.assertTrue(resolutionAdvisoriesComponentsRecord[3]);

    }

    @Test
    public void resolutionAdvisoryTerminated() {

        boolean resolutionAdvisoryTerminated = ACASActiveResolutionAdvisoryReport.extractResolutionAdvisoryTerminated(msg);
        Assert.assertTrue(resolutionAdvisoryTerminated);

    }

    @Test
    public void multipleThreatEncounter() {

        boolean multipleThreatEncounter = ACASActiveResolutionAdvisoryReport.extractMultipleThreatEncounter(msg);
        Assert.assertTrue(multipleThreatEncounter);

    }

    @Test
    public void threatTypeIndicator() {

        short threatTypeIndicator = ACASActiveResolutionAdvisoryReport.extractThreatTypeIndicator(msg);
        Assert.assertEquals(3, threatTypeIndicator);

    }

    @Test
    public void threatIdentityData() throws BadFormatException {

        ThreatIdentityData threatIdentityData0 = ACASActiveResolutionAdvisoryReport.extractThreatIdentityData((short) 0, msg);
        Assert.assertNull(threatIdentityData0);

        ThreatIdentityData threatIdentityData1 = ACASActiveResolutionAdvisoryReport.extractThreatIdentityData((short) 1, msg);
        Assert.assertNotNull(threatIdentityData1);
        Assert.assertEquals(0L, threatIdentityData1.getIcao24().longValue());
        Assert.assertNull(threatIdentityData1.getAltitudeCode());
        Assert.assertNull(threatIdentityData1.getEncodedRange());
        Assert.assertNull(threatIdentityData1.getEncodedBearing());

        ThreatIdentityData threatIdentityData2 = ACASActiveResolutionAdvisoryReport.extractThreatIdentityData((short) 2, msg);
        Assert.assertNotNull(threatIdentityData2);
        Assert.assertNull(threatIdentityData2.getIcao24());
        Assert.assertEquals(0, threatIdentityData2.getAltitudeCode().shortValue());
        Assert.assertEquals(0, threatIdentityData2.getEncodedRange().shortValue());
        Assert.assertEquals(0, threatIdentityData2.getEncodedBearing().shortValue());

        ThreatIdentityData threatIdentityData3 = ACASActiveResolutionAdvisoryReport.extractThreatIdentityData((short) 3, msg);
        Assert.assertNull(threatIdentityData3);

    }

}
