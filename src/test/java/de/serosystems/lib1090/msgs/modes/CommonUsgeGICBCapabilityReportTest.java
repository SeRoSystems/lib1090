package de.serosystems.lib1090.msgs.modes;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

public class CommonUsgeGICBCapabilityReportTest {

    private static byte[] msg;

    @BeforeClass
    public static void setup() {

        msg = new byte[]{
                (byte) 0b11111010, (byte) 0b10000001, (byte) 0b11000001, (byte) 0b00000000, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };
        for (byte b : msg)
            System.out.print(StringUtils.leftPad(Integer.toBinaryString(b & 0xFF), 8, '0') + " ");

    }

    @Test
    public void commonGICBCapabilityReport() {

        Map<String, Boolean> map = CommonUsageGICBCapabilityReport.extractCommonGICBCapabilityReport(msg);

        Assert.assertTrue(map.get("BDS05"));
        Assert.assertTrue(map.get("BDS06"));
        Assert.assertTrue(map.get("BDS07"));
        Assert.assertTrue(map.get("BDS08"));
        Assert.assertTrue(map.get("BDS09"));
        Assert.assertFalse(map.get("BDS0A"));
        Assert.assertTrue(map.get("BDS20"));
        Assert.assertFalse(map.get("BDS21"));

        Assert.assertTrue(map.get("BDS40"));
        Assert.assertFalse(map.get("BDS41"));
        Assert.assertFalse(map.get("BDS42"));
        Assert.assertFalse(map.get("BDS43"));
        Assert.assertFalse(map.get("BDS44"));
        Assert.assertFalse(map.get("BDS45"));
        Assert.assertFalse(map.get("BDS48"));
        Assert.assertTrue(map.get("BDS50"));

        Assert.assertTrue(map.get("BDS51"));
        Assert.assertTrue(map.get("BDS52"));
        Assert.assertFalse(map.get("BDS53"));
        Assert.assertFalse(map.get("BDS54"));
        Assert.assertFalse(map.get("BDS55"));
        Assert.assertFalse(map.get("BDS56"));
        Assert.assertFalse(map.get("BDS5F"));
        Assert.assertTrue(map.get("BDS60"));

        Assert.assertFalse(map.get("BDSE1"));
        Assert.assertFalse(map.get("BDSE2"));
        Assert.assertFalse(map.get("BDSF1"));

    }

}
