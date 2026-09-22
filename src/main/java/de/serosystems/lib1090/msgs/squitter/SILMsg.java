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

import de.serosystems.lib1090.decoding.SourceIntegrityLevel;

/**
 * Common API for messages that transmit a Source Integrity Level (SIL).
 * <p>
 * ADS-B version 0 defines no such field, so this is not part of {@link OperationalStatusMsg} itself;
 * the version 1 and later operational status messages and the target state and status messages carry
 * it.
 */
public interface SILMsg {

    /**
     * @return the raw encoded source integrity level, which indicates the probability of the true
     * position exceeding the NIC containment radius, ED-102B §2.2.3.2.7.2.9 TABLE 2-70; not to be
     * confused with the "SIL Supplement" TABLE A-15
     */
    byte getSILEncoded();

    /**
     * The probability of exceeding the NIC containment radius that the reported level guarantees,
     * ED-102B §2.2.3.2.7.2.9 TABLE 2-70.
     * <p>
     * The figure is per flight hour or per sample, as the SIL supplement says — a separate bit, which
     * version 1 does not transmit at all and the later versions expose as {@code getSILSupplement()}.
     *
     * @return what the level guarantees; level 0 guarantees nothing, reading "unknown or more than
     * 10&#94;-3"
     */
    default SourceIntegrityLevel getSourceIntegrityLevel() {
        return SourceIntegrityLevel.forSIL(getSILEncoded());
    }
}
