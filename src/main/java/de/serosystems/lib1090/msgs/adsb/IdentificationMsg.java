package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.Identification;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

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
 * Decoder for ADS-B identification messages
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class IdentificationMsg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 3475444849066416732L;

	private byte emitter_category;
	private byte[] identity;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected IdentificationMsg() { }

	/**
	 * @param raw_message the identification message in hex representation
	 * @throws BadFormatException if message has the wrong typecode
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public IdentificationMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message the identification message as byte array
	 * @throws BadFormatException if message has the wrong typecode
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public IdentificationMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this identification msg
	 * @throws BadFormatException if message has the wrong typecode
	 */
	public IdentificationMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSB_IDENTIFICATION);

		if (getFormatTypeCode() < 1 || getFormatTypeCode() > 4) {
			throw new BadFormatException("Identification messages must have typecode of 1-4.");
		}

		byte[] msg = this.getMessage();
		emitter_category = (byte) (msg[0] & 0x7);

		// extract identity
		identity = Identification.decodeAircraftIdentification(msg);
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
		return Identification.mapChar(identity);
	}

	/**
	 * @return the decription of the emitter's category according to
	 *         the ADS-B message format specification
	 */
	public String getCategoryDescription() {
		return Identification.categoryDescription(getFormatTypeCode(), emitter_category);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tIdentificationMsg{" +
				"emitter_category=" + emitter_category +
				", identity=" + Arrays.toString(identity) +
				'}';
	}
}
