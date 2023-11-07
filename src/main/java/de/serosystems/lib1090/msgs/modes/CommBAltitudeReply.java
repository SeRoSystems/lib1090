package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;
import java.util.Arrays;

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
 * Decoder for Mode S surveillance altitude replies with Comm-B message (DF 20)
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
@SuppressWarnings("unused")
public class CommBAltitudeReply extends ModeSDownlinkMsg implements Serializable {

	private static final long serialVersionUID = -7022055256214864570L;
	
	private byte flight_status;
	private byte downlink_request;
	private byte utility_msg;
	private short altitude_code;
	private byte[] message;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected CommBAltitudeReply() { }

	/**
	 * @param raw_message raw comm-b altitude reply as hex string
	 * @throws BadFormatException if message is not comm-b altitude reply or 
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CommBAltitudeReply(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param raw_message raw comm-b altitude reply as byte array
	 * @throws BadFormatException if message is not comm-b altitude reply or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CommBAltitudeReply(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param reply Mode S reply which contains this comm-b altitude reply
	 * @throws BadFormatException if message is not comm-b altitude reply or 
	 * contains wrong values.
	 */
	public CommBAltitudeReply(ModeSDownlinkMsg reply) throws BadFormatException {
		super(reply);
		setType(subtype.COMM_B_ALTITUDE_REPLY);

		if (getDownlinkFormat() != 20) {
			throw new BadFormatException("Message is not an altitude reply!");
		}

		byte[] payload = getPayload();
		flight_status = getFirstField();
		downlink_request = (byte) ((payload[0]>>>3) & 0x1F);
		utility_msg = (byte) ((payload[0]&0x7)<<3 | (payload[1]>>>5)&0x7);
		altitude_code = (short) ((payload[1]<<8 | payload[2]&0xFF)&0x1FFF);

		// extract Comm-B message
		message = new byte[7];
		System.arraycopy(payload, 3, message, 0, 7);
	}

	/**
	 * Indicates alerts, whether SPI is enabled, and if the plane is on ground.
	 * @return The 3 bits flight status. The coding is:<br>
	 * <ul>
	 * <li>0 signifies no alert and no SPI, aircraft is airborne</li>
	 * <li>1 signifies no alert and no SPI, aircraft is on the ground</li>
	 * <li>2 signifies alert, no SPI, aircraft is airborne</li>
	 * <li>3 signifies alert, no SPI, aircraft is on the ground</li>
	 * <li>4 signifies alert and SPI, aircraft is airborne or on the ground</li>
	 * <li>5 signifies no alert and SPI, aircraft is airborne or on the ground</li>
	 * <li>6 reserved</li>
	 * <li>7 not assigned</li>
	 * </ul>
	 * @see #hasAlert()
	 * @see #hasSPI()
	 * @see #isAirborne()
	 */
	public byte getFlightStatus() {
		return flight_status;
	}

	/**
	 * @return whether flight status indicates alert
	 */
	public boolean hasAlert() {
		return flight_status>=2 && flight_status<=4;
	}

	/**
	 * @return whether flight status indicates special purpose indicator
	 */
	public boolean hasSPI() {
		return flight_status==4 || flight_status==5;
	}

	/**
	 * Whether flight status indicates that the aircraft is airborne.
	 * @return true if airborne, false if on ground or null if ground status is unknown
	 */
	public Boolean isAirborne() {
		if (flight_status == 0 || flight_status == 2) {
			return true;
		} else if (flight_status == 1 || flight_status == 3) {
			return false;
		}
		return null;
	}

	/**
	 * indicator for downlink requests
	 * @return the 5 bits downlink request. The coding is:<br>
     * <ul>
     * <li>0 signifies no downlink request</li>
	 * <li>1 signifies request to send Comm-B message</li>
	 * <li>2 reserved for ACAS</li>
	 * <li>3 reserved for ACAS</li>
	 * <li>4 signifies Comm-B broadcast message 1 available</li>
	 * <li>5 signifies Comm-B broadcast message 2 available</li>
	 * <li>6 reserved for ACAS</li>
	 * <li>7 reserved for ACAS</li>
	 * <li>8-15 not assigned</li>
	 * <li>16-31 see downlink ELM protocol (3.1.2.7.7.1)</li>
     * </ul>
	 */
	public byte getDownlinkRequest() {
		return downlink_request;
	}

	/**
	 * @return The 6 bits utility message (see ICAO Annex 10 V4)
	 */
	public byte getUtilityMsg() {
		return utility_msg;
	}

	/**
	 * Note that this is not the same identifier as the one contained in all-call replies.
	 * 
	 * @return the 4-bit interrogator identifier subfield of the
	 * utility message which reports the identifier of the
	 * interrogator that is reserved for multisite communications.
	 */
	public byte getInterrogatorIdentifier() {
		return (byte) ((utility_msg>>>2)&0xF);
	}

	/**
	 * Assigned coding is:<br>
	 * 0 signifies no information<br>
	 * 1 signifies IIS contains Comm-B II code<br>
	 * 2 signifies IIS contains Comm-C II code<br>
	 * 3 signifies IIS contains Comm-D II code<br>
	 * @return the 2-bit identifier designator subfield of the
	 * utility message which reports the type of reservation made
	 * by the interrogator identified in
	 * {@link #getInterrogatorIdentifier() getInterrogatorIdentifier}.
	 */
	public byte getIdentifierDesignator() {
		return (byte) (utility_msg&0x3);
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

	/**
	 * @return the 7-byte Comm-B message (BDS register)
	 */
	public byte[] getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tCommBAltitudeReply{" +
				"flight_status=" + flight_status +
				", downlink_request=" + downlink_request +
				", utility_msg=" + utility_msg +
				", altitude_code=" + altitude_code +
				", message=" + Arrays.toString(message) +
				'}';
	}
}
