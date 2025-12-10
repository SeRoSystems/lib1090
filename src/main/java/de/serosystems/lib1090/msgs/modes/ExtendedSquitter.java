package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.Tools;
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
 * Decoder for Mode S extended squitters
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class ExtendedSquitter extends ModeSDownlinkMsg implements Serializable {

	private static final long serialVersionUID = 8396282390353645782L;

	private byte[] message;
	private byte format_type_code;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected ExtendedSquitter() { }

	/**
	 * @param raw_message raw extended squitter as hex string
	 * @throws BadFormatException if message is not extended squitter or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ExtendedSquitter(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param raw_message raw extended squitter as byte array
	 * @throws BadFormatException if message is not extended squitter or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ExtendedSquitter(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param reply Mode S reply containing this extended squitter
	 * @throws BadFormatException if message is not extended squitter or 
	 * contains wrong values.
	 */
	public ExtendedSquitter(ModeSDownlinkMsg reply) throws BadFormatException {
		super(reply);
		setType(subtype.EXTENDED_SQUITTER);

		if (getDownlinkFormat() < 17 || getDownlinkFormat() > 19 ||
				getDownlinkFormat() == 18 && (getFirstField() == 4 || getFirstField() == 7) ||
				getDownlinkFormat() == 19 && getFirstField() > 0)
			throw new BadFormatException("Message is not an extended squitter!");

		byte[] payload = getPayload();

		// extract ADS-B message
		message = new byte[7];
		System.arraycopy(payload, 3, message, 0, 7);

		format_type_code = (byte) ((message[0] >>> 3) & 0x1F);
	}

	/**
	 * Copy constructor for subclasses
	 * 
	 * @param squitter instance of ExtendedSquitter to copy from
	 */
	public ExtendedSquitter(ExtendedSquitter squitter) {
		super(squitter);

		message = squitter.getMessage();
		format_type_code = squitter.getFormatTypeCode();
	}

	/**
	 * @return The message's format type code (see ICAO Annex 10 V4)
	 */
	public byte getFormatTypeCode() {
		return format_type_code;
	}

	/**
	 * @return The message as 7-byte array
	 */
	public byte[] getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tExtendedSquitter{" +
				"message=" + Tools.toHexString(message) +
				", format_type_code=" + format_type_code +
				'}';
	}
}
