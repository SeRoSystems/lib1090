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

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class SurfaceOperationalStatusV2Msg extends SurfaceOperationalStatusV1Msg implements Serializable {

	private static final long serialVersionUID = 5774750859726557576L;

	private boolean sil_supplement;

	/**
	 * protected no-arg constructor e.g. for serialization with Kryo
	 **/
	protected SurfaceOperationalStatusV2Msg() {
	}

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException     if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public SurfaceOperationalStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSB_SURFACE_STATUS_V2);

		byte[] msg = this.getMessage();

		if ((byte) (msg[5] >>> 5) != 2)
			throw new BadFormatException("Not a DO-260B/version 2 status message.");

		sil_supplement = ((msg[6] & 0x2) != 0);
	}

	/**
	 * @return whether aircraft has an UAT receiver
	 */
	public boolean hasUATIn() {
		return (capability_class_code & 0x100) != 0;
	}

	/**
	 * @return navigation accuracy category for velocity
	 */
	public byte getNACv() {
		return (byte) ((capability_class_code & 0xE0) >>> 5);
	}

	/**
	 * @return NIC supplement C for use on the surface
	 */
	public boolean getNICSupplementC() {
		return (capability_class_code & 0x10) != 0;
	}

	/**
	 * @return whether aircraft uses a single antenna or two
	 */
	public boolean hasSingleAntenna() {
		return (operational_mode_code & 0x400) != 0;
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
	 * @return encoded longitudinal and lateral distance of the GPS Antenna from the NOSE of the aircraft
	 * (see Table 2-66 and 2-67, RTCA DO-260B)
	 */
	public byte getGPSAntennaOffset() {
		return (byte) (operational_mode_code & 0xFF);
	}

	@Override
	public boolean hasPositionOffsetApplied() {
		// Note: using definition of ED-129B, which is a bit more explicit than DO-260B
		return getGPSAntennaOffset() == 0x1;
	}

	/**
	 * Get lateral axis GPS antenna offset.
	 * <ul>
	 *     <li>values are measured from the longitudinal center line (=roll axis) of the aircraft</li>
	 *     <li>values are given in meters</li>
	 *     <li>positive values mean "toward left wing tip"</li>
	 *     <li>negative values mean "toward right wind tip</li>
	 *     <li>values have a resolution of 2m</li>
	 *     <li>values are capped at 6m</li>
	 *     <li>{@code null} means "no data"</li>
	 * </ul>
	 *
	 * @return lateral axis GPS Antenna offset in meters
	 * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
	 */
	public Integer getLateralAxisGPSAntennaOffset() {
		int offset = getGPSAntennaOffset() & 0x60;
		boolean right = (getGPSAntennaOffset() & 0x80) != 0;
		return !right && offset == 0 ? null :
				2 * (right ? -offset : offset);
	}

	/**
	 * Get longitudinal axis GPS antenna offset.
	 * <ul>
	 *     <li>values are measured from the nose of the aircraft</li>
	 *     <li>values are given in meters</li>
	 *     <li>values have a resolution of 2m</li>
	 *     <li>values are capped at 60m</li>
	 *     <li>{@code null} means "no data"</li>
	 * </ul>
	 *
	 * @return longitudinal axis GPS Antenna offset in meters
	 * @see #hasPositionOffsetApplied() to check if the aircraft already corrects the antenna offset. In that case, this function won't return meaningful data.
	 */
	public Integer getLongitudinalAxisGPSAntennaOffset() {
		int offset = getGPSAntennaOffset() & 0x1e;
		return (offset & 0x30) == 0 ? null : 2 * (offset - 1);
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

	@Override
	public String toString() {
		return "SurfaceOperationalStatusV2Msg{" +
				"sil_supplement=" + sil_supplement +
				", capability_class_code=" + capability_class_code +
				", operational_mode_code=" + operational_mode_code +
				'}';
	}
}
