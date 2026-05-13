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

package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.Tools;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public class QualifiedAddress {
	/**
	 * Different types of addresses in the AA field (see Table 2-11 in DO-260B)
	 */
	public enum Type {
		// ICAO 24-bit address
		ICAO24,
		// NON-ICAO 24-bit address
		NON_ICAO,
		// Anonymous address or ground vehicle address or fixed obstacle address of transmitting ADS-B Participant
		ANONYMOUS, // DF=18 with CF=1 or CF=6 and IMF=1
		// 12-bit Mode A code and track file number
		MODEA_TRACK, // DF=18 with CF=2/3 and IMF=1
		// TIS-B/ADS-R management information
		TISB_MANAGEMENT_INFO, // DF=18 with CF=4
		// Reserved (e.g. for military use)
		RESERVED, // DF=19 with AF>0 or DF=18 with CF=5 and IMF=1 or DF=18 and CF=7
		// Not (yet) determined
		UNKNOWN
	}

	private int address;
	private Type type;

	/**
	 * protected no-arg constructor e.g. internal usage or for serialization with Kryo
	 **/
	protected QualifiedAddress() {
	}

	public QualifiedAddress(int address, Type type) {
		this.address = address;
		this.type = type;
	}

	public QualifiedAddress(QualifiedAddress other) {
		this(other.address, other.type);
	}

	public QualifiedAddress(String address, Type type) {
		this(Integer.parseInt(address, 16), type);
	}

	/**
	 * @return type of address (e.g. ICAO 24-bit)
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the address in integer representation
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * @return address as 6 digit hex string
	 */
	public String getHexAddress() {
		return Tools.toHexString(address, 6);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		QualifiedAddress that = (QualifiedAddress) o;

		if (address != that.address) return false;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = address;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "QualifiedAddress{" +
				"address=" + address +
				", type=" + type +
				'}';
	}
}
