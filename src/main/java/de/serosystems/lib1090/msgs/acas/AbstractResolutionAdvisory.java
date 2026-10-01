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

import java.io.Serializable;

/**
 * Base implementation of {@link ResolutionAdvisory}: holds message bits 41-88 and the width of the layout's ARA
 * subfield, and leaves every other subfield to the interfaces' default methods and the subclasses' accessors.
 * <p>
 * Instances are immutable. Subclasses are expected to add only subfield accessors, no state, so that the encoded
 * value stays the single source of truth.
 */
public abstract class AbstractResolutionAdvisory implements ResolutionAdvisory, Serializable {
    private static final long serialVersionUID = 4178262955473130411L;

    private long encoded;
    private byte activeRALastBit;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AbstractResolutionAdvisory() {
    }

    /**
     * @param encoded         message bits 41-88, right-aligned
     * @param activeRALastBit the last message bit of the layout's ARA subfield, which starts at bit 41
     * @throws IllegalArgumentException if {@code encoded} has bits set above its 48 bits
     */
    protected AbstractResolutionAdvisory(long encoded, int activeRALastBit) {
        if ((encoded >>> 48) != 0)
            throw new IllegalArgumentException("Encoded value 0x" + Long.toHexString(encoded) + " exceeds 48 bits");
        this.encoded = encoded;
        this.activeRALastBit = (byte) activeRALastBit;
    }

    @Override
    public long getEncoded() {
        return encoded;
    }

    @Override
    public int getActiveRAEncoded() {
        return getBits(41, activeRALastBit);
    }

    @Override
    public boolean[] getActiveResolutionAdvisories() {
        boolean[] bits = new boolean[activeRALastBit - 40];
        for (int bit = 41; bit <= activeRALastBit; bit++)
            bits[bit - 41] = getBit(bit);
        return bits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return encoded == ((AbstractResolutionAdvisory) o).encoded;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(encoded);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{bits 41-88=0x" + Long.toHexString(encoded) + '}';
    }
}
