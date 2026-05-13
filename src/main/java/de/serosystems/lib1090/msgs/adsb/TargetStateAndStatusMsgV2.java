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
 * Decoder for ADS-B target state and status message as specified in DO-260B §2.2.3.2.7.1
 *
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class TargetStateAndStatusMsgV2 extends ExtendedSquitter implements Serializable, TargetStateAndStatusMsg {

	private static final long serialVersionUID = 7485953168219273935L;

	private boolean sil_suppl;
	private boolean selected_altitude_type;
	private int selected_altitude;
	private int barometric_pressure_setting;
	private boolean selected_heading_status;
	private boolean selected_heading_sign;
	private int selected_heading;
	private byte nac_p;
	private boolean nic_baro;
	private byte sil;
	private boolean mcp_fcu_status;
	private boolean autopilot_engaged;
	private boolean vnav_mode_engaged;
	private boolean altitude_hold_mode;
	private boolean approach_mode;
	private boolean has_operational_tcas;
	private boolean lnav_mode_engaged;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected TargetStateAndStatusMsgV2() { }

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV2(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException if message has the wrong typecode or ADS-B version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV2(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException  if message has the wrong typecode or if reserved bits are set
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public TargetStateAndStatusMsgV2(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSB_TARGET_STATE_AND_STATUS);

		if (getFormatTypeCode() != 29) {
			throw new BadFormatException("Target state and status messages must have typecode 29.");
		}

		byte[] msg = this.getMessage();

		byte subtype_code = (byte) ((msg[0]>>>1) & 0x3);
		if (subtype_code != 1) // all others are reserved
			throw new UnspecifiedFormatError("Target state and status message subtype "+subtype_code+" reserved.");

		// message with ME bit 11 set to 1 should be discarded, but only for ADS-B v0 transmitters
		// ModeSDecoder class takes care of that as ADS-B version is unknown at this place

		sil_suppl = ((msg[0] & 0x01) != 0);
		selected_altitude_type = (((msg[1]>>>7) & 0x01) != 0);

		selected_altitude = (((msg[1]&0x7F)<<4) | ((msg[2]>>>4)&0x0F)) & 0x7FF;

		barometric_pressure_setting = (((msg[2]&0x0F)<<5) | ((msg[3]>>>3)&0x1F)) & 0x1FF;

		selected_heading_status = ((msg[3] & 0x04) != 0);
		selected_heading_sign = ((msg[3] & 0x02) != 0);

		selected_heading = (((msg[3]&0x01)<<7) | ((msg[4]>>>1)&0x7F)) & 0xFF;
		nac_p = (byte) ((((msg[4]&0x01)<<3) | ((msg[5]>>>5)&0x07)) & 0x0F);

		nic_baro = ((msg[5] & 0x10) != 0);
		sil = (byte) ((msg[5]>>>2)&0x3);

		mcp_fcu_status = ((msg[5] & 0x02) != 0);

		// the following are only valid if mcp_fcu_status is true
		autopilot_engaged = ((msg[5] & 0x01) != 0);
		vnav_mode_engaged = ((msg[6] & 0x80) != 0);
		altitude_hold_mode = ((msg[6] & 0x40) != 0);
		approach_mode = ((msg[6] & 0x10) != 0);
		lnav_mode_engaged = ((msg[6] & 0x04) != 0);

		// this is always set and valid
		has_operational_tcas = ((msg[6] & 0x08) != 0);

		// DO-260B 2.2.3.2.7.1.3.19
		if ((msg[6]&0x03) != 0)
			throw new BadFormatException("Target state and status message reserved bits must be 0.");
	}

	/**
	 * DO-260B 2.2.3.2.7.1.3.1
	 *
	 * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
	 * 			it's based on "per hour".
	 */
	public boolean hasSILSupplement() {
		return sil_suppl;
	}

	@Override
	public boolean hasSelectedAltitudeInfo() {
		return selected_altitude > 0;
	}

	@Override
	public Integer getSelectedAltitude() {
		return selected_altitude != 0 ? (selected_altitude  - 1) * 32 : null;
	}

	@Override
	public int getSelectedAltitudeRaw() {
		return selected_altitude;
	}

	/**
	 * Source for selected altitude according to DO-260B 2.2.3.2.7.1.3.2
	 *
	 * @return true is the value of {@link #getSelectedAltitude()} is derived from the Flight Management System (FMS),
	 *         false if it is derived from the Control Panel/Flight Control Unit (MCP/FCU)
	 */
	public boolean isFMSSelectedAltitude() {
		return selected_altitude_type;
	}

	/**
	 * DO-260B 2.2.3.2.7.1.3.4
	 *
	 * @return whether the Barometric Pressure Setting info is available, i.e. {@link #getBarometricPressureSetting()}
	 *         returns a non-null value
	 */
	public boolean hasBarometricPressureSettingInfo() {
		return barometric_pressure_setting != 0;
	}

	/**
	 * The barometric pressure setting (minus 800 millibars) according to DO-260B 2.2.3.2.7.1.3.4
	 *
	 * Availability of this information can also be checked with {@link #hasBarometricPressureSettingInfo()}.
	 *
	 * @return the barometric pressure settings that has been adjusted by subtracting 800 millibars from the pressure
	 *         source (in millibars), or null if the information is not available.
	 */
	public Float getBarometricPressureSetting() {
		return barometric_pressure_setting != 0 ? (barometric_pressure_setting - 1) * 0.8F : null;
	}

	@Override
	public boolean hasSelectedHeadingInfo() {
		return selected_heading_status;
	}

	@Override
	public Float getSelectedHeading() {
		if (!selected_heading_status) {
			return null;
		}

		return selected_heading * (180.f / 256) + (selected_heading_sign ? 180F : 0F);
	}

	@Override
	public byte getNACp() {
		return nac_p;
	}

	// TODO generify getPositionUncertainty() method from AirborneOperationalStatusV0 message and provide here as well

	@Override
	public boolean getBarometricAltitudeIntegrityCode() {
		return nic_baro;
	}

	@Override
	public byte getSIL() {
		return sil;
	}

	/**
	 * MCP/FCU mode status bit information according to DO-260B 2.2.3.2.7.1.3.11
	 *
	 * A value of false indidates that information of {@link #hasAutopilotEngaged()}, {@link #hasVNAVModeEngaged()},
	 * {@link #hasActiveAltitudeHoldMode()}, and {@link #hasActiveApproachMode()} is not provided by the aircraft.
	 *
	 * @return true if Mode information is deliberately being provided, false otherwise
	 */
	public boolean hasModeInfo() {
		return mcp_fcu_status;
	}

	/**
	 * Auto pilot engaged flag according to DO-260B 2.2.3.2.7.1.3.12
	 *
	 * Information is only available if {@link #hasModeInfo()} is true.
	 *
	 * @return true if the autopilot system is engaged, false if not engaged or status unknown, null if information
	 *         is not available.
	 */
	public Boolean hasAutopilotEngaged() {
		if (!mcp_fcu_status) return null;
		return autopilot_engaged;
	}

	/**
	 * VNAV Mode Engaged flag according to DO-260B 2.2.3.2.7.1.3.13
	 *
	 * Information is only available if {@link #hasModeInfo()} is true.
	 *
	 * @return true if vertical navigation mode is active, false otherwise or if status unknown, null if information is
	 *         not available
	 */
	public Boolean hasVNAVModeEngaged() {
		if (!mcp_fcu_status) return null;
		return vnav_mode_engaged;
	}

	/**
	 * Altitude Hold Mode Engaged flag according to DO-260B 2.2.3.2.7.1.3.14
	 *
	 * Information is only available if {@link #hasModeInfo()} is true.
	 *
	 * @return true if altitude hold mode is active, false if inactive or status unknown, null if information is not
	 *         available
	 */
	public Boolean hasActiveAltitudeHoldMode() {
		if (!mcp_fcu_status) return null;
		return altitude_hold_mode;
	}

	/**
	 * Approach Mode Engaged flag according to DO-260B 2.2.3.2.7.1.3.16
	 *
	 * Information is only available if {@link #hasModeInfo()} is true.
	 *
	 * @return true if approach mode is active, false if inactive or status unknown, null if information is not
	 *         available
	 */
	public Boolean hasActiveApproachMode() {
		if (!mcp_fcu_status) return null;
		return approach_mode;
	}

	@Override
	public boolean hasOperationalTCAS() {
		return has_operational_tcas;
	}

	/**
	 * LNAV Mode Engaged flag according to DO-260B 2.2.3.2.7.1.3.18
	 *
	 * Information is only available if {@link #hasModeInfo()} is true.
	 *
	 * @return true if the lateral navigation mode is active, false otherwise or if status unknown, null if information
	 *         is not available
	 */
	public Boolean hasLNAVModeEngaged() {
		if (!mcp_fcu_status) return null;
		return lnav_mode_engaged;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tTargetStateAndStatusMsg{" +
				"sil_suppl=" + sil_suppl +
				", selected_altitude_type=" + selected_altitude_type +
				", selected_altitude=" + selected_altitude +
				", barometric_pressure_setting=" + barometric_pressure_setting +
				", selectected_heading_status=" + selected_heading_status +
				", selectected_heading_sign=" + selected_heading_sign +
				", selected_heading=" + selected_heading +
				", nac_p=" + nac_p +
				", nic_baro=" + nic_baro +
				", sil=" + sil +
				", mcp_fcu_status=" + mcp_fcu_status +
				", autopilot_engaged=" + autopilot_engaged +
				", vnav_mode_engaged=" + vnav_mode_engaged +
				", altitude_hold_mode=" + altitude_hold_mode +
				", approach_mode=" + approach_mode +
				", has_operational_tcas=" + has_operational_tcas +
				", lnav_mode_engaged=" + lnav_mode_engaged +
				'}';
	}
}
