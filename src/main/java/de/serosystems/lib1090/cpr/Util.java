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

package de.serosystems.lib1090.cpr;

final class Util {
    private Util() {}

    /**
     * Euclidean modulo operator.
     * <br>
     * Let {@code a, b} be given integers, then {@code a = b * k + r} for some integers {@code k, r} such that {@code 0 <= r < |b|}. Then this function returns {@code r}.
     * An alternative definition is {@code r = a - floor(a/b)*b}.
     *
     * @param a some a
     * @param b some b > 0
     * @return a mod b following Euclidean definition.
     */
    public static int mod(int a, int b) {
        int m = a % b;
        if (m < 0)
            m += b;
        return m;
    }
}
