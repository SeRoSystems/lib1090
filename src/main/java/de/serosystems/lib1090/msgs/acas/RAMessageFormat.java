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
 * The RA message format (RMF) of an ACAS resolution advisory report, broadcast or coordination reply: the collision
 * avoidance system that generated bits 41-88 of the message, and so their layout, ICAO Annex 10 Volume IV (6th
 * edition) §4.3.8.4.2.2.2.3.
 * <p>
 * RMF occupies bits 53-54. In the TCAS layout, §4.3.8.4.2.2.1.1, these bits belong to ARA and are "Reserved for ACAS
 * III", so every TCAS II version transmits 0 there.
 */
public enum RAMessageFormat {

    /**
     * "All TCAS II versions": the TCAS layout of §4.3.8.4.2.2.1.
     */
    TCAS_II(0),
    /**
     * "ACAS X-compliant system": the ACAS X layout of §4.3.8.4.2.2.2.
     */
    ACAS_X(1),
    /**
     * "Reserved for ACAS III": no layout is defined.
     */
    ACAS_III(2),
    /**
     * "Unallocated": no layout is defined.
     */
    UNALLOCATED(3);

    private static final RAMessageFormat[] BY_CODE = values();

    private final byte encoded;

    RAMessageFormat(int encoded) {
        this.encoded = (byte) encoded;
    }

    /**
     * @param encoded the 2-bit encoded RMF
     * @return the message format it stands for
     * @throws IllegalArgumentException if the value is not 0 to 3
     */
    public static RAMessageFormat forEncoded(int encoded) {
        if (encoded < 0 || encoded >= BY_CODE.length)
            throw new IllegalArgumentException("RMF is a 2-bit field, got " + encoded);
        return BY_CODE[encoded];
    }

    /**
     * @return the encoded value
     */
    public byte getEncoded() {
        return encoded;
    }

    /**
     * @return whether the standard defines the layout of this format, i.e. whether it is {@link #TCAS_II} or
     * {@link #ACAS_X}
     */
    public boolean hasDefinedLayout() {
        return this == TCAS_II || this == ACAS_X;
    }
}
