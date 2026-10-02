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

package de.serosystems.lib1090;

import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;
import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAlt;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisory;
import de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisoryReport;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
import de.serosystems.lib1090.msgs.squitter.SurfaceOperationalModeCodeV2V3;
import de.serosystems.lib1090.msgs.tisb.FineAirbornePositionMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class StatefulModeSDecoderTest {

    private StatefulModeSDecoder decoder;

    @BeforeEach
    public void setUp() {
        decoder = newDecoder();
    }

    @Test
    public void tssBeforeVersionKnownMe11Set_decodesAsV3() throws UnspecifiedFormatError, BadFormatException {
        // the version is not known yet: subtype 1 is decoded as version 3 (ED-102B §N.1.2), and ME bit 11, part of
        // the selected altitude, does not keep it from being decoded
        final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV2MsgTest.TSS_WITH_ME11_BIT_SET, Instant.EPOCH);

        assertInstanceOf(TargetStateAndStatusV3Msg.class, reply);
        assertEquals((907 - 1) * 32, ((TargetStateAndStatusV3Msg) reply).getSelectedAltitude().intValue());
    }

    @Test
    public void tssV2ME11Set_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
        // tell decoder that the aircraft uses ADS-B v2
        decoder.decode(AirborneOperationalStatusV2MsgTest.A_OPSTAT_V2, Instant.EPOCH);

        // decode message with ME bit 11 set
        final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV2MsgTest.TSS_WITH_ME11_BIT_SET, Instant.EPOCH);

        assertInstanceOf(TargetStateAndStatusV2Msg.class, reply);

        TargetStateAndStatusV2Msg tss = (TargetStateAndStatusV2Msg) reply;

        assertFalse(tss.getSILSupplement());
        assertFalse(tss.isFMSSelectedAltitude());
        assertEquals((907 - 1) * 32, tss.getSelectedAltitude().intValue());
    }

    @Test
    public void tssV1_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
        decoder.decode(TargetStateAndStatusV1MsgTest.A_OPSTAT_V1, Instant.EPOCH);

        final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV1MsgTest.TSS_V1, Instant.EPOCH);

        assertInstanceOf(TargetStateAndStatusV1Msg.class, reply);

        TargetStateAndStatusV1Msg tss = (TargetStateAndStatusV1Msg) reply;

        assertEquals(9, tss.getNACpEncoded());
    }

    @Test
    public void modeACodeV1_shouldDecode() throws UnspecifiedFormatError, BadFormatException {
        decoder.decode(TargetStateAndStatusV1MsgTest.A_OPSTAT_V1, Instant.EPOCH);

        final ModeSDownlinkMsg reply = decoder.decode(ModeACodeV1MsgTest.MODE_A_CODE_V1, Instant.EPOCH);

        assertInstanceOf(ModeACodeV1Msg.class, reply);
        assertEquals("6513", ((ModeACodeV1Msg) reply).getIdentity());
    }

    // ------------------------------------------------------------------ separation by address source

    private static final String ADDRESS = "485020";

    /**
     * The given first byte (DF and CA/CF), the address, the given 7-byte ME field and zero parity.
     */
    private static byte[] frame(int firstByte, byte[] me) {
        StringBuilder hex = new StringBuilder(String.format("%02X", firstByte)).append(ADDRESS);
        for (byte b : me) hex.append(String.format("%02X", b));
        return Tools.hexStringToByteArray(hex + "000000");
    }

    private static final int DF17 = 0x8D;
    private static final int DF18_CF0 = 0x90;
    private static final int DF18_TISB = 0x92;
    private static final int DF18_ADSR = 0x96;

    /**
     * Airborne operational status of the given version, NIC supplement A at ME 44.
     */
    private static byte[] opStatus(int firstByte, int version, boolean nicSupplementA) {
        byte[] me = new byte[7];
        me[0] = (byte) (31 << 3);
        me[5] = (byte) (version << 5 | (nicSupplementA ? 0x10 : 0x00));
        return frame(firstByte, me);
    }

    /**
     * Airborne position of type code 11, whose NIC depends on supplement A.
     */
    private static byte[] position(int firstByte) {
        byte[] me = new byte[7];
        me[0] = (byte) (11 << 3);
        return frame(firstByte, me);
    }

    /**
     * Velocity over ground; in TIS-B it carries NIC supplement A at ME 47.
     */
    private static byte[] tisbVelocity(boolean nicSupplementA) {
        byte[] me = new byte[7];
        me[0] = (byte) 0x99;
        me[5] = (byte) (nicSupplementA ? 0x02 : 0x00);
        return frame(DF18_TISB, me);
    }

    /**
     * Airborne velocity over ground with the given Difference from Barometric Altitude, ME bits 49-56, and ME bit 36
     * set, which TIS-B requires for the difference.
     */
    private static byte[] velocity(int firstByte, int diffBaroAltEncoded) {
        byte[] me = new byte[7];
        me[0] = (byte) 0x99;
        me[4] = 0x10;
        me[6] = (byte) diffBaroAltEncoded;
        return frame(firstByte, me);
    }

    /**
     * A report without a difference replaces the last one: the transmitter sends all zeros once it has lost
     * geometric or barometric altitude (ED-102B §2.2.3.2.6.1.15), and the stored difference used to outlive that.
     */
    @Test
    public void diffBaroAlt_isReplacedByAReportWithoutDifference() throws UnspecifiedFormatError, BadFormatException {
        decoder.decode("8D3C6586F8000000004000069135", Instant.EPOCH);
        ModeSDownlinkMsg v2 = decoder.decode("8D3C65869900650CA0040A020B82", Instant.EPOCH);
        assertEquals(212.5, decoder.getDiffBaroAlt(v2).getDifference().getLower());
        decoder.decode("8D3C65869900650CA004000267F5", Instant.EPOCH);
        assertEquals(DiffBaroAlt.Status.UNKNOWN, decoder.getDiffBaroAlt(v2).getStatus());
        assertNull(decoder.getDiffBaroAlt(v2).getDifference());

        decoder.decode("8D3C6587F8000000006000460F1E", Instant.EPOCH);
        ModeSDownlinkMsg v3 = decoder.decode("8D3C65879900650CA0048A7BD760", Instant.EPOCH);
        assertTrue(decoder.getDiffBaroAlt(v3).hasDifference());
        for (String unavailable : new String[]{"8D3C65879900650CA004007CBDD7", "8D3C65879980650CA00400ED7AA8",
                "8D3C65879900650CA0051072508F"}) {
            decoder.decode("8D3C65879900650CA0048A7BD760", Instant.EPOCH);
            decoder.decode(unavailable, Instant.EPOCH);
            assertFalse(decoder.getDiffBaroAlt(v3).hasDifference(), unavailable);
        }

        // ADS-R velocity is decoded once the version is known
        decoder.decode(opStatus(DF18_ADSR, 2, false), Instant.EPOCH);
        for (int firstByte : new int[]{DF18_ADSR, DF18_TISB}) {
            ModeSDownlinkMsg reply = decoder.decode(velocity(firstByte, 10), Instant.EPOCH);
            assertTrue(decoder.getDiffBaroAlt(reply).hasDifference());
            decoder.decode(velocity(firstByte, 0), Instant.EPOCH);
            assertEquals(DiffBaroAlt.Status.UNKNOWN, decoder.getDiffBaroAlt(reply).getStatus());
        }
    }

    @Test
    public void addressSource_isDerivedFromDownlinkFormatAndCF() throws UnspecifiedFormatError, BadFormatException {
        assertEquals(QualifiedAddress.Source.TRANSPONDER, decoder.decode(position(DF17), Instant.EPOCH).getAddress().getSource());
        assertEquals(QualifiedAddress.Source.NON_TRANSPONDER, decoder.decode(position(DF18_CF0), Instant.EPOCH).getAddress().getSource());
        assertEquals(QualifiedAddress.Source.TIS_B, decoder.decode(position(DF18_TISB), Instant.EPOCH).getAddress().getSource());
        assertEquals(QualifiedAddress.Source.ADS_R, decoder.decode(position(DF18_ADSR), Instant.EPOCH).getAddress().getSource());
    }

    /**
     * ED-102B §2.2.18.4: ADS-R reports are distinct from ADS-B reports, so the version an ADS-R
     * operational status announces must not change how direct ADS-B receptions are decoded.
     */
    @Test
    public void adsrVersion_doesNotLeakIntoAdsb() throws UnspecifiedFormatError, BadFormatException {
        ModeSDownlinkMsg adsr = decoder.decode(opStatus(DF18_ADSR, 2, false), Instant.EPOCH);
        ModeSDownlinkMsg adsb = decoder.decode(position(DF17), Instant.EPOCH);

        assertEquals(2, decoder.getAdsbVersion(adsr));
        assertEquals(0, decoder.getAdsbVersion(adsb));
        assertInstanceOf(AirbornePositionV0Msg.class, adsb);
    }

    /**
     * ED-102B §2.2.10.1.2: reports are organized by address source, too, so a transponder and a
     * non-transponder device using the same address keep their own version.
     */
    @Test
    public void transponderAndNonTransponder_areTrackedSeparately() throws UnspecifiedFormatError, BadFormatException {
        ModeSDownlinkMsg transponder = decoder.decode(opStatus(DF17, 2, false), Instant.EPOCH);
        ModeSDownlinkMsg nonTransponder = decoder.decode(position(DF18_CF0), Instant.EPOCH);

        assertEquals(2, decoder.getAdsbVersion(transponder));
        assertEquals(0, decoder.getAdsbVersion(nonTransponder));
    }

    /**
     * ED-102B §2.2.17.4: TIS-B is processed independently of ADS-B, so NIC supplement A from a TIS-B
     * velocity message grades neither ADS-B positions, nor does the ADS-B one grade TIS-B positions.
     */
    @Test
    public void nicSupplementA_staysWithItsSource() throws UnspecifiedFormatError, BadFormatException {
        decoder.decode(opStatus(DF17, 2, false), Instant.EPOCH);
        decoder.decode(tisbVelocity(true), Instant.EPOCH);
        AirbornePositionV2Msg adsb = (AirbornePositionV2Msg) decoder.decode(position(DF17), Instant.EPOCH);
        assertEquals((byte) 8, adsb.getNICEncoded());

        decoder = newDecoder();
        decoder.decode(opStatus(DF17, 2, true), Instant.EPOCH);
        FineAirbornePositionMsg tisb = (FineAirbornePositionMsg) decoder.decode(position(DF18_TISB), Instant.EPOCH);
        assertEquals((byte) 8, tisb.getNICEncoded());
    }

    /**
     * ED-102B §2.2.17.4: TIS-B keeps its own track file, so an even ADS-B and an odd TIS-B position
     * are never combined in a global decode, while the same frames both received via ADS-B are.
     */
    @Test
    public void cprFrames_areNotCombinedAcrossSources() throws UnspecifiedFormatError, BadFormatException {
        // the well-known pair for 40621D; with the odd frame the newer, it decodes to 52.2658°N 3.9389°E
        final String even = "8D40621D58C382D690C8AC2863A7";
        final String odd = "8D40621D58C386435CC412692AD6";
        final String oddViaTisb = "92" + odd.substring(2); // DF=18, CF=2, IMF=0: same address and ME
        final Instant t0 = Instant.ofEpochSecond(1_600_000_000L);
        final Instant t1 = t0.plusSeconds(1);

        assertNull(extractPosition(even, t0));
        Position both = extractPosition(odd, t1);
        assertNotNull(both);
        assertEquals(52.2658, both.getLatitude(), 1e-4);
        assertEquals(3.9389, both.getLongitude(), 1e-4);

        decoder = newDecoder();
        assertNull(extractPosition(even, t0));
        assertNull(extractPosition(oddViaTisb, t1));
    }

    /**
     * A surface position is ambiguous by a 90° quadrant without a reference, so without a receiver position and
     * without a previous position of the target, an even/odd surface pair does not decode rather than throw.
     */
    @Test
    public void surfacePair_withoutReference_doesNotDecode() throws UnspecifiedFormatError, BadFormatException {
        // an even and an odd surface position of A53436, taken from the surface position test data
        final String even = "8CA534363BFFF39B73400B6286F4";
        final String odd = "8CA534363BBFE5E18CF64C90C79F";
        final Instant t0 = Instant.ofEpochMilli(1_664_965_023_061L);

        assertNull(extractPosition(even, t0));
        assertNull(extractPosition(odd, t0.plusMillis(1_519)));
    }

    /**
     * The automatic cleanup runs once every million messages, whatever the number of targets, and removes the
     * targets not seen for more than an hour. The counter used not to be reset, so that it ran on every message
     * after the first million, and only with more than 30000 targets.
     */
    @Test
    public void automaticCleanup_runsEveryMillionMessages() throws UnspecifiedFormatError, BadFormatException {
        Instant t0 = Instant.EPOCH;
        ModeSDownlinkMsg stale = new ModeSDownlinkMsg(opStatus(DF17, 2, false));
        QualifiedAddress address = stale.getAddress();
        ModeSDownlinkMsg other = new ModeSDownlinkMsg("8DABCDEF2004200000000082AA9F");

        for (int round = 1; round <= 2; round++) {
            Instant later = t0.plusSeconds(round * 7200L);
            decoder.decode(stale, later.minusSeconds(7200));
            for (int i = 1; i < 999_999; i++) decoder.decode(other, later);
            assertEquals(2, decoder.getAdsbVersion(address));
            decoder.decode(other, later);
            assertEquals(0, decoder.getAdsbVersion(address));
        }
    }

    /**
     * A query before the first decode used to create an entry without a time, on which a later cleanup threw.
     */
    @Test
    public void queryBeforeFirstDecode_doesNotBreakCleanup() throws UnspecifiedFormatError, BadFormatException {
        assertEquals(0, decoder.getAdsbVersion(new ModeSDownlinkMsg("8DABCDEF2004200000000082AA9F")));
        assertNull(decoder.getDiffBaroAlt(new ModeSDownlinkMsg("8DABCDEF2004200000000082AA9F")));
        decoder.decode("8D4840D6202CC371C32CE0576098", Instant.EPOCH);
        assertDoesNotThrow(decoder::clearDecoders);
    }

    /**
     * A position extracted before the first decode gives its entry the time of its CPR frame.
     */
    @Test
    public void extractPositionBeforeFirstDecode_doesNotBreakCleanup()
            throws UnspecifiedFormatError, BadFormatException {
        ModeSDownlinkMsg msg = newDecoder().decode("8D40621D58C382D690C8AC2863A7", Instant.EPOCH);
        assertNull(decoder.extractPosition(msg.getAddress(), (PositionMsg) msg, null));
        assertDoesNotThrow(decoder::clearDecoders);
        decoder.decode("8D4840D6202CC371C32CE0576098", Instant.EPOCH);
        assertDoesNotThrow(decoder::clearDecoders);
    }

    /**
     * ED-102B §2.2.4.5 a: a corrupted extended squitter is rejected before it reaches the per-target state. Here, one
     * flipped ME bit turns a version 2 operational status into a version 0 one.
     */
    @Test
    public void corruptedExtendedSquitter_isRejected() throws UnspecifiedFormatError, BadFormatException {
        StatefulModeSDecoder checking = new StatefulModeSDecoder();
        QualifiedAddress address = checking.decode("8DABCDEFF8000000004930CF8AD4", Instant.EPOCH).getAddress();
        assertEquals(2, checking.getAdsbVersion(address));

        assertThrows(BadFormatException.class, () -> checking.decode("8DABCDEFF8000000000930CF8AD4", Instant.EPOCH));
        assertEquals(2, checking.getAdsbVersion(address));
        assertInstanceOf(IdentificationV2Msg.class, checking.decode("8DABCDEF2004200000000082AA9F", Instant.EPOCH));
    }

    /**
     * With the parity check disabled, the corrupted message is decoded and changes the state.
     */
    @Test
    public void corruptedExtendedSquitter_isDecodedWithoutParityCheck()
            throws UnspecifiedFormatError, BadFormatException {
        StatefulModeSDecoder unchecked = StatefulModeSDecoder.builder().checkParity(false).build();
        QualifiedAddress address = unchecked.decode("8DABCDEFF8000000004930CF8AD4", Instant.EPOCH).getAddress();
        unchecked.decode("8DABCDEFF8000000000930CF8AD4", Instant.EPOCH);
        assertEquals(0, unchecked.getAdsbVersion(address));
    }

    /**
     * If the CRC has already been subtracted from the parity field, an intact message has a parity field of 0.
     */
    @Test
    public void parityCheck_respectsNoCRC() throws UnspecifiedFormatError, BadFormatException {
        StatefulModeSDecoder checking = new StatefulModeSDecoder();
        assertInstanceOf(AirborneOperationalStatusV2Msg.class,
                checking.decode("8DABCDEFF8000000004930000000", true, Instant.EPOCH));
        assertThrows(BadFormatException.class,
                () -> checking.decode("8DABCDEFF8000000004930CF8AD4", true, Instant.EPOCH));
        assertThrows(BadFormatException.class, () -> checking.decode("8DABCDEFF8000000004930000000", Instant.EPOCH));
    }

    /**
     * ED-102B TABLE 2-9 and TABLE 2-46: TYPE Code 31 subtypes 2 to 7 are reserved and carry no version, so they leave
     * the tracked version alone. The decoder skips the parity check, the messages being synthetic.
     */
    @Test
    public void reservedOperationalStatusSubtypes_leaveVersionAlone()
            throws UnspecifiedFormatError, BadFormatException {
        QualifiedAddress address = decoder.decode("8D4840D9F8000000004000000000", Instant.EPOCH).getAddress();
        assertEquals(2, decoder.getAdsbVersion(address));

        decoder.decode("8D4840D9FA000000000000000000", Instant.EPOCH); // subtype 2, version bits 0
        assertEquals(2, decoder.getAdsbVersion(address));
        decoder.decode("8D4840D9FF000000008000000000", Instant.EPOCH); // subtype 7, version bits 4
        assertEquals(2, decoder.getAdsbVersion(address));
        assertInstanceOf(IdentificationV2Msg.class, decoder.decode("8D4840D920042000000000000000", Instant.EPOCH));
    }

    /**
     * The same holds for ADS-R.
     */
    @Test
    public void reservedOperationalStatusSubtypes_leaveADSRVersionAlone()
            throws UnspecifiedFormatError, BadFormatException {
        QualifiedAddress address = decoder.decode("964840D9F8000000004000000000", Instant.EPOCH).getAddress();
        assertEquals(2, decoder.getAdsbVersion(address));

        decoder.decode("964840D9FA000000000000000000", Instant.EPOCH);
        assertEquals(2, decoder.getAdsbVersion(address));
        decoder.decode("964840D9FF000000008000000000", Instant.EPOCH);
        assertEquals(2, decoder.getAdsbVersion(address));
    }

    /**
     * Version 0 defines no surface operational status, so subtype 1 with version 0 leaves the version alone too.
     */
    @Test
    public void surfaceOperationalStatusOfVersion0_leavesVersionAlone()
            throws UnspecifiedFormatError, BadFormatException {
        QualifiedAddress address = decoder.decode("8D4840D9F8000000004000000000", Instant.EPOCH).getAddress();
        decoder.decode("8D4840D9F9000000000000000000", Instant.EPOCH);
        assertEquals(2, decoder.getAdsbVersion(address));
    }

    /**
     * ED-102B §2.2.3.2.7.1.3.19: a target state and status message with its reserved ME bits 55-56 set decodes for
     * every version it is defined in, instead of throwing out of decode().
     */
    @Test
    public void targetStateWithReservedBitsSet_decodes() throws UnspecifiedFormatError, BadFormatException {
        final String tss = "8D3C6586EA3E90000000022EB763"; // ME 55 set
        decoder.decode("8D3C6586F8000000004000000000", Instant.EPOCH); // version 2
        assertInstanceOf(TargetStateAndStatusV2Msg.class, decoder.decode(tss, Instant.EPOCH));
        decoder.decode("8D3C6586F8000000006000000000", Instant.EPOCH); // version 3
        assertInstanceOf(TargetStateAndStatusV3Msg.class, decoder.decode(tss, Instant.EPOCH));
        decoder.decode("963C6586F8000000006000000000", Instant.EPOCH); // ADS-R, version 3
        assertInstanceOf(de.serosystems.lib1090.msgs.adsr.TargetStateAndStatusV3Msg.class,
                decoder.decode("963C6586EA3E90000000022EB763", Instant.EPOCH));
    }

    /**
     * ED-102B TABLE 2-59 and TABLE 2-60: the top codes of the GPS antenna offset have no upper end.
     */
    @Test
    public void gpsAntennaOffsetTopCodes_areOpenEnded() throws UnspecifiedFormatError, BadFormatException {
        SurfaceOperationalStatusV3Msg status =
                (SurfaceOperationalStatusV3Msg) decoder.decode("8D4840D6F9000F007F6000000000", Instant.EPOCH);
        SurfaceOperationalModeCodeV2V3 om = (SurfaceOperationalModeCodeV2V3) status.getOperationalMode();
        assertEquals(Interval.of(Bound.MORE_THAN, 4, Bound.NONE, Double.NaN), om.getLateralAxisGPSAntennaOffset());
        assertEquals(Interval.of(Bound.MORE_THAN, 58, Bound.NONE, Double.NaN),
                om.getLongitudinalAxisGPSAntennaOffset());
    }

    /**
     * ED-102B TABLE 2-8 reserves DF=18 CF=2 with IMF=1, which ED-102A Table 2-11 uses for a TIS-B target addressed
     * by its Mode A code and track file number: it is decoded as TIS-B only in TIS-B v2 compatibility mode.
     */
    @Test
    public void tisbModeATrackAddress_isDecodedOnlyInCompatibilityMode()
            throws UnspecifiedFormatError, BadFormatException {
        final String modeATrack = "9240621D59C386435CC412692AD6"; // TIS-B fine airborne position, IMF=1
        final String icao = "9240621D58C386435CC412692AD6"; // the same with IMF=0

        ModeSDownlinkMsg compatible = decoder.decode(modeATrack, Instant.EPOCH);
        assertInstanceOf(FineAirbornePositionMsg.class, compatible);
        assertEquals(QualifiedAddress.Type.MODEA_TRACK, compatible.getAddress().getType());

        StatefulModeSDecoder strict = StatefulModeSDecoder.builder()
                .tisbV2CompatibilityMode(false)
                .checkParity(false) // the synthetic messages carry no valid parity
                .build();
        assertEquals(ExtendedSquitter.class, strict.decode(modeATrack, Instant.EPOCH).getClass());
        assertInstanceOf(FineAirbornePositionMsg.class, strict.decode(icao, Instant.EPOCH));
    }

    // ------------------------------------------------------------------ decoding before the version is known

    /**
     * A message that version 0 does not define and the class it is expected to decode to: before the version is
     * known (no or a version 0 operational status) with {@code decodeBeforeVersionKnown} off and on, and once the
     * operational status says version 1, 2 or 3. {@code null} stands for an undecoded
     * {@link TypeCodedExtendedSquitter}.
     */
    private static final class VersionCase {
        final String name;
        final byte[] message;
        final Class<?>[] expected;

        VersionCase(String name, byte[] message, Class<?> off, Class<?> on, Class<?> v1, Class<?> v2, Class<?> v3) {
            this.name = name;
            this.message = message;
            this.expected = new Class<?>[]{off, on, v1, v2, v3};
        }
    }

    /**
     * An ADS-B message with the given first ME byte and ME bit 11 set as given.
     */
    private static byte[] me(int firstByte, boolean me11) {
        byte[] me = new byte[7];
        me[0] = (byte) firstByte;
        me[1] = (byte) (me11 ? 0x20 : 0x00);
        return frame(DF17, me);
    }

    private static final VersionCase[] VERSION_CASES = {
            new VersionCase("Mode A code", me(23 << 3 | 7, false),
                    null, ModeACodeV1Msg.class, ModeACodeV1Msg.class, null, null),
            new VersionCase("TSS subtype 0", me(29 << 3, false),
                    null, TargetStateAndStatusV1Msg.class, TargetStateAndStatusV1Msg.class, null, null),
            new VersionCase("TSS subtype 0, ME bit 11 set", me(29 << 3, true),
                    null, null, TargetStateAndStatusV1Msg.class, null, null),
            new VersionCase("TSS subtype 1, ME bit 11 set", me(29 << 3 | 1 << 1, true),
                    null, TargetStateAndStatusV3Msg.class, null, TargetStateAndStatusV2Msg.class,
                    TargetStateAndStatusV3Msg.class),
            new VersionCase("TCAS RA", me(28 << 3 | 2, false),
                    null, ACASResolutionAdvisoryMsg.class, null, ACASResolutionAdvisoryMsg.class,
                    ACASResolutionAdvisoryMsg.class),
            new VersionCase("HVA position", me(25 << 3, false),
                    null, HVAPositionMsg.class, null, null, HVAPositionMsg.class),
            new VersionCase("HVA velocity", me(25 << 3 | 1 << 1, false),
                    null, HVAVelocityMsg.class, null, null, HVAVelocityMsg.class),
            new VersionCase("Wx AIREP aircraft state", me(26 << 3, false),
                    null, WxAIREPAircraftStateMsg.class, null, null, WxAIREPAircraftStateMsg.class),
            new VersionCase("Wx AIREP weather state", me(26 << 3 | 1 << 1, false),
                    null, WxAIREPWeatherStateMsg.class, null, null, WxAIREPWeatherStateMsg.class),
            new VersionCase("Wx AIREP alternate weather state", me(26 << 3 | 2 << 1, false),
                    null, WxAIREPAlternateWeatherStateMsg.class, null, null, WxAIREPAlternateWeatherStateMsg.class),
            new VersionCase("CAS operational coordination", me(28 << 3 | 3, false),
                    null, CASOperationalCoordinationMsg.class, null, null, CASOperationalCoordinationMsg.class),
            new VersionCase("UAS/RPAS contingency", me(28 << 3 | 4, false),
                    null, UASRPASContingencyMsg.class, null, null, UASRPASContingencyMsg.class),
    };

    @Test
    public void messagesNotDefinedInVersion0_dependOnVersionAndOption()
            throws UnspecifiedFormatError, BadFormatException {
        // null: no operational status; 0: a version 0 operational status, which does not confirm version 0
        final Integer[] versions = {null, 0, 1, 2, 3};
        for (VersionCase c : VERSION_CASES) {
            for (boolean option : new boolean[]{false, true}) {
                for (Integer version : versions) {
                    StatefulModeSDecoder d = StatefulModeSDecoder.builder()
                            .decodeBeforeVersionKnown(option)
                            .checkParity(false)
                            .build();
                    if (version != null) d.decode(opStatus(DF17, version, false), Instant.EPOCH);

                    Class<?> expected = version == null || version == 0 ?
                            c.expected[option ? 1 : 0] : c.expected[version + 1];
                    if (expected == null) expected = TypeCodedExtendedSquitter.class;

                    assertEquals(expected, d.decode(c.message, Instant.EPOCH).getClass(),
                            c.name + ", decodeBeforeVersionKnown " + option + ", version " + version);
                }
            }
        }
    }

    /**
     * An ES TCAS RA broadcast with an unassigned threat bearing (code 61) decodes instead of throwing out of
     * decode().
     */
    @Test
    public void tcasRAWithUnassignedBearing_decodes() throws UnspecifiedFormatError, BadFormatException {
        StatefulModeSDecoder d = StatefulModeSDecoder.builder().build(); // the frames carry valid parity
        d.decode("8D4840D6F800020049490034BAFF", Instant.EPOCH); // version 2 operational status

        ModeSDownlinkMsg msg = d.decode("8D4840D6E28000094282BDD2CD09", Instant.EPOCH);

        assertInstanceOf(ACASResolutionAdvisoryMsg.class, msg);
        ResolutionAdvisory ra = ((ACASResolutionAdvisoryMsg) msg).getResolutionAdvisory();
        assertInstanceOf(TCASResolutionAdvisoryReport.class, ra);
        assertNull(((TCASResolutionAdvisoryReport) ra).getThreatIdentityData().getBearing());
    }

    private Position extractPosition(String raw, Instant timestamp) throws UnspecifiedFormatError, BadFormatException {
        ModeSDownlinkMsg msg = decoder.decode(raw, timestamp);
        return decoder.extractPosition(msg.getAddress(), (PositionMsg) msg, null);
    }

    /**
     * The synthetic messages of these tests carry no valid parity, so the parity check is disabled.
     */
    private static StatefulModeSDecoder newDecoder() {
        return StatefulModeSDecoder.builder().checkParity(false).build();
    }

}
