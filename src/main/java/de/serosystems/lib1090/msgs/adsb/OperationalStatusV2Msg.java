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

/**
 * Common API for ADS-B operational status version 2 messages.
 */
public interface OperationalStatusV2Msg extends OperationalStatusV1Msg {

	/**
	 * @return whether aircraft has an UAT receiver
	 */
	boolean hasUATIn();

	/**
	 * @return whether aircraft uses a single antenna or two
	 */
	boolean hasSingleAntenna();

	/**
	 * For interpretation see Table 2-65 in DO-260B
	 *
	 * @return system design assurance (see A.1.4.10.14 in RTCA DO-260B)
	 */
	byte getSystemDesignAssurance();

	/**
	 * DO-260B 2.2.3.2.7.2.14
	 *
	 * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
	 * it's based on "per hour".
	 */
	boolean hasSILSupplement();
}
