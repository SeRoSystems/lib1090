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
 * The "Emergency State" subfield of ADS-B versions 1 and 2, ED-102B §N.5.2 FIGURE N-13 and §N.5.3
 * FIGURE N-21, with its mapping to version 3, ED-102B §N.3.3.4 TABLE N-19. Version 1 also carries the
 * subfield in its Target State and Status Message, ED-102B §N.5.2 FIGURE N-14.
 * <p>
 * Version 3 redefined "Lifeguard/medical Emergency" (2), "Downed Aircraft" (6) and the reserved value
 * (7), so the first two report as "General Emergency" and the last as "No Reported Emergency".
 * <p>
 * ED-102B gives no mapping for version 2: §N.4.3 leaves every Mode Status parameter it does not address
 * to the version 3 decoding, and does not address this one. Taken literally, that would read version 2's
 * "Lifeguard/Medical" as "UAS/RPAS - Lost Link" and "Downed aircraft" as "Aircraft in Distress -
 * Automatic Activation". FIGURE N-21 defines every value exactly as version 1 does, so this library
 * applies TABLE N-19 to version 2 as well.
 */
public enum EmergencyStateV1V2 implements EmergencyState {

    NO_EMERGENCY(0, "No Emergency", EmergencyStateV3.NO_REPORTED_EMERGENCY),
    GENERAL_EMERGENCY(1, "General Emergency", EmergencyStateV3.GENERAL_EMERGENCY),
    LIFEGUARD_MEDICAL_EMERGENCY(2, "Lifeguard/medical Emergency", EmergencyStateV3.GENERAL_EMERGENCY),
    MINIMUM_FUEL(3, "Minimum Fuel", EmergencyStateV3.MINIMUM_FUEL),
    NO_COMMUNICATIONS(4, "No Communications", EmergencyStateV3.NO_COMMUNICATIONS),
    UNLAWFUL_INTERFERENCE(5, "Unlawful Interference", EmergencyStateV3.UNLAWFUL_INTERFERENCE),
    DOWNED_AIRCRAFT(6, "Downed Aircraft", EmergencyStateV3.GENERAL_EMERGENCY),
    RESERVED_7(7, "Reserved", EmergencyStateV3.NO_REPORTED_EMERGENCY);

    /** In declaration order, which is the order of the encoded values. */
    private static final EmergencyStateV1V2[] VALUES = values();

    private final byte encoded;
    private final String text;
    private final EmergencyStateV3 reported;

    EmergencyStateV1V2(int encoded, String text, EmergencyStateV3 reported) {
        this.encoded = (byte) encoded;
        this.text = text;
        this.reported = reported;
    }

    /**
     * @param encoded the encoded emergency state, 0 to 7
     * @return the constant for that value
     * @throws IllegalArgumentException if the value is outside the three bits the field occupies
     */
    public static EmergencyStateV1V2 forEncoded(byte encoded) {
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
     * @return the version 3 value this one is reported as, ED-102B §N.3.3.4 TABLE N-19
     */
    @Override
    public EmergencyStateV3 getReported() {
        return reported;
    }
}
