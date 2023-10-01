package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.decoding.SurfacePosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV2Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

import static de.serosystems.lib1090.decoding.SurfacePosition.groundSpeed;
import static de.serosystems.lib1090.decoding.SurfacePosition.groundSpeedResolution;
import static de.serosystems.lib1090.decoding.SurfacePosition.decodeEPU;
import static de.serosystems.lib1090.decoding.SurfacePosition.decodeHCR;

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
 * Decoder for ADS-R surface position messages
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class SurfacePositionV0Msg extends ExtendedSquitter implements Serializable, PositionMsg {

	private static final long serialVersionUID = -257270493057596465L;

	private boolean horizontal_position_available;
	private byte movement;
	private boolean heading_status; // is heading valid?
	private byte ground_track;
	private boolean imf;
	private CPREncodedPosition position;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected SurfacePositionV0Msg() { }

	/**
	 * @param raw_message raw ADS-R surface position message as hex string
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public SurfacePositionV0Msg(String raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param raw_message raw ADS-R surface position message as byte array
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public SurfacePositionV0Msg(byte[] raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param squitter extended squitter which contains this surface position msg
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 */
	public SurfacePositionV0Msg(ExtendedSquitter squitter, Long timestamp) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSR_SURFACE_POSITION_V0);

		if (!(getFormatTypeCode() == 0 ||
				(getFormatTypeCode() >= 5 && getFormatTypeCode() <= 8)))
			throw new BadFormatException("This is not a position message! Wrong format type code ("+getFormatTypeCode()+").");

		byte[] msg = getMessage();

		horizontal_position_available = getFormatTypeCode() != 0;

		movement = (byte) ((((msg[0]&0x7)<<4) | ((msg[1]&0xF0)>>>4))&0x7F);
		heading_status = (msg[1]&0x8) != 0;
		ground_track = (byte) ((((msg[1]&0x7)<<4) | ((msg[2]&0xF0)>>>4))&0x7F);

		imf = ((msg[2]>>>3)&0x1) == 1;
		boolean cpr_format = ((msg[2]>>>2)&0x1) == 1;
		int cpr_encoded_lat = (((msg[2]&0x3)<<15) | ((msg[3]&0xFF)<<7) | ((msg[4]>>>1)&0x7F)) & 0x1FFFF;
		int cpr_encoded_lon = (((msg[4]&0x1)<<16) | ((msg[5]&0xFF)<<8) | (msg[6]&0xFF)) & 0x1FFFF;

		position = new CPREncodedPosition(
				cpr_format, cpr_encoded_lat, cpr_encoded_lon, 17, true,
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
		return decodeHCR(getFormatTypeCode());

	}

	/**
	 * Navigation accuracy category according to DO-260B Table N-7. In ADS-R version 1+ this information is contained
	 * in the operational status message. For version 0 it is derived from the format type code.
	 *
	 * For a value in meters, use {@link #getPositionUncertainty()}.
	 *
	 * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
	 * {@link AirborneOperationalStatusV1Msg}.
	 */
	public byte getNACp() {
		return this.getNIC();
	}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
	 *
	 * The concept of NACp has been introduced in ADS-R version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link SurfaceOperationalStatusV1Msg}'s and
	 * {@link SurfaceOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-R
	 * version 1 and 2.
	 *
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public double getPositionUncertainty() {
		return decodeEPU(getFormatTypeCode());
	}

	/**
	 * @return Navigation integrity category. A NIC of 0 means "unkown". Values according to DO-260B Table N-4.
	 */
	public byte getNIC() {
		return SurfacePosition.decodeNIC(getFormatTypeCode());
	}

	/**
	 * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
	 *
	 * The concept of SIL has been introduced in ADS-R version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link SurfaceOperationalStatusV1Msg}'s and
	 * {@link SurfaceOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-R
	 * version 1 and 2.
	 *
	 * @return the source integrity level (SIL) which indicates the propability of exceeding
	 *         the NIC containment radius.
	 */
	public byte getSIL() {
		return (byte) (getFormatTypeCode() == 0 ? 0 : 2);
	}

	/**
	 * @return whether ground speed information is available
	 */
	public boolean hasGroundSpeed() {
		return movement >= 1 && movement <= 124;
	}

	/**
	 * @return speed in knots or null if ground speed is not available. The latter can also be checked with
	 * {@link #hasGroundSpeed()}.
	 */
	public Double getGroundSpeed() {
		return groundSpeed(movement);
	}

	/**
	 * @return speed resolution (accuracy) in knots or null if ground speed is not available. The latter can also be
	 * checked with {@link #hasGroundSpeed()}.
	 */
	public Double getGroundSpeedResolution() {
		return groundSpeedResolution(movement);
	}

	/**
	 * @return whether valid heading information is available
	 */
	public boolean hasValidHeading() {
		return heading_status;
	}

	/**
	 * @return heading in decimal degrees ([0, 360]). 0° = geographic north. Returns null if heading is not available.
	 * This can also be checked using {@link #hasValidHeading()}
	 */
	public Double getHeading() {
		if (!heading_status) return null;

		return ground_track*360D/128D;
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	@Override
	public CPREncodedPosition getCPREncodedPosition() {
		return position;
	}

	@Override
	public boolean hasValidPosition() {
		return horizontal_position_available;
	}

	@Override
	public boolean hasValidAltitude() {
		return true;
	}

	@Override
	public Integer getAltitude() {
		return 0;
	}

	@Override
	public Position.AltitudeType getAltitudeType() {
		return Position.AltitudeType.ABOVE_GROUND_LEVEL;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tSurfacePositionV0Msg{" +
				"horizontal_position_available=" + horizontal_position_available +
				", movement=" + movement +
				", heading_status=" + heading_status +
				", ground_track=" + ground_track +
				", imf=" + imf +
				", position=" + position +
				'}';
	}
}
