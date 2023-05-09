package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonUsgeGICBCapabilityReportTest {

    private static byte[] msg;

    @BeforeAll
    public static void setup() {

        msg = new byte[]{
                (byte) 0b11111010, (byte) 0b10000001, (byte) 0b11000001, (byte) 0b00000000, (byte) 0b00000000,
                (byte) 0b00000000, (byte) 0b00000000
        };

    }

    @Test
    public void commonGICBCapabilityReport() {

        Map<String, Boolean> map = CommonUsageGICBCapabilityReport.extractCommonGICBCapabilityReport(msg);

        assertTrue(map.get("BDS05"));
        assertTrue(map.get("BDS06"));
        assertTrue(map.get("BDS07"));
        assertTrue(map.get("BDS08"));
        assertTrue(map.get("BDS09"));
        assertFalse(map.get("BDS0A"));
        assertTrue(map.get("BDS20"));
        assertFalse(map.get("BDS21"));

        assertTrue(map.get("BDS40"));
        assertFalse(map.get("BDS41"));
        assertFalse(map.get("BDS42"));
        assertFalse(map.get("BDS43"));
        assertFalse(map.get("BDS44"));
        assertFalse(map.get("BDS45"));
        assertFalse(map.get("BDS48"));
        assertTrue(map.get("BDS50"));

        assertTrue(map.get("BDS51"));
        assertTrue(map.get("BDS52"));
        assertFalse(map.get("BDS53"));
        assertFalse(map.get("BDS54"));
        assertFalse(map.get("BDS55"));
        assertFalse(map.get("BDS56"));
        assertFalse(map.get("BDS5F"));
        assertTrue(map.get("BDS60"));

        assertFalse(map.get("BDSE1"));
        assertFalse(map.get("BDSE2"));
        assertFalse(map.get("BDSF1"));

    }

}
