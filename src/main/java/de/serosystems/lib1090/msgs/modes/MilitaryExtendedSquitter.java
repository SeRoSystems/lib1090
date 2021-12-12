package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;

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
 * Decoder for Mode S military extended squitters (DF19)<br>
 * Note: this format is practically unspecified
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class MilitaryExtendedSquitter extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 2459913562133769670L;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected MilitaryExtendedSquitter() { }

	/**
	 * @param raw_message raw military extended squitter as hex string
	 * @throws BadFormatException if message is not military extended squitter or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public MilitaryExtendedSquitter(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param raw_message raw military extended squitter as byte array
	 * @throws BadFormatException if message is not military extended squitter or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public MilitaryExtendedSquitter(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param reply Mode S reply containing this military extended squitter
	 * @throws BadFormatException if message is not a military extended squitter or 
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public MilitaryExtendedSquitter(ModeSDownlinkMsg reply) throws BadFormatException, UnspecifiedFormatError {
		super(reply);
		setType(subtype.MILITARY_EXTENDED_SQUITTER);

		if (getDownlinkFormat() != 19)
			throw new BadFormatException("Message is not a military extended squitter!");

		if (getFirstField() != 0)
			throw new UnspecifiedFormatError("Military extended squitters are only specified for AF=0.");
	}

	/**
	 * Copy constructor for subclasses
	 * 
	 * @param squitter instance of MilitaryExtendedSquitter to copy from
	 */
	public MilitaryExtendedSquitter(MilitaryExtendedSquitter squitter) {
		super(squitter);
	}
}
