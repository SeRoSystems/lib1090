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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.cpr.PositionDecoder;
import de.serosystems.lib1090.cpr.PositionDecoderSupplier;
import de.serosystems.lib1090.decoding.diffbaroalt.DiffBaroAlt;
import de.serosystems.lib1090.decoding.quality.NICSupplements;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.*;
import de.serosystems.lib1090.msgs.squitter.*;
import de.serosystems.lib1090.msgs.tisb.CoarsePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineAirbornePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineSurfacePositionMsg;
import de.serosystems.lib1090.msgs.tisb.ManagementMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Generic stateful decoder for Mode S Messages.
 * <p>
 * The decoder keeps state per target, such as its ADS-B version, NIC supplements and the CPR frames for position
 * decoding, and that state depends on the order in which a target's messages are decoded. It is not thread-safe:
 * an instance must only be used by one thread at a time, including {@link #extractPosition} and the other getters,
 * which read and update the same state. To decode on several threads, use one instance per thread and route each
 * message to an instance by its address, e.g. {@code Math.floorMod(msg.getAddress().getAddress(), n)} for
 * {@code n} instances, so that a target's messages always reach the same instance in the order they were received.
 */
@SuppressWarnings("unused")
public class StatefulModeSDecoder {
    private static final Duration DECODER_TIMEOUT = Duration.ofMillis(3600_000L);
    private static final int CLEANUP_INTERVAL = 1_000_000;

    // the state of a new target, built once rather than as a capturing lambda on every lookup
    private final Function<QualifiedAddress, DecoderData> newDecoderData;
    private final boolean decodeDf19Adsb;
    private final boolean tisbV2CompatibilityMode;
    private final boolean checkParity;
    private final boolean decodeBeforeVersionKnown;
    // keyed by the qualified address including its source, so that ADS-B, ADS-R and TIS-B receptions
    // for the same address never share version, NIC supplements or CPR state (see QualifiedAddress)
    private final Map<QualifiedAddress, DecoderData> decoderData = new HashMap<>();
    private int afterLastCleanup;
    private Instant latestTimestamp;

    /**
     * Create an instance of the stateful decoder with default parameters.
     * Same as {@code StatefulModeSDecoder.builder().build()}.
     */
    public StatefulModeSDecoder() {
        this(new Builder());
    }

    private StatefulModeSDecoder(Builder builder) {
        this.newDecoderData = builder.positionDecoderSupplier.andThen(DecoderData::new);
        this.decodeDf19Adsb = builder.decodeDf19Adsb;
        this.tisbV2CompatibilityMode = builder.tisbV2CompatibilityMode;
        this.checkParity = builder.checkParity;
        this.decodeBeforeVersionKnown = builder.decodeBeforeVersionKnown;
    }

    /**
     * This function decodes a half-decoded Mode S reply to its
     * deepest possible specialization. Use getType() or instanceof to check its
     * actual type afterward.
     *
     * @param modes     the incompletely decoded Mode S message
     * @param timestamp time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws BadFormatException if format contains error, or if the parity check is enabled (see
     *                            {@link Builder#checkParity(boolean)}) and an extended squitter fails it
     */
    public ModeSDownlinkMsg decode(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException {
        Objects.requireNonNull(timestamp, "timestamp");

        // reject corrupted extended squitters before they reach the per-target state, see ED-102B §2.2.4.5 a; the
        // other formats overlay their parity with the address or the interrogator code and keep no state here
        if (checkParity && (modes.getDownlinkFormat() == 17 || modes.getDownlinkFormat() == 18 ||
                modes.getDownlinkFormat() == 19 && modes.getFirstField() == 0 && decodeDf19Adsb) &&
                !modes.checkParity())
            throw new BadFormatException("Parity check failed", modes.getHexMessage());

        if (++afterLastCleanup >= CLEANUP_INTERVAL) {
            afterLastCleanup = 0;
            clearDecoders();
        }

        latestTimestamp = timestamp;

        switch (modes.getDownlinkFormat()) {
            case 0:
                return new ShortACAS(modes);
            case 4:
                return new AltitudeReply(modes);
            case 5:
                return new IdentifyReply(modes);
            case 11:
                return new AllCallReply(modes);
            case 16:
                return new LongACAS(modes);
            case 17:
            case 18:
            case 19:
                // check whether this is an ADS-B message, see ED-102B §2.2.3.2 Figure 2-3
                // note: ED-102B §2.2.3.2 itself (citing RTCA DO-181F §2.2.14.5 / EUROCAE ED-73F
                // §3.18.5) states that non-military ADS-B receivers shall not accept DF=19 messages,
                // and its accompanying NOTE records that DF=19/AF=0 was previously accepted but is
                // no longer guaranteed to conform to the ADS-B format; so it is only decoded as such
                // if explicitly enabled (see Builder#decodeDf19Adsb)
                if (modes.getDownlinkFormat() == 17 ||
                        modes.getDownlinkFormat() == 18 && modes.getFirstField() < 2 ||
                        modes.getDownlinkFormat() == 19 && modes.getFirstField() == 0 && decodeDf19Adsb) {
                    return decodeADSB(modes, timestamp);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 2 && !tisbV2CompatibilityMode &&
                        modes.getAddress().getType() == QualifiedAddress.Type.MODEA_TRACK) {
                    // CF=2 with IMF=1 (Mode A code and track file number) is reserved per ED-102B; still
                    // decoded as TIS-B in TIS-B v2 compatibility mode (see Builder#tisbV2CompatibilityMode)
                    return new ExtendedSquitter(modes);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 2 ||
                        modes.getDownlinkFormat() == 18 && modes.getFirstField() == 5) {
                    return decodeTISB(modes, timestamp);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 3) {
                    // CF=3 (coarse TIS-B airborne position) is reserved per ED-102B; still
                    // decoded as such in TIS-B v2 compatibility mode (see Builder#tisbV2CompatibilityMode)
                    ExtendedSquitter es1090 = new ExtendedSquitter(modes);
                    if (tisbV2CompatibilityMode) return new CoarsePositionMsg(es1090, timestamp);
                    return es1090;
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 4) {
                    // TIS-B or ADS-R Management Message
                    return new ManagementMessage(modes);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 6) {
                    return decodeADSR(modes, timestamp);
                } else if (modes.getDownlinkFormat() == 19) {
                    return new MilitaryExtendedSquitter(modes);
                }
                return modes; // this should never happen
            case 20:
                return new CommBAltitudeReply(modes);
            case 21:
                return new CommBIdentifyReply(modes);
            case 24:
                return new CommDExtendedLengthMsg(modes);
            default:
                return modes; // unknown mode s reply
        }
    }

    // dispatches on FTC per the same message-type table ADS-B rebroadcast uses, ED-102B
    // §2.2.3.2.2 TABLE 2-9, as required for ADS-R Message Processing, ED-102B §2.2.18.4; a
    // combination outside TABLE 2-9 falls back to es1090
    private TypeCodedExtendedSquitter decodeADSR(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException {
        // interpret ME field as ADS-R
        TypeCodedExtendedSquitter es1090 = new TypeCodedExtendedSquitter(modes);

        // ADS-R has not been specified for version 0 at all; version 3 and any
        // higher (not yet defined) version is decoded as version 3, since, per
        // ED-102B §2.2.7.1, newer versions are expected to be backward compatible
        // with version 3

        // we need stateful decoding, because ADS-R version > 0 can only be assumed
        // if matching version info in operational status has been found.
        DecoderData dd = getDecoderData(modes.getAddress(), timestamp);

        // what kind of extended squitter?
        byte ftc = es1090.getFormatTypeCode();

        if (ftc == 31) { // operational status message, determines the assumed version
            int subtype = es1090.getMessage()[0] & 0x7;
            byte version = (byte) ((es1090.getMessage()[5] >>> 5) & 0x7);
            if (!carriesVersion(subtype, version)) return es1090;

            dd.adsbVersion = version;
            if (dd.adsbVersion == 0) return es1090; // ADS-R is not specified for version 0
            if (subtype == 0) {
                // airborne
                switch (dd.adsbVersion) {
                    case 1:
                        de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV1Msg s1 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV1Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s1.getNICSupplementA());
                        captureNICSupplements(dd, s1.getCapabilityClass());
                        return s1;
                    case 2:
                        de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg s2 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s2.getNICSupplementA());
                        captureNICSupplements(dd, s2.getCapabilityClass());
                        return s2;
                    case 3:
                    default:
                        de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV3Msg s3 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV3Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s3.getNICSupplementA());
                        captureNICSupplements(dd, s3.getCapabilityClass());
                        return s3;
                }
            } else if (subtype == 1) {
                // surface
                switch (dd.adsbVersion) {
                    case 1:
                        de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV1Msg s1 =
                                new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV1Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s1.getNICSupplementA());
                        return s1;
                    case 2:
                        de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg s2 =
                                new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s2.getNICSupplementA());
                        captureNICSupplements(dd, s2.getCapabilityClass());
                        return s2;
                    case 3:
                    default:
                        de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV3Msg s3 =
                                new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV3Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s3.getNICSupplementA());
                        captureNICSupplements(dd, s3.getCapabilityClass());
                        return s3;
                }
            }

            return es1090;
        }

        // the target's decoder data assumes version 0; ADS-R is not specified for
        // version 0, so we cannot decode this message any further
        if (dd.adsbVersion == 0) return es1090;

        if (ftc >= 1 && ftc <= 4) {
            // identification message
            switch (dd.adsbVersion) {
                case 1:
                    return new de.serosystems.lib1090.msgs.adsr.IdentificationV1Msg(es1090);
                case 2:
                    return new de.serosystems.lib1090.msgs.adsr.IdentificationV2Msg(es1090);
                case 3:
                default:
                    if (ftc == 1) break; // format type code 1 is not defined for identification in version 3
                    return new de.serosystems.lib1090.msgs.adsr.IdentificationV3Msg(es1090);
            }
        }

        if (ftc >= 5 && ftc <= 8) {
            // surface position message
            switch (dd.adsbVersion) {
                case 1:
                    return new de.serosystems.lib1090.msgs.adsr.SurfacePositionV1Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 2:
                    return new de.serosystems.lib1090.msgs.adsr.SurfacePositionV2Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 3:
                default:
                    return new de.serosystems.lib1090.msgs.adsr.SurfacePositionV3Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
            }
        }

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            // airborne position message
            switch (dd.adsbVersion) {
                case 1:
                    return new de.serosystems.lib1090.msgs.adsr.AirbornePositionV1Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 2:
                    return new de.serosystems.lib1090.msgs.adsr.AirbornePositionV2Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 3:
                default:
                    return new de.serosystems.lib1090.msgs.adsr.AirbornePositionV3Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
            }
        }

        if (ftc == 19) { // possible velocity message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1 || subtype == 2) { // velocity over ground
                AirborneVelocityMsg velocity;
                switch (dd.adsbVersion) {
                    case 1:
                        velocity = new de.serosystems.lib1090.msgs.adsr.VelocityOverGroundV1Msg(es1090);
                        break;
                    case 2:
                        velocity = new de.serosystems.lib1090.msgs.adsr.VelocityOverGroundV2Msg(es1090);
                        break;
                    case 3:
                    default:
                        de.serosystems.lib1090.msgs.adsr.AirborneVelocityV3Msg velocityV3 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneVelocityV3Msg(es1090);
                        // the only message carrying NIC supplement D, and only when it reports no
                        // barometric altitude difference in its place; a velocity message without it
                        // says nothing about D, so what was known of it survives
                        if (velocityV3.hasNICSupplementD())
                            dd.nicSupplements = dd.nicSupplements.withD(velocityV3.getNICSupplementD());
                        velocity = velocityV3;
                        break;
                }
                dd.geoMinusBaro = velocity.getDiffBaroAlt();
                return (TypeCodedExtendedSquitter) velocity;
            } else if (subtype == 3 || subtype == 4) {  // airspeed & heading
                switch (dd.adsbVersion) {
                    case 1:
                        de.serosystems.lib1090.msgs.adsr.AirspeedHeadingV1Msg a1 =
                                new de.serosystems.lib1090.msgs.adsr.AirspeedHeadingV1Msg(es1090);
                        dd.geoMinusBaro = a1.getDiffBaroAlt();
                        return a1;
                    case 2:
                        de.serosystems.lib1090.msgs.adsr.AirspeedHeadingV2Msg a2 =
                                new de.serosystems.lib1090.msgs.adsr.AirspeedHeadingV2Msg(es1090);
                        dd.geoMinusBaro = a2.getDiffBaroAlt();
                        return a2;
                    case 3:
                    default:
                        break; // subtypes 3/4 are not defined for version 3 and up, ED-102B §2.2.18.4.4 NOTE 3
                }
            }
        }

        if (ftc == 28) { // aircraft status message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1) { // emergency/priority status
                switch (dd.adsbVersion) {
                    case 1:
                        return new de.serosystems.lib1090.msgs.adsr.EmergencyOrPriorityStatusV1Msg(es1090);
                    case 2:
                        return new de.serosystems.lib1090.msgs.adsr.EmergencyOrPriorityStatusV2Msg(es1090);
                    case 3:
                    default:
                        return new de.serosystems.lib1090.msgs.adsr.EmergencyOrPriorityStatusV3Msg(es1090);
                }
            }
        }

        if (ftc == 29) {
            // unlike ADS-B, ADS-R of version 1 targets uses subtype 1 in the version 2 format, ED-102A §2.2.18.4.6
            // NOTE 1; the version 1 ADS-B format, subtype 0, is not used for ADS-R
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 1 && dd.adsbVersion <= 2)
                return new de.serosystems.lib1090.msgs.adsr.TargetStateAndStatusV2Msg(es1090);
            else if (subtype == 1 && dd.adsbVersion >= 3)
                return new de.serosystems.lib1090.msgs.adsr.TargetStateAndStatusV3Msg(es1090);
        }

        if (ftc == 26 && dd.adsbVersion >= 3) { // Wx AIREP message, check subtype
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 0)
                return new de.serosystems.lib1090.msgs.adsr.WxAIREPAircraftStateMsg(es1090);
            else if (subtype == 1)
                return new de.serosystems.lib1090.msgs.adsr.WxAIREPWeatherStateMsg(es1090);
            else if (subtype == 2)
                return new de.serosystems.lib1090.msgs.adsr.WxAIREPAlternateWeatherStateMsg(es1090);
        }

        return es1090;
    }

    private TypeCodedExtendedSquitter decodeTISB(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException {
        // interpret ME field as standard ADS-B
        TypeCodedExtendedSquitter es1090 = new TypeCodedExtendedSquitter(modes);

        DecoderData dd = getDecoderData(modes.getAddress(), timestamp);

        // what kind of extended squitter?
        byte ftc = es1090.getFormatTypeCode();

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            return new FineAirbornePositionMsg.WithNICSupplements(es1090, timestamp, dd.nicSupplements);
        } else if (ftc >= 5 && ftc <= 8) {
            return new FineSurfacePositionMsg.WithNICSupplements(es1090, timestamp, dd.nicSupplements);
        } else if (ftc == 19) {
            int subtype = es1090.getMessage()[0] & 0x7;
            if (subtype == 1 || subtype == 2) {
                de.serosystems.lib1090.msgs.tisb.VelocityOverGroundMsg vog =
                        new de.serosystems.lib1090.msgs.tisb.VelocityOverGroundMsg(es1090);
                dd.geoMinusBaro = vog.getDiffBaroAlt();
                dd.nicSupplements = dd.nicSupplements.withA(vog.getNICSupplementA());
                return vog;
            } else if ((subtype == 3 || subtype == 4) && tisbV2CompatibilityMode) {
                // subtypes 3/4 (airspeed & heading) are reserved per ED-102B; still decoded
                // as such in TIS-B v2 compatibility mode (see Builder#tisbV2CompatibilityMode)
                de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg ash =
                        new de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg(es1090);
                dd.geoMinusBaro = ash.getDiffBaroAlt();
                dd.nicSupplements = dd.nicSupplements.withA(ash.getNICSupplementA());
                return ash;
            }
        } else if (ftc >= 1 && ftc <= 4) {
            return new de.serosystems.lib1090.msgs.tisb.IdentificationMsg(es1090);
        }

        return es1090;
    }

    // dispatches on FTC per ED-102B §2.2.3.2.2 TABLE 2-9 (message-type determination table); a
    // combination outside TABLE 2-9 falls back to es1090
    private TypeCodedExtendedSquitter decodeADSB(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException {
        // interpret ME field as standard ADS-B
        TypeCodedExtendedSquitter es1090 = new TypeCodedExtendedSquitter(modes);

        // only (assumed or confirmed) version 0 is decoded as such; version 3 and any
        // higher (not yet defined) version is decoded as version 3, since, per
        // ED-102B §2.2.7.1, newer versions are expected to be backward compatible
        // with version 3

        // we need stateful decoding, because ADS-B version > 0 can only be assumed
        // if matching version info in operational status has been found.
        DecoderData dd = getDecoderData(modes.getAddress(), timestamp);

        // whether messages that version 0 does not define are decoded although the target's version is not
        // known, in the format of the version that defines them, see Builder#decodeBeforeVersionKnown
        boolean versionUnknown = decodeBeforeVersionKnown && dd.adsbVersion == 0;

        // what kind of extended squitter?
        byte ftc = es1090.getFormatTypeCode();

        if (ftc >= 1 && ftc <= 4) {
            // identification message
            switch (dd.adsbVersion) {
                case 0:
                    return new IdentificationV0Msg(es1090);
                case 1:
                    return new IdentificationV1Msg(es1090);
                case 2:
                    return new IdentificationV2Msg(es1090);
                case 3:
                default:
                    if (ftc == 1) break; // format type code 1 is not defined for identification in version 3
                    return new IdentificationV3Msg(es1090);
            }
        }

        if (ftc >= 5 && ftc <= 8) {
            switch (dd.adsbVersion) {
                case 0:
                    return new SurfacePositionV0Msg(es1090, timestamp);
                case 1:
                    return new SurfacePositionV1Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 2:
                    return new SurfacePositionV2Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 3:
                default:
                    return new SurfacePositionV3Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
            }
        }

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            // airborne position message
            switch (dd.adsbVersion) {
                case 0:
                    return new AirbornePositionV0Msg(es1090, timestamp);
                case 1:
                    return new AirbornePositionV1Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 2:
                    return new AirbornePositionV2Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
                case 3:
                default:
                    return new AirbornePositionV3Msg.WithNICSupplements(
                            es1090, timestamp, dd.nicSupplements);
            }
        }

        if (ftc == 19) { // possible velocity message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1 || subtype == 2) { // velocity over ground
                AirborneVelocityMsg velocity;
                switch (dd.adsbVersion) {
                    case 0:
                        velocity = new VelocityOverGroundV0Msg(es1090);
                        break;
                    case 1:
                        velocity = new VelocityOverGroundV1Msg(es1090);
                        break;
                    case 2:
                        velocity = new VelocityOverGroundV2Msg(es1090);
                        break;
                    case 3:
                    default:
                        AirborneVelocityV3Msg velocityV3 =
                                new AirborneVelocityV3Msg(es1090);
                        // the only message carrying NIC supplement D, and only when it reports no
                        // barometric altitude difference in its place; a velocity message without it
                        // says nothing about D, so what was known of it survives
                        if (velocityV3.hasNICSupplementD())
                            dd.nicSupplements = dd.nicSupplements.withD(velocityV3.getNICSupplementD());
                        velocity = velocityV3;
                        break;
                }
                dd.geoMinusBaro = velocity.getDiffBaroAlt();
                return (TypeCodedExtendedSquitter) velocity;
            } else if (subtype == 3 || subtype == 4) {  // airspeed & heading
                switch (dd.adsbVersion) {
                    case 0:
                        AirspeedHeadingV0Msg a0 = new AirspeedHeadingV0Msg(es1090);
                        dd.geoMinusBaro = a0.getDiffBaroAlt();
                        return a0;
                    case 1:
                        AirspeedHeadingV1Msg a1 = new AirspeedHeadingV1Msg(es1090);
                        dd.geoMinusBaro = a1.getDiffBaroAlt();
                        return a1;
                    case 2:
                        AirspeedHeadingV2Msg a2 = new AirspeedHeadingV2Msg(es1090);
                        dd.geoMinusBaro = a2.getDiffBaroAlt();
                        return a2;
                    case 3:
                    default:
                        break; // subtypes 3/4 are not defined for ADS-B version 3 and up
                }
            }
        }

        if (ftc == 23) { // Test Message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;
            if (subtype == 7 && (dd.adsbVersion == 1 || versionUnknown)) // Mode A code
                return new ModeACodeV1Msg(es1090);
        }

        if (ftc == 24) {
            int subtype = es1090.getMessage()[0] & 0x7;
            // not gated on the version: surface system status is not transmitted by ADS-B equipment, but by the
            // surface surveillance system that generated it (ED-102B §2.2.3.2.7.4.3), so the sender's ADS-B
            // version does not apply
            if (subtype == 1)
                return new SurfaceSystemStatusMsg(es1090);
        }

        // High Velocity and/or Altitude (HVA) message, check subtype
        if (ftc == 25 && (dd.adsbVersion >= 3 || versionUnknown)) {
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 0)
                return new HVAPositionMsg(es1090);
            else if (subtype == 1)
                return new HVAVelocityMsg(es1090);
        }

        if (ftc == 26 && (dd.adsbVersion >= 3 || versionUnknown)) { // Wx AIREP message, check subtype
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 0)
                return new WxAIREPAircraftStateMsg(es1090);
            else if (subtype == 1)
                return new WxAIREPWeatherStateMsg(es1090);
            else if (subtype == 2)
                return new WxAIREPAlternateWeatherStateMsg(es1090);
        }

        // TODO: Wx PIREP message (FTC=27)

        if (ftc == 28) { // aircraft status message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1) {
                if (dd.adsbVersion >= 3)
                    return new EmergencyOrPriorityStatusV3Msg(es1090);
                else if (dd.adsbVersion == 2)
                    return new EmergencyOrPriorityStatusV2Msg(es1090);
                else if (dd.adsbVersion == 1)
                    return new EmergencyOrPriorityStatusV1Msg(es1090);
                else
                    return new EmergencyOrPriorityStatusV0Msg(es1090);
            } else if (subtype == 2 && (dd.adsbVersion > 1 || versionUnknown))
                return new ACASResolutionAdvisoryMsg(es1090);
            else if (subtype == 3 && (dd.adsbVersion >= 3 || versionUnknown))
                return new CASOperationalCoordinationMsg(es1090);
            else if (subtype == 4 && (dd.adsbVersion >= 3 || versionUnknown))
                return new UASRPASContingencyMsg(es1090);
        }

        if (ftc == 29) {
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            // DO-260A reserved ME bit 11 of TYPE 29 as ZERO; set to ONE, it marks a version 0 TCP/TCP+1
            // message, which is to be discarded, ED-102B §N.2.5 NOTE 1. In subtype 1, ME bit 11 is part
            // of the selected altitude.
            boolean hasMe11Bit = (es1090.getMessage()[1] & 0x20) != 0;
            if (subtype == 0 && (dd.adsbVersion == 1 || versionUnknown && !hasMe11Bit)) {
                return new TargetStateAndStatusV1Msg(es1090);
            } else if (subtype == 1 && dd.adsbVersion == 2) {
                return new TargetStateAndStatusV2Msg(es1090);
            } else if (subtype == 1 && (dd.adsbVersion >= 3 || versionUnknown)) {
                return new TargetStateAndStatusV3Msg(es1090);
            }
        }

        if (ftc == 31) { // operational status message
            int subtype = es1090.getMessage()[0] & 0x7;
            byte version = (byte) ((es1090.getMessage()[5] >>> 5) & 0x7);
            if (!carriesVersion(subtype, version)) return es1090;

            dd.adsbVersion = version;
            if (subtype == 0) {
                // airborne
                switch (dd.adsbVersion) {
                    case 0:
                        return new OperationalStatusV0Msg(es1090);
                    case 1:
                        AirborneOperationalStatusV1Msg s1 = new AirborneOperationalStatusV1Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s1.getNICSupplementA());
                        return s1;
                    case 2:
                        AirborneOperationalStatusV2Msg s2 = new AirborneOperationalStatusV2Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s2.getNICSupplementA());
                        return s2;
                    case 3:
                    default:
                        AirborneOperationalStatusV3Msg s3 = new AirborneOperationalStatusV3Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s3.getNICSupplementA());
                        return s3;
                }
            } else if (subtype == 1) {
                // surface
                switch (dd.adsbVersion) {
                    case 1:
                        SurfaceOperationalStatusV1Msg s1 = new SurfaceOperationalStatusV1Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s1.getNICSupplementA());
                        return s1;
                    case 2:
                        SurfaceOperationalStatusV2Msg s2 = new SurfaceOperationalStatusV2Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s2.getNICSupplementA());
                        captureNICSupplements(dd, s2.getCapabilityClass());
                        return s2;
                    case 3:
                    default:
                        SurfaceOperationalStatusV3Msg s3 = new SurfaceOperationalStatusV3Msg(es1090);
                        dd.nicSupplements = dd.nicSupplements.withA(s3.getNICSupplementA());
                        captureNICSupplements(dd, s3.getCapabilityClass());
                        return s3;
                }
            }
        }

        return es1090;
    }

    /**
     * @param rawMessage the Mode S message as byte array
     * @param timestamp  time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError propagated from the ModeSDownlinkMsg constructor
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage), timestamp);
    }

    /**
     * @param rawMessage the Mode S message as byte array
     * @param noCRC      indicates whether the CRC has been subtracted from the parity field
     * @param timestamp  time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError propagated from the ModeSDownlinkMsg constructor
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(byte[] rawMessage, boolean noCRC, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage, noCRC), timestamp);
    }

    /**
     * @param rawMessage the Mode S message in hex representation
     * @param timestamp  time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError propagated from the ModeSDownlinkMsg constructor
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage), timestamp);
    }

    /**
     * @param rawMessage the Mode S message in hex representation
     * @param noCRC      indicates whether the CRC has been subtracted from the parity field
     * @param timestamp  time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError propagated from the ModeSDownlinkMsg constructor
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(String rawMessage, boolean noCRC, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage, noCRC), timestamp);
    }

    /**
     * Decode CPR encoded position from airborne and surface position messages.
     *
     * @param address  the target's qualified address to decode position for
     * @param msg      which contains the encoded position
     * @param receiver position for reasonableness test and as reference for surface positions (can be null); without
     *                 it, a surface position decodes only once the target has a previous position, e.g. from its
     *                 airborne phase
     * @return decoded WGS84 position or null if message doesn't have a valid position or decoding fails
     */
    public Position extractPosition(QualifiedAddress address, PositionMsg msg, Position receiver) {
        Objects.requireNonNull(address, "address must not be null");

        if (msg == null || !msg.hasValidPosition())
            return null;

        CPREncodedPosition cpr = msg.getCPREncodedPosition();
        DecoderData dd = getDecoderData(address, cpr.getTimestamp());
        Position pos = dd.posDec.decodePosition(cpr, receiver);

        if (pos != null && msg.hasValidAltitude()) {
            pos.setAltitude(Double.valueOf(msg.getAltitude()));
            pos.setAltitudeType(msg.getAltitudeType());
        }

        return pos;
    }

    /**
     * @param reply a Mode S message
     * @return the ADS-B version as tracked by the decoder. Version 0 is assumed until an Operational Status message
     * for a higher version is received for the given target
     */
    public byte getAdsbVersion(ModeSDownlinkMsg reply) {
        if (reply == null) return 0;
        return getAdsbVersion(reply.getAddress());
    }

    /**
     * @param address a qualified address
     * @return the ADS-B version as tracked by the decoder. Version 0 is assumed until an Operational Status message
     * for a higher version is received for the given target
     */
    public byte getAdsbVersion(QualifiedAddress address) {
        if (address == null) return 0;
        DecoderData dd = decoderData.get(address);
        return dd == null ? 0 : dd.adsbVersion;
    }

    /**
     * Get the difference between geometric and barometric altitude as tracked by the decoder. The value is derived
     * from ADS-B {@link AirspeedHeadingMsg} and {@link VelocityOverGroundMsg}. The method returns the most recent
     * report, also if it reports no difference, which the transmitter signals when it has lost geometric or
     * barometric altitude: check {@link DiffBaroAlt#hasDifference()}.
     *
     * @param reply a Mode S message
     * @return the most recently reported difference between geometric and barometric altitude, or null if none has
     * been received
     */
    public DiffBaroAlt getDiffBaroAlt(ModeSDownlinkMsg reply) {
        if (reply == null) return null;
        DecoderData dd = decoderData.get(reply.getAddress());
        return dd == null ? null : dd.geoMinusBaro;
    }

    /**
     * Clean state by removing decoders not used for more than an hour. This happens automatically
     * every 1 Mio messages.
     */
    public void clearDecoders() {
        // nothing decoded yet, so no time to measure the age of an entry against
        if (latestTimestamp == null) return;
        Instant cutoff = latestTimestamp.minus(DECODER_TIMEOUT);
        decoderData.values().removeIf(dd -> dd.lastUsed.isBefore(cutoff));
    }

    private DecoderData getDecoderData(QualifiedAddress address, Instant timestamp) {
        DecoderData dd = decoderData.computeIfAbsent(address, newDecoderData);
        dd.lastUsed = timestamp;
        return dd;
    }

    /**
     * Create a new builder for this decoder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents the state of a decoder for a certain target
     */
    private static class DecoderData {
        byte adsbVersion;
        NICSupplements nicSupplements = NICSupplements.none();
        DiffBaroAlt geoMinusBaro;
        Instant lastUsed;
        PositionDecoder posDec;

        DecoderData(PositionDecoder posDec) {
            adsbVersion = 0;
            this.posDec = posDec;
        }
    }

    /**
     * Builder for {@link StatefulModeSDecoder}.
     */
    public static class Builder {
        private PositionDecoderSupplier positionDecoderSupplier = PositionDecoderSupplier.statefulPositionDecoder();
        private boolean decodeDf19Adsb = false;
        private boolean tisbV2CompatibilityMode = true;
        private boolean checkParity = true;
        private boolean decodeBeforeVersionKnown = true;

        private Builder() {
        }

        /**
         * Sets a custom position decoding logic. Note that the default logic uses quite strict
         * reasonableness tests. If your data comes from a heterogeneous receiver network with
         * fluctuating timestamps, you might want to use {@link #positionDecoderSupplierDefault(boolean)}
         * with speed tests disabled.
         *
         * @param positionDecoderSupplier a custom {@link PositionDecoderSupplier}
         * @return this builder
         */
        public Builder positionDecoderSupplier(PositionDecoderSupplier positionDecoderSupplier) {
            this.positionDecoderSupplier = positionDecoderSupplier;
            return this;
        }

        /**
         * Sets the default position decoder supplier, but can disable the speed test.
         *
         * @param disableSpeedTest disable speed test
         * @return this builder
         */
        public Builder positionDecoderSupplierDefault(boolean disableSpeedTest) {
            this.positionDecoderSupplier = PositionDecoderSupplier.statefulPositionDecoder(disableSpeedTest);
            return this;
        }

        /**
         * Enables decoding of downlink format 19 with application field 0 as ADS-B.
         * <p>
         * Per ED-102B, this combination shall no longer be used for ADS-B, since we cannot be
         * sure that it actually contains an ADS-B message. Note that even under ED-102A, processing
         * of this message was also only optional.
         * Defaults to false; enable only if you rely on this legacy behavior.
         *
         * @param decodeDf19Adsb whether to decode DF=19/AF=0 as ADS-B
         * @return this builder
         */
        public Builder decodeDf19Adsb(boolean decodeDf19Adsb) {
            this.decodeDf19Adsb = decodeDf19Adsb;
            return this;
        }

        /**
         * Enables TIS-B version 2 compatibility mode.
         * <p>
         * Per ED-102B, CF=3 (coarse TIS-B airborne position), CF=2 with IMF=1 (a target addressed by
         * its Mode A code and track file number, ED-102B §2.2.17.3.1.2) and velocity message subtypes
         * 3/4 (airspeed and heading) are reserved and no longer used. In compatibility mode, they are
         * still decoded as such, as a fallback for TIS-B services that have not yet transitioned
         * away from them. TIS-B itself does not distinguish versions, so this is a purely
         * decoder-side compatibility switch, not a per-target version assumption.
         * Defaults to true.
         *
         * @param tisbV2CompatibilityMode whether to decode CF=3, CF=2 with IMF=1 and velocity subtypes 3/4
         * @return this builder
         */
        public Builder tisbV2CompatibilityMode(boolean tisbV2CompatibilityMode) {
            this.tisbV2CompatibilityMode = tisbV2CompatibilityMode;
            return this;
        }

        /**
         * Enables the parity check of extended squitters (DF=17, DF=18, and DF=19 if decoded as ADS-B, see
         * {@link #decodeDf19Adsb(boolean)}).
         * <p>
         * With the check enabled, {@link #build() the decoder} throws {@link BadFormatException} for an extended
         * squitter whose parity does not match, before it reaches the per-target state. Disable it only if the input
         * has already been checked, e.g. by the receiver: without the check, a corrupted message can change the
         * target's ADS-B version, NIC supplements and CPR frames, and create state for a garbage address.
         * Defaults to true.
         *
         * @param checkParity whether to reject extended squitters that fail the parity check
         * @return this builder
         * @see ModeSDownlinkMsg#checkParity()
         */
        public Builder checkParity(boolean checkParity) {
            this.checkParity = checkParity;
            return this;
        }

        /**
         * Enables decoding of ADS-B messages that version 0 does not define before the target's version is known.
         * <p>
         * The decoder learns a target's ADS-B version only from its Aircraft Operational Status message and assumes
         * version 0 until then; version 0 itself cannot be confirmed, since it has no version field (ED-102A
         * §N.2.3.1). With this option enabled, the default, the decoder follows ED-102B §N.1.2: "Prior to receiving
         * the Version Number, exceptions to assuming Version 0 include messages that were not defined in Version 0."
         * Until the version is known, each message that ED-102B TABLE N-1 does not define for version 0 is decoded
         * in the format of the version that defines it:
         * <ul>
         *     <li>TYPE Code 23 subtype 7, the Mode A code, as version 1, whose layout DO-260 Change 1 also allows
         *     version 0 transmitters</li>
         *     <li>TYPE Code 29 subtype 0, target state and status, as version 1, unless ME bit 11 is set, which marks
         *     a version 0 TCP/TCP+1 message (ED-102B §N.2.5 NOTE 1)</li>
         *     <li>TYPE Code 29 subtype 1, target state and status, as version 3, as §N.1.2 prescribes; a version 2
         *     message read this way loses only NIC<sub>BARO</sub>, ME bit 44, which version 3 reserves</li>
         *     <li>TYPE Code 28 subtype 2, the TCAS resolution advisory, which version 2 introduced</li>
         *     <li>the message types and subtypes version 3 introduced: TYPE Code 25 (HVA), TYPE Code 26 (Wx AIREP)
         *     and TYPE Code 28 subtypes 3 and 4</li>
         * </ul>
         * Once the version is known, this option has no effect. ADS-R is not affected: it is not defined for version
         * 0, so an ADS-R message is decoded only once the version is known.
         * <p>
         * Disabled, a message that version 0 does not define is decoded only once the version is known, as ED-102A
         * §N.1.2 prescribes, and as ED-102A and ED-102B §N.2.5 NOTE 2 require for TYPE Code 29, contrary to ED-102B
         * §N.1.2: "Prior to generation of a Target Status Report, the 1090 MHz ADS-B Receiving Subsystem must
         * positively confirm that any received message with a TYPE Code of 29 has originated from a target aircraft
         * with an ADS-B Version Number other than Zero (0)."
         * Defaults to true.
         *
         * @param decodeBeforeVersionKnown whether to decode messages that version 0 does not define before the
         *                                 target's version is known
         * @return this builder
         */
        public Builder decodeBeforeVersionKnown(boolean decodeBeforeVersionKnown) {
            this.decodeBeforeVersionKnown = decodeBeforeVersionKnown;
            return this;
        }

        /**
         * @return a new {@link StatefulModeSDecoder} instance configured by this builder
         */
        public StatefulModeSDecoder build() {
            return new StatefulModeSDecoder(this);
        }
    }

    /**
     * Whether a TYPE Code 31 message is an operational status, and so carries the ADS-B version: subtype 0
     * (airborne) in every version, subtype 1 (surface) from version 1 on. Subtypes 2 to 7 are reserved, ED-102B
     * §2.2.3.2.2 TABLE 2-9 and §2.2.3.2.7.2.2 TABLE 2-46, and version 0 defines no surface subtype.
     */
    private static boolean carriesVersion(int subtype, byte version) {
        return subtype == 0 || subtype == 1 && version != 0;
    }

    /**
     * Take the NIC supplements a capability class layout supplies, if it supplies any.
     * <p>
     * NIC supplement B and C live inside the Capability Class Code, so which of them a message carries
     * depends on the layout its format selector selects — supplement B only on the ADS-R airborne
     * layouts, supplement C only on the version 2 and 3 surface layouts. A layout that carries neither,
     * including the fallback for a selector this library does not model, leaves the decoder state
     * untouched rather than overwriting it with a default.
     * <p>
     * That is deliberately the same behavior as before the operational status refactor: an
     * unrecognized selector used to make the whole message throw, so the state was never updated. The
     * difference is that the rest of the message now decodes.
     */
    private static void captureNICSupplements(DecoderData dd, CapabilityClassCode cc) {
        if (cc instanceof ADSRAirborneCapabilityClassCode)
            dd.nicSupplements = dd.nicSupplements.withB(
                    ((ADSRAirborneCapabilityClassCode) cc).getNICSupplementB());
        if (cc instanceof SurfaceCapabilityClassCodeV2V3)
            dd.nicSupplements = dd.nicSupplements.withC(
                    ((SurfaceCapabilityClassCodeV2V3) cc).getNICSupplementC());
    }
}
