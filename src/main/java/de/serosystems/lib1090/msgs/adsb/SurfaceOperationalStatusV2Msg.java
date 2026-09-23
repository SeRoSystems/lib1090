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
public class SurfaceOperationalStatusV2Msg extends SurfaceOperationalStatusV1Msg implements Serializable {

	private static final long serialVersionUID = 5774750859726557576L;

	private boolean sil_supplement;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected SurfaceOperationalStatusV2Msg() { }

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException  if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSB_SURFACE_STATUS_V2);

		byte[] msg = this.getMessage();

		if ((byte) (msg[5]>>>5) != 2)
			throw new BadFormatException("Not a DO-260B/version 2 status message.");

		sil_supplement = ((msg[6] & 0x2) != 0);
	}

	/**
	 * @return whether aircraft has an UAT receiver, ME bit 16
	 */
	@Override
	public boolean hasUATIn() {
		return (capability_class_code & 0x10) != 0;
	}

	/**
	 * @return navigation accuracy category for velocity, ME bits 17-19
	 */
	@Override
	public byte getNACv() {
		return (byte) ((capability_class_code & 0xE) >>> 1);
	}

	/**
	 * @return NIC supplement C for use on the surface, ME bit 20
	 */
	@Override
	public boolean getNICSupplementC() {
		return (capability_class_code & 0x1) != 0;
	}

	/**
	 * @return whether aircraft uses a single antenna or two, ME bit 30
	 */
	@Override
	public boolean hasSingleAntenna() {
		return (operational_mode_code & 0x400) != 0;
	}

	/**
	 * For interpretation see Table 2-65 in DO-260B
	 * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B), ME bits 31-32
	 */
	@Override
	public byte getSystemDesignAssurance() {
		return (byte) ((operational_mode_code & 0x300) >>> 8);
	}

	/**
	 * Encoded lateral (ME bits 33-35) and longitudinal (ME bits 36-40) distance of the GPS antenna,
	 * see DO-260B Tables 2-66 and 2-67.
	 *
	 * @return encoded GPS antenna offset (8 bits); mask with {@code 0xFF} to obtain the unsigned value
	 * @see #getLateralAxisGPSAntennaOffset()
	 * @see #getLongitudinalAxisGPSAntennaOffset()
	 */
	@Override
	public byte getGPSAntennaOffset() {
		return (byte) (operational_mode_code & 0xFF);
	}

	/**
	 * Lateral axis GPS antenna offset, derived from ME bits 33-35 (DO-260B Table 2-66).
	 * <ul>
	 *     <li>values are measured from the longitudinal center line (=roll axis) of the aircraft</li>
	 *     <li>values are given in meters</li>
	 *     <li>values denote an upper bound</li>
	 *     <li>positive values mean "toward left wing tip"</li>
	 *     <li>negative values mean "toward right wing tip"</li>
	 *     <li>values have a resolution of 2m</li>
	 *     <li>values are capped at 6m, i.e. 6 means "or above"</li>
	 * </ul>
	 *
	 * @return lateral axis GPS antenna offset in meters or {@code null} for "no data"
	 * @see #hasPositionOffsetApplied() if the aircraft already corrects the antenna offset, this is not meaningful
	 */
	public Integer getLateralAxisGPSAntennaOffset() {
		boolean right = (operational_mode_code & 0x80) != 0;
		int magnitude = (operational_mode_code & 0x60) >>> 5;
		if (!right && magnitude == 0)
			return null;
		return 2 * (right ? -magnitude : magnitude);
	}

	/**
	 * Longitudinal axis GPS antenna offset, derived from ME bits 36-40 (DO-260B Table 2-67).
	 * <ul>
	 *     <li>values are measured from the nose of the aircraft</li>
	 *     <li>values are given in meters</li>
	 *     <li>values denote an upper bound</li>
	 *     <li>values have a resolution of 2m</li>
	 *     <li>values are capped at 60m, i.e. 60 means "or above"</li>
	 * </ul>
	 *
	 * @return longitudinal axis GPS antenna offset in meters or {@code null} for "no data"
	 * @see #hasPositionOffsetApplied() if the aircraft already corrects the antenna offset, this is not meaningful
	 */
	public Integer getLongitudinalAxisGPSAntennaOffset() {
		int offset = operational_mode_code & 0x1F;
		return offset == 0 ? null : 2 * (offset - 1);
	}

	/**
	 * From version 2 on, POA is no longer a capability class code flag (ME bit 11 is reserved) but encoded
	 * as value 1 of the longitudinal GPS antenna offset (ME bits 36-40), see ED-102B.
	 *
	 * @return true if the reported position has already been corrected for the GPS antenna offset
	 */
	@Override
	public boolean hasPositionOffsetApplied() {
		return (operational_mode_code & 0x1F) == 1;
	}

	/**
	 * DO-260B 2.2.3.2.7.2.14
	 * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
	 * 			it's based on "per hour".
	 */
	public boolean hasSILSupplement() {
		return sil_supplement;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tSurfaceOperationalStatusV2Msg{" +
				"sil_supplement=" + sil_supplement +
				'}';
	}
}
