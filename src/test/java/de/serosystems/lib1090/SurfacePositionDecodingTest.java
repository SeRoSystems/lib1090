package de.serosystems.lib1090;

import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.PositionMsg;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.util.Base64;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurfacePositionDecodingTest {

    private final static String DATA_FILE = "20221005-DEDUPLICATED_MODE_S-Surface-Positions-a53436.json";

    private StatefulModeSDecoder decoder;

    @Before
    public void setUp() {
        decoder = new StatefulModeSDecoder();
    }

    @Test
    public void decodeSurfacePos() throws Exception {
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(DATA_FILE)).getFile();
        JSONArray json;
        try (FileReader reader = new FileReader(path)) {
            json = (JSONArray) new JSONParser().parse(reader);
        }

        // decode
        for (Object dedup : json) {
            JSONObject reply = (JSONObject) ((JSONArray) ((JSONObject) dedup).get("reply")).get(0);
            byte[] raw = Base64.getDecoder().decode((String) reply.get("reply"));
            long timestamp = Long.parseLong((String) reply.get("sensorTimestamp"));

            ModeSDownlinkMsg msg = decoder.decode(raw, timestamp);

            if (StatefulModeSDecoder.isPosition(msg)) {
                Position pos = decoder.extractPosition(msg.getAddress(), (PositionMsg) msg, null);
                if (StatefulModeSDecoder.isSurfacePosition(msg)) {
                    assertTrue(pos.isReasonable());
                    assertEquals(Position.AltitudeType.ABOVE_GROUND_LEVEL, pos.getAltitudeType());
                    assertEquals(0., pos.getAltitude(), 0.);
                    assertEquals(38.85, pos.getLatitude(), 0.005);
                    assertEquals(-77.038, pos.getLongitude(), 0.0003);
                }
            }
        }
    }

}
