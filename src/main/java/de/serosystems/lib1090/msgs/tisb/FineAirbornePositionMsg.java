package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

import static de.serosystems.lib1090.msgs.adsb.AirbornePositionV0Msg.*;

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
 * Decoder for TIS-B fine airborne position (DO-260B, 2.2.17.3.1).
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class FineAirbornePositionMsg extends ExtendedSquitter implements Serializable, PositionMsg {

	private static final long serialVersionUID = 8691583253646403341L;

	// bits 6-7
	private byte surveillance_status;
	// bit 8
	private boolean imf;
	// bits 9-20
	private short encoded_altitude;
	// bit 21 -> reserved

	// bites 22-56
	CPREncodedPosition position;


	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected FineAirbornePositionMsg() { }

	/**
	 * @param raw_message raw TIS-B fine airborne position message as hex string
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public FineAirbornePositionMsg(String raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param raw_message raw TIS-B fine airborne position message as byte array
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public FineAirbornePositionMsg(byte[] raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param squitter extended squitter containing the airborne position msg in high resolution
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 */
	public FineAirbornePositionMsg(ExtendedSquitter squitter, Long timestamp) throws BadFormatException {
		super(squitter);

		setType(subtype.TISB_FINE_AIRBORNE_POSITION);

		if (getDownlinkFormat() != 18) {
			throw new BadFormatException("TIS-B messages must have downlink format 18.");
		}

		if (!((getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18) ||
				(getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)))
			throw new BadFormatException("This is not a TIS-B position message! Wrong format type code.");

		// Table 2-13
		if (getFirstField() != 2 && getFirstField() != 5)
			throw new BadFormatException("Fine TIS-B messages must have CF value 2 or 5.");

		byte[] msg = getMessage();

		surveillance_status = (byte) ((msg[0]>>>1)&0x3);
		imf = (msg[0]&0x1) == 1;
		encoded_altitude = (short) (((msg[1]<<4)|((msg[2]>>>4)&0xF))&0xFFF);

		boolean cpr_format = ((msg[2]>>>2)&0x1) == 1;
		int cpr_encoded_lat = (((msg[2]&0x3)<<15) | ((msg[3]&0xFF)<<7) | ((msg[4]>>>1)&0x7F)) & 0x1FFFF;
		int cpr_encoded_lon = (((msg[4]&0x1)<<16) | ((msg[5]&0xFF)<<8) | (msg[6]&0xFF)) & 0x1FFFF;

		position = new CPREncodedPosition(
				cpr_format, cpr_encoded_lat, cpr_encoded_lon, 17, false,
				timestamp == null ? System.currentTimeMillis() : timestamp);

	}

	/**
	 * The position error, i.e., 95% accuracy for the horizontal position. Values according to DO-260B Table N-4.
	 *
	 *  The horizontal containment radius is also known as "horizontal protection level".
	 *
	 * @return horizontal containment radius limit in meters. A return value of -1 means "unkown".
	 */
	public double getHorizontalContainmentRadiusLimit() {
		return typeCodeToHCR(getFormatTypeCode());
	}

	/**
	 * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
	 * in the operational status message. For version 0 it is derived from the format type code.
	 *
	 * For a value in meters, use {@link #getPositionUncertainty()}.
	 *
	 * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
	 * {@link AirborneOperationalStatusV1Msg}.
	 */
	public byte getNACp() {
		return typeCodeToNACp(getFormatTypeCode());
	}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
	 *
	 * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
	 * {@link AirborneOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public double getPositionUncertainty() {
		return typeCodeToPositionUncertainty(getFormatTypeCode());
	}

	/**
	 * @return Navigation integrity category. A NIC of 0 means "unkown".
	 */
	public byte getNIC() {
		return typeCodeToNIC(getFormatTypeCode());
	}

	/**
	 * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
	 *
	 * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
	 * {@link AirborneOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the source integrity level (SIL) which indicates the propability of exceeding
	 *         the NIC containment radius.
	 */
	public byte getSIL() {
		return typeCodeToSIL(getFormatTypeCode());
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	/**
	 * @see #getSurveillanceStatusDescription()
	 * @return the surveillance status
	 */
	public byte getSurveillanceStatus() {
		return surveillance_status;
	}

	/**
	 * This is a function of the surveillance status field in the position
	 * message.
	 *
	 * @return surveillance status description as defines in DO-260B
	 */
	public String getSurveillanceStatusDescription() {
		String[] desc = {
				"No condition information",
				"Permanent alert (emergency condition)",
				"Temporary alert (change in Mode A identity code oter than emergency condition)",
				"SPI condition"
		};

		return desc[surveillance_status];
	}

	@Override
	public boolean hasValidPosition() {
		return getFormatTypeCode() >= 9;
	}

	@Override
	public CPREncodedPosition getCPREncodedPosition() {
		return position;
	}

	@Override
	public boolean hasValidAltitude() {
		return getFormatTypeCode() >= 9;
	}

	@Override
	public Integer getAltitude() {
		if (!hasValidAltitude()) return null;
		return Altitude.decode12BitAltitude(encoded_altitude);
	}

	@Override
	public Position.AltitudeType getAltitudeType () {
		if (getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18)
			return Position.AltitudeType.BAROMETRIC_ALTITUDE;
		else if (getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)
			return Position.AltitudeType.ABOVE_WGS84_ELLIPSOID;
		else return Position.AltitudeType.UNKNOWN;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tFineAirbornePositionMsg{" +
				"surveillance_status=" + surveillance_status +
				", imf=" + imf +
				", encoded_altitude=" + encoded_altitude +
				", position=" + position +
				'}';
	}
}
