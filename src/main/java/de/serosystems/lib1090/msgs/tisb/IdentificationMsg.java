package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.util.Arrays;

import static de.serosystems.lib1090.msgs.adsb.IdentificationMsg.categoryDescription;
import static de.serosystems.lib1090.msgs.adsb.IdentificationMsg.mapChar;

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
 * Decoder for TIS-B Identification and Category Message (DO-260B, 2.2.17.3.3).
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class IdentificationMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = -1692656992966148114L;

	private byte emitter_category;
	private byte[] identity;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected IdentificationMsg() { }

	/**
	 * @param raw_message raw TIS-B identification and category message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public IdentificationMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw TIS-B identity and category message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public IdentificationMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter containing the identity and category message
	 * @throws BadFormatException if message has wrong format
	 */
	public IdentificationMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.TISB_IDENTIFICATION);

		if (getDownlinkFormat() != 18) {
			throw new BadFormatException("TIS-B messages must have downlink format 18.");
		}

		if (getFormatTypeCode() < 1 || getFormatTypeCode() > 4) {
			throw new BadFormatException("Identification messages must have typecode of 1-4.");
		}

		// Table 2-13
		if (getFirstField() != 2 && getFirstField() != 5)
			throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

		byte[] msg = this.getMessage();
		emitter_category = (byte) (msg[0] & 0x7);

		// extract identity
		identity = new byte[8];
		int byte_off, bit_off;
		for (int i=8; i>=1; i--) {
			// calculate offsets
			byte_off = (i*6)/8; bit_off = (i*6)%8;

			// char aligned with byte?
			if (bit_off == 0) identity[i-1] = (byte) (msg[byte_off]&0x3F);
			else {
				++byte_off;
				identity[i-1] = (byte) (msg[byte_off]>>>(8-bit_off)&(0x3F>>>(6-bit_off)));
				// should we add bits from the next byte?
				if (bit_off < 6) identity[i-1] |= msg[byte_off-1]<<bit_off&0x3F;
			}
		}
	}

	/**
	 * @return the emitter's category (numerical)
	 */
	public byte getEmitterCategory() {
		return emitter_category;
	}

	/**
	 * @return the call sign as 8 characters array
	 */
	public char[] getIdentity() {
		return mapChar(identity);
	}

	/**
	 * @return the decription of the emitter's category according to
	 *         the ADS-B message format specification
	 */
	public String getCategoryDescription () {
		return categoryDescription(getFormatTypeCode(), emitter_category);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tIdentificationMsg{" +
				"emitter_category=" + emitter_category +
				", identity=" + Arrays.toString(identity) +
				'}';
	}
}
