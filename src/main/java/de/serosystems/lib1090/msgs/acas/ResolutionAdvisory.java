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
 * The content of an ACAS resolution advisory message, message bits 41-88, ICAO Annex 10 Volume IV (6th edition)
 * §4.3.8.4.2.2: what every layout has, the active RAs (ARA) and the RACs record (RAC).
 * <p>
 * The accessors address bits by their message bit number, as the Annex does; the bit accessors are public as the
 * escape hatch for bits this library does not model.
 */
public interface ResolutionAdvisory {

    /**
     * @return message bits 41-88 as transmitted, right-aligned; bit {@code k} of the returned value is message bit
     * {@code 88 - k}
     */
    long getEncoded();

    /**
     * @param messageBit a message bit number from 41 to 88
     * @return whether that bit is set
     */
    default boolean getBit(int messageBit) {
        return getBits(messageBit, messageBit) == 1;
    }

    /**
     * @param first the first message bit number, inclusive, from 41 on
     * @param last  the last message bit number, inclusive, up to 88
     * @return the bits, right-aligned
     */
    default int getBits(int first, int last) {
        if (first < 41 || last > 88 || last < first || last - first > 31)
            throw new IllegalArgumentException("No field of up to 32 bits within 41-88: " + first + "-" + last);
        return (int) ((getEncoded() >>> (88 - last)) & ((1L << (last - first + 1)) - 1));
    }

    /**
     * @return the collision avoidance system that generated the content, and so its layout
     */
    RAMessageFormat getRAMessageFormat();

    /**
     * @return the ARA (active RAs) subfield as transmitted; its width and meaning depend on the layout
     */
    int getActiveRAEncoded();

    /**
     * @return the ARA bits one by one, bit 41 first; their meaning depends on the layout
     */
    boolean[] getActiveResolutionAdvisories();

    /**
     * @return the RAC (RACs record) subfield as transmitted, bits 55-58: the currently active RACs received from other
     * ACAS aircraft
     */
    default byte getRACRecordEncoded() {
        return (byte) getBits(55, 58);
    }

    /**
     * @return whether the RAC "Do not pass below" is active, bit 55
     */
    default boolean isDoNotPassBelowActive() {
        return getBit(55);
    }

    /**
     * @return whether the RAC "Do not pass above" is active, bit 56
     */
    default boolean isDoNotPassAboveActive() {
        return getBit(56);
    }
}
