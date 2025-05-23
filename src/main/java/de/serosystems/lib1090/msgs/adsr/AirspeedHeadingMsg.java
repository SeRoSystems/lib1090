package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

import static de.serosystems.lib1090.decoding.Airspeed.decodeNACv;

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
 * Decoder for ADS-R airspeed and heading messages
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class AirspeedHeadingMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = -5847938116356997891L;
	
	private byte msg_subtype;
	private boolean imf;
	private boolean ifr_capability;
	private byte navigation_accuracy_category;
	private boolean heading_status_bit;
	private double heading; // in degrees
	private boolean true_airspeed; // 0 = indicated AS, 1 = true AS
	private short airspeed; // in knots
	private boolean airspeed_available;
	private boolean vertical_source; // 0 = geometric, 1 = barometric
	private boolean vertical_rate_down; // 0 = up, 1 = down
	private short vertical_rate; // in ft/s
	private boolean vertical_rate_info_available;
	private int geo_minus_baro; // in ft
	private boolean geo_minus_baro_available;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected AirspeedHeadingMsg() { }

	/**
	 * @param raw_message raw ADS-R airspeed and heading message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirspeedHeadingMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw ADS-R airspeed and heading message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirspeedHeadingMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter containing the airspeed and heading msg
	 * @throws BadFormatException if message has wrong format
	 */
	public AirspeedHeadingMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSR_AIRSPEED);

		if (this.getFormatTypeCode() != 19) {
			throw new BadFormatException("Airspeed and heading messages must have typecode 19.");
		}

		byte[] msg = this.getMessage();

		msg_subtype = (byte) (msg[0]&0x7);
		if (msg_subtype != 3 && msg_subtype != 4) {
			throw new BadFormatException("Airspeed and heading messages have subtype 3 or 4.");
		}

		imf = (msg[1]&0x80)>0;
		ifr_capability = (msg[1]&0x40)>0;
		navigation_accuracy_category = (byte) ((msg[1]>>>3)&0x7);

		// check this later
		vertical_rate_info_available = true;
		geo_minus_baro_available = true;

		// heading available in ADS-R version 1+, indicates true/magnetic north for version 0
		heading_status_bit = (msg[1]&0x4)>0;
		heading = ((msg[1]&0x3)<<8 | msg[2]&0xFF) * 360./1024.;

		true_airspeed = (msg[3]&0x80)>0;
		airspeed = (short) (((msg[3]&0x7F)<<3 | msg[4]>>>5&0x07)-1);
		if (airspeed != -1) {
			airspeed_available = true;
			if (msg_subtype == 4) airspeed<<=2;
		}

		vertical_source = (msg[4]&0x10)>0;
		vertical_rate_down = (msg[4]&0x08)>0;
		vertical_rate = (short) ((((msg[4]&0x07)<<6 | msg[5]>>>2&0x3F)-1)<<6);

		geo_minus_baro = (short) (((msg[6]&0x7F)-1)*25);
		if ((msg[6]&0x80)>0) geo_minus_baro *= -1;
	}

	/**
	 * For ADS-R version 1 and 2 this must be checked before retrieving heading information.
	 * 
	 * @return Depending on the ADS-R version, different interpretations:
	 * 	<ul>
	 * 	    <li><strong>Version 0</strong> the flag indicates whether heading is relative to magnetic north (true) or
	 * 	    	true north (false)</li>
	 * 	    <li><strong>Version 1+</strong> the flag indicates whether heading information is available or not</li>
	 * 	</ul>
	 */
	public boolean hasHeadingStatusFlag() {
		return heading_status_bit;
	}

	/**
	 * Must be checked before accessing airspeed!
	 * 
	 * @return whether airspeed info is available
	 */
	public boolean hasAirspeedInfo() {
		return airspeed_available;
	}
	
	/**
	 * Must be checked before accessing vertical rate!
	 * 
	 * @return whether vertical rate info is available
	 */
	public boolean hasVerticalRateInfo() {
		return vertical_rate_info_available;
	}

	/**
	 * Must be checked before accessing geo minus baro!
	 * 
	 * @return whether geo-baro difference info is available
	 */
	public boolean hasGeoMinusBaroInfo() {
		return geo_minus_baro_available;
	}

	/**
	 * @return If supersonic, velocity has only 4 kts accuracy, otherwise 1 kt
	 */
	public boolean isSupersonic() {
		return msg_subtype == 4;
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	/**
	 * Note: only in ADS-R version 0 and 1 transponders!!
	 * @return true, iff aircraft has equipage class A1 or higher
	 */
	public boolean hasIFRCapability() {
		return ifr_capability;
	}

	/**
	 * The 95% accuracy for horizontal velocity. We interpret the coding according to
	 * DO-260B Table 2-22 for all ADS-R versions.
	 * @return Navigation Accuracy Category for velocity according to RTCA DO-260B 2.2.3.2.6.1.5 in m/s, -1 means
	 * "unknown" or &gt;10m
	 */
	public double getNACv() {
		return decodeNACv(navigation_accuracy_category);
	}

	/**
	 * @return airspeed in knots or null if information is not available. The latter can also be checked using
	 * {@link #hasAirspeedInfo()}.
	 */
	public Integer getAirspeed() {
		if (!airspeed_available) return null;
		return (int) airspeed;
	}


	/**
	 * @return whether altitude is derived by barometric sensor or GNSS
	 */
	public boolean isBarometricVerticalSpeed() {
		return vertical_source;
	}


	/**
	 * @return vertical rate in feet/min (negative value means descending) or null if information is not available. The
	 * latter can also be checked with {@link #hasVerticalRateInfo()}.
	 */
	public Integer getVerticalRate() {
		if (!vertical_rate_info_available) return null;
		return (vertical_rate_down ? -vertical_rate : vertical_rate);
	}


	/**
	 * @return difference between barometric and geometric altitude in feet or null if no information is available.
	 * The latter can also be checked using {@link #hasGeoMinusBaroInfo()}.
	 */
	public Integer getGeoMinusBaro() {
		if (!geo_minus_baro_available) return null;
		return geo_minus_baro;
	}
	
	/**
	 * @return heading in decimal degrees ([0, 360]). 0° = geographic north or null if no information is available.
	 * The latter can also be checked using {@link #hasHeadingStatusFlag()}.
	 */
	public Double getHeading() {
		if (!heading_status_bit) return null;
		return heading;
	}
	
	/**
	 * @return true if airspeed is true airspeed, false if airspeed is indicated airspeed
	 */
	public boolean isTrueAirspeed() {
		return true_airspeed;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tAirspeedHeadingMsg{" +
				"msg_subtype=" + msg_subtype +
				", imf=" + imf +
				", ifr_capability=" + ifr_capability +
				", navigation_accuracy_category=" + navigation_accuracy_category +
				", heading_status_bit=" + heading_status_bit +
				", heading=" + heading +
				", true_airspeed=" + true_airspeed +
				", airspeed=" + airspeed +
				", airspeed_available=" + airspeed_available +
				", vertical_source=" + vertical_source +
				", vertical_rate_down=" + vertical_rate_down +
				", vertical_rate=" + vertical_rate +
				", vertical_rate_info_available=" + vertical_rate_info_available +
				", geo_minus_baro=" + geo_minus_baro +
				", geo_minus_baro_available=" + geo_minus_baro_available +
				'}';
	}
}
