package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

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
 * Decoder for Mode S long air-air ACAS replies (DF 0)
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class LongACAS extends ModeSDownlinkMsg implements Serializable {

	private static final long serialVersionUID = 1052613416840618986L;

	private boolean airborne;
	private byte sensitivity_level;
	private byte reply_information;
	private short altitude_code;
	private boolean valid_rac;
	private short active_resolution_advisories;
	private byte racs_record; // RAC = resolution advisory complement
	private boolean ra_terminated;
	private boolean multiple_threat_encounter;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected LongACAS() { }

	/**
	 * @param raw_message raw long air-to-air ACAS reply as hex string
	 * @throws BadFormatException if message is not long air-to-air ACAS reply or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public LongACAS(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param raw_message raw long air-to-air ACAS reply as byte array
	 * @throws BadFormatException if message is not long air-to-air ACAS reply or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public LongACAS(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param reply Mode S reply containing this long air-to-air ACAS reply
	 * @throws BadFormatException if message is not long air-to-air ACAS reply or 
	 * contains wrong values.
	 */
	public LongACAS(ModeSDownlinkMsg reply) throws BadFormatException {
		super(reply);
		setType(subtype.LONG_ACAS);

		if (getDownlinkFormat() != 16) {
			throw new BadFormatException("Message is not a long ACAS (air-air) message!");
		}

		byte[] payload = getPayload();
		airborne = (getFirstField()&0x4)==0;
		sensitivity_level = (byte) ((payload[0]>>>5)&0x7);
		reply_information = (byte) ((payload[0]&0x7)<<1 | (payload[1]>>>7)&0x1);
		altitude_code = (short) ((payload[1]<<8 | payload[2]&0xFF)&0x1FFF);

		// extract MV/air-air coordination info; see Annex 10 Vol 4: 4.3.8.4.2.4
		valid_rac = payload[3] == 0x30;
		active_resolution_advisories = (short) ((payload[4]<<6 | (payload[5]>>>2)&0x3F)&0x3FFF);
		racs_record = (byte) ((payload[5]<<2 | (payload[6]>>>6)&0x3)&0xF);
		ra_terminated = ((payload[6]>>>5)&0x1) == 1;
		multiple_threat_encounter = ((payload[6]>>>4)&0x1) == 1;
	}

	/**
	 * Important note: check this before using any of
	 * {@link #getActiveResolutionAdvisories()},
	 * {@link #noPassBelow()}, {@link #noPassAbove()},
	 * {@link #noTurnLeft()}, {@link #noTurnRight()},
	 * {@link #hasTerminated()}, {@link #hasMultipleThreats()}
	 * @return true if resolution advisory complement is valid
	 */
	public boolean hasValidRAC() {
		return valid_rac;
	}

	/**
	 * @return the binary encoded information about active
	 * resolution advisories (see Annex 10V4; 4.3.8.4.2.2.1.1)
	 */
	public short getActiveResolutionAdvisories() {
		return active_resolution_advisories;
	}

	/**
	 * @return the binary encoded resolution advisory complement
	 * @see #noPassBelow()
	 * @see #noPassAbove()
	 * @see #noTurnLeft()
	 * @see #noTurnRight()
	 */
	public byte getResolutionAdvisoryComplement() {
		return racs_record;
	}

	/**
	 * @return true iff do not pass below advisory is active
	 */
	public boolean noPassBelow() {
		return (racs_record&8)==8;
	}

	/**
	 * @return true iff do not pass above advisory is active
	 */
	public boolean noPassAbove() {
		return (racs_record&4)==4;
	}

	/**
	 * @return true iff do not turn left advisory is active
	 */
	public boolean noTurnLeft() {
		return (racs_record&2)==2;
	}

	/**
	 * @return true iff do not turn right advisory is active
	 */
	public boolean noTurnRight() {
		return (racs_record&1)==1;
	}


	/**
	 * @return true if aircraft is airborne, false if it is on the ground
	 */
	public boolean isAirborne() {
		return airborne;
	}

	/**
	 * @return true iff the RA from {@link #getActiveResolutionAdvisories()} has been terminated
	 */
	public boolean hasTerminated() {
		return ra_terminated;
	}

	/**
	 * @return true iff two or more threats are being processed
	 */
	public boolean hasMultipleThreats() {
		return multiple_threat_encounter;
	}

	/**
	 * @return the sensitivity level at which ACAS is currently operating
	 */
	public byte getSensitivityLevel() {
		return sensitivity_level;
	}

	/**
	 * This field is used to report the aircraft's maximum cruising 
	 * true airspeed capability and type of reply to interrogating aircraft
	 * @return the air-to-air reply information according to 3.1.2.8.2.2
	 * @see #getMaximumAirspeed()
	 * @see #hasOperatingACAS()
	 */
	public byte getReplyInformation() {
		return reply_information;
	}

	/**
	 * @return whether a/c has operating ACARS (derived from reply information)
	 * @see #getReplyInformation()
	 */
	public boolean hasOperatingACAS() {
		return getReplyInformation() != 0;
	}

	/**
	 * @return the maximum airspeed in kt as specified in ICAO Annex 10V4 3.1.2.8.2.2<br>
	 * null if unknown<br>Integer.MAX_VALUE if unbound
	 */
	public Integer getMaximumAirspeed() {
		return ShortACAS.decodeMaximumAirspeed(getReplyInformation());
	}

	/**
	 * @return The 13 bits altitude code (see ICAO Annex 10 V4)
	 */
	public short getAltitudeCode() {
		return altitude_code;
	}

	/**
	 * @return the decoded altitude in feet or null if not available
	 */
	public Integer getAltitude() {
		return Altitude.decode13BitAltitude(altitude_code);
	}

	/**
	 * Decode Q bit for the altitude according to Annex 10 V4 3.1.2.6.5.4
	 * @return value of the Q bit, null if altitude is not available or M bit is set
	 */
	public Boolean hasQBit() {
		return Altitude.decode13BitQBit(altitude_code);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tLongACAS{" +
				"airborne=" + airborne +
				", sensitivity_level=" + sensitivity_level +
				", reply_information=" + reply_information +
				", altitude_code=" + altitude_code +
				", valid_rac=" + valid_rac +
				", active_resolution_advisories=" + active_resolution_advisories +
				", racs_record=" + racs_record +
				", ra_terminated=" + ra_terminated +
				", multiple_threat_encounter=" + multiple_threat_encounter +
				'}';
	}

}
