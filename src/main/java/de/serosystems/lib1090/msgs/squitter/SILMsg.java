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

import de.serosystems.lib1090.decoding.quality.SourceIntegrityLevel;

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
     * confused with the SIL supplement of {@link #getSILSupplement()}, ED-102B TABLE 2-32
     */
    byte getSILEncoded();

    /**
     * The probability of exceeding the NIC containment radius that the reported level guarantees,
     * ED-102B §2.2.3.2.7.2.9 TABLE 2-70.
     * <p>
     * The figure is per flight hour or per sample, as {@link #getSILSupplement()} says. It is not
     * comparable across the two, so a caller weighing one target's integrity against another's reads
     * both.
     *
     * @return what the level guarantees; level 0 guarantees nothing, reading "unknown or more than
     * 10&#94;-3"
     */
    default SourceIntegrityLevel getSourceIntegrityLevel() {
        return SourceIntegrityLevel.forSIL(getSILEncoded());
    }

    /**
     * What the probability of {@link #getSourceIntegrityLevel()} is measured per, ED-102B
     * TABLE 2-32.
     * <p>
     * The standard describes the field twice, once per message carrying it: §2.2.3.2.7.1.3.1 for the
     * target state and status message, which is where TABLE 2-32 defines it, and §2.2.3.2.7.2.14 for
     * the aircraft operational status message, which refers to that definition. Both are the same
     * field, which is why one accessor serves both here.
     * <p>
     * Every message carrying a SIL can answer this, but only version 2 and later transmit a bit for
     * it; version 1 fixes the basis instead, and says so where it does.
     *
     * @return true if the SIL is based on a "per sample" probability, false if on a "per flight hour"
     * one
     */
    boolean getSILSupplement();
}
