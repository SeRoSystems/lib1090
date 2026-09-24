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
 * The "Emergency State" subfield of ADS-B version 0, ED-102B §N.5.1 FIGURE N-6, with its mapping to
 * version 3, ED-102B §N.2.3.4 TABLE N-7.
 * <p>
 * Version 3 redefined "Lifeguard/medical Emergency" (2) and the two reserved values (6 and 7), so these
 * report as "General Emergency" and "No Reported Emergency" respectively.
 */
public enum EmergencyStateV0 implements EmergencyState {

    NO_EMERGENCY(0, "No Emergency", EmergencyStateV3.NO_REPORTED_EMERGENCY),
    GENERAL_EMERGENCY(1, "General Emergency", EmergencyStateV3.GENERAL_EMERGENCY),
    LIFEGUARD_MEDICAL_EMERGENCY(2, "Lifeguard/medical Emergency", EmergencyStateV3.GENERAL_EMERGENCY),
    MINIMUM_FUEL(3, "Minimum Fuel", EmergencyStateV3.MINIMUM_FUEL),
    NO_COMMUNICATIONS(4, "No Communications", EmergencyStateV3.NO_COMMUNICATIONS),
    UNLAWFUL_INTERFERENCE(5, "Unlawful Interference", EmergencyStateV3.UNLAWFUL_INTERFERENCE),
    RESERVED_6(6, "Reserved", EmergencyStateV3.NO_REPORTED_EMERGENCY),
    RESERVED_7(7, "Reserved", EmergencyStateV3.NO_REPORTED_EMERGENCY);

    /** In declaration order, which is the order of the encoded values. */
    private static final EmergencyStateV0[] VALUES = values();

    private final byte encoded;
    private final String text;
    private final EmergencyStateV3 reported;

    EmergencyStateV0(int encoded, String text, EmergencyStateV3 reported) {
        this.encoded = (byte) encoded;
        this.text = text;
        this.reported = reported;
    }

    /**
     * @param encoded the encoded emergency state, 0 to 7
     * @return the constant for that value
     * @throws IllegalArgumentException if the value is outside the three bits the field occupies
     */
    public static EmergencyStateV0 forEncoded(byte encoded) {
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
     * @return the version 3 value this one is reported as, ED-102B §N.2.3.4 TABLE N-7
     */
    @Override
    public EmergencyStateV3 getReported() {
        return reported;
    }
}
