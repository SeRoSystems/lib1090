package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;

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
public class ModeSReply implements Serializable {
	private static final long serialVersionUID = 5369519167589262290L;

	/*
	 * Attributes
	 */
	private byte downlink_format; // 0-31
	private byte first_field; // the 3 bits after downlink format
	private byte[] payload; // 3 or 10 bytes
	private byte[] parity; // 3 bytes
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

		// TIS-B subtypes
		TISB_FINE_AIRBORNE_POSITION,
		TISB_FINE_SURFACE_POSITION,
		TISB_IDENTIFICATION,
		TISB_VELOCITY,
		TISB_COARSE_POSITION
	}
	private subtype type;

	public static class QualifiedAddress {
		/**
		 * Different types of addresses in the AA field (see Table 2-11 in DO-260B)
		 */
		public enum Types {
			// ICAO 24-bit address
			ICAO24,
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

		private byte[] address; // 3 bytes
		private Types type;

		/**
		 * @return type of address (e.g. ICAO 24-bit)
		 */
		public Types getType() {
			return type;
		}

		/**
		 * @return the address in integer representation
		 */
		public int getAddress() {
			return (address[0]&0xff) << 16 | (address[1]&0xff) << 8 | (address[2]&0xff);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			QualifiedAddress that = (QualifiedAddress) o;

			if (!Arrays.equals(address, that.address)) return false;
			return type == that.type;
		}

		@Override
		public int hashCode() {
			int result = Arrays.hashCode(address);
			result = 31 * result + (type != null ? type.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "QualifiedAddress{" +
					"icao24=" + Tools.toHexString(address) +
					", address_type=" + type +
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
	protected ModeSReply() { }

	/**
	 *
	 * @param reply the bytes of the reply
	 * @param noCRC indicates whether the CRC has been subtracted from the parity field
	 * @throws BadFormatException if message has invalid length or downlink format
	 */
	public ModeSReply (byte[] reply, boolean noCRC) throws BadFormatException {
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
		parity = Arrays.copyOfRange(reply,reply.length-3, reply.length);

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
				address.address = noCRC ? parity : Tools.xor(calcParity(), parity);
				break;

			case 11: // all call replies
			case 17: case 18: // Extended squitter
				address.address = new byte[3];
				System.arraycopy(payload, 0, address.address, 0, 3);
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
					address.type = QualifiedAddress.Types.ICAO24;
					break;
				case 1:
					address.type = QualifiedAddress.Types.ANONYMOUS;
					break;
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
					// TODO: check IMF subfield
					address.type = QualifiedAddress.Types.UNKNOWN;
					break;
				case 7:
					address.type = QualifiedAddress.Types.RESERVED;
					break;
				default:
					address.type = QualifiedAddress.Types.UNKNOWN;
			}
		} else if (downlink_format == 19) {
			// check AF field
			address.type = first_field == 0 ? QualifiedAddress.Types.ICAO24 : QualifiedAddress.Types.RESERVED;
		} else {
			address.type = QualifiedAddress.Types.ICAO24;
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
	 */
	public ModeSReply (byte[] raw_message) throws BadFormatException {
		this(raw_message, false);
	}

	/**
	 * We assume the following message format:<br>
	 * | DF (5) | FF (3) | Payload (24/80) | PI/AP (24) |
	 *
	 * @param raw_message Mode S message in hex representation
	 * @throws BadFormatException if message has invalid length or payload does
	 * not match specification or parity has invalid length
	 */
	public ModeSReply (String raw_message) throws BadFormatException {
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
	 */
	public ModeSReply (String raw_message, boolean noCRC) throws BadFormatException {
		this(Tools.hexStringToByteArray(raw_message), noCRC);
	}

	/**
	 * Copy constructor for subclasses
	 *
	 * @param reply instance of ModeSReply to copy from
	 */
	public ModeSReply (ModeSReply reply) {
		downlink_format = reply.downlink_format;
		first_field = reply.first_field;
		payload = Arrays.copyOf(reply.payload, reply.payload.length);
		parity = Arrays.copyOf(reply.parity, reply.parity.length);
		type = reply.type;
		noCRC = reply.noCRC;
		address = new QualifiedAddress();
		address.address = Arrays.copyOf(reply.address.address, reply.address.address.length);
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
	 * @return the 24-bit representation of the address (3-byte array)
	 */
	public byte[] getRawAddress() {
		return address.address;
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
	 * @return parity field from message as 3-byte array
	 */
	public byte[] getParity() {
		return parity;
	}

	/**
	 * @return calculates Mode S parity as 3-byte array
	 */
	public byte[] calcParity() {
		byte[] message = new byte[payload.length+1];

		message[0] = (byte) (downlink_format<<3 | first_field);
		System.arraycopy(payload, 0, message, 1, payload.length);

		return calcParity(message);
	}

	/**
	 * Re-builds the message from the fields and returns it as a hex string
	 * @return the reply as a hex string
	 */
	public String getHexMessage() {
		byte[] msg = new byte[4+payload.length];
		msg[0] = (byte) (downlink_format<<3 | first_field);
		System.arraycopy(payload, 0, msg, 1, payload.length);
		byte[] crc = noCRC ? Tools.xor(getParity(), calcParity()) : getParity();
		for (int i = 0; i < 3; ++i) msg[1+payload.length+i] = crc[i];
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
		return Tools.areEqual(calcParity(), getParity());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		ModeSReply other = (ModeSReply)o;

		// same type?
		if (this.getDownlinkFormat() != other.getDownlinkFormat())
			return false;

		// most common
		if (this.getDownlinkFormat() == 11 &&
				!Tools.areEqual(this.getRawAddress(), other.getRawAddress()))
			return false;

		// ads-b
		if (this.getDownlinkFormat() == 17 &&
				!Tools.areEqual(this.getRawAddress(), other.getRawAddress()))
			return false;
		if (this.getDownlinkFormat() == 18 &&
				!Tools.areEqual(this.getRawAddress(), other.getRawAddress()))
			return false;

		// check the full payload
		if (!Tools.areEqual(this.getPayload(), other.getPayload()) ||
				this.getFirstField() != other.getFirstField())
			return false;

		// and finally the parity
		if (Tools.areEqual(this.getParity(), other.getParity()))
			return true;

		// Note: the following checks are necessary since some receivers set
		// the parity field to the remainder of the CRC (0 if correct)
		// while others do not touch it. This combination should be extremely
		// rare so the performance can be more or less neglected.

		if (Tools.areEqual(this.getParity(), other.calcParity()))
			return true;

		if (Tools.areEqual(this.calcParity(), other.getParity()))
			return true;

		if (this.getDownlinkFormat() == 11) {
			// check interrogator code
			if (Tools.areEqual(Tools.xor(calcParity(), getParity()),
					other.getParity()))
				return true;

			if (Tools.areEqual(Tools.xor(other.calcParity(),
					other.getParity()), this.getParity()))
				return true;
		}

		return Tools.areEqual(this.getRawAddress(), other.getParity()) ||
				Tools.areEqual(this.getParity(), other.getRawAddress());
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
		// same method used by String
		int sum = downlink_format<<3|first_field;
		for (int i = 0; i<payload.length; ++i)
			sum += payload[i]*31^(payload.length-i);

		byte[] effective_partiy = parity;
		if (noCRC) effective_partiy = Tools.xor(parity, calcParity());

		for (int i = 0; i<effective_partiy.length; ++i)
			sum += effective_partiy[i]*31^(payload.length+effective_partiy.length-i);
		return sum;
	}
}
