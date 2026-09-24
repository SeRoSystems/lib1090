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

package de.serosystems.lib1090.decoding.emergency;

/**
 * The value of an "Emergency State" subfield as the table of the transmitting ADS-B version defines it.
 * <p>
 * The same encoded value means different things in different versions — code 6 is "Reserved" in
 * version 0, "Downed Aircraft" in versions 1 and 2 and "Aircraft in Distress - Automatic Activation" in
 * version 3. {@link #getReported()} maps each to the version 3 meaning, which is what to compare across
 * versions.
 */
public interface EmergencyState {

    /**
     * @return the encoded value, as the transmitting version's table numbers it
     */
    byte getEncoded();

    /**
     * @return the meaning of the encoded value in the transmitting version
     */
    String getText();

    /**
     * @return the version 3 value ED-102B has the receiving subsystem report for this one,
     * ED-102B §2.2.3.2.7.8.1.1 TABLE 2-97
     */
    EmergencyStateV3 getReported();
}
