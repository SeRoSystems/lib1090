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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.PositionMsg;
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
    public void tssV0Me11Set_shouldNotDecode() throws UnspecifiedFormatError, BadFormatException {
        // decoder assumes ADS-B v0 and should not decode TSS
        final ModeSDownlinkMsg reply = decoder.decode(TargetStateAndStatusV2MsgTest.TSS_WITH_ME11_BIT_SET, Instant.EPOCH);

        assertEquals(TypeCodedExtendedSquitter.class, reply.getClass());
        assertFalse(reply instanceof TargetStateAndStatusV1Msg);
        assertFalse(reply instanceof TargetStateAndStatusV2Msg);
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
