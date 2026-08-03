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
 * Common API for ADS-B emergency and priority status messages across supported versions.
 */
public interface EmergencyOrPriorityStatusMsg {

    /**
     * @return the subtype code of the aircraft status report (should always be 1)
     */
    byte getSubtype();

    /**
     * @return the emergency state code (see DO-260B, Appendix A, Page A-83)
     */
    byte getEmergencyStateCode();

    /**
     * @return the human readable emergency state (see DO-260B, Appendix A, Page A-83)
     */
    default String getEmergencyStateText() {
        switch (getEmergencyStateCode()) {
            case 0:
                return "no emergency";
            case 1:
                return "general emergency";
            case 2:
                return "lifeguard/medical";
            case 3:
                return "minimum fuel";
            case 4:
                return "no communications";
            case 5:
                return "unlawful interference";
            case 6:
                return "downed aircraft";
            default:
                return "unknown";
        }
    }
}
