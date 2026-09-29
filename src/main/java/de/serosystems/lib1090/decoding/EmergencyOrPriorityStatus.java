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
     * The eddy dissipation rate (EDR) in m^(2/3)/s as the interval its code stands for, shared by the "Mean EDR"
     * subfield, ED-102B §2.2.3.2.7.8.1.4 TABLE 2-100, and the "Peak EDR" subfield, §2.2.3.2.7.8.1.5 TABLE 2-101,
     * which use the same coding. The encoding rounds up, so each code covers the step up to and including its
     * value: code 1 is [0, 0.002], codes 2 to 126 run in steps of 0.002, 0.005 and 0.010 up to 0.850, and code 127
     * is any EDR above 0.850.
     *
     * @param n the encoded EDR value, 0 meaning "no data"
     * @return the interval of the EDR, or {@code null} for code 0
     */
    public static Interval edr(int n) {
        if (n == 0) return null;
        if (n == 1) return Interval.of(Bound.AT_LEAST, 0, Bound.AT_MOST, 0.002);
        if (n == 127) return Interval.of(Bound.MORE_THAN, 0.850, Bound.NONE, Double.NaN);
        return Interval.of(Bound.MORE_THAN, edrUpperEnd(n - 1), Bound.AT_MOST, edrUpperEnd(n));
    }

    /**
     * @param n an EDR code from 1 to 126
     * @return the largest EDR the code stands for
     */
    private static double edrUpperEnd(int n) {
        if (n <= 10) return n * 0.002;
        if (n <= 76) return 0.020 + (n - 10) * 0.005;
        return 0.350 + (n - 76) * 0.010;
    }

    /**
     * The Relative Time of Peak Window Closure of the peak EDR in seconds, ED-102B §2.2.3.2.7.8.1.6 TABLE 2-102:
     * code n is (-7.5 (n + 1), -7.5 n]. Code 0, (-7.5, 0], is also what is transmitted when the offset is not
     * available, which the code alone cannot tell apart.
     *
     * @param n the encoded peak EDR offset, 0 to 7
     * @return the interval of the offset in seconds before the message
     */
    public static Interval peakEdrOffset(int n) {
        return Interval.of(Bound.MORE_THAN, -7.5 * (n + 1), Bound.AT_MOST, 0 - 7.5 * n);
    }

    /**
     * The water vapor in kg/kg as the interval its code stands for, ED-102B §2.2.3.2.7.8.1.7 TABLE 2-103: code 1
     * is less than 0.00001, code n up to 4094 is [0.00001 (n - 1), 0.00001 n), and code 4095 is 0.04094 or more.
     *
     * @param n the encoded water vapor, 0 meaning "no data"
     * @return the interval of the water vapor, or {@code null} for code 0
     */
    public static Interval waterVapor(int n) {
        if (n == 0) return null;
        if (n == 4095) return Interval.of(Bound.AT_LEAST, 4094 / 1e5, Bound.NONE, Double.NaN);
        return Interval.of(Bound.AT_LEAST, (n - 1) / 1e5, Bound.BELOW, n / 1e5);
    }
}
