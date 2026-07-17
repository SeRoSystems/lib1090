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
 * Identification message decoding
 */
public final class Identification {

    private Identification() {
    }

    /**
     * Maps ADS-B encoded to readable characters
     *
     * @param digit encoded digit
     * @return readable character
     */
    public static char mapChar(byte digit) {
        if (digit > 0 && digit < 27) return (char) ('A' + digit - 1);
        else if (digit > 47 && digit < 58) return (char) ('0' + digit - 48);
        else return ' ';
    }

    /**
     * Maps ADS-B encoded to readable characters
     *
     * @param digits array of encoded digits
     * @return array of decoded characters
     */
    public static char[] mapChar(byte[] digits) {
        char[] result = new char[digits.length];

        for (int i = 0; i < digits.length; i++)
            result[i] = mapChar(digits[i]);

        return result;
    }

    /**
     * @param identificationEncoded the raw 48-bit identification field
     * @return the 8 encoded (6-bit) identification digits, in order
     */
    public static byte[] identificationDigits(long identificationEncoded) {
        byte[] digits = new byte[8];

        // the 8 characters are consecutive 6-bit values, most significant first
        for (int i = 0; i < digits.length; i++)
            digits[i] = (byte) ((identificationEncoded >>> (42 - 6 * i)) & 0x3F);

        return digits;
    }

    /**
     * @param typeCode        format type code of identification message
     * @param emitterCategory reported emitter category
     * @param version         ADS-B version of the reporting aircraft
     * @return a textual description of the emitter's category according to DO-260B
     * @throws IllegalArgumentException if version is greater than 7
     */
    public static String categoryDescription(byte typeCode, byte emitterCategory, int version) {
        if (version < 0 || version > 7)
            throw new IllegalArgumentException("Unsupported ADS-B version: " + version);

        // versions above 3 are decoded like version 3, per DO-260C, §2.2.7.1
        int effectiveVersion = Math.min(version, 3);

        // category descriptions according
        // to the ADS-B specification
        String[][] categories = {{
                "No ADS-B Emitter Category Information",
                "Light (< 15500 lbs)",
                "Small (15500 to 75000 lbs)",
                "Large (75000 to 300000 lbs)",
                "High-Vortex Large (aircraft such as B-757)",
                "Heavy (> 300000 lbs)",
                "High Performance (> 5g acceleration and > 400 kts)",
                "Rotorcraft"
        }, {
                "No ADS-B Emitter Category Information",
                "Glider / sailplane",
                "Lighter-than-air",
                "Parachutist / Skydiver",
                "Ultralight / hang-glider / paraglider",
                "Reserved",
                "Unmanned Aerial Vehicle",
                "Space / Trans-atmospheric vehicle",
        }, {
                "No ADS-B Emitter Category Information",
                "Surface Vehicle – Emergency Vehicle",
                "Surface Vehicle – Service Vehicle",
                "Point Obstacle (includes tethered balloons)",
                "Cluster Obstacle",
                "Line Obstacle",
                "Reserved",
                "Reserved"
        }, {
                "No ADS-B Emitter Category Information",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved"
        }};

        if (effectiveVersion == 0 && typeCode == 2) {
            // version 0 exceptions to the version 2 table for category set C
            if (emitterCategory == 3)
                return "Fixed Ground or Tethered Obstruction";
            if (emitterCategory >= 4 && emitterCategory <= 7) return "Reserved";
        }

        if (effectiveVersion == 3) {
            if (typeCode == 4) {
                // version 3 exceptions to the version 2 table for category set A
                switch (emitterCategory) {
                    case 1: return "MTOW < 15500 lbs";
                    case 2: return "15500 <= MTOW < 75000 lbs";
                    case 3: return "75000 <= MTOW < 300000 lbs";
                    case 4:
                    case 6:
                        return "Reserved";
                    case 5: return "MTOW >= 300000 lbs";
                }
            } else if (typeCode == 3 && (emitterCategory == 3 || emitterCategory == 6 || emitterCategory == 7)) {
                // version 3 exceptions to the version 2 table for category set B
                return "Reserved";
            }
        }

        return categories[4 - typeCode][emitterCategory];
    }
}
