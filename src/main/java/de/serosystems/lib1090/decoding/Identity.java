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
public final class Identity {


	private Identity() {}

	/**
	 * @return The identity/Mode A code (see ICAO Annex 10 V4).
	 * Special codes are<br>
	 * <ul>
	 * <li> 7700 indicates emergency<br>
	 * <li> 7600 indicates radiocommunication failure</li>
	 * <li> 7500 indicates unlawful interference</li>
	 * <li> 2000 indicates that transponder is not yet operated</li>
	 * </ul>
	 */
	public static String decodeIdentity(short identity) {
		int C1 = (0x1000&identity)>>>12;
		int A1 = (0x800&identity)>>>11;
		int C2 = (0x400&identity)>>>10;
		int A2 = (0x200&identity)>>>9;
		int C4 = (0x100&identity)>>>8;
		int A4 = (0x080&identity)>>>7;
		// ZERO
		int B1 = (0x020&identity)>>>5;
		int D1 = (0x010&identity)>>>4;
		int B2 = (0x008&identity)>>>3;
		int D2 = (0x004&identity)>>>2;
		int B4 = (0x002&identity)>>>1;
		int D4 = (0x001&identity);

		String A = Integer.toString((A4<<2)+(A2<<1)+A1);
		String B = Integer.toString((B4<<2)+(B2<<1)+B1);
		String C = Integer.toString((C4<<2)+(C2<<1)+C1);
		String D = Integer.toString((D4<<2)+(D2<<1)+D1);

		return A+B+C+D;
	}
}
