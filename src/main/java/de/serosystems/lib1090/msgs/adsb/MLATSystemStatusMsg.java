package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

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
 * Decoder for Surface System Status messages (2.2.3.2.7.4)
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class MLATSystemStatusMsg extends ExtendedSquitter implements Serializable {

	byte[] system_status;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected MLATSystemStatusMsg() { }

	/**
	 * @param raw_message the MLAT system status message in hex representation
	 * @throws BadFormatException if message has the wrong typecode
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public MLATSystemStatusMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message the MLAT system status message as byte array
	 * @throws BadFormatException if message has the wrong typecode
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public MLATSystemStatusMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this identification msg
	 * @throws BadFormatException if message has the wrong typecode
	 */
	public MLATSystemStatusMsg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.SURFACE_SYSTEM_STATUS);

		if (getFormatTypeCode() != 24) {
			throw new BadFormatException("MLAT system status messages must have typecode of 24.");
		}

		byte[] msg = getMessage();

		int subtype = msg[0]&0x7;
		if (subtype != 1) {
			throw new BadFormatException("Surface system status messages have subtype 1.");
		}

		system_status = Arrays.copyOfRange(msg, 1, msg.length);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tMLATSystemStatusMsg{" +
				"system_status=" + Tools.toHexString(system_status) +
				'}';
	}
}
