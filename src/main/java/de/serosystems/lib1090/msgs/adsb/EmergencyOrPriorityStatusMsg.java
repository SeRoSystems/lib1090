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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.Identity;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-B emergency and priority status messages
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class EmergencyOrPriorityStatusMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 7380235047641841128L;

	private byte msgsubtype;
	private byte emergency_state;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected EmergencyOrPriorityStatusMsg() { }

	/**
	 * @param raw_message raw ADS-B aircraft status message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public EmergencyOrPriorityStatusMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw ADS-B aircraft status message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public EmergencyOrPriorityStatusMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this emergency or priority status msg
	 * @throws BadFormatException if message has wrong format
	 */
	public EmergencyOrPriorityStatusMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSB_EMERGENCY);

		if (this.getFormatTypeCode() != 28) {
			throw new BadFormatException("Emergency and Priority Status messages must have typecode 28.");
		}

		byte[] msg = this.getMessage();

		msgsubtype = (byte) (msg[0]&0x7);
		if (msgsubtype != 1) {
			throw new BadFormatException("Emergency and priority status reports have subtype 1.");
		}

		emergency_state = (byte) ((msg[1]&0xFF)>>>5);
	}

	/**
	 * @return the subtype code of the aircraft status report (should always be 1)
	 */
	public byte getSubtype() {
		return msgsubtype;
	}

	/**
	 * @return the emergency state code (see DO-260B, Appendix A, Page A-83)
	 */
	public byte getEmergencyStateCode() {
		return emergency_state;
	}

	/**
	 * @return the human readable emergency state (see DO-260B, Appendix A, Page A-83)
	 */
	public String getEmergencyStateText() {
		switch (emergency_state) {
		case 0: return "no emergency";
		case 1: return "general emergency";
		case 2: return "lifeguard/medical";
		case 3: return "minimum fuel";
		case 4: return "no communications";
		case 5: return "unlawful interference";
		case 6: return "downed aircraft";
		default: return "unknown";
		}
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tEmergencyOrPriorityStatusMsg{" +
				"msgsubtype=" + msgsubtype +
				", emergency_state=" + emergency_state +
				'}';
	}
}
