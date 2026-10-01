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

package de.serosystems.lib1090.msgs.acas;

/**
 * Picks the layout of ACAS resolution advisory content, message bits 41-88, by its RA message format (RMF, bits
 * 53-54), ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.2.3.
 * <p>
 * No method throws on an RA message format without a defined layout, 2 (reserved for ACAS III) and 3 (unallocated):
 * they return null, and the container still gives the raw bits and {@link #messageFormat(long)}.
 */
public final class ResolutionAdvisories {

    /**
     * Bits 59-88 of the content, which a TCAS II version 6.04 RA report leaves not assigned.
     */
    private static final long BITS_59_TO_88 = (1L << 30) - 1;

    private ResolutionAdvisories() {
    }

    /**
     * @param encoded message bits 41-88, right-aligned
     * @return the RA message format, bits 53-54
     */
    public static RAMessageFormat messageFormat(long encoded) {
        return RAMessageFormat.forEncoded((int) ((encoded >>> (88 - 54)) & 0x3));
    }

    /**
     * The content of an RA report, the register BDS 3,0 or the 1090ES RA broadcast.
     *
     * @param encoded message bits 41-88, right-aligned
     * @return a {@link TCASResolutionAdvisoryReport} or an {@link ACASXResolutionAdvisoryReport}; a
     * {@link TCAS6ResolutionAdvisory} for the TCAS layout with bits 59-88 all zero, which is how a TCAS II version
     * 6.04 system reports and also how a later TCAS reports no RA; or null for a format without a defined layout
     */
    public static ResolutionAdvisory report(long encoded) {
        switch (messageFormat(encoded)) {
            case TCAS_II:
                return (encoded & BITS_59_TO_88) == 0
                        ? new TCAS6ResolutionAdvisory(encoded)
                        : new TCASResolutionAdvisoryReport(encoded);
            case ACAS_X:
                return new ACASXResolutionAdvisoryReport(encoded);
            default:
                return null;
        }
    }

    /**
     * The content of a coordination reply, the MV field of DF=16, §4.3.8.4.2.4.2, whose bits 61-88 are not assigned.
     *
     * @param encoded message bits 41-88, right-aligned
     * @return a {@link TCASResolutionAdvisory} or an {@link ACASXResolutionAdvisory}, or null for a format without a
     * defined layout
     */
    public static ResolutionAdvisoryState coordinationReply(long encoded) {
        switch (messageFormat(encoded)) {
            case TCAS_II:
                return new TCASResolutionAdvisory(encoded);
            case ACAS_X:
                return new ACASXResolutionAdvisory(encoded);
            default:
                return null;
        }
    }
}
