package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.AirbornePosition;
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
 * Decoder for ADS-B airborne position messages version 1 (DO-260A)
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class AirbornePositionV1Msg extends AirbornePositionV0Msg implements Serializable {

	private static final long serialVersionUID = 1143167908544789169L;

	private boolean nic_suppl_a;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected AirbornePositionV1Msg() { }

	/**
	 * @param raw_message raw ADS-B airborne position message as hex string
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirbornePositionV1Msg(String raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param raw_message raw ADS-B airborne position message as byte array
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirbornePositionV1Msg(byte[] raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param squitter extended squitter containing the airborne position msg
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 */
	public AirbornePositionV1Msg(ExtendedSquitter squitter, Long timestamp) throws BadFormatException {
		super(squitter, timestamp);
		setType(subtype.ADSB_AIRBORN_POSITION_V1);
	}

	/**
	 * @param nic_suppl Navigation Integrity Category (NIC) supplement from operational status message.
	 *        Otherwise worst case is assumed for containment radius limit and NIC. ADS-B version 1+ only!
	 */
	public void setNICSupplementA(boolean nic_suppl) {
		this.nic_suppl_a = nic_suppl;
	}

	/**
	 * @return NIC supplement that was set before
	 */
	public boolean hasNICSupplementA() {
		return nic_suppl_a;
	}

	/**
	 * The position error, i.e., 95% accuracy for the horizontal position. For the navigation accuracy category
	 * (NACp) see {@link AirborneOperationalStatusV1Msg}. Values according to DO-260B Table N-11.
	 *
	 * The horizontal containment radius is also known as "horizontal protection level".
	 *
	 * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
	 *         If aircraft uses ADS-B version 1+, set NIC supplement A from Operational Status Message
	 *         for better precision.
	 */
	public double getHorizontalContainmentRadiusLimit() {
		return AirbornePosition.decodeHCR(getFormatTypeCode(), nic_suppl_a);
	}

	/**
	 * Values according to DO-260B Table N-11
	 * @return Navigation integrity category. A NIC of 0 means "unkown".
	 */
	public byte getNIC() {
		return AirbornePosition.decodeNIC(getFormatTypeCode(), nic_suppl_a);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tAirbornePositionV1Msg{" +
				"nic_suppl_a=" + nic_suppl_a +
				'}';
	}
}
