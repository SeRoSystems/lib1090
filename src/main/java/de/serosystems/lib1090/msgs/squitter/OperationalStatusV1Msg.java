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

package de.serosystems.lib1090.msgs.squitter;

/**
 * Common API for ADS-B operational status version 1 messages. The Version One (1) Aircraft
 * Operational Status Message format is documented in ED-102B §N.5.2 Figure N-15.
 */
public interface OperationalStatusV1Msg extends OperationalStatusMsg {

    /**
     * @return the subtype code, 0 for airborne operational status messages and 1 for surface operational status messages
     */
    byte getSubtypeCode();

    /**
     * @return the NIC supplement A to the format type code of position messages, ED-102B
     * §2.2.3.2.7.2.6
     */
    boolean getNICSupplementA();

    /**
     * @return the navigation accuracy for position messages, ED-102B §2.2.3.2.7.2.7 TABLE 2-68
     */
    byte getNACpEncoded();

    /**
     * Source integrity level encoding.
     * <ul>
     *     <li>0: unknown or &gt; 1e-3</li>
     *     <li>1: &lt;= 1e-3</li>
     *     <li>2: &lt;= 1e-5</li>
     *     <li>3: &lt;= 1e-7</li>
     * </ul>
     *
     * @return the source integrity level (SIL) which indicates the probability of exceeding
     * the NIC containment radius, ED-102B §2.2.3.2.7.2.9 TABLE 2-70; not to be confused with
     * the "SIL Supplement" TABLE A-15
     */
    byte getSILEncoded();

    /**
     * @return true if the horizontal reference direction is magnetic north, false if true north,
     * ED-102B §2.2.3.2.7.2.13 TABLE 2-73
     */
    boolean isHeadingReferencedToMagneticNorth();
}
