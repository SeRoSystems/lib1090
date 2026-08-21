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

package de.serosystems.lib1090.decoding;

public final class EmergencyOrPriorityStatus {

    private EmergencyOrPriorityStatus() {
    }

    /**
     * Decodes the upper bound of an eddy dissipation rate (EDR) value in m^(2/3)/s;
     * the top range is unbounded, so 0.850 denotes an EDR of 0.850 or larger.
     * Shared by both the "Mean EDR" subfield, coded per ED-102B §2.2.3.2.7.8.1.4 TABLE 2-100,
     * and the "Peak EDR" subfield, coded per ED-102B §2.2.3.2.7.8.1.5 TABLE 2-101 — the two use
     * the same coding and resolution.
     *
     * @param n the encoded EDR value; must not be 0 (i.e. only call when EDR is available)
     * @return the upper bound of the eddy dissipation rate (EDR) in m^(2/3)/s
     */
    public static double decodeEdr(int n) {
        if (n <= 10) return n * 0.002;
        else if (n <= 76) return (n - 10) * 0.005 + 0.02;
        else if (n <= 126) return (n - 76) * 0.01 + 0.35;
        else return 0.850;
    }
}
