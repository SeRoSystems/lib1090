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

package de.serosystems.lib1090.msgs.squitter;

/**
 * Common API for messages that expose the ADS-B Receiver Version (see DO-260C).
 */
public interface ADSBReceiverVersionMsg {

    /**
     * @return the encoded ADS-B Receiver Version
     */
    byte getADSBReceiverVersionEncoded();

    /**
     * ADS-B Receiver Version
     */
    enum ADSBReceiverVersion {
        /**
         * Not available, unknown or ADS-B Version 0, 1 or 2.
         */
        UNAVAILABLE_OR_LEGACY,
        /**
         * Receiver Capability is ADS-B Version 3
         */
        RECEIVER_CAPABILITY_VN3,
        /**
         * Reserved for future use
         */
        RESERVED,
    }

    /**
     * @return the ADS-B Receiver Version
     */
    default ADSBReceiverVersion getADSBReceiverVersion() {
        switch (getADSBReceiverVersionEncoded()) {
            case 0:
                return ADSBReceiverVersion.UNAVAILABLE_OR_LEGACY;
            case 1:
                return ADSBReceiverVersion.RECEIVER_CAPABILITY_VN3;
            default:
                return ADSBReceiverVersion.RESERVED;
        }
    }
}
