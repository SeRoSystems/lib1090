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

/**
 * The "Difference From Barometric Altitude" of the airborne velocity message: geometric minus barometric
 * altitude, as a magnitude code and a separate sign bit.
 * <p>
 * The field comes in three resolutions, which are one coding: the 11 bits of ADS-B version 3, ED-102B
 * §2.2.3.2.6.1.15 TABLE 2-27 as revised by Change 1; the 10 bits of ADS-R version 3, which lack the
 * least significant bit since ADS-R uses it for the IMF, TABLE 2-186; and the 7 bits every earlier
 * version transmits, which version 3 still carries in the same place so that older receivers read a
 * compatible value. Only TABLE 2-27 is implemented. A code of the coarser codings stands for exactly
 * the version 3 codes that share its bits, and its interval is theirs combined, which makes the
 * three agree by construction.
 * <p>
 * No code states a difference. Each states an interval of the magnitude, (lower, upper] in every
 * coding, which the sign mirrors to the negative side.
 */
package de.serosystems.lib1090.decoding.diffbaroalt;
