package de.serosystems.lib1090.msgs.adsr;

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
 * Decoder for ADS-R emergency and priority status messages
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class EmergencyOrPriorityStatusMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 2611795026824285668L;
	
	private byte msgsubtype;
	private byte emergency_state;
	private short mode_a_code;
	private boolean imf;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected EmergencyOrPriorityStatusMsg() { }

	/**
	 * @param raw_message raw ADS-R aircraft status message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public EmergencyOrPriorityStatusMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw ADS-R aircraft status message as byte array
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
		setType(subtype.ADSR_EMERGENCY);

		if (this.getFormatTypeCode() != 28) {
			throw new BadFormatException("Emergency and Priority Status messages must have typecode 28.");
		}

		byte[] msg = this.getMessage();

		msgsubtype = (byte) (msg[0]&0x7);
		if (msgsubtype != 1) {
			throw new BadFormatException("Emergency and priority status reports have subtype 1.");
		}

		emergency_state = (byte) ((msg[1]&0xFF)>>>5);
		mode_a_code = (short) (((msg[1]&0x1F)<<8) | (msg[2] & 0xFF));
		imf = (msg[6] & 0x1) != 0;
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

	/**
	 * @return the four-digit Mode A (4096) code (only ADS-R version 2)
	 */
	public byte[] getModeACode() {
		// the sequence is C1, A1, C2, A2, C4, A4, ZERO, B1, D1, B2, D2, B4, D4
		int C1 = (mode_a_code>>>12)&0x1;
		int A1 = (mode_a_code>>>11)&0x1;
		int C2 = (mode_a_code>>>10)&0x1;
		int A2 = (mode_a_code>>>9)&0x1;
		int C4 = (mode_a_code>>>8)&0x1;
		int A4 = (mode_a_code>>>7)&0x1;
		int B1 = (mode_a_code>>>5)&0x1;
		int D1 = (mode_a_code>>>4)&0x1;
		int B2 = (mode_a_code>>>3)&0x1;
		int D2 = (mode_a_code>>>2)&0x1;
		int B4 = (mode_a_code>>>1)&0x1;
		int D4 = mode_a_code&0x1;
		return new byte[] {
				(byte) (A1+(A2<<1)+(A4<<2)),
				(byte) (B1+(B2<<1)+(B4<<2)),
				(byte) (C1+(C2<<1)+(C4<<2)),
				(byte) (D1+(D2<<1)+(D4<<2))};
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tEmergencyOrPriorityStatusMsg{" +
				"msgsubtype=" + msgsubtype +
				", emergency_state=" + emergency_state +
				", mode_a_code=" + mode_a_code +
				", imf=" + imf +
				'}';
	}
}
