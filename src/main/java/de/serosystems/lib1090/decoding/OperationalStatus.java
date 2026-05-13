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

package de.serosystems.lib1090.decoding;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class OperationalStatus {

	private OperationalStatus() {}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value, see table A-13 in RCTA DO-260B
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public static double nacPtoEPU(byte nacPos) {
		switch (nacPos) {
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
	 * According to DO-260B Table 2-74. Compatible with ADS-B version 1 and 2
	 * @return the airplane's length in meters; -1 for unknown
	 */
	public static int decodeAirplaneLength(byte airplaneLenWidth) {
		switch (airplaneLenWidth) {
			case 1: return 15;
			case 2: case 3: return 25;
			case 4: case 5: return 35;
			case 6: case 7: return 45;
			case 8: case 9: return 55;
			case 10: case 11: return 65;
			case 12: case 13: return 75;
			case 14: case 15: return 85;
			default: return -1;
		}
	}

	/**
	 * According to DO-260B Table 2-74. Compatible with ADS-B version 1 and 2.
	 * @return the airplane's width in meters
	 */
	public static double decodeAirplaneWidth(byte airplaneLenWidth) {
		switch (airplaneLenWidth) {
			case 1: return 23;
			case 2: return 28.5;
			case 3: return 34;
			case 4: return 33;
			case 5: return 38;
			case 6: return 39.5;
			case 7: case 8: return 45;
			case 9: return 52;
			case 10: return 59.5;
			case 11: return 67;
			case 12: return 72.5;
			case 13: case 14: return 80;
			case 15: return 90;
			default: return -1;
		}
	}
}
