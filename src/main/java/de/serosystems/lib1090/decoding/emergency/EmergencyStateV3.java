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

package de.serosystems.lib1090.decoding.emergency;

/**
 * The "Emergency/Priority Status" subfield of ADS-B version 3, ED-102B §2.2.3.2.7.8.1.1 TABLE 2-97.
 * <p>
 * Version 3 is the version every other one is mapped to, so each constant reports itself.
 */
public enum EmergencyStateV3 implements EmergencyState {

    NO_REPORTED_EMERGENCY(0, "No Reported Emergency"),
    GENERAL_EMERGENCY(1, "General Emergency"),
    UAS_RPAS_LOST_LINK(2, "UAS/RPAS - Lost Link"),
    MINIMUM_FUEL(3, "Minimum Fuel"),
    NO_COMMUNICATIONS(4, "No Communications"),
    UNLAWFUL_INTERFERENCE(5, "Unlawful Interference"),
    AIRCRAFT_IN_DISTRESS_AUTOMATIC_ACTIVATION(6, "Aircraft in Distress - Automatic Activation"),
    AIRCRAFT_IN_DISTRESS_MANUAL_ACTIVATION(7, "Aircraft in Distress - Manual Activation");

    /** In declaration order, which is the order of the encoded values. */
    private static final EmergencyStateV3[] VALUES = values();

    private final byte encoded;
    private final String text;

    EmergencyStateV3(int encoded, String text) {
        this.encoded = (byte) encoded;
        this.text = text;
    }

    /**
     * @param encoded the encoded emergency state, 0 to 7
     * @return the constant for that value
     * @throws IllegalArgumentException if the value is outside the three bits the field occupies
     */
    public static EmergencyStateV3 forEncoded(byte encoded) {
        if (encoded < 0 || encoded >= VALUES.length)
            throw new IllegalArgumentException(
                    "Emergency state " + encoded + " does not fit the three bits it occupies");

        return VALUES[encoded];
    }

    @Override
    public byte getEncoded() {
        return encoded;
    }

    @Override
    public String getText() {
        return text;
    }

    /**
     * @return this constant, version 3 being the version all others are reported as
     */
    @Override
    public EmergencyStateV3 getReported() {
        return this;
    }
}
