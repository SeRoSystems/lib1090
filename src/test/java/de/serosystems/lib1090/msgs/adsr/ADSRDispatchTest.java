
package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Which class the decoder produces for an ADS-R message, across every format type code and version the
 * dispatch handles.
 * <p>
 * ADS-R arrives as DF=18 with CF=6, and its decoding is <b>stateful</b>: the version comes from an
 * operational status message seen earlier for the same address, so each case primes the decoder first.
 * That switch is hand-written per version and had never been exercised — it is the same shape of code
 * that left ADS-R version 3 airborne missing three subfields.
 */
class ADSRDispatchTest {

    private static final Instant T = Instant.ofEpochSecond(1_600_000_000L);
    private static final String ADDRESS = "ABCDEF";

    private static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) s.append(String.format("%02X", x));
        return s.toString();
    }

    /** DF=18, CF=6, the given 7-byte ME field, zero parity. */
    private static byte[] frame(byte[] me) {
        return Tools.hexStringToByteArray("96" + ADDRESS + hex(me) + "000000");
    }

    /** Operational status, whose ME 41-43 is what tells the decoder which version to assume. */
    private static byte[] opStatus(int subtype, int version) {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3 | subtype);
        me[5] = (byte) (version << 5);
        return frame(me);
    }

    /** A message whose subtype, where it has one, sits in ME 6-8. */
    private static byte[] message(int ftc, int subtype) {
        byte[] me = new byte[7];
        me[0] = (byte) (ftc << 3 | subtype);
        return frame(me);
    }

    /** Format type codes 26 and 29 carry their subtype in ME 6-7 instead. */
    private static byte[] messageSubtypeInME67(int ftc, int subtype) {
        byte[] me = new byte[7];
        me[0] = (byte) (ftc << 3 | subtype << 1);
        return frame(me);
    }

    /** Prime the decoder to the given version, then decode {@code raw} as the same target. */
    private static ModeSDownlinkMsg decodeAtVersion(int version, int opStatusSubtype, byte[] raw)
            throws Exception {
        StatefulModeSDecoder decoder = new StatefulModeSDecoder();
        decoder.decode(opStatus(opStatusSubtype, version), T);
        return decoder.decode(raw, T);
    }

    private static void assertDispatch(int version, byte[] raw, Class<?> expected) throws Exception {
        ModeSDownlinkMsg msg = decodeAtVersion(version, 0, raw);
        assertEquals(expected, msg.getClass(), "version " + version + " -> " + expected.getSimpleName());
        if (expected != ExtendedSquitter.class)
            assertInstanceOf(ADSRMsg.class, msg, "every decoded ADS-R message carries the marker");
    }

    // ------------------------------------------------------------------ operational status

    @Test
    void operationalStatus() throws Exception {
        StatefulModeSDecoder d = new StatefulModeSDecoder();
        assertEquals(AirborneOperationalStatusV1Msg.class, d.decode(opStatus(0, 1), T).getClass());
        assertEquals(AirborneOperationalStatusV2Msg.class, d.decode(opStatus(0, 2), T).getClass());
        assertEquals(AirborneOperationalStatusV3Msg.class, d.decode(opStatus(0, 3), T).getClass());
        assertEquals(SurfaceOperationalStatusV1Msg.class, d.decode(opStatus(1, 1), T).getClass());
        assertEquals(SurfaceOperationalStatusV2Msg.class, d.decode(opStatus(1, 2), T).getClass());
        assertEquals(SurfaceOperationalStatusV3Msg.class, d.decode(opStatus(1, 3), T).getClass());
    }

    /** ADS-R is not specified for version 0, so the message stays an undecoded extended squitter. */
    @Test
    void versionZeroIsNotDecoded() throws Exception {
        StatefulModeSDecoder d = new StatefulModeSDecoder();
        assertEquals(ExtendedSquitter.class, d.decode(opStatus(0, 0), T).getClass());
        // and with no version established, neither is anything that follows
        assertEquals(ExtendedSquitter.class, d.decode(message(11, 0), T).getClass());
    }

    /** Versions above 3 are decoded as version 3, newer versions being backwards compatible. */
    @Test
    void unknownVersionsDecodeAsVersion3() throws Exception {
        StatefulModeSDecoder d = new StatefulModeSDecoder();
        assertEquals(AirborneOperationalStatusV3Msg.class, d.decode(opStatus(0, 7), T).getClass());
    }

    // ------------------------------------------------------------------ per format type code

    @Test
    void identification() throws Exception {
        for (int ftc = 2; ftc <= 4; ftc++) {
            assertDispatch(1, message(ftc, 0), IdentificationV1Msg.class);
            assertDispatch(2, message(ftc, 0), IdentificationV2Msg.class);
            assertDispatch(3, message(ftc, 0), IdentificationV3Msg.class);
        }
    }

    @Test
    void surfacePosition() throws Exception {
        for (int ftc = 5; ftc <= 8; ftc++) {
            assertDispatch(1, message(ftc, 0), SurfacePositionV1Msg.class);
            assertDispatch(2, message(ftc, 0), SurfacePositionV2Msg.class);
            assertDispatch(3, message(ftc, 0), SurfacePositionV3Msg.WithNICSupplements.class);
        }
    }

    @Test
    void airbornePosition() throws Exception {
        for (int ftc : new int[]{9, 12, 18, 20, 22}) {
            assertDispatch(1, message(ftc, 0), AirbornePositionV1Msg.class);
            assertDispatch(2, message(ftc, 0), AirbornePositionV2Msg.class);
            assertDispatch(3, message(ftc, 0), AirbornePositionV3Msg.WithNICSupplements.class);
        }
    }

    @Test
    void velocityOverGround() throws Exception {
        for (int subtype : new int[]{1, 2}) {
            assertDispatch(1, message(19, subtype), VelocityOverGroundV1Msg.class);
            assertDispatch(2, message(19, subtype), VelocityOverGroundV2Msg.class);
            assertDispatch(3, message(19, subtype), AirborneVelocityV3Msg.class);
        }
    }

    @Test
    void airspeedHeading() throws Exception {
        for (int subtype : new int[]{3, 4}) {
            assertDispatch(1, message(19, subtype), AirspeedHeadingV1Msg.class);
            assertDispatch(2, message(19, subtype), AirspeedHeadingV2Msg.class);
            // version 3 has no airspeed/heading class of its own and falls back to version 2
            assertDispatch(3, message(19, subtype), AirspeedHeadingV2Msg.class);
        }
    }

    @Test
    void emergencyOrPriorityStatus() throws Exception {
        assertDispatch(1, message(28, 1), EmergencyOrPriorityStatusV1Msg.class);
        assertDispatch(2, message(28, 1), EmergencyOrPriorityStatusV2Msg.class);
        assertDispatch(3, message(28, 1), EmergencyOrPriorityStatusV3Msg.class);
        // only subtype 1 is handled
        assertDispatch(2, message(28, 2), ExtendedSquitter.class);
    }

    @Test
    void targetStateAndStatus() throws Exception {
        assertDispatch(2, messageSubtypeInME67(29, 1), TargetStateAndStatusV2Msg.class);
        assertDispatch(3, messageSubtypeInME67(29, 1), TargetStateAndStatusV3Msg.class);
        assertDispatch(2, messageSubtypeInME67(29, 0), ExtendedSquitter.class);
    }

    /**
     * <b>Records a defect.</b> {@code decodeADSR} has a Wx AIREP branch for version 3, but it is
     * unreachable, and so is identification at format type code 1.
     * <p>
     * For DF=18 the address type comes from the IMF flag, and
     * {@code ModeSDownlinkMsg.extractIMF} knows where that flag sits only for format type codes 2–4,
     * 5–8, 9–22, 19, 28, 29 and 31. For anything else it returns {@code null}, the address type becomes
     * {@link de.serosystems.lib1090.msgs.QualifiedAddress.Type#UNKNOWN}, and since the decoder keys its
     * per-target state on the qualified address <i>including its type</i>, such a message lands on a
     * fresh {@code DecoderData} whose version is 0 — where ADS-R decoding stops, being unspecified for
     * version 0.
     * <p>
     * Fixing it needs the standard's IMF position for these format type codes. Until then this test
     * pins the behaviour so the gap is visible rather than silent; it will fail once the cause is
     * addressed, which is the point.
     */
    @Test
    void wxAIREPAndFormatTypeCode1AreUnreachable() throws Exception {
        assertDispatch(3, messageSubtypeInME67(26, 0), ExtendedSquitter.class);
        assertDispatch(3, messageSubtypeInME67(26, 1), ExtendedSquitter.class);
        assertDispatch(3, messageSubtypeInME67(26, 2), ExtendedSquitter.class);
        assertDispatch(1, message(1, 0), ExtendedSquitter.class);
        assertDispatch(2, message(1, 0), ExtendedSquitter.class);

        // the address type is what goes wrong, and it is observable on the message itself
        StatefulModeSDecoder d = new StatefulModeSDecoder();
        d.decode(opStatus(0, 3), T);
        assertEquals(de.serosystems.lib1090.msgs.QualifiedAddress.Type.UNKNOWN,
                d.decode(messageSubtypeInME67(26, 0), T).getAddress().getType());
        assertEquals(de.serosystems.lib1090.msgs.QualifiedAddress.Type.ICAO24,
                d.decode(message(9, 0), T).getAddress().getType());
    }

    /** A format type code the table does not assign stays an undecoded extended squitter. */
    @Test
    void unassignedFormatTypeCodes() throws Exception {
        for (int ftc : new int[]{0, 23, 24, 25, 27, 30}) {
            assertDispatch(2, message(ftc, 0), ExtendedSquitter.class);
        }
    }
}
