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

package de.serosystems.lib1090.msgs.bds;

import java.io.Serializable;

/**
 * The address of a transponder register as a Comm-B Data Selector (BDS) code, the pair BDS1,BDS2 of
 * which ICAO Doc 9871 names every register, e.g. "BDS code 6,0" for the heading and speed report.
 * Both parts are four bits.
 */
public final class BDSCode implements Serializable {
    private static final long serialVersionUID = 4640587719234806611L;

    private final byte bds1;
    private final byte bds2;

    /**
     * @param bds1 the first part of the code, 0 to 15
     * @param bds2 the second part of the code, 0 to 15
     * @throws IllegalArgumentException if either part does not fit four bits
     */
    public BDSCode(int bds1, int bds2) {
        if (bds1 < 0 || bds1 > 15 || bds2 < 0 || bds2 > 15)
            throw new IllegalArgumentException("BDS code " + bds1 + "," + bds2 + " does not fit two four-bit parts");

        this.bds1 = (byte) bds1;
        this.bds2 = (byte) bds2;
    }

    /**
     * @return the first part of the code, e.g. 6 for register 6,0
     */
    public byte getBDS1() {
        return bds1;
    }

    /**
     * @return the second part of the code, e.g. 0 for register 6,0
     */
    public byte getBDS2() {
        return bds2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDSCode)) return false;
        BDSCode other = (BDSCode) o;
        return bds1 == other.bds1 && bds2 == other.bds2;
    }

    @Override
    public int hashCode() {
        return 16 * bds1 + bds2;
    }

    /**
     * @return the code as Doc 9871 writes it, each part a hexadecimal digit: "6,0", "5,F"
     */
    @Override
    public String toString() {
        return Integer.toHexString(bds1).toUpperCase() + "," + Integer.toHexString(bds2).toUpperCase();
    }
}
