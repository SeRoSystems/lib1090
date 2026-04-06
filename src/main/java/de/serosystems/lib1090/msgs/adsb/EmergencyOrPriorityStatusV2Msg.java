package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.Identity;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

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
 * Decoder for ADS-B emergency and priority status messages
 */
public class EmergencyOrPriorityStatusV2Msg extends EmergencyOrPriorityStatusMsg implements Serializable {

	private static final long serialVersionUID = 596314337728216721L;

	private short mode_a_code;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected EmergencyOrPriorityStatusV2Msg() { }

	/**
	 * @param raw_message raw ADS-B aircraft status message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public EmergencyOrPriorityStatusV2Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw ADS-B aircraft status message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public EmergencyOrPriorityStatusV2Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this emergency or priority status msg
	 * @throws BadFormatException if message has wrong format
	 */
	public EmergencyOrPriorityStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSB_EMERGENCY_V2);

		byte[] msg = this.getMessage();

		mode_a_code = (short) (((msg[1]&0x1F)<<8) | (msg[2] & 0xFF));
	}

	/**
	 * @return the four-digit Mode A (4096) code (only ADS-B version 2)
	 */
	public short getModeACode() {
		return mode_a_code;
	}

	public String getIdentity() {
		return Identity.decodeIdentity(mode_a_code);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tEmergencyOrPriorityStatusMsgV2{" +
				", mode_a_code=" + mode_a_code +
				'}';
	}
}
