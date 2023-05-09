package de.serosystems.lib1090.msgs.adsr;

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
 * Decoder for ADS-R operational status message as specified in DO-260A (ADS-R version 1) with
 * subtype 0 (airborne)
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class AirborneOperationalStatusV1Msg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = 1284570176022959367L;

	private byte subtype_code;
	private int capability_class_code; // actually 16 bit unsigned
	private int operational_mode_code; // actually 16 bit unsigned
	private byte version;
	private boolean nic_suppl; // may be passed to position messages
	private byte nac_pos; // navigational accuracy category - position
	private byte geometric_vertical_accuracy; // bit 49 and 50
	private byte sil; // surveillance integrity level
	private boolean nic_trk_hdg; // NIC baro for airborne status, heading/ground track info else
	private boolean hrd; // heading info is based on true north (0) or magnetic north (1)
	private boolean imf;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected AirborneOperationalStatusV1Msg() { }

	/**
	 * @param raw_message The full Mode S message in hex representation
	 * @throws BadFormatException if message has the wrong typecode or ADS-R version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV1Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message The full Mode S message as byte array
	 * @throws BadFormatException if message has the wrong typecode or ADS-R version
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV1Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this message
	 * @throws BadFormatException  if message has the wrong typecode or ADS-R version or is not an airborne
	 * 								operational status message or the capability code is invalid.
	 * @throws UnspecifiedFormatError if message has the wrong subtype
	 */
	public AirborneOperationalStatusV1Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
		super(squitter);
		setType(subtype.ADSR_AIRBORN_STATUS_V1);

		if (getFormatTypeCode() != 31) {
			throw new BadFormatException("Operational status messages must have typecode 31.");
		}

		byte[] msg = this.getMessage();

		subtype_code = (byte)(msg[0] & 0x7);
		if (subtype_code > 1) { // currently only 0 and 1 specified, 2-7 are reserved
			throw new UnspecifiedFormatError("Operational status message subtype " + subtype_code + " reserved.");
		} else if (subtype_code != 0) {
			throw new BadFormatException("Not an airborne operational status message");
		}

		capability_class_code = ((msg[1] & 0xFF) <<8) | (msg[2] & 0xFF);
		operational_mode_code = ((msg[3] & 0xFF) <<8) | (msg[4] & 0xFF);
		version = (byte) ((msg[5]>>>5) & 0x07);

		if ((capability_class_code & 0xC000) != 0)
			throw new BadFormatException("Unknown capability class code!");

		nic_suppl = ((msg[5] & 0x10) != 0);
		nac_pos = (byte) (msg[5] & 0xF);
		geometric_vertical_accuracy = (byte) ((msg[6] >>> 6) & 0x3);
		sil = (byte) ((msg[6]>>>4)&0x3);
		nic_trk_hdg = ((msg[6] & 0x8) != 0);
		hrd = ((msg[6] & 0x4) != 0);
		imf = (msg[6] & 0x1) != 0;
	}

	/**
	 * @return whether operational TCAS is available
	 */
	public boolean hasOperationalTCAS() {
		return (capability_class_code & 0x2000) != 0;
	}

	/**
	 * @return whether 1090ES IN is available
	 */
	public boolean has1090ESIn() {
		return (capability_class_code & 0x1000) != 0;
	}

	/**
	 * @return whether aircraft has capability of sending messages to support Air-Referenced
	 *         Velocity Reports
	 */
	public boolean hasAirReferencedVelocity() {
		return (capability_class_code & 0x200) != 0;
	}

	/**
	 * @return whether aircraft has capability of sending messages to support Target
	 *         State Reports
	 */
	public boolean hasTargetStateReport() {
		return (capability_class_code & 0x100) != 0;
	}

	/**
	 * @return whether target change reports are supported
	 */
	public boolean supportsTargetChangeReport() {
		byte target_change_report_capability = (byte) ((capability_class_code & 0xC0)>>>6);
		return target_change_report_capability == 1 || target_change_report_capability == 2;
	}

	/**
	 * @return whether aircraft has an UAT receiver
	 */
	public boolean hasUATIn() {
		return (capability_class_code & 0x20) != 0;
	}

	/**
	 * @return NIC supplement B
	 */
	public boolean getNICSupplementB() {
		return (capability_class_code & 0x10) != 0;
	}

	/**
	 * @return whether TCAS Resolution Advisory (RA) is active
	 */
	public boolean hasTCASResolutionAdvisory() {
		return (operational_mode_code&0x2000) != 0;
	}

	/**
	 * @return whether the IDENT switch is active
	 */
	public boolean hasActiveIDENTSwitch() {
		return (operational_mode_code&0x1000) != 0;
	}

	/**
	 * @return whether aircraft uses a single antenna or two
	 */
	public boolean hasSingleAntenna() {
		return (operational_mode_code&0x400) != 0;
	}

	/**
	 * For interpretation see Table 2-65 in DO-260B
	 * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B)
	 */
	public byte getSystemDesignAssurance() {
		return (byte) ((operational_mode_code&0x300)>>>8);
	}

	/**
	 * @return the version number of the formats and protocols in use on the aircraft installation.<br>
	 *         0: Conformant to DO-260/ED-102 and DO-242<br>
	 *         1: Conformant to DO-260A and DO-242A<br>
	 *         2: Conformant to DO-260B/ED-102A and DO-242B<br>
	 *         3-7: reserved
	 */
	public byte getVersion() {
		return version;
	}

	/**
	 * @return the NIC supplement A to the format type code of position messages
	 */
	public boolean hasNICSupplementA() {
		return nic_suppl;
	}

	/**
	 * @return the navigation accuracy for position messages; rather use getPositionUncertainty
	 */
	public byte getNACp() {
		return nac_pos;
	}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value, see table A-13 in RCTA DO-260B
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public double getPositionUncertainty() {
		switch (nac_pos) {
			case 1: return 18520;
			case 2: return 7408;
			case 3: return 3704;
			case 4: return 1852.0;
			case 5: return 926.0;
			case 6: return 555.6;
			case 7: return 185.2;
			case 8: return 92.6;
			case 9: return 30.0;
			case 10: return 10.0;
			case 11: return 3.0;
			default: return -1;
		}
	}

	/**
	 * @return the geometric vertical accuracy in meters or -1 for unknown
	 */
	public int getGeometricVerticalAccuracy() {
		if (geometric_vertical_accuracy == 1)
			return 150;
		else if (geometric_vertical_accuracy == 2)
			return 45;
		else return -1;
	}

	/**
	 * @return the encoded geometric vertical accuracy (see DO-260B 2.2.3.2.7.2.8)
	 */
	public byte getGVA() {
		return geometric_vertical_accuracy;
	}

	/**
	 * @return the source integrity level (SIL) which indicates the propability of exceeding
	 *         the NIC containment radius (see table A-15 in RCTA DO-260B)
	 */
	public byte getSIL() {
		return sil;
	}

	/**
	 * @return the barometric altitude integrity code which indicates whether
	 *         barometric pressure altitude has been cross-checked against other
	 *         sources of pressure altitude. If false, altitude data has not been
	 *         cross-checked.
	 */
	public boolean getBarometricAltitudeIntegrityCode() {
		return nic_trk_hdg;
	}

	/**
	 * @return 0 if horizontal reference direction is the true north, 1 if magnetic north
	 */
	public boolean getHorizontalReferenceDirection() {
		return hrd;
	}

	/**
	 * @return the ICAO Mode A Flag (for address type determination)
	 */
	public boolean getIMF () {
		return imf;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tAirborneOperationalStatusV1Msg{" +
				"subtype_code=" + subtype_code +
				", capability_class_code=" + capability_class_code +
				", operational_mode_code=" + operational_mode_code +
				", version=" + version +
				", nic_suppl=" + nic_suppl +
				", nac_pos=" + nac_pos +
				", geometric_vertical_accuracy=" + geometric_vertical_accuracy +
				", sil=" + sil +
				", nic_trk_hdg=" + nic_trk_hdg +
				", hrd=" + hrd +
				", imf=" + imf +
				'}';
	}
}
