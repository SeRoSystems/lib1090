package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

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
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class AirborneOperationalStatusV2Msg extends AirborneOperationalStatusV1Msg implements Serializable {

	private static final long serialVersionUID = -2032348919695227545L;

	private byte geometric_vertical_accuracy; // bit 49 and 50
	private boolean sil_supplement;

	/**
	 * protected no-arg constructor e.g. for serialization with Kryo
	 **/
	protected AirborneOperationalStatusV2Msg() {
	}

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV2Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV2Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSB_AIRBORN_STATUS_V2);

		byte[] msg = this.getMessage();

		if ((byte) (msg[5] >>> 5) != 2)
			throw new BadFormatException("Not a DO-260B/version 2 status message.");

		geometric_vertical_accuracy = baq;
		// Bit 55
		sil_supplement = ((msg[6] & 0x2) != 0);
	}

	/**
	 * @return whether operational TCAS is available
	 */
	public boolean hasOperationalTCAS() {
		return (capability_class_code & 0x2000) != 0;
	}

	/**
	 * @return whether 1090ES IN / CDTI is available
	 */
	public boolean has1090ESIn() {
		return (capability_class_code & 0x1000) != 0;
	}

	/**
	 * @return whether aircraft has an UAT receiver
	 */
	public boolean hasUATIn() {
		return (capability_class_code & 0x20) != 0;
	}

	/**
	 * @return whether aircraft uses a single antenna or two
	 */
	public boolean hasSingleAntenna() {
		return (operational_mode_code & 0x400) != 0;
	}

	/**
	 * @return the encoded geometric vertical accuracy (see DO-260B 2.2.3.2.7.2.8)
	 */
	public byte getGVA() {
		return geometric_vertical_accuracy;
	}

	/**
	 * @return the geometric vertical accuracy in meters or -1 for "unknown or above 150m"
	 */
	public int getGeometricVerticalAccuracy() {
		if (geometric_vertical_accuracy == 1)
			return 150;
		else if (geometric_vertical_accuracy == 2)
			return 45;
		else return -1;
	}

	/**
	 * For interpretation see Table 2-65 in DO-260B
	 *
	 * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B)
	 */
	public byte getSystemDesignAssurance() {
		return (byte) ((operational_mode_code & 0x300) >>> 8);
	}

	/**
	 * DO-260B 2.2.3.2.7.2.14
	 *
	 * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
	 * it's based on "per hour".
	 */
	public boolean hasSILSupplement() {
		return sil_supplement;
	}

	public byte getBAQ() {
		throw new UnsupportedOperationException("BAQ not present in version 2");
	}

	@Override
	public String toString() {
		return "AirborneOperationalStatusV2Msg{" +
				"geometric_vertical_accuracy=" + geometric_vertical_accuracy +
				", sil_supplement=" + sil_supplement +
				", capability_class_code=" + capability_class_code +
				", operational_mode_code=" + operational_mode_code +
				", baq=" + baq +
				'}';
	}
}
