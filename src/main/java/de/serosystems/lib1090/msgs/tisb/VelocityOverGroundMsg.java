package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/*
 *  This file is part of org.opensky.libadsb.
 *
 *  org.opensky.libadsb is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  org.opensky.libadsb is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with org.opensky.libadsb.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoder for TIS-B velocity message (DO-260B, 2.2.17.3.4).
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class VelocityOverGroundMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 4633940058263818959L;

	private byte msg_subtype;
	private boolean imf;
	private byte nacp;
	private boolean direction_west; // 0 = east, 1 = west
	private short east_west_velocity; // in kn
	private boolean velocity_info_available;
	private boolean direction_south; // 0 = north, 1 = south
	private short north_south_velocity; // in kn

	private boolean vertical_rate_down; // 0 = up, 1 = down
	private short vertical_rate; // in ft/min
	private boolean vertical_rate_info_available;

	private Integer geo_minus_baro; // in ft

	private Byte nacv;
	private Byte sil;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected VelocityOverGroundMsg() { }

	/**
	 * @param raw_message raw TIS-B velocity message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public VelocityOverGroundMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw TIS-B velocity message as byte array
	 * @throws BadFormatException if message has wrong format
	 */
	public VelocityOverGroundMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter containing the velocity message
	 * @throws BadFormatException if message has wrong format
	 */
	public VelocityOverGroundMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.TISB_VELOCITY);

		if (getDownlinkFormat() != 18)
			throw new BadFormatException("TIS-B messages must have downlink format 18.");

		if (this.getFormatTypeCode() != 19)
			throw new BadFormatException("Velocity messages must have typecode 19.");

		// Table 2-13
		if (getFirstField() != 2 && getFirstField() != 5)
			throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

		byte[] msg = this.getMessage();

		msg_subtype = (byte) (msg[0]&0x7);
		if (msg_subtype != 1 && msg_subtype != 2) {
			throw new BadFormatException("Ground speed messages have subtype 1 or 2.");
		}

		imf = (msg[1]&0x80)>0;
		nacp = (byte) ((msg[1]>>>3)&0xF);

		// check this later
		velocity_info_available = true;
		vertical_rate_info_available = true;

		direction_west = (msg[1]&0x4)>0;
		east_west_velocity = (short) (((msg[1]&0x3)<<8 | msg[2]&0xFF)-1);
		if (east_west_velocity == -1) velocity_info_available = false;
		if (msg_subtype == 2) east_west_velocity<<=2;

		direction_south = (msg[3]&0x80)>0;
		north_south_velocity = (short) (((msg[3]&0x7F)<<3 | msg[4]>>>5&0x07)-1);
		if (north_south_velocity == -1) velocity_info_available = false;
		if (msg_subtype == 2) north_south_velocity<<=2;

		// 0 = no geo data available, 1 = geo data available
		boolean geo_flag = (msg[4] & 0x10) > 0;

		vertical_rate_down = (msg[4]&0x08)>0;
		vertical_rate = (short) ((((msg[4]&0x07)<<6 | msg[5]>>>2&0x3F)-1)<<6);

		if (geo_flag) {
			geo_minus_baro = msg[6] & 0x7F;
			geo_minus_baro = (geo_minus_baro - 1) * 25;
			if ((msg[6] & 0x80) > 0) geo_minus_baro *= -1;

			nacv = null;
			sil = null;
		} else {
			geo_minus_baro = null;
			nacv = (byte) (((msg[5]&0x1)<<2) | ((msg[6]>>>6)&0x3));
			sil = (byte) ((msg[6]>>>4)&0x3);
		}
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	/**
	 * @return whether velocity info is available
	 */
	public boolean hasVelocityInfo() {
		return velocity_info_available;
	}

	/**
	 * @return whether vertical rate info is available
	 */
	public boolean hasVerticalRateInfo() {
		return vertical_rate_info_available;
	}

	/**
	 * @return whether geo-baro difference info is available
	 */
	public boolean hasGeoMinusBaroInfo() {
		return geo_minus_baro != null;
	}

	/**
	 * @return If supersonic, velocity has only 4 kts accuracy, otherwise 1 kt
	 */
	public boolean isSupersonic() {
		return msg_subtype == 2;
	}

	/**
	 * @return the raw encoded Navigation Accuracy Category for velocity according to RTCA DO-260B 2.2.3.2.6.1.5 or null
	 * if not available
	 */
	public Byte getNACv() {
		return nacv;
	}

	/**
	 * The 95% accuracy for horizontal velocity. We interpret the coding according to
	 * DO-260B Table 2-22 for all ADS-B versions.
	 * @return Navigation Accuracy Category for velocity according to RTCA DO-260B 2.2.3.2.6.1.5 in m/s, -1 means
	 * "unknown" or &gt;10m; null if not reported in this message
	 */
	public Float getAccuracyBound() {
		if (nacv == null)
			return null;

		switch(nacv) {
			case 1:
				return 10.f;
			case 2:
				return 3.f;
			case 3:
				return 1.f;
			case 4:
				return 0.3f;
			default:
				return -1.f;
		}
	}


	/**
	 * @return velocity from east to south in knots or null if information is not available
	 */
	public Integer getEastToWestVelocity() {
		if (!velocity_info_available) return null;
		return (direction_west ? east_west_velocity : -east_west_velocity);
	}


	/**
	 * @return velocity from north to south in knots or null if information is not available
	 */
	public Integer getNorthToSouthVelocity() {
		if (!velocity_info_available) return null;
		return (direction_south ? north_south_velocity : -north_south_velocity);
	}

	/**
	 * @return vertical rate in feet/min (negative value means descending) or null if information is not available. The
	 * latter can also be checked with {@link #hasVerticalRateInfo()}
	 */
	public Integer getVerticalRate() {
		if (!vertical_rate_info_available) return null;
		return (vertical_rate_down ? -vertical_rate : vertical_rate);
	}


	/**
	 * @return difference between barometric and geometric altitude in feet or null if information is not available. The
	 * latter can also be checked with {@link #hasGeoMinusBaroInfo()}
	 */
	public Integer getGeoMinusBaro() {
		return geo_minus_baro;
	}

	/**
	 * @return heading in decimal degrees ([0, 360]) clockwise from geographic north or null if information is not available.
	 * The latter can also be checked with {@link #hasVelocityInfo()}.
	 */
	public Double getHeading() {
		if (!velocity_info_available) return null;
		double angle = Math.toDegrees(Math.atan2(
				-this.getEastToWestVelocity(),
				-this.getNorthToSouthVelocity()));

		// if negative => clockwise
		if (angle < 0) return 360+angle;
		else return angle;
	}

	/**
	 * @return speed over ground in knots or null if information is not available. The latter can also be checked
	 * with {@link #hasVelocityInfo()}.
	 */
	public Double getVelocity() {
		if (!velocity_info_available) return null;
		return Math.hypot(north_south_velocity, east_west_velocity);
	}

	/**
	 * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
	 * in the operational status message. For version 0 it is derived from the format type code.
	 *
	 * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
	 * {@link AirborneOperationalStatusV1Msg}. Returns null if not available.
	 */
	public byte getNACp() {
		return nacp;
	}

	/**
	 * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
	 *
	 * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
	 * {@link AirborneOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the source integrity level (SIL) which indicates the propability of exceeding
	 *         the NIC containment radius. Returns null if not available.
	 */
	public Byte getSIL() {
		return sil;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tVelocityOverGroundMsg{" +
				"msg_subtype=" + msg_subtype +
				", imf=" + imf +
				", nacp=" + nacp +
				", direction_west=" + direction_west +
				", east_west_velocity=" + east_west_velocity +
				", velocity_info_available=" + velocity_info_available +
				", direction_south=" + direction_south +
				", north_south_velocity=" + north_south_velocity +
				", vertical_rate_down=" + vertical_rate_down +
				", vertical_rate=" + vertical_rate +
				", vertical_rate_info_available=" + vertical_rate_info_available +
				", geo_minus_baro=" + geo_minus_baro +
				", nacv=" + nacv +
				", sil=" + sil +
				'}';
	}
}
