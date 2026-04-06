package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.BitReader;
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
 * Decoder for ADS-B target state and status message as specified in DO-260A (ADS-B version 1).
 */
public class TargetStateAndStatusMsgV1 extends ExtendedSquitter implements Serializable, TargetStateAndStatusMsg {

	private static final long serialVersionUID = -3226687215928593692L;

	private byte vertical_data_available_and_source_indicator;
	private boolean target_altitude_type;
	private byte target_altitude_capability;
	private byte vertical_mode_indicator;
	private int target_altitude;
	private byte horizontal_data_available_and_source_indicator;
	private short target_heading_track_angle;
	private boolean target_heading_track_indicator;
	private byte horizontal_mode_indicator;
	private byte nac_p;
	private boolean nic_baro;
	private byte sil;
	private boolean capability_not_tcas;
	private boolean capability_tcas_ra_active;
	private byte emergency_priority_status;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected TargetStateAndStatusMsgV1() { }

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV1(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV1(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException if message has the wrong typecode
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV1(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSB_TARGET_STATE_AND_STATUS);

		if (getFormatTypeCode() != 29) {
			throw new BadFormatException("Target state and status messages must have typecode 29.");
		}

		byte[] msg = this.getMessage();
		BitReader b = BitReader.forBigEndian(msg);

		byte subtype_code = b.readByte(6, 7);
		if (subtype_code != 0) {
			throw new UnspecifiedFormatError("Target state and status message subtype " + subtype_code + " reserved.");
		}

		vertical_data_available_and_source_indicator = b.readByte(8, 9);
		target_altitude_type = b.readByte(10, 10) == 1;
		target_altitude_capability = b.readByte(12, 13);
		vertical_mode_indicator = b.readByte(14, 15);
		target_altitude = b.readInt(16, 25);
		horizontal_data_available_and_source_indicator = b.readByte(26, 27);
		target_heading_track_angle = b.readShort(28, 36);
		target_heading_track_indicator = b.readByte(37, 37) == 1;
		horizontal_mode_indicator = b.readByte(38, 39);
		nac_p = b.readByte(40, 43);
		nic_baro = b.readByte(44, 44) == 1;
		sil = b.readByte(45, 46);
		capability_not_tcas = b.readByte(52, 52) == 1;
		capability_tcas_ra_active = b.readByte(53, 53) == 1;
		emergency_priority_status = b.readByte(54, 56);
	}

	/**
	 * @return the raw vertical data available and source indicator value
	 */
	public byte getVerticalDataAvailableAndSourceIndicator() {
		return vertical_data_available_and_source_indicator;
	}

	@Override
	public boolean hasSelectedAltitudeInfo() {
		return target_altitude_capability == 1 || target_altitude_capability == 2;
	}

	@Override
	public int getSelectedAltitudeRaw() {
		return target_altitude;
	}

	@Override
	public Integer getSelectedAltitude() {
		return hasSelectedAltitudeInfo() ? 100 * target_altitude - 1000 : null;
	}

	@Override
	public boolean hasSelectedHeadingInfo() {
		return horizontal_data_available_and_source_indicator != 0;
	}

	@Override
	public Float getSelectedHeading() {
		if (!hasSelectedHeadingInfo()) {
			return null;
		}

		return target_heading_track_angle * (360.f / 512);
	}

	@Override
	public byte getNACp() {
		return nac_p;
	}

	@Override
	public boolean getBarometricAltitudeIntegrityCode() {
		return nic_baro;
	}

	@Override
	public byte getSIL() {
		return sil;
	}

	@Override
	public boolean hasOperationalTCAS() {
		return !capability_not_tcas;
	}

	/**
	 * @return true if a TCAS resolution advisory is active
	 */
	public boolean hasActiveTCASResolutionAdvisory() {
		return capability_tcas_ra_active;
	}

	/**
	 * @return the raw emergency / priority status field value
	 */
	public byte getEmergencyPriorityStatus() {
		return emergency_priority_status;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tTargetStateAndStatusMsgV1{" +
				"vertical_data_available_and_source_indicator=" + vertical_data_available_and_source_indicator +
				", target_altitude_type=" + target_altitude_type +
				", target_altitude_capability=" + target_altitude_capability +
				", vertical_mode_indicator=" + vertical_mode_indicator +
				", target_altitude=" + target_altitude +
				", horizontal_data_available_and_source_indicator=" + horizontal_data_available_and_source_indicator +
				", target_heading_track_angle=" + target_heading_track_angle +
				", target_heading_track_indicator=" + target_heading_track_indicator +
				", horizontal_mode_indicator=" + horizontal_mode_indicator +
				", nac_p=" + nac_p +
				", nic_baro=" + nic_baro +
				", sil=" + sil +
				", capability_not_tcas=" + capability_not_tcas +
				", capability_tcas_ra_active=" + capability_tcas_ra_active +
				", emergency_priority_status=" + emergency_priority_status +
				'}';
	}
}
