/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 1 Mode A code messages.
 */
public class ModeACodeV1Msg extends ExtendedSquitter implements Serializable, ModeACodeMsg {

	private static final long serialVersionUID = -6444923523067947936L;

	private byte messageSubtype;
	private short modeACode;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected ModeACodeV1Msg() { }

	/**
	 * @param rawMessage raw ADS-B Mode A code message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeACodeV1Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(rawMessage));
	}

	/**
	 * @param rawMessage raw ADS-B Mode A code message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeACodeV1Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(rawMessage));
	}

	/**
	 * @param squitter extended squitter which contains this Mode A code message
	 * @throws BadFormatException if message has wrong format
	 */
	public ModeACodeV1Msg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSB_MODE_A_CODE_V1);

		if (this.getFormatTypeCode() != 23)
            throw new BadFormatException("Mode A code messages must have typecode 23.");

		BitReader reader = BitReader.forBigEndian(this.getMessage());
		messageSubtype = reader.readByte(6, 8);
		if (messageSubtype != 7)
            throw new BadFormatException("Mode A code messages must have subtype 7.");

		modeACode = reader.readShort(9, 21);
	}

	/**
	 * @return the subtype code of the message (should always be 7)
	 */
	public byte getSubtype() {
		return messageSubtype;
	}

	/**
	 * @return the four-digit Mode A (4096) code
	 */
	@Override
	public short getModeACode() {
		return modeACode;
	}

	@Override
	public String toString() {
		return "ModeACodeV1Msg{" + super.toString() +
				", messageSubtype=" + messageSubtype +
				", modeACode=" + modeACode +
				'}';
	}
}
