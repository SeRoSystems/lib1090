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

import java.io.Serializable;

/**
 * Base implementation of {@link MEField}: holds a right-aligned field value together with the ME bit
 * range it occupies, and leaves the bit arithmetic to the interface's default methods.
 * <p>
 * Instances are immutable. Subclasses are expected to add only subfield accessors, no state, so that
 * the encoded value stays the single source of truth.
 */
public abstract class AbstractMEField implements MEField, Serializable {

    private static final long serialVersionUID = 7526035182648317441L;

    private int encoded;
    private byte firstMEBit;
    private byte lastMEBit;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AbstractMEField() {
    }

    /**
     * @param encoded    the field as transmitted, right-aligned; bits above the field width must be zero
     * @param firstMEBit the ME bit number at which the field starts, inclusive
     * @param lastMEBit  the ME bit number at which the field ends, inclusive
     * @throws IllegalArgumentException if the range is not a valid ME bit range, or if {@code encoded}
     *                                  has bits set outside the field width
     */
    protected AbstractMEField(int encoded, int firstMEBit, int lastMEBit) {
        if (firstMEBit < 1)
            throw new IllegalArgumentException("First ME bit must be >= 1");
        if (lastMEBit < firstMEBit)
            throw new IllegalArgumentException(
                    "Last ME bit " + lastMEBit + " < first ME bit " + firstMEBit);
        if (lastMEBit > 56)
            throw new IllegalArgumentException("Last ME bit must be <= 56");

        int width = lastMEBit - firstMEBit + 1;
        if (width > 32)
            throw new IllegalArgumentException("Field wider than 32 bits");
        if (width < 32 && (encoded >>> width) != 0)
            throw new IllegalArgumentException(
                    "Encoded value 0x" + Integer.toHexString(encoded) + " exceeds " + width + " bits");

        this.encoded = encoded;
        this.firstMEBit = (byte) firstMEBit;
        this.lastMEBit = (byte) lastMEBit;
    }

    @Override
    public int getEncoded() {
        return encoded;
    }

    @Override
    public int getFirstMEBit() {
        return firstMEBit;
    }

    @Override
    public int getLastMEBit() {
        return lastMEBit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractMEField other = (AbstractMEField) o;
        return encoded == other.encoded
                && firstMEBit == other.firstMEBit
                && lastMEBit == other.lastMEBit;
    }

    @Override
    public int hashCode() {
        int result = encoded;
        result = 31 * result + firstMEBit;
        result = 31 * result + lastMEBit;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ME " + firstMEBit + "-" + lastMEBit
                + "=0x" + Integer.toHexString(encoded) + '}';
    }
}
