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

import de.serosystems.lib1090.decoding.EstimatedPositionUncertainty;

/**
 * Common API for messages that report a Navigation Accuracy Category for position (NACp).
 * <p>
 * ADS-B version 1 introduced the field, which the operational status and target state and status
 * messages transmit. Version 0 transmits no such field but its format type code maps to one, so the
 * version 0 position messages report it here too.
 */
public interface NACpMsg {

    /**
     * @return the raw encoded navigation accuracy category for position, ED-102B §2.2.3.2.7.2.7
     * TABLE 2-68
     */
    byte getNACpEncoded();

    /**
     * The 95% horizontal accuracy bound the reported category stands for, ED-102B §2.2.3.2.7.2.7
     * TABLE 2-68.
     * <p>
     * Category 0 guarantees no accuracy at all, the table annotating it "Unknown accuracy"; the
     * categories the standard reserves guarantee nothing either.
     *
     * @return what the category reports, never null
     */
    default EstimatedPositionUncertainty getEstimatedPositionUncertainty() {
        return EstimatedPositionUncertainty.forNACp(getNACpEncoded());
    }
}
