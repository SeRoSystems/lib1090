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
public class ManagementMessage extends ExtendedSquitter implements Serializable {

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected ManagementMessage() { }

	/**
	 * @param raw_message raw TIS-B identification and category message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ManagementMessage(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw TIS-B identity and category message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ManagementMessage(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter containing the identity and category message
	 * @throws BadFormatException if message has wrong format
	 */
	public ManagementMessage(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.TISB_IDENTIFICATION);

		if (getDownlinkFormat() != 18) {
			throw new BadFormatException("TIS-B messages must have downlink format 18.");
		}

		// Table 2-13
		if (getFirstField() != 4)
			throw new BadFormatException("TIS-B management messages must have CF value 6.");

		// not specified further
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tManagementMessage{}";
	}
}
