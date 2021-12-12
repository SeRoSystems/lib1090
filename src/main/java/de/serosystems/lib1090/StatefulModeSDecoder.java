package de.serosystems.lib1090;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.*;
import de.serosystems.lib1090.msgs.tisb.CoarsePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineAirbornePositionMsg;
import de.serosystems.lib1090.msgs.tisb.FineSurfacePositionMsg;
import de.serosystems.lib1090.msgs.tisb.ManagementMessage;

import java.util.HashMap;
import java.util.Map;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Generic stateful decoder for Mode S Messages.
 *
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class StatefulModeSDecoder {
	// mapping from icao24 to Decoder, note that we cannot use byte[] as key!
	private final Map<ModeSDownlinkMsg.QualifiedAddress, DecoderData> decoderData = new HashMap<>();
	private int afterLastCleanup;
	private long latestTimestamp;

	private DecoderData getDecoderData (ModeSDownlinkMsg.QualifiedAddress address) {
		DecoderData dd = decoderData.computeIfAbsent(address, a -> new DecoderData());
		dd.lastUsed = latestTimestamp;
		return dd;
	}

	/**
	 * This function decodes a half-decoded Mode S reply to its
	 * deepest possible specialization. Use getType() to check its
	 * actual type afterwards.
	 * @param modes the incompletely decoded Mode S message
	 * @param timestamp time of applicability (or reception) of the message in milliseconds
	 * @return an instance of the most specialized ModeSReply possible
	 * @throws UnspecifiedFormatError if format is not specified
	 * @throws BadFormatException if format contains error
	 */
	public ModeSDownlinkMsg decode(ModeSDownlinkMsg modes, long timestamp) throws BadFormatException, UnspecifiedFormatError {
		if (++afterLastCleanup > 1000000 && decoderData.size() > 30000) clearDecoders();

		latestTimestamp = timestamp;

		switch (modes.getDownlinkFormat()) {
			case 0: return new ShortACAS(modes);
			case 4: return new AltitudeReply(modes);
			case 5: return new IdentifyReply(modes);
			case 11: return new AllCallReply(modes);
			case 16: return new LongACAS(modes);
			case 17: case 18: case 19:
				// check whether this is an ADS-B message (see Figure 2-2, RTCA DO-260B)
				if (modes.getDownlinkFormat() == 17 ||
						modes.getDownlinkFormat() == 18 && modes.getFirstField() < 2 ||
						modes.getDownlinkFormat() == 19 && modes.getFirstField() == 0) {

					// interpret ME field as standard ADS-B
					ExtendedSquitter es1090 = new ExtendedSquitter(modes);

					// we need stateful decoding, because ADS-B version > 0 can only be assumed
					// if matching version info in operational status has been found.
					DecoderData dd = getDecoderData(modes.getAddress());

					// what kind of extended squitter?
					byte ftc = es1090.getFormatTypeCode();

					if (ftc >= 1 && ftc <= 4) // identification message
						return new de.serosystems.lib1090.msgs.adsb.IdentificationMsg(es1090);

					if (ftc >= 5 && ftc <= 8) {
						// surface position message
						switch(dd.adsbVersion) {
							case 1:
								SurfacePositionV1Msg s1 = new SurfacePositionV1Msg(es1090, timestamp);
								s1.setNICSupplementA(dd.nicSupplA);
								return s1;
							case 2:
								SurfacePositionV2Msg s2 = new SurfacePositionV2Msg(es1090, timestamp);
								s2.setNICSupplementA(dd.nicSupplA);
								s2.setNICSupplementC(dd.nicSupplC);
								return s2;
							default:
								// implicit by version 0
								return new SurfacePositionV0Msg(es1090, timestamp);
						}
					}

					if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
						// airborne position message
						switch(dd.adsbVersion) {
							case 1:
								AirbornePositionV1Msg a1 = new AirbornePositionV1Msg(es1090, timestamp);
								a1.setNICSupplementA(dd.nicSupplA);
								return a1;
							case 2:
								AirbornePositionV2Msg a2 = new AirbornePositionV2Msg(es1090, timestamp);
								a2.setNICSupplementA(dd.nicSupplA);
								return a2;
							default:
								// implicit by version 0
								return new AirbornePositionV0Msg(es1090, timestamp);
						}
					}

					if (ftc == 19) { // possible velocity message, check subtype
						int subtype = es1090.getMessage()[0] & 0x7;

						if (subtype == 1 || subtype == 2) { // velocity over ground
							de.serosystems.lib1090.msgs.adsb.VelocityOverGroundMsg velocity =
									new de.serosystems.lib1090.msgs.adsb.VelocityOverGroundMsg(es1090);
							if (velocity.hasGeoMinusBaroInfo()) dd.geoMinusBaro = velocity.getGeoMinusBaro();
							return velocity;
						} else if (subtype == 3 || subtype == 4) {  // airspeed & heading
							de.serosystems.lib1090.msgs.adsb.AirspeedHeadingMsg airspeed =
									new de.serosystems.lib1090.msgs.adsb.AirspeedHeadingMsg(es1090);
							if (airspeed.hasGeoMinusBaroInfo()) dd.geoMinusBaro = airspeed.getGeoMinusBaro();
							return airspeed;
						}
					}

					if (ftc == 24) {
						int subtype = es1090.getMessage()[0] & 0x7;
						if (subtype == 1)
							return new MLATSystemStatusMsg(es1090);
					}

					if (ftc == 28) { // aircraft status message, check subtype
						int subtype = es1090.getMessage()[0] & 0x7;

						if (subtype == 1) // emergency/priority status
							return new EmergencyOrPriorityStatusMsg(es1090);
						if (subtype == 2) // TCAS resolution advisory report
							return new TCASResolutionAdvisoryMsg(es1090);
					}

					if (ftc == 29) {
						int subtype = (es1090.getMessage()[0]>>>1) & 0x3;
						// DO-260B 2.2.3.2.7.1: ignore for ADS-B v0 transponders if ME bit 11 != 0
						boolean hasMe11Bit = (es1090.getMessage()[1]&0x20) != 0;

						if (subtype == 1 && (dd.adsbVersion > 0 || !hasMe11Bit)) {
							return new TargetStateAndStatusMsg(es1090);
						}
					}

					if (ftc == 31) { // operational status message
						int subtype = es1090.getMessage()[0] & 0x7;

						dd.adsbVersion = (byte) ((es1090.getMessage()[5]>>>5) & 0x7);
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
								default:
									throw new BadFormatException("Airborne operational status has invalid version: " + dd.adsbVersion);
							}
						} else if (subtype == 1) {
							// surface
							switch (dd.adsbVersion) {
								case 0:
									return new OperationalStatusV0Msg(es1090);
								case 1:
									SurfaceOperationalStatusV1Msg s1 = new SurfaceOperationalStatusV1Msg(es1090);
									dd.nicSupplA = s1.hasNICSupplementA();
									dd.nicSupplC = s1.getNICSupplementC();
									return s1;
								case 2:
									SurfaceOperationalStatusV2Msg s2 = new SurfaceOperationalStatusV2Msg(es1090);
									dd.nicSupplA = s2.hasNICSupplementA();
									dd.nicSupplC = s2.getNICSupplementC();
									return s2;
								default:
									throw new BadFormatException("Surface operational status has invalid version: " + dd.adsbVersion);
							}
						}
					}

					return es1090; // unknown extended squitter
				} else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 2 ||
						modes.getDownlinkFormat() == 18 && modes.getFirstField() == 5) {

					// interpret ME field as standard ADS-B
					ExtendedSquitter es1090 = new ExtendedSquitter(modes);

					// we need stateful decoding, because ADS-B version > 0 can only be assumed
					// if matching version info in operational status has been found.
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
								dd.geoMinusBaro = vog.getGeoMinusBaro();
							return vog;
						} else if (subtype == 3 || subtype == 4) {
							de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg ash =
									new de.serosystems.lib1090.msgs.tisb.AirspeedHeadingMsg(es1090);
							if (ash.hasGeoMinusBaroInfo())
								dd.geoMinusBaro = ash.getGeoMinusBaro();
							return ash;
						}
					} else if (ftc >= 1 && ftc <= 4) {
						return new de.serosystems.lib1090.msgs.tisb.IdentificationMsg(es1090);
					}

					return es1090; // unknown TIS-B message
				} else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 3) {
					ExtendedSquitter es1090 = new ExtendedSquitter(modes);
					return new CoarsePositionMsg(es1090, timestamp);
				} else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 4) {
					// TIS-B or ADS-R Management Message
					return new ManagementMessage(new ExtendedSquitter(modes));
				} else if (modes.getDownlinkFormat() == 18 && modes.getFirstField() == 6) {

					// interpret ME field as ADS-R
					ExtendedSquitter es1090 = new ExtendedSquitter(modes);

					// we need stateful decoding, because ADS-R version > 0 can only be assumed
					// if matching version info in operational status has been found.
					DecoderData dd = getDecoderData(modes.getAddress());

					// what kind of extended squitter?
					byte ftc = es1090.getFormatTypeCode();

					if (ftc >= 1 && ftc <= 4) // identification message
						return new de.serosystems.lib1090.msgs.adsr.IdentificationMsg(es1090);

					if (ftc >= 5 && ftc <= 8) {
						// surface position message
						switch(dd.adsbVersion) {
							case 1:
								de.serosystems.lib1090.msgs.adsr.SurfacePositionV1Msg s1 =
										new de.serosystems.lib1090.msgs.adsr.SurfacePositionV1Msg(es1090, timestamp);
								s1.setNICSupplementA(dd.nicSupplA);
								return s1;
							case 2:
								de.serosystems.lib1090.msgs.adsr.SurfacePositionV2Msg s2 =
										new de.serosystems.lib1090.msgs.adsr.SurfacePositionV2Msg(es1090, timestamp);
								s2.setNICSupplementA(dd.nicSupplA);
								s2.setNICSupplementC(dd.nicSupplC);
								return s2;
							default:
								// implicit by version 0
								return new de.serosystems.lib1090.msgs.adsr.SurfacePositionV0Msg(es1090, timestamp);
						}
					}

					if ((ftc >= 9 && ftc <= 18) || (ftc >= 20 && ftc <= 22)) {
						// airborne position message
						switch(dd.adsbVersion) {
							case 1:
								de.serosystems.lib1090.msgs.adsr.AirbornePositionV1Msg a1 =
										new de.serosystems.lib1090.msgs.adsr.AirbornePositionV1Msg(es1090, timestamp);
								a1.setNICSupplementA(dd.nicSupplA);
								return a1;
							case 2:
								de.serosystems.lib1090.msgs.adsr.AirbornePositionV2Msg a2 =
										new de.serosystems.lib1090.msgs.adsr.AirbornePositionV2Msg(es1090, timestamp);
								a2.setNICSupplementA(dd.nicSupplA);
								return a2;
							default:
								// implicit by version 0
								return new de.serosystems.lib1090.msgs.adsr.AirbornePositionV0Msg(es1090, timestamp);
						}
					}

					if (ftc == 19) { // possible velocity message, check subtype
						int subtype = es1090.getMessage()[0] & 0x7;

						if (subtype == 1 || subtype == 2) { // velocity over ground
							de.serosystems.lib1090.msgs.adsr.VelocityOverGroundMsg velocity =
									new de.serosystems.lib1090.msgs.adsr.VelocityOverGroundMsg(es1090);
							if (velocity.hasGeoMinusBaroInfo()) dd.geoMinusBaro = velocity.getGeoMinusBaro();
							return velocity;
						} else if (subtype == 3 || subtype == 4) {  // airspeed & heading
							de.serosystems.lib1090.msgs.adsr.AirspeedHeadingMsg airspeed =
									new de.serosystems.lib1090.msgs.adsr.AirspeedHeadingMsg(es1090);
							if (airspeed.hasGeoMinusBaroInfo()) dd.geoMinusBaro = airspeed.getGeoMinusBaro();
							return airspeed;
						}
					}

					if (ftc == 28) { // aircraft status message, check subtype
						int subtype = es1090.getMessage()[0] & 0x7;

						if (subtype == 1) // emergency/priority status
							return new de.serosystems.lib1090.msgs.adsr.EmergencyOrPriorityStatusMsg(es1090);
					}

					if (ftc == 29) {
						int subtype = (es1090.getMessage()[0]>>>1) & 0x3;
						// DO-260B 2.2.3.2.7.1: ignore for ADS-B v0 transponders if ME bit 11 != 0
						boolean hasMe11Bit = (es1090.getMessage()[1]&0x20) != 0;

						if (subtype == 1 && (dd.adsbVersion > 0 || !hasMe11Bit)) {
							return new de.serosystems.lib1090.msgs.adsr.TargetStateAndStatusMsg(es1090);
						}
					}

					if (ftc == 31) { // operational status message
						int subtype = es1090.getMessage()[0] & 0x7;

						dd.adsbVersion = (byte) ((es1090.getMessage()[5]>>>5) & 0x7);
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
									// TODO: store NIC supplement B as well
									de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg s2 =
											new de.serosystems.lib1090.msgs.adsr.AirborneOperationalStatusV2Msg(es1090);
									dd.nicSupplA = s2.hasNICSupplementA();
									return s2;
								default:
									throw new BadFormatException("Airborne operational status has invalid version: " + dd.adsbVersion);
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
									de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg s2 =
											new de.serosystems.lib1090.msgs.adsr.SurfaceOperationalStatusV2Msg(es1090);
									dd.nicSupplA = s2.hasNICSupplementA();
									dd.nicSupplC = s2.getNICSupplementC();
									return s2;
								default:
									throw new BadFormatException("Surface operational status has invalid version: " + dd.adsbVersion);
							}
						}
					}

					return es1090; // unknown extended squitter
				} else if (modes.getDownlinkFormat() == 19) {
					return new MilitaryExtendedSquitter(modes);
				}

				return modes; // this should never happen
			case 20: return new CommBAltitudeReply(modes);
			case 21: return new CommBIdentifyReply(modes);
			default:
				if (modes.getDownlinkFormat()>=24)
					return new CommDExtendedLengthMsg(modes);
				else return modes; // unknown mode s reply
		}
	}

	/**
	 * @param raw_message the Mode S message as byte array
	 * @param timestamp time of applicability (or reception) of the message in milliseconds
	 * @return an instance of the most specialized ModeSReply possible
	 * @throws UnspecifiedFormatError if format is not specified
	 * @throws BadFormatException if format contains error
	 */
	public ModeSDownlinkMsg decode(byte[] raw_message, long timestamp) throws BadFormatException, UnspecifiedFormatError {
		return decode(new ModeSDownlinkMsg(raw_message), timestamp);
	}

	/**
	 * @param raw_message the Mode S message as byte array
	 * @param noCRC indicates whether the CRC has been subtracted from the parity field
	 * @param timestamp time of applicability (or reception) of the message in milliseconds
	 * @return an instance of the most specialized ModeSReply possible
	 * @throws UnspecifiedFormatError if format is not specified
	 * @throws BadFormatException if format contains error
	 */
	public ModeSDownlinkMsg decode(byte[] raw_message, boolean noCRC, long timestamp) throws BadFormatException, UnspecifiedFormatError {
		return decode(new ModeSDownlinkMsg(raw_message, noCRC), timestamp);
	}

	/**
	 * @param raw_message the Mode S message in hex representation
	 * @param timestamp time of applicability (or reception) of the message in milliseconds
	 * @return an instance of the most specialized ModeSReply possible
	 * @throws UnspecifiedFormatError if format is not specified
	 * @throws BadFormatException if format contains error
	 */
	public ModeSDownlinkMsg decode(String raw_message, long timestamp) throws BadFormatException, UnspecifiedFormatError {
		return decode(new ModeSDownlinkMsg(raw_message), timestamp);
	}

	/**
	 * @param raw_message the Mode S message in hex representation
	 * @param noCRC indicates whether the CRC has been subtracted from the parity field
	 * @param timestamp time of applicability (or reception) of the message in milliseconds
	 * @return an instance of the most specialized ModeSReply possible
	 * @throws UnspecifiedFormatError if format is not specified
	 * @throws BadFormatException if format contains error
	 */
	public ModeSDownlinkMsg decode(String raw_message, boolean noCRC, long timestamp) throws BadFormatException, UnspecifiedFormatError {
		return decode(new ModeSDownlinkMsg(raw_message, noCRC), timestamp);
	}

	/**
	 * Decode CPR encoded position from airborne position message.
	 * @param msg which contains the encoded position
	 * @param receiver position for reasonableness test (can be null)
	 * @return decoded WGS84 position
	 */
	public Position extractPosition(ModeSDownlinkMsg.QualifiedAddress address, PositionMsg msg, Position receiver) {
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
	 * @param <T> {@link ModeSDownlinkMsg} or one of its sub classes
	 * for a higher version is received for the given aircraft.
	 */
	public <T extends ModeSDownlinkMsg> byte getAdsbVersion(T reply) {
		if (reply == null) return 0;
		DecoderData dd = getDecoderData(reply.getAddress());
		return dd.adsbVersion;
	}

	/**
	 * Get the difference between geometric and barometric altitude as tracked by the decoder. The value is derived
	 * from ADS-B {@link AirspeedHeadingMsg} and {@link VelocityOverGroundMsg}. The method returns the most recent
	 * value.
	 * @param reply a Mode S message
	 * @param <T> {@link ModeSDownlinkMsg} or one of its sub classes
	 * @return the difference between geometric and barometric altitude in feet or null if not present
	 */
	public <T extends ModeSDownlinkMsg> Integer getGeoMinusBaro(T reply) {
		if (reply == null) return null;
		DecoderData dd = getDecoderData(reply.getAddress());
		return dd.geoMinusBaro;
	}

	/**
	 * Check whether a ModeSReply is an airborne position (of any version), i.e., it
	 * is of type {@link AirbornePositionV0Msg}, {@link AirbornePositionV1Msg} or {@link AirbornePositionV2Msg}
	 * @param reply the ModeSReply to check
	 * @param <T> {@link ModeSDownlinkMsg} or one of its sub classes
	 */
	public static <T extends ModeSDownlinkMsg> boolean isAirbornePosition(T reply) {
		if (reply == null) return false;
		ModeSDownlinkMsg.subtype t = reply.getType();
		return (t == ModeSDownlinkMsg.subtype.ADSB_AIRBORN_POSITION_V0 ||
				t == ModeSDownlinkMsg.subtype.ADSB_AIRBORN_POSITION_V1 ||
				t == ModeSDownlinkMsg.subtype.ADSB_AIRBORN_POSITION_V2);
	}

	/**
	 * Check whether a ModeSReply is a surface position (of any version), i.e., it
	 * is of type {@link SurfacePositionV0Msg}, {@link SurfacePositionV1Msg} or {@link SurfacePositionV2Msg}
	 * @param reply the ModeSReply to check
	 * @param <T> {@link ModeSDownlinkMsg} or one of its sub classes
	 */
	public static <T extends ModeSDownlinkMsg> boolean isSurfacePosition(T reply) {
		if (reply == null) return false;
		ModeSDownlinkMsg.subtype t = reply.getType();
		return (t == ModeSDownlinkMsg.subtype.ADSB_SURFACE_POSITION_V0 ||
				t == ModeSDownlinkMsg.subtype.ADSB_SURFACE_POSITION_V1 ||
				t == ModeSDownlinkMsg.subtype.ADSB_SURFACE_POSITION_V2);
	}

	/**
	 * Check whether a ModeSReply is either an airborne or a surface position
	 * @see #isAirbornePosition(ModeSDownlinkMsg)
	 * @see #isSurfacePosition(ModeSDownlinkMsg)
	 * @param reply the ModeSReply to check
	 * @param <T> {@link ModeSDownlinkMsg} or one of its sub classes
	 */
	public static <T extends ModeSDownlinkMsg> boolean isPosition(T reply) {
		return isAirbornePosition(reply) || isSurfacePosition(reply);
	}

	/**
	 * Clean state by removing decoders not used for more than an hour. This happens automatically
	 * every 1 Mio messages if more than 30000 aircraft are tracked.
	 */
	public void clearDecoders() {
		decoderData.values().removeIf(dd -> latestTimestamp - dd.lastUsed > 3600_000L);
	}

	/**
	 * Represents the state of a decoder for a certain aircraft
	 */
	private static class DecoderData {
		byte adsbVersion = 0;
		boolean nicSupplA;
		boolean nicSupplC;
		Integer geoMinusBaro;
		long lastUsed = System.currentTimeMillis();
		CompactPositionReporting.StatefulPositionDecoder posDec = new CompactPositionReporting.StatefulPositionDecoder();
	}
}
