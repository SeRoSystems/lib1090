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
 * The "Emergency/Priority Status" subfield, called "Emergency State" in the message figures, and the
 * mapping ED-102B gives from each version's encoding to version 3's.
 * <p>
 * The subfield is three bits wide in every version, but the meaning of its values is not: version 3
 * redefined codes 2, 6 and 7. Each version's table is therefore its own enum, and all of them implement
 * {@code EmergencyState}. Every enum covers all eight values, so each value a message can carry has a
 * constant.
 * <p>
 * A constant answers two questions. {@code getText()} is what the transmitting version meant by the
 * value; {@code getReported()} is the version 3 value ED-102B Appendix N has the receiving subsystem
 * report in its place. Compare the latter when messages of different versions must be compared.
 */
package de.serosystems.lib1090.decoding.emergency;
