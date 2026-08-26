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

/**
 * A subfield of the "ME" field of an extended squitter, addressed by <b>ME bit number</b> rather
 * than by a mask.
 * <p>
 * Implementations hold the field right-aligned in an {@code int} — as produced by
 * {@link BitReader#readInt(int, int)} — together with the ME bit range the field occupies. Everything
 * else follows arithmetically, so a subfield accessor can be written directly in the numbering used by
 * the standard:
 * <pre>
 *     // ED-102B TABLE 2-49: "1090ES IN" is ME bit 12
 *     default boolean is1090ESInOperational() {
 *         return getMEBit(12);
 *     }
 * </pre>
 * The same accessor then works for every layout carrying that subfield at that ME bit, regardless of
 * how wide the enclosing field is: the Capability Class Code spans ME 9–24 when airborne and ME 9–20
 * when on the surface, which would otherwise require two different masks
 * ({@code 0x1000} and {@code 0x100}) for one and the same bit.
 * <p>
 * <b>Indexing convention:</b> ME bit numbers are 1-based and count from the MSB of the ME field,
 * matching ED-102B and the {@link BitReader} convention for the message as a whole. ME bit 1 is the
 * first bit of the ME field, i.e. the MSB of the format type code.
 * <p>
 * The bit accessors are part of the public API deliberately: they are the escape hatch for reading
 * bits this library does not model, and on Java 8 a {@code default} method cannot reach a
 * non-public member of its implementor anyway.
 *
 * @see AbstractMEField for the state-holding implementation
 */
public interface MEField {

    /**
     * @return the field as transmitted, right-aligned in an {@code int}; bit {@code k} of the
     * returned value is ME bit {@code getLastMEBit() - k}
     */
    int getEncoded();

    /**
     * @return the ME bit number at which this field starts, inclusive
     */
    int getFirstMEBit();

    /**
     * @return the ME bit number at which this field ends, inclusive
     */
    int getLastMEBit();

    /**
     * @return the number of bits this field occupies
     */
    default int getWidth() {
        return getLastMEBit() - getFirstMEBit() + 1;
    }

    /**
     * Read a single bit by its ME bit number.
     *
     * @param meBit the ME bit number, which must lie within this field
     * @return true if the bit is set
     * @throws IllegalArgumentException if {@code meBit} lies outside this field
     */
    default boolean getMEBit(int meBit) {
        return getMEBits(meBit, meBit) != 0;
    }

    /**
     * Read a range of bits by their ME bit numbers.
     *
     * @param fromMEBit the first ME bit number, inclusive
     * @param toMEBit   the last ME bit number, inclusive
     * @return the bits in {@code [fromMEBit, toMEBit]}, right-aligned
     * @throws IllegalArgumentException if the range is empty or does not lie within this field
     */
    default int getMEBits(int fromMEBit, int toMEBit) {
        if (toMEBit < fromMEBit)
            throw new IllegalArgumentException(
                    "End ME bit " + toMEBit + " < start ME bit " + fromMEBit);
        if (fromMEBit < getFirstMEBit() || toMEBit > getLastMEBit())
            throw new IllegalArgumentException(
                    "ME bits " + fromMEBit + "-" + toMEBit + " are outside field ME "
                            + getFirstMEBit() + "-" + getLastMEBit());

        int width = toMEBit - fromMEBit + 1;
        int mask = width == 32 ? -1 : (1 << width) - 1;
        return (getEncoded() >>> (getLastMEBit() - toMEBit)) & mask;
    }
}
