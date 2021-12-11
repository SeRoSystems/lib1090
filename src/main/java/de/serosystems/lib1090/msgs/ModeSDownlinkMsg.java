package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;

import java.io.Serializable;
import java.util.Arrays;

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
 * Decoder for Mode S replies
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class ModeSDownlinkMsg implements Serializable {
	private static final long serialVersionUID = 5369519167589262290L;

	/*
	 * Attributes
	 */
	private byte downlink_format; // 0-31
	private byte first_field; // the 3 bits after downlink format
	private byte[] payload; // 3 or 10 bytes
	private int parity; // 3 bytes
	private boolean noCRC;

	/**
	 * Indicator set by all specializations of this class to tell
	 * users which message format is encapsulated in this Mode S message.
	 */
	public enum subtype {
		// Mode S downlink formats
		MODES_REPLY, // unknown mode s reply
		SHORT_ACAS,
		ALTITUDE_REPLY,
		IDENTIFY_REPLY,
		ALL_CALL_REPLY,
		LONG_ACAS,
		EXTENDED_SQUITTER,
		MILITARY_EXTENDED_SQUITTER,
		COMM_B_ALTITUDE_REPLY,
		COMM_B_IDENTIFY_REPLY,
		COMM_D_ELM,

		// ADS-B subtypes
		ADSB_AIRBORN_POSITION_V0,
		ADSB_AIRBORN_POSITION_V1,
		ADSB_AIRBORN_POSITION_V2,
		ADSB_SURFACE_POSITION_V0,
		ADSB_SURFACE_POSITION_V1,
		ADSB_SURFACE_POSITION_V2,
		ADSB_AIRSPEED,
		ADSB_EMERGENCY,
		ADSB_TCAS,
		ADSB_VELOCITY,
		ADSB_IDENTIFICATION,
		ADSB_STATUS_V0,
		ADSB_AIRBORN_STATUS_V1,
		ADSB_SURFACE_STATUS_V1,
		ADSB_AIRBORN_STATUS_V2,
		ADSB_SURFACE_STATUS_V2,
		ADSB_TARGET_STATE_AND_STATUS,
		SURFACE_SYSTEM_STATUS,

		// TIS-B subtypes
		TISB_FINE_AIRBORNE_POSITION,
		TISB_FINE_SURFACE_POSITION,
		TISB_IDENTIFICATION,
		TISB_VELOCITY,
		TISB_COARSE_POSITION,

		// ADS-R subtypes
		ADSR_AIRBORN_POSITION_V1,
		ADSR_AIRBORN_POSITION_V2,
		ADSR_AIRBORN_POSITION_V0,
		ADSR_SURFACE_POSITION_V0,
		ADSR_SURFACE_POSITION_V1,
		ADSR_SURFACE_POSITION_V2,
		ADSR_AIRSPEED,
		ADSR_EMERGENCY,
		ADSR_VELOCITY,
		ADSR_IDENTIFICATION,
		ADSR_STATUS_V0,
		ADSR_AIRBORN_STATUS_V1,
		ADSR_SURFACE_STATUS_V1,
		ADSR_AIRBORN_STATUS_V2,
		ADSR_SURFACE_STATUS_V2,
		ADSR_TARGET_STATE_AND_STATUS,
	}
	private subtype type;

	public static class QualifiedAddress {
		/**
		 * Different types of addresses in the AA field (see Table 2-11 in DO-260B)
		 */
		public enum Type {
			// ICAO 24-bit address
			ICAO24,
			// NON-ICAO 24-bit address
			NON_ICAO,
			// Anonymous address or ground vehicle address or fixed obstacle address of transmitting ADS-B Participant
			ANONYMOUS, // DF=18 with CF=1 or CF=6 and IMF=1
			// 12-bit Mode A code and track file number
			MODEA_TRACK, // DF=18 with CF=2/3 and IMF=1
			// TIS-B/ADS-R management information
			TISB_MANAGEMENT_INFO, // DF=18 with CF=4
			// Reserved (e.g. for military use)
			RESERVED, // DF=19 with AF>0 or DF=18 with CF=5 and IMF=1 or DF=18 and CF=7
			// Not (yet) determined
			UNKNOWN
		}

		private int address;
		private Type type;

		/**
		 * @return type of address (e.g. ICAO 24-bit)
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return the address in integer representation
		 */
		public int getAddress() {
			return address;
		}

		/**
		 * @return address as 6 digit hex string
		 */
		public String getHexAddress() {
			return String.format("%06x", address);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			QualifiedAddress that = (QualifiedAddress) o;

			if (address != that.address) return false;
			return type == that.type;
		}

		@Override
		public int hashCode() {
			int result = address;
			result = 31 * result + (type != null ? type.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "QualifiedAddress{" +
					"address=" + address +
					", type=" + type +
					'}';
		}
	}

	private QualifiedAddress address;

	/*
	 * Static fields and functions
	 */

	/**
	 * polynomial for the cyclic redundancy check<br>
	 * Note: we assume that the degree of the polynomial
	 * is divisible by 8 (holds for Mode S) and the msb is left out
	 */
	public static final byte[] CRC_polynomial = {
			(byte)0xFF,
			(byte)0xF4,
			(byte)0x09 // according to Annex 10 V4
	};

	/**
	 * @param msg raw message as byte array
	 * @return calculated parity field as 3-byte array. We used the implementation from<br>
	 *         http://www.eurocontrol.int/eec/gallery/content/public/document/eec/report/1994/022_CRC_calculations_for_Mode_S.pdf
	 */
	public static byte[] calcParity(byte[] msg) {
		byte[] pi = Arrays.copyOf(msg, CRC_polynomial.length);

		boolean invert;
		int byteidx, bitshift;
		for (int i = 0; i < msg.length*8; ++i) { // bit by bit
			invert = ((pi[0] & 0x80) != 0);

			// shift left
			pi[0] <<= 1;
			for (int b = 1; b < CRC_polynomial.length; ++b) {
				pi[b-1] |= (pi[b]>>>7) & 0x1;
				pi[b] <<= 1;
			}

			// get next bit from message
			byteidx = ((CRC_polynomial.length*8)+i) / 8;
			bitshift = 7-(i%8);
			if (byteidx < msg.length)
				pi[pi.length-1] |= (msg[byteidx]>>>bitshift) & 0x1;

			// xor
			if (invert)
				for (int b = 0; b < CRC_polynomial.length; ++b)
					pi[b] ^= CRC_polynomial[b];
		}

		return Arrays.copyOf(pi, CRC_polynomial.length);
	}

	public static int getExpectedLength(byte downlink_format) {
		if (downlink_format < 16) return 7;
		else return 14;
	}

	/*
	 * Constructors
	 */

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected ModeSDownlinkMsg() { }

	/**
	 * NOTE: use this method only for CF 2 and 5
	 * @return the IMF field from TIS-B messages or null if unknown
	 */
	private static Boolean extractIMF(byte[] payload) {
		// format type code
		int ftc = (payload[3] >>> 3) & 0x1F;

		boolean imf;
		if (ftc >= 9 && ftc <= 18 || ftc >= 20 && ftc <= 22)
			// airborne position
			imf = (payload[3]&0x1) == 1;
		else if (ftc >= 5 && ftc <= 8)
			// surface position
			imf = ((payload[5]>>>3)&0x1) == 1;
		else if (ftc >= 2 && ftc <= 4)
			// ID and category -> no IMF, always ICAO 24
			imf = false; // -> will result in ICAO 24
		else if (ftc == 19)
			// velocity / airspeed
			imf = (payload[4]&0x80) > 0;
		else return null;

		return imf;
	}

	/**
	 *
	 * @param reply the bytes of the reply
	 * @param noCRC indicates whether the CRC has been subtracted from the parity field
	 * @throws BadFormatException if message has invalid length or downlink format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeSDownlinkMsg(byte[] reply, boolean noCRC) throws BadFormatException, UnspecifiedFormatError {
		// check format invariants
		this.noCRC = noCRC;

		if (reply.length != 7 && reply.length != 14) // initial test
			throw new BadFormatException("Raw message has an invalid length of "+reply.length);

		downlink_format = reply[0];
		first_field = (byte) (downlink_format & 0x7);
		downlink_format = (byte) (downlink_format>>>3 & 0x1F);

		if (reply.length != getExpectedLength(downlink_format)) {
			throw new BadFormatException(
					String.format("Downlink format %d has length %d, but only %d bytes provided.",
							downlink_format, getExpectedLength(downlink_format), reply.length));
		}

		// extract payload
		payload = Arrays.copyOfRange(reply, 1, reply.length-3);

		// extract parity field
		parity = rawAPToInt(Arrays.copyOfRange(reply,reply.length-3, reply.length));

		// extract ICAO24 address
		address = new QualifiedAddress();
		switch (downlink_format) {
			case 0: // Short air-air (ACAS)
			case 4: // Short altitude reply
			case 5: // Short identity reply
			case 16: // Long air-air (ACAS)
			case 20: // Long Comm-B, altitude reply
			case 21: // Long Comm-B, identity reply
			case 24: // Long Comm-D (ELM)
				address.address = noCRC ? parity : calcParity()^parity;
				break;

			case 11: // all call replies
			case 17: case 18: case 19: // Extended squitter
				byte[] raw_address = new byte[3];
				System.arraycopy(payload, 0, raw_address, 0, 3);
				address.address = rawAPToInt(raw_address);

				if (downlink_format == 18 && first_field==4 || // TIS-B/ADS-R Management Message
						downlink_format == 18 && first_field==7 || // Reserved
						downlink_format == 19 && first_field != 0) { // Reserved for Military Applications
					// TIS-B management frame or military reserved
					// no address given here
					throw new UnspecifiedFormatError("Format unknown or not specified.");
				}

				break;

			default: // unkown downlink format
				// throw exception
				throw new BadFormatException(
						String.format("Invalid downlink format %d detected.", downlink_format));
		}

		// determine address type according to table 2-11 of DO-260B
		if (downlink_format == 18) {
			// check CF
			switch (first_field) {
				case 0:
					address.type = QualifiedAddress.Type.ICAO24;
					break;
				case 1:
					address.type = QualifiedAddress.Type.ANONYMOUS;
					break;
				case 2:
				case 5:
					Boolean imf = extractIMF(payload);
					if (imf == null)
						address.type = QualifiedAddress.Type.UNKNOWN;
					else if (first_field == 2)
						address.type = imf ? QualifiedAddress.Type.MODEA_TRACK : QualifiedAddress.Type.ICAO24;
					else // first_field == 5
						address.type = imf ? QualifiedAddress.Type.RESERVED : QualifiedAddress.Type.NON_ICAO;

					break;
				case 3:
					// coarse position
					if ((payload[3]&0x80) > 0) // IMF field
						address.type = QualifiedAddress.Type.ICAO24;
					else
						address.type = QualifiedAddress.Type.MODEA_TRACK;

					break;
				case 4:
					address.type = QualifiedAddress.Type.TISB_MANAGEMENT_INFO;
					break;
				case 6:
					// TODO: ADS-R
					break;
				case 7:
					address.type = QualifiedAddress.Type.RESERVED;
					break;
				default:
					address.type = QualifiedAddress.Type.UNKNOWN;
			}
		} else if (downlink_format == 19) {
			// check AF field
			address.type = first_field == 0 ? QualifiedAddress.Type.ICAO24 : QualifiedAddress.Type.RESERVED;
		} else {
			address.type = QualifiedAddress.Type.ICAO24;
		}

		setType(subtype.MODES_REPLY);
	}

	/**
	 * We assume the following message format:<br>
	 * | DF (5) | FF (3) | Payload (24/80) | PI/AP (24) |
	 *
	 * @param raw_message Mode S message as byte array
	 * @throws BadFormatException if message has invalid length or payload does
	 * not match specification or parity has invalid length
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeSDownlinkMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(raw_message, false);
	}

	/**
	 * We assume the following message format:<br>
	 * | DF (5) | FF (3) | Payload (24/80) | PI/AP (24) |
	 *
	 * @param raw_message Mode S message in hex representation
	 * @throws BadFormatException if message has invalid length or payload does
	 * not match specification or parity has invalid length
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeSDownlinkMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(Tools.hexStringToByteArray(raw_message), false);
	}

	/**
	 * We assume the following message format:<br>
	 * | DF (5) | FF (3) | Payload (24/80) | PI/AP (24) |
	 *
	 * @param raw_message Mode S message in hex representation
	 * @param noCRC indicates whether the CRC has been subtracted from the parity field
	 * @throws BadFormatException if message has invalid length or payload does
	 * not match specification or parity has invalid length
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeSDownlinkMsg(String raw_message, boolean noCRC) throws BadFormatException, UnspecifiedFormatError {
		this(Tools.hexStringToByteArray(raw_message), noCRC);
	}

	/**
	 * Copy constructor for subclasses
	 *
	 * @param reply instance of ModeSReply to copy from
	 */
	public ModeSDownlinkMsg(ModeSDownlinkMsg reply) {
		downlink_format = reply.downlink_format;
		first_field = reply.first_field;
		payload = Arrays.copyOf(reply.payload, reply.payload.length);
		parity = reply.parity;
		type = reply.type;
		noCRC = reply.noCRC;
		address = new QualifiedAddress();
		address.address = reply.address.address;
		address.type = reply.address.type;
	}

	/**
	 * @return the subtype
	 */
	public subtype getType() {
		return type;
	}

	/**
	 * @param subtype the subtype to set
	 */
	protected void setType(subtype subtype) {
		this.type = subtype;
	}

	/**
	 * @return downlink format of the Mode S reply
	 */
	public byte getDownlinkFormat() {
		return downlink_format;
	}

	/**
	 * Note: the definition of this field depends on the return value of {@link #getDownlinkFormat()}:<br>
	 * - if 17: CA (capability) field<br>
	 * - if 18: CF (TIS-B coarse format) field<br>
	 * - if 19: AF (application) field<br>
	 * @return the first field (three bits after downlink format)
	 */
	public byte getFirstField() {
		return first_field;
	}

	/**
	 * @return fully qualified address (with type)
	 */
	public QualifiedAddress getAddress () {
		return address;
	}

	/**
	 * @return payload as 3- or 10-byte array containing the Mode S
	 * reply without the first and the last three bytes. 
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * @return parity field from message
	 */
	public int getParity() {
		return parity;
	}

	/**
	 * @return calculates Mode S parity as 3-byte array
	 */
	public int calcParity() {
		byte[] message = new byte[payload.length+1];

		message[0] = (byte) (downlink_format<<3 | first_field);
		System.arraycopy(payload, 0, message, 1, payload.length);

		return rawAPToInt(calcParity(message));
	}

	/**
	 * @param raw the three bytes AP field
	 * @return the three bytes converted to an integer
	 */
	private static int rawAPToInt (byte[] raw) {
		if (raw.length != 3)
			throw new RuntimeException("AP can only have 3 bytes!");

		return (raw[0]&0xff) << 16 | (raw[1]&0xff) << 8 | (raw[2]&0xff);
	}

	/**
	 * Re-builds the message from the fields and returns it as a hex string
	 * @return the reply as a hex string
	 */
	public String getHexMessage() {
		byte[] msg = new byte[4+payload.length];
		msg[0] = (byte) (downlink_format<<3 | first_field);
		System.arraycopy(payload, 0, msg, 1, payload.length);
		int crc = noCRC ? getParity()^calcParity() : getParity();
		msg[1+payload.length]   = (byte) ((crc>>16)&0xff);
		msg[1+payload.length+1] = (byte) ((crc>>8)&0xff);
		msg[1+payload.length+2] = (byte) (crc&0xff);
		return Tools.toHexString(msg);
	}

	/**
	 * Important note: use this method for extended
	 * squitter/ADS-B messages (DF 17, 18) only! Other messages may have
	 * their parity field XORed with an ICAO24 transponder address
	 * or an interrogator ID.
	 * @return true if parity in message matched calculated parity
	 */
	public boolean checkParity() {
		return calcParity() == getParity();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		ModeSDownlinkMsg other = (ModeSDownlinkMsg)o;

		// same type?
		if (this.getDownlinkFormat() != other.getDownlinkFormat())
			return false;

		// most common
		if (this.getDownlinkFormat() == 11 && !this.address.equals(other.address))
			return false;

		// ads-b
		if (this.getDownlinkFormat() == 17 && !this.address.equals(other.address))
			return false;
		if (this.getDownlinkFormat() == 18 && !this.address.equals(other.address))
			return false;

		// check the full payload
		if (!Tools.areEqual(this.getPayload(), other.getPayload()) ||
				this.getFirstField() != other.getFirstField())
			return false;

		// and finally the parity
		if (this.getParity() == other.getParity())
			return true;

		// Note: the following checks are necessary since some receivers set
		// the parity field to the remainder of the CRC (0 if correct)
		// while others do not touch it. This combination should be extremely
		// rare so the performance can be more or less neglected.

		if (this.getParity() == other.calcParity())
			return true;

		if (this.calcParity() == other.getParity())
			return true;

		if (this.getDownlinkFormat() == 11) {
			// check interrogator code
			if ((getParity()^calcParity()) == other.getParity())
				return true;

			if ((other.getParity()^other.calcParity()) == this.getParity())
				return true;
		}

		return this.getAddress().address == other.getParity() ||
				this.getParity() == other.getAddress().address;
	}

	@Override
	public String toString() {
		return "ModeSReply{" +
				"downlink_format=" + downlink_format +
				", first_field=" + first_field +
				", payload=" + Tools.toHexString(payload) +
				", noCRC=" + noCRC +
				", type=" + type +
				", address=" + address.toString() +
				'}';
	}

	@Override
	public int hashCode() {
		int result = downlink_format;
		result = 31 * result + (int) first_field;
		result = 31 * result + Arrays.hashCode(payload);
		result = 31 * result + address.address;

		int effective_parity = parity;
		if (noCRC) effective_parity = parity^calcParity();
		result = 31 * result + effective_parity;

		return result;
	}
}
