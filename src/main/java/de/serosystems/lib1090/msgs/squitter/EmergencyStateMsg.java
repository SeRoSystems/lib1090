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

import de.serosystems.lib1090.decoding.emergency.EmergencyState;
import de.serosystems.lib1090.decoding.emergency.EmergencyStateV3;

/**
 * Common API for messages that transmit the "Emergency/Priority Status" subfield, which the message
 * figures call "Emergency State".
 * <p>
 * Every version's Aircraft Status Message, TYPE=28 Subtype=1, transmits it, and so does the version 1
 * Target State and Status Message. The meaning of the encoded value depends on the version, so
 * {@link #getEmergencyState()} returns the transmitting version's constant and
 * {@link #getReportedEmergencyState()} the version 3 value ED-102B maps it to.
 */
public interface EmergencyStateMsg {

    /**
     * @return the encoded emergency state as transmitted; its meaning depends on the version
     */
    byte getEmergencyStateEncoded();

    /**
     * @return the emergency state as the transmitting version defines it
     */
    EmergencyState getEmergencyState();

    /**
     * The emergency state as version 3 defines it, which is what to compare across versions:
     * ED-102B §2.2.3.2.7.8.1.1 TABLE 2-97, older versions mapped per Appendix N.
     *
     * @return the version 3 emergency state reported for this message
     */
    default EmergencyStateV3 getReportedEmergencyState() {
        return getEmergencyState().getReported();
    }
}
