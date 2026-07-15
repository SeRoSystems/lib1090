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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.Identification;

public interface IdentificationMsg {

    /**
     * The four sets of emitter categories, as distinguished by the message's format type code.
     */
    enum CategorySet {
        A, B, C, D
    }

    /**
     * @return the message's format type code.
     */
    byte getFormatTypeCode();

    /**
     * @return the emitter's category (numerical)
     */
    byte getEmitterCategoryEncoded();

    /**
     * @return the call sign as 8 characters array
     */
    default char[] getIdentification() {
        return Identification.mapChar(getIdentificationDigits());
    }

    /**
     * @return the identification as an array of 8 encoded (6-bit) digits, in order
     */
    default byte[] getIdentificationDigits() {
        return Identification.identificationDigits(getIdentificationEncoded());
    }

    /**
     * @return the raw 48-bit identification field (bits 9-56 of the message), as encoded
     */
    long getIdentificationEncoded();

    /**
     * @return the description of the emitter's category according to
     * the ADS-B message format specification
     */
    String getEmitterCategory();

    /**
     * @return the emitter category set this message's emitter category belongs to, as determined
     * by the format type code
     */
    default CategorySet getCategorySet() {
        switch (getFormatTypeCode()) {
            case 4: return CategorySet.A;
            case 3: return CategorySet.B;
            case 2: return CategorySet.C;
            case 1: return CategorySet.D;
            default: throw new IllegalStateException("Unexpected format type code: " + getFormatTypeCode());
        }
    }
}
