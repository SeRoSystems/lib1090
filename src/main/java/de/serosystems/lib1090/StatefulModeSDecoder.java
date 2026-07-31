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

import de.serosystems.lib1090.cpr.PositionDecoder;
import de.serosystems.lib1090.cpr.PositionDecoderSupplier;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.*;
import de.serosystems.lib1090.msgs.tisb.CoarsePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineAirbornePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineSurfacePositionMsg;
import de.serosystems.lib1090.msgs.tisb.ManagementMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generic stateful decoder for Mode S Messages.
 */
@SuppressWarnings("unused")
public class StatefulModeSDecoder {
    private static final Duration DECODER_TIMEOUT = Duration.ofMillis(3600_000L);

    private final PositionDecoderSupplier positionDecoderSupplier;
    private final boolean decodeDf19Adsb;
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
        this.positionDecoderSupplier = builder.positionDecoderSupplier;
        this.decodeDf19Adsb = builder.decodeDf19Adsb;
    }

    /**
     * This function decodes a half-decoded Mode S reply to its
     * deepest possible specialization. Use getType() or instanceof to check its
     * actual type afterward.
     *
     * @param modes     the incompletely decoded Mode S message
     * @param timestamp time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError if format is not specified
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        Objects.requireNonNull(timestamp, "timestamp");

        if (++afterLastCleanup > 1000000 && decoderData.size() > 30000) clearDecoders();

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
                // check whether this is an ADS-B message (see Figure 2-2, RTCA DO-260C)
                // note: per DO-260C/DO-181D, DF=19/AF=0 shall no longer be assumed to be ADS-B,
                // so it is only decoded as such if explicitly enabled (see Builder#decodeDf19Adsb)
                if (modes.getDownlinkFormat() == 17 ||
                        modes.getDownlinkFormat() == 18 && modes.getFirstField() < 2 ||
                        modes.getDownlinkFormat() == 19 && modes.getFirstField() == 0 && decodeDf19Adsb) {
                    return decodeADSB(modes, timestamp);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 2 ||
                        modes.getDownlinkFormat() == 18 && modes.getFirstField() == 5) {
                    return decodeTISB(modes, timestamp);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 3) {
                    ExtendedSquitter es1090 = new ExtendedSquitter(modes);
                    return new CoarsePositionMsg(es1090, timestamp);
                } else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 4) {
                    // TIS-B or ADS-R Management Message
                    return new ManagementMessage(new ExtendedSquitter(modes));
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

    private ExtendedSquitter decodeADSR(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        // interpret ME field as ADS-R
        ExtendedSquitter es1090 = new ExtendedSquitter(modes);

        // only (assumed or confirmed) version 0 is decoded as such; version 2 and any
        // higher (not yet defined) version is decoded as version 2, since, per
        // DO-260B, §2.2.7.1, newer versions are expected to be backwards compatible
        // with version 2

        // we need stateful decoding, because ADS-R version > 0 can only be assumed
        // if matching version info in operational status has been found.
        DecoderData dd = getDecoderData(modes.getAddress());

        // what kind of extended squitter?
        byte ftc = es1090.getFormatTypeCode();

        if (ftc >= 1 && ftc <= 4) // identification message
            return new de.serosystems.lib1090.msgs.adsr.IdentificationMsg(es1090);

        if (ftc >= 5 && ftc <= 8) {
            // surface position message
            switch (dd.adsbVersion) {
                case 0:
                    return new de.serosystems.lib1090.msgs.adsr.SurfacePositionV0Msg(es1090, timestamp);
                case 1:
                    de.serosystems.lib1090.msgs.adsr.SurfacePositionV1Msg s1 =
                            new de.serosystems.lib1090.msgs.adsr.SurfacePositionV1Msg(es1090, timestamp);
                    s1.setNICSupplementA(dd.nicSupplA);
                    return s1;
                case 2:
                default:
                    de.serosystems.lib1090.msgs.adsr.SurfacePositionV2Msg s2 =
                            new de.serosystems.lib1090.msgs.adsr.SurfacePositionV2Msg(es1090, timestamp);
                    s2.setNICSupplementA(dd.nicSupplA);
                    s2.setNICSupplementC(dd.nicSupplC);
                    return s2;
            }
        }

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            // airborne position message
            switch (dd.adsbVersion) {
                case 0:
                    return new de.serosystems.lib1090.msgs.adsr.AirbornePositionV0Msg(es1090, timestamp);
                case 1:
                    de.serosystems.lib1090.msgs.adsr.AirbornePositionV1Msg a1 =
                            new de.serosystems.lib1090.msgs.adsr.AirbornePositionV1Msg(es1090, timestamp);
                    a1.setNICSupplementA(dd.nicSupplA);
                    return a1;
                case 2:
                default:
                    de.serosystems.lib1090.msgs.adsr.AirbornePositionV2Msg a2 =
                            new de.serosystems.lib1090.msgs.adsr.AirbornePositionV2Msg(es1090, timestamp);
                    a2.setNICSupplementA(dd.nicSupplA);
                    return a2;
            }
        }

        if (ftc == 19) { // possible velocity message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1 || subtype == 2) { // velocity over ground
                de.serosystems.lib1090.msgs.adsr.VelocityOverGroundMsg velocity =
                        new de.serosystems.lib1090.msgs.adsr.VelocityOverGroundMsg(es1090);
                if (velocity.hasGeoMinusBaroInfo()) dd.geoMinusBaro = (double) velocity.getGeoMinusBaro();
                return velocity;
            } else if (subtype == 3 || subtype == 4) {  // airspeed & heading
                de.serosystems.lib1090.msgs.adsr.AirspeedHeadingMsg airspeed =
                        new de.serosystems.lib1090.msgs.adsr.AirspeedHeadingMsg(es1090);
                if (airspeed.hasGeoMinusBaroInfo()) dd.geoMinusBaro = (double) airspeed.getGeoMinusBaro();
                return airspeed;
            }
        }

        if (ftc == 28) { // aircraft status message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;

            if (subtype == 1) // emergency/priority status
                return new de.serosystems.lib1090.msgs.adsr.EmergencyOrPriorityStatusMsg(es1090);
        }

        if (ftc == 29) {
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            // DO-260B 2.2.3.2.7.1: ignore for ADS-B v0 transponders if ME bit 11 != 0
            boolean hasMe11Bit = (es1090.getMessage()[1] & 0x20) != 0;

            if (subtype == 1 && (dd.adsbVersion > 0 || !hasMe11Bit)) {
                return new de.serosystems.lib1090.msgs.adsr.TargetStateAndStatusMsg(es1090);
            }
        }

        if (ftc == 31) { // operational status message
            int subtype = es1090.getMessage()[0] & 0x7;

            dd.adsbVersion = (byte) ((es1090.getMessage()[5] >>> 5) & 0x7);
            if (subtype == 0) {
                // airborne
                switch (dd.adsbVersion) {
                    case 0:
                        return new de.serosystems.lib1090.msgs.adsr.OperationalStatusV0Msg(es1090);
                    case 1:
                        // TODO: store NIC supplement B as well
                        de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV1Msg s1 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV1Msg(es1090);
                        dd.nicSupplA = s1.hasNICSupplementA();
                        return s1;
                    case 2:
                    default:
                        // TODO: store NIC supplement B as well
                        de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg s2 =
                                new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg(es1090);
                        dd.nicSupplA = s2.hasNICSupplementA();
                        return s2;
                }
            } else if (subtype == 1) {
                // surface
                switch (dd.adsbVersion) {
                    case 0:
                        return new de.serosystems.lib1090.msgs.adsr.OperationalStatusV0Msg(es1090);
                    case 1:
                        de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV1Msg s1 =
                                new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV1Msg(es1090);
                        dd.nicSupplA = s1.hasNICSupplementA();
                        dd.nicSupplC = s1.getNICSupplementC();
                        return s1;
                    case 2:
                    default:
                        de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg s2 =
                                new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg(es1090);
                        dd.nicSupplA = s2.hasNICSupplementA();
                        dd.nicSupplC = s2.getNICSupplementC();
                        return s2;
                }
            }
        }

        return es1090;
    }

    private ExtendedSquitter decodeTISB(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException {
        // interpret ME field as standard ADS-B
        ExtendedSquitter es1090 = new ExtendedSquitter(modes);

        DecoderData dd = getDecoderData(modes.getAddress());

        // what kind of extended squitter?
        byte ftc = es1090.getFormatTypeCode();

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            return new FineAirbornePositionMsg(es1090, timestamp);
        } else if (ftc >= 5 && ftc <= 8) {
            return new FineSurfacePositionMsg(es1090, timestamp);
        } else if (ftc == 19) {
            int subtype = es1090.getMessage()[0] & 0x7;
            if (subtype == 1 || subtype == 2) {
                de.serosystems.lib1090.msgs.tisb.VelocityOverGroundMsg vog =
                        new de.serosystems.lib1090.msgs.tisb.VelocityOverGroundMsg(es1090);
                if (vog.hasGeoMinusBaroInfo())
                    dd.geoMinusBaro = (double) vog.getGeoMinusBaro();
                return vog;
            } else if (subtype == 3 || subtype == 4) {
                de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg ash =
                        new de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg(es1090);
                if (ash.hasGeoMinusBaroInfo())
                    dd.geoMinusBaro = (double) ash.getGeoMinusBaro();
                return ash;
            }
        } else if (ftc >= 1 && ftc <= 4) {
            return new de.serosystems.lib1090.msgs.tisb.IdentificationMsg(es1090);
        }

        return es1090;
    }

    private ExtendedSquitter decodeADSB(ModeSDownlinkMsg modes, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        // interpret ME field as standard ADS-B
        ExtendedSquitter es1090 = new ExtendedSquitter(modes);

        // only (assumed or confirmed) version 0 is decoded as such; version 3 and any
        // higher (not yet defined) version is decoded as version 3, since, per
        // DO-260C, §2.2.7.1, newer versions are expected to be backwards compatible
        // with version 3

        // we need stateful decoding, because ADS-B version > 0 can only be assumed
        // if matching version info in operational status has been found.
        DecoderData dd = getDecoderData(modes.getAddress());

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
                    return new SurfacePositionV1Msg.WithNICSupplementA(es1090, timestamp, dd.nicSupplA);
                case 2:
                    return new SurfacePositionV2Msg.WithNICSupplements(es1090, timestamp, dd.nicSupplA, dd.nicSupplC);
                case 3:
                default:
                    return new SurfacePositionV3Msg.WithNICSupplements(es1090, timestamp, dd.nicSupplA, dd.nicSupplC);
            }
        }

        if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
            // airborne position message
            switch (dd.adsbVersion) {
                case 0:
                    return new AirbornePositionV0Msg(es1090, timestamp);
                case 1:
                    return new AirbornePositionV1Msg.WithNICSupplementA(es1090, timestamp, dd.nicSupplA);
                case 2:
                    return new AirbornePositionV2Msg.WithNICSupplementA(es1090, timestamp, dd.nicSupplA);
                case 3:
                default:
                    return new AirbornePositionV3Msg.WithNICSupplements(es1090, timestamp, dd.nicSupplA, dd.nicSupplD);
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
                        velocity = new AirborneVelocityV3Msg(es1090);
                        break;
                }
                if (velocity.hasDiffBaroAlt()) dd.geoMinusBaro = velocity.getDiffBaroAlt();
                return (ExtendedSquitter) velocity;
            } else if (subtype == 3 || subtype == 4) {  // airspeed & heading
                switch (dd.adsbVersion) {
                    case 0:
                        AirspeedHeadingV0Msg a0 = new AirspeedHeadingV0Msg(es1090);
                        if (a0.hasDiffBaroAlt()) dd.geoMinusBaro = a0.getDiffBaroAlt();
                        return a0;
                    case 1:
                        AirspeedHeadingV1Msg a1 = new AirspeedHeadingV1Msg(es1090);
                        if (a1.hasDiffBaroAlt()) dd.geoMinusBaro = a1.getDiffBaroAlt();
                        return a1;
                    case 2:
                        AirspeedHeadingV2Msg a2 = new AirspeedHeadingV2Msg(es1090);
                        if (a2.hasDiffBaroAlt()) dd.geoMinusBaro = a2.getDiffBaroAlt();
                        return a2;
                    case 3:
                    default:
                        break; // subtypes 3/4 are not defined for ADS-B version 3 and up
                }
            }
        }

        if (ftc == 23) { // Test Message, check subtype
            int subtype = es1090.getMessage()[0] & 0x7;
            if (subtype == 7 && dd.adsbVersion == 1) // Mode A code
                return new ModeACodeV1Msg(es1090);
        }

        if (ftc == 24) {
            int subtype = es1090.getMessage()[0] & 0x7;
            if (subtype == 1)
                return new MLATSystemStatusMsg(es1090);
        }

        if (ftc == 25 && dd.adsbVersion >= 3) { // High Velocity and/or Altitude (HVA) message, check subtype
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 0)
                return new HVAPositionMsg(es1090);
            else if (subtype == 1)
                return new HVAVelocityMsg(es1090);
        }

        if (ftc == 26 && dd.adsbVersion >= 3) { // Wx AIREP message, check subtype
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
                else
                    return new EmergencyOrPriorityStatusV0V1Msg(es1090);
            } else if (subtype == 2 && dd.adsbVersion > 1)
                return new TCASResolutionAdvisoryMsg(es1090);
            else if (subtype == 3 && dd.adsbVersion >= 3)
                return new CASOperationalCoordinationMsg(es1090);
            else if (subtype == 4 && dd.adsbVersion >= 3)
                return new UASRPASContingencyMsg(es1090);
        }

        if (ftc == 29) {
            int subtype = (es1090.getMessage()[0] >>> 1) & 0x3;
            if (subtype == 0 && dd.adsbVersion == 1) {
                return new TargetStateAndStatusV1Msg(es1090);
            } else if (subtype == 1 && dd.adsbVersion >= 2) {
                return new TargetStateAndStatusV2Msg(es1090);
            }
        }

		if (ftc == 31) { // operational status message
			int subtype = es1090.getMessage()[0] & 0x7;

			dd.adsbVersion = (byte) ((es1090.getMessage()[5] >>> 5) & 0x7);
			if (subtype == 0) {
				// airborne
				switch (dd.adsbVersion) {
					case 0:
						return new OperationalStatusV0Msg(es1090);
					case 1:
						AirborneOperationalStatusV1Msg s1 = new AirborneOperationalStatusV1Msg(es1090);
						dd.nicSupplA = s1.hasNICSupplementA();
						return s1;
					case 2:
						AirborneOperationalStatusV2Msg s2 = new AirborneOperationalStatusV2Msg(es1090);
						dd.nicSupplA = s2.hasNICSupplementA();
						return s2;
					case 3:
					default:
						AirborneOperationalStatusV3Msg s3 = new AirborneOperationalStatusV3Msg(es1090);
						dd.nicSupplA = s3.hasNICSupplementA();
						return s3;
				}
			} else if (subtype == 1) {
				// surface
				switch (dd.adsbVersion) {
					case 0: // undefined subtype for v0, handle like any other undefined subtype
						break;
					case 1:
						SurfaceOperationalStatusV1Msg s1 = new SurfaceOperationalStatusV1Msg(es1090);
						dd.nicSupplA = s1.hasNICSupplementA();
						return s1;
					case 2:
						SurfaceOperationalStatusV2Msg s2 = new SurfaceOperationalStatusV2Msg(es1090);
						dd.nicSupplA = s2.hasNICSupplementA();
						dd.nicSupplC = s2.getNICSupplementC();
						return s2;
					case 3:
					default:
						SurfaceOperationalStatusV3Msg s3 = new SurfaceOperationalStatusV3Msg(es1090);
						dd.nicSupplA = s3.hasNICSupplementA();
						dd.nicSupplC = s3.getNICSupplementC();
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
     * @throws UnspecifiedFormatError if format is not specified
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
     * @throws UnspecifiedFormatError if format is not specified
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(byte[] rawMessage, boolean noCRC, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage, noCRC), timestamp);
    }

    /**
     * @param rawMessage the Mode S message in hex representation
     * @param timestamp  time of applicability (or reception) of the message
     * @return an instance of the most specialized ModeSReply possible
     * @throws UnspecifiedFormatError if format is not specified
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
     * @throws UnspecifiedFormatError if format is not specified
     * @throws BadFormatException     if format contains error
     */
    public ModeSDownlinkMsg decode(String rawMessage, boolean noCRC, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        return decode(new ModeSDownlinkMsg(rawMessage, noCRC), timestamp);
    }

    /**
     * Decode CPR encoded position from airborne position messages.
     *
     * @param address  the target's qualified address to decode position for
     * @param msg      which contains the encoded position
     * @param receiver position for reasonableness test (can be null)
     * @return decoded WGS84 position or null if message doesn't have a valid position or decoding fails
     */
    public Position extractPosition(QualifiedAddress address, PositionMsg msg, Position receiver) {
        Objects.requireNonNull(address, "address must not be null");

        if (msg == null || !msg.hasValidPosition())
            return null;

        DecoderData dd = getDecoderData(address);
        Position pos = dd.posDec.decodePosition(msg.getCPREncodedPosition(), receiver);

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
        DecoderData dd = getDecoderData(reply.getAddress());
        return dd.adsbVersion;
    }

    /**
     * Get the difference between geometric and barometric altitude as tracked by the decoder. The value is derived
     * from ADS-B {@link AirspeedHeadingMsg} and {@link VelocityOverGroundMsg}. The method returns the most recent
     * value.
     *
     * @param reply a Mode S message
     * @return the difference between geometric and barometric altitude in feet or null if not present
     */
    public Double getDiffBaroAlt(ModeSDownlinkMsg reply) {
        if (reply == null) return null;
        DecoderData dd = getDecoderData(reply.getAddress());
        return dd.geoMinusBaro;
    }

    /**
     * Clean state by removing decoders not used for more than an hour. This happens automatically
     * every 1 Mio messages if more than 30000 targets are tracked.
     */
    public void clearDecoders() {
        decoderData.values().removeIf(dd -> Duration.between(dd.lastUsed, latestTimestamp).compareTo(DECODER_TIMEOUT) > 0);
    }

    private DecoderData getDecoderData(QualifiedAddress address) {
        DecoderData dd = decoderData.computeIfAbsent(
                address,
                a -> positionDecoderSupplier.andThen(DecoderData::new).apply(a)
        );
        dd.lastUsed = latestTimestamp;
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
        boolean nicSupplA;
        boolean nicSupplC;
        byte nicSupplD;
        Double geoMinusBaro;
        Instant lastUsed;
        PositionDecoder posDec;

        DecoderData(PositionDecoder posDec) {
            adsbVersion = 0;
            lastUsed = Instant.now();
            this.posDec = posDec;
        }
    }

    /**
     * Builder for {@link StatefulModeSDecoder}.
     */
    public static class Builder {
        private PositionDecoderSupplier positionDecoderSupplier = PositionDecoderSupplier.statefulPositionDecoder();
        private boolean decodeDf19Adsb = false;

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
         * Per DO-260C, this combination shall no longer be used for ADS-B, since we cannot be
         * sure that it actually contains an ADS-B message. Note that even under DO-260B, processing
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
         * @return a new {@link StatefulModeSDecoder} instance configured by this builder
         */
        public StatefulModeSDecoder build() {
            return new StatefulModeSDecoder(this);
        }
    }
}
