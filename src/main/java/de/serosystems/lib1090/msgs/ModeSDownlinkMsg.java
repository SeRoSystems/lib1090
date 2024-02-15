package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;

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
 * Decoder for Mode S replies
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class ModeSDownlinkMsg implements Serializable {

	private static final long serialVersionUID = 4429516110651295544L;

	/*
	 * Attributes
	 */
	private byte downlink_format; // 0-24
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
			return Tools.toHexString(address, 6);
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
	 * is divisible by 8 (holds for Mode S) and the msb is left out<br>
	 * Deprecated, kept for compatibility. Use {@link #CRC_POLYNOMIAL} instead.
	 */
	@Deprecated
	public static final byte[] CRC_polynomial = {
			(byte) 0xFF,
			(byte) 0xF4,
			(byte) 0x09 // according to Annex 10 V4
	};

	/**
	 * CRC Polynomial as of Annex 10 V4, without leading coefficient.
	 */
	public static final int CRC_POLYNOMIAL = 0xfff409;

	/**
	 * Precomputed CRC table.
	 * For an element CRC_TABLE[i]=j, interpret the bits (MSB = leading coefficient)
	 * of index i as coefficients of a polynomial in group F2[X] and multiply it by X^24.
	 * Then j is the remainder when dividing this polynomial by the generator polynomial defined by Annex 10 V4.
	 */
	private static final int[] CRC_TABLE = new int[]{
			0x000000, 0xfff409, 0x001c1b, 0xffe812, 0x003836, 0xffcc3f, 0x00242d, 0xffd024,
			0x00706c, 0xff8465, 0x006c77, 0xff987e, 0x00485a, 0xffbc53, 0x005441, 0xffa048,
			0x00e0d8, 0xff14d1, 0x00fcc3, 0xff08ca, 0x00d8ee, 0xff2ce7, 0x00c4f5, 0xff30fc,
			0x0090b4, 0xff64bd, 0x008caf, 0xff78a6, 0x00a882, 0xff5c8b, 0x00b499, 0xff4090,
			0x01c1b0, 0xfe35b9, 0x01ddab, 0xfe29a2, 0x01f986, 0xfe0d8f, 0x01e59d, 0xfe1194,
			0x01b1dc, 0xfe45d5, 0x01adc7, 0xfe59ce, 0x0189ea, 0xfe7de3, 0x0195f1, 0xfe61f8,
			0x012168, 0xfed561, 0x013d73, 0xfec97a, 0x01195e, 0xfeed57, 0x010545, 0xfef14c,
			0x015104, 0xfea50d, 0x014d1f, 0xfeb916, 0x016932, 0xfe9d3b, 0x017529, 0xfe8120,
			0x038360, 0xfc7769, 0x039f7b, 0xfc6b72, 0x03bb56, 0xfc4f5f, 0x03a74d, 0xfc5344,
			0x03f30c, 0xfc0705, 0x03ef17, 0xfc1b1e, 0x03cb3a, 0xfc3f33, 0x03d721, 0xfc2328,
			0x0363b8, 0xfc97b1, 0x037fa3, 0xfc8baa, 0x035b8e, 0xfcaf87, 0x034795, 0xfcb39c,
			0x0313d4, 0xfce7dd, 0x030fcf, 0xfcfbc6, 0x032be2, 0xfcdfeb, 0x0337f9, 0xfcc3f0,
			0x0242d0, 0xfdb6d9, 0x025ecb, 0xfdaac2, 0x027ae6, 0xfd8eef, 0x0266fd, 0xfd92f4,
			0x0232bc, 0xfdc6b5, 0x022ea7, 0xfddaae, 0x020a8a, 0xfdfe83, 0x021691, 0xfde298,
			0x02a208, 0xfd5601, 0x02be13, 0xfd4a1a, 0x029a3e, 0xfd6e37, 0x028625, 0xfd722c,
			0x02d264, 0xfd266d, 0x02ce7f, 0xfd3a76, 0x02ea52, 0xfd1e5b, 0x02f649, 0xfd0240,
			0x0706c0, 0xf8f2c9, 0x071adb, 0xf8eed2, 0x073ef6, 0xf8caff, 0x0722ed, 0xf8d6e4,
			0x0776ac, 0xf882a5, 0x076ab7, 0xf89ebe, 0x074e9a, 0xf8ba93, 0x075281, 0xf8a688,
			0x07e618, 0xf81211, 0x07fa03, 0xf80e0a, 0x07de2e, 0xf82a27, 0x07c235, 0xf8363c,
			0x079674, 0xf8627d, 0x078a6f, 0xf87e66, 0x07ae42, 0xf85a4b, 0x07b259, 0xf84650,
			0x06c770, 0xf93379, 0x06db6b, 0xf92f62, 0x06ff46, 0xf90b4f, 0x06e35d, 0xf91754,
			0x06b71c, 0xf94315, 0x06ab07, 0xf95f0e, 0x068f2a, 0xf97b23, 0x069331, 0xf96738,
			0x0627a8, 0xf9d3a1, 0x063bb3, 0xf9cfba, 0x061f9e, 0xf9eb97, 0x060385, 0xf9f78c,
			0x0657c4, 0xf9a3cd, 0x064bdf, 0xf9bfd6, 0x066ff2, 0xf99bfb, 0x0673e9, 0xf987e0,
			0x0485a0, 0xfb71a9, 0x0499bb, 0xfb6db2, 0x04bd96, 0xfb499f, 0x04a18d, 0xfb5584,
			0x04f5cc, 0xfb01c5, 0x04e9d7, 0xfb1dde, 0x04cdfa, 0xfb39f3, 0x04d1e1, 0xfb25e8,
			0x046578, 0xfb9171, 0x047963, 0xfb8d6a, 0x045d4e, 0xfba947, 0x044155, 0xfbb55c,
			0x041514, 0xfbe11d, 0x04090f, 0xfbfd06, 0x042d22, 0xfbd92b, 0x043139, 0xfbc530,
			0x054410, 0xfab019, 0x05580b, 0xfaac02, 0x057c26, 0xfa882f, 0x05603d, 0xfa9434,
			0x05347c, 0xfac075, 0x052867, 0xfadc6e, 0x050c4a, 0xfaf843, 0x051051, 0xfae458,
			0x05a4c8, 0xfa50c1, 0x05b8d3, 0xfa4cda, 0x059cfe, 0xfa68f7, 0x0580e5, 0xfa74ec,
			0x05d4a4, 0xfa20ad, 0x05c8bf, 0xfa3cb6, 0x05ec92, 0xfa189b, 0x05f089, 0xfa0480,
	};

	/**
	 * Interpret a given message as coefficients of a polynomial of group F2[X], multiplied by X^24.
	 * Then compute the remainder when dividing that polynomial by the CRC generator polynomial defined by Annex 10 V4.<br>
	 * Note: multiplying the polynomial with X^24 has the same effect as appending 24 zero bits (i.e. zero 3 bytes) to the message.
	 * The payload given into this function does not include the parity, thus this is exactly what we want here.<br>
	 * We used a LUT optimized implementation of<br>
	 * <a href="http://www.eurocontrol.int/eec/gallery/content/public/document/eec/report/1994/022_CRC_calculations_for_Mode_S.pdf">an algorithm described here</a>.
	 *
	 * @param msg raw message as byte array
	 * @return parity field as 24 bit integer
	 */
	public static int calcParityInt(byte[] msg) {
		int remainder = 0;
		assert CRC_TABLE.length == 1 << 8;
		for (byte b : msg) {
			/* multiply remainder by X^8, creating a polynomial that has potentially a degree higher than 24.
			   We split the remainder into a polynomial of those leading monomials (called dividend) and the rest (will be the new remainder).
			   Furthermore, we add another 8 coefficients (corresponds to one byte) from the message.
			   As we multiply those 8 coefficients by X^24, they will add to the dividend.
			   Note: we have precomputed the outcome of this division in the lookup table.
			 */
			int dividend = remainder >>> (24 - 8); // extract leading coefficients that will have higher degree than 24 after multiplication
			dividend = (dividend ^ b) & 0xff; // add new 8 coefficients (and remove some bits that are not 0 due to overflows and sign extension)
			remainder <<= 8; // multiply by X^8
			remainder ^= CRC_TABLE[dividend]; // compute remainder (i.e. look it up), then subtract it.
		}
		// return remainder and omit overflowing bits
		return remainder & 0xffffff;
	}

	/**
	 * See {@link #calcParityInt(byte[])}. This function converts the integer to a 3 byte array.
	 * Deprecated, kept for compatibility.
	 *
	 * @param msg raw message as byte array
	 * @return parity field as 3 byte array.
	 */
	@Deprecated
	public static byte[] calcParity(byte[] msg) {
		int parity = calcParityInt(msg);
		return new byte[]{
				(byte) ((parity >> 16) & 0xff),
				(byte) ((parity >> 8) & 0xff),
				(byte) (parity & 0xff)
		};
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
	 * NOTE: use this method only for CF 2, 5, and 6
	 * @return the IMF field from TIS-B and ADS-R messages or null if unknown
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
		else if (ftc == 28)
			// emergency and prio status
			imf = (payload[9] & 0x1) != 0;
		else if (ftc == 29)
			// target state and status
			imf = ((payload[9] & 0x20) != 0);
		else if (ftc == 31)
			// operational status
			imf = (payload[9] & 0x1) != 0;

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

		// DF 24 is a special case
		if (downlink_format > 23) {
			// verify that the third most significant bit is 1
			if ((downlink_format & 0b00000100) != 0) {
				throw new BadFormatException("Third MSB of Comm-D Extended Length Message must be 1");
			}

			downlink_format = 24;
		}

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
				address.address = noCRC ? parity : calcParityInt()^parity;
				break;

			case 11: // all call replies
			case 17: case 18: case 19: // Extended squitter
				byte[] raw_address = new byte[3];
				System.arraycopy(payload, 0, raw_address, 0, 3);
				address.address = rawAPToInt(raw_address);

				if (downlink_format == 18 && first_field==4)
					throw new UnspecifiedFormatError("TIS-B/ADS-R management frames not implemented.");
				else if (downlink_format == 18 && first_field == 7)
					throw new UnspecifiedFormatError("Got invalid (reserved) format.");
				else if (downlink_format == 19 && first_field != 0)
					throw new UnspecifiedFormatError("Military frame not implemented.");

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
				case 6:
					Boolean imf = extractIMF(payload);
					if (imf == null)
						address.type = QualifiedAddress.Type.UNKNOWN;
					else if (first_field == 2) // TIS-B
						address.type = imf ? QualifiedAddress.Type.MODEA_TRACK : QualifiedAddress.Type.ICAO24;
					else if (first_field == 5) // TIS-B
						address.type = imf ? QualifiedAddress.Type.RESERVED : QualifiedAddress.Type.NON_ICAO;
					else // first_field == 6 // ADS-R
						address.type = imf ? QualifiedAddress.Type.ANONYMOUS : QualifiedAddress.Type.ICAO24;
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
	public QualifiedAddress getAddress() {
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
	 * @return calculates Mode S parity as 24 bit integer
	 */
	public int calcParityInt() {
		byte[] message = new byte[payload.length + 1];

		message[0] = (byte) (downlink_format << 3 | first_field);
		System.arraycopy(payload, 0, message, 1, payload.length);

		return calcParityInt(message);
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
		int crc = noCRC ? getParity()^ calcParityInt() : getParity();
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
		return calcParityInt() == getParity();
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

		if (this.getParity() == other.calcParityInt())
			return true;

		if (this.calcParityInt() == other.getParity())
			return true;

		if (this.getDownlinkFormat() == 11) {
			// check interrogator code
			if ((getParity() ^ calcParityInt()) == other.getParity())
				return true;

			if ((other.getParity()^other.calcParityInt()) == this.getParity())
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
		if (noCRC) effective_parity = parity^ calcParityInt();
		result = 31 * result + effective_parity;

		return result;
	}
}
