package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

import static de.serosystems.lib1090.msgs.adsb.AirbornePositionV0Msg.decodeAltitude;

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
 * Decoder for TIS-B coarse position (DO-260B, 2.2.17.3.5).
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class CoarsePositionMsg extends ExtendedSquitter implements Serializable, PositionMsg {

	private static final long serialVersionUID = -8532037642870724311L;

	private boolean imf;
	private byte surveillance_status;
	private byte svid;
	private short encoded_altitude;
	private boolean ground_track_status;
	private byte ground_track_angle;
	private byte ground_speed;
	CPREncodedPosition position;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected CoarsePositionMsg() { }

	/**
	 * @param raw_message raw TIS-B coarse position message as hex string
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CoarsePositionMsg(String raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param raw_message raw TIS-B coarse position message as byte array
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CoarsePositionMsg(byte[] raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param squitter extended squitter containing the TIS-B position and velocity in low resolution
	 * @param timestamp timestamp for this position message in milliseconds
	 * @throws BadFormatException if message has wrong format
	 */
	public CoarsePositionMsg(ExtendedSquitter squitter, Long timestamp) throws BadFormatException {
		super(squitter);
		setType(subtype.TISB_COARSE_POSITION);

		if (getDownlinkFormat() != 18) {
			throw new BadFormatException("TIS-B messages must have downlink format 18.");
		}

		// Table 2-13
		if (getFirstField() != 3)
			throw new BadFormatException("Coarse TIS-B messages must have CF value 3.");

		byte[] msg = getMessage();

		imf = (msg[0]&0x80) > 0;
		surveillance_status = (byte) ((msg[0]>>>5)&0x3);
		svid = (byte) ((msg[0]>>>1)&0xf);
		encoded_altitude = (short) (((msg[0]&0x1)<<11) | ((msg[1]&0xff)<<3) | ((msg[2]>>>5)&0x7));
		ground_track_status = (msg[2]&0x10) > 0;
		ground_track_angle = (byte) (((msg[2]&0xf)<<1) | ((msg[3]>>>7)&0x1));
		ground_speed = (byte) ((msg[3]>>>1)&0x3f);

		boolean cpr_format = (msg[3]&0x1) > 0;
		short cpr_encoded_lat = (short) (((msg[4]&0xff)<<4) | ((msg[5]&0xff)>>4));
		short cpr_encoded_lon = (short) (((msg[5]&0x0f)<<8) | (msg[6]&0xff));

		position = new CPREncodedPosition(
				cpr_format, cpr_encoded_lat, cpr_encoded_lon, 12, false,
				timestamp == null ? System.currentTimeMillis() : timestamp);

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

	/**
	 * @return ID to identify TIS-B site
	 */
	public byte getServiceVolumeID() {
		return svid;
	}

	/**
	 * @return ground track angle in degrees clockwise from true north
	 */
	public Float getGroundTrackAngle () {
		if (!ground_track_status) return null;
		return ground_track_angle*11.25f;
	}

	/**
	 * See also {@link #getMaxGroundSpeed()}.
	 * @return ground speed in knots (lower end of possible 32 knots window)
	 */
	public Integer getMinGroundSpeed () {
		if (ground_speed == 0) return null;
		else if (ground_speed == 1) return 0;
		else return 16 + (ground_speed-2)*32;
	}

	/**
	 * See also {@link #getMinGroundSpeed()}.
	 * @return ground speed in knots (upper end of possible 32 knots window)
	 */
	public Integer getMaxGroundSpeed () {
		if (ground_speed == 0) return null;
		else if (ground_speed == 1) return 16;
		else return 16 + (ground_speed-1)*32;
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
		return decodeAltitude(encoded_altitude);
	}

	@Override
	public Position.AltitudeType getAltitudeType () {
		return Position.AltitudeType.BAROMETRIC_ALTITUDE;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tCoarsePositionMsg{" +
				"imf=" + imf +
				", surveillance_status=" + surveillance_status +
				", svid=" + svid +
				", encoded_altitude=" + encoded_altitude +
				", ground_track_status=" + ground_track_status +
				", ground_track_angle=" + getGroundTrackAngle() +
				", ground_speed=" + getMinGroundSpeed()+"-"+getMaxGroundSpeed() +
				", position=" + position +
				'}';
	}
}
