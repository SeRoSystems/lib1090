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
 * Decoder for TIS-B airspeed+heading message (DO-260B, 2.2.17.3.4).
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class AirspeedHeadingMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 944130622021621845L;

	private byte msg_subtype;
	private boolean imf;
	private byte nacp;

	private boolean vertical_rate_down; // 0 = up, 1 = down
	private short vertical_rate; // in ft/min
	private boolean vertical_rate_info_available;

	private boolean heading_status_bit;
	private double heading; // in degrees
	private boolean true_airspeed; // 0 = indicated AS, 1 = true AS
	private short airspeed; // in knots
	private boolean airspeed_available;

	private Integer geo_minus_baro; // in ft

	private Byte nacv;
	private Byte sil;
	private Boolean magnetic_heading;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected AirspeedHeadingMsg() { }

	/**
	 * @param raw_message raw TIS-B velocity message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirspeedHeadingMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw TIS-B velocity message as byte array
	 * @throws BadFormatException if message has wrong format
	 */
	public AirspeedHeadingMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter containing the velocity message
	 * @throws BadFormatException if message has wrong format
	 */
	public AirspeedHeadingMsg(ExtendedSquitter squitter) throws BadFormatException {
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
		if (msg_subtype != 3 && msg_subtype != 4) {
			throw new BadFormatException("Ground speed messages have subtype 1 or 2.");
		}

		imf = (msg[1]&0x80)>0;
		nacp = (byte) ((msg[1]>>>3)&0xF);

		// heading available in ADS-B version 1+, indicates true/magnetic north for version 0
		heading_status_bit = (msg[1]&0x4)>0;
		heading = ((msg[1]&0x3)<<8 | msg[2]&0xFF) * 360./1024.;

		true_airspeed = (msg[3]&0x80)>0;
		airspeed = (short) (((msg[3]&0x7F)<<3 | msg[4]>>>5&0x07)-1);
		if (airspeed != -1) {
			airspeed_available = true;
			if (msg_subtype == 4) airspeed<<=2;
		}

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
			magnetic_heading = null;
		} else {
			geo_minus_baro = null;
			nacv = (byte) (((msg[5]&0x1)<<2) | ((msg[6]>>>6)&0x3));
			sil = (byte) ((msg[6]>>>4)&0x3);
			magnetic_heading = (msg[6]&0x2) > 0;
		}
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
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
	 * @return heading in decimal degrees ([0, 360]). 0° = geographic north or null if no information is available.
	 */
	public Double getHeading() {
		if (!heading_status_bit) return null;
		return heading;
	}

	/**
	 * @return airspeed in knots or null if information is not available.
	 */
	public Integer getAirspeed() {
		if (!airspeed_available) return null;
		return (int) airspeed;
	}

	/**
	 * @return true if airspeed is true airspeed, false if airspeed is indicated airspeed
	 */
	public boolean isTrueAirspeed() {
		return true_airspeed;
	}

	/**
	 * @return If supersonic, velocity has only 4 kts accuracy, otherwise 1 kt
	 */
	public boolean isSupersonic() {
		return msg_subtype == 4;
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

	/**
	 * According to DO-260B, 2.2.3.2.7.2.13
	 * @return true if horizontal reference direction is magnet north; false if true north; null if info not available
	 */
	public Boolean isMagneticHeading () {
		return magnetic_heading;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tAirspeedHeadingMsg{" +
				"msg_subtype=" + msg_subtype +
				", imf=" + imf +
				", nacp=" + nacp +
				", vertical_rate_down=" + vertical_rate_down +
				", vertical_rate=" + vertical_rate +
				", vertical_rate_info_available=" + vertical_rate_info_available +
				", heading_status_bit=" + heading_status_bit +
				", heading=" + heading +
				", true_airspeed=" + true_airspeed +
				", airspeed=" + airspeed +
				", airspeed_available=" + airspeed_available +
				", geo_minus_baro=" + geo_minus_baro +
				", nacv=" + nacv +
				", sil=" + sil +
				", magnetic_heading=" + magnetic_heading +
				'}';
	}
}
