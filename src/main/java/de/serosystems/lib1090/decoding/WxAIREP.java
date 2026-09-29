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

import de.serosystems.lib1090.exceptions.BadFormatException;

/**
 * Decoding of the Wx AIREP messages, TYPE Code 26, ED-102B §2.2.3.2.7.6, shared by ADS-B and ADS-R, whose
 * formats differ only in ME bit 56, the IMF for ADS-R (ED-102B §2.2.18.4.8). Each method takes the encoded
 * field and returns {@code null} for code 0, "no data".
 */
public final class WxAIREP {

    private WxAIREP() {
    }

    /**
     * Validate that a message is the Wx AIREP message of the given subtype, ME bits 6-7.
     *
     * @param formatTypeCode the message's format type code
     * @param br             bit reader over the 7-byte ME field
     * @param subtype        the expected subtype: 0 aircraft state, 1 weather state, 2 alternate weather state
     * @param name           the subtype's name, for the exception text
     * @throws BadFormatException if the TYPE Code is not 26 or the subtype not the expected one
     */
    public static void validateFormat(byte formatTypeCode, BitReader br, int subtype, String name)
            throws BadFormatException {
        if (formatTypeCode != 26)
            throw new BadFormatException("Wx AIREP messages must have typecode 26");
        if (br.readByte(6, 7) != subtype)
            throw new BadFormatException("Wx AIREP " + name + " message must have subtype " + subtype);
    }

    /**
     * The aircraft type, IA5 encoded as 4 consecutive 6-bit characters, ED-102B §2.2.3.2.7.6.3.3.
     *
     * @param encoded the raw 24-bit field
     * @return the aircraft type as a 4 character array, or {@code null} if unavailable; a character whose code
     * is not defined is decoded as a space, see {@link #isValidAircraftType(int)}
     */
    public static char[] aircraftType(int encoded) {
        if (encoded == 0) return null;
        return InternationalAlphabet5.mapChar(InternationalAlphabet5.toDigits(encoded, 4));
    }

    /**
     * @param encoded the raw 24-bit aircraft type field
     * @return true if the aircraft type is available and every character has a code that ICAO Annex 10 Volume IV
     * §3.1.2.9.1.2 TABLE 3-8 defines
     */
    public static boolean isValidAircraftType(int encoded) {
        return encoded != 0 && InternationalAlphabet5.isDefined(InternationalAlphabet5.toDigits(encoded, 4));
    }

    /**
     * The gross weight, ED-102B §2.2.3.2.7.6.3.4 TABLE 2-85, as the lower end of the range its code stands for.
     *
     * @param encoded the raw 12-bit field
     * @return a lower bound for the gross weight in lbs, {@code 1514015} meaning "equal to or greater than 1514015
     * lbs", or {@code null} if unavailable
     */
    public static Double grossWeight(int encoded) {
        if (encoded == 0) return null;
        if (encoded == 1) return 0.;
        if (encoded <= 386) return 55. + (encoded - 2) * 40.;
        if (encoded <= 1150) return 15455. + (encoded - 387) * 80.;
        if (encoded <= 2546) return 76575. + (encoded - 1151) * 160.;
        if (encoded <= 3536) return 299935. + (encoded - 2547) * 480.;
        if (encoded <= 4005) return 775135. + (encoded - 3537) * 1120.;
        if (encoded <= 4094) return 1300415. + (encoded - 4006) * 2400.;
        return 1514015.;
    }

    /**
     * The wingspan as the interval its code stands for, ED-102B §2.2.3.2.7.6.3.5: the encoding truncates, so
     * code N covers [W(N), W(N + 1)) with W(N) = ((1 + (N - 2) * 0.004)² - 0.952) / 0.008 ft; code 1 is any
     * wingspan less than 6 ft, and code 255 any of 387.018 ft or more.
     *
     * @param encoded the raw 8-bit field
     * @return the interval of the wingspan in feet, or {@code null} if unavailable
     */
    public static Interval wingspan(int encoded) {
        if (encoded == 0) return null;
        if (encoded == 1) return Interval.of(Bound.AT_LEAST, 0, Bound.BELOW, 6);
        if (encoded == 255) return Interval.of(Bound.AT_LEAST, wingspanLowerEnd(255), Bound.NONE, Double.NaN);
        return Interval.of(Bound.AT_LEAST, wingspanLowerEnd(encoded), Bound.BELOW, wingspanLowerEnd(encoded + 1));
    }

    /**
     * @param code a wingspan code from 2 to 255
     * @return the smallest wingspan in feet the encoding formula maps to {@code code}
     */
    private static double wingspanLowerEnd(int code) {
        double t = 1 + (code - 2) * 0.004;
        return (t * t - 0.952) / 0.008;
    }

    /**
     * @param encoded the raw 8-bit wind speed field
     * @return a lower bound for the wind speed in knots, or {@code null} if unavailable
     */
    public static Short windSpeed(int encoded) {
        if (encoded == 0) return null;
        return (short) (encoded - 1);
    }

    /**
     * @param encoded the raw 10-bit wind direction field
     * @return a lower bound for the wind direction in degrees [0,360) clockwise from true north, or {@code null}
     * if unavailable
     */
    public static Double windDirection(int encoded) {
        if (encoded == 0) return null;
        return (encoded - 1) / 1023. * 360;
    }

    /**
     * @param encoded the raw 10-bit roll angle field
     * @return the roll angle in degrees as a lower bound, a value less than -90 denoting "below -90°", or
     * {@code null} if unavailable
     */
    public static Double rollAngle(int encoded) {
        if (encoded == 0) return null;
        return -90 + (encoded - 2) * 180. / 1021;
    }

    /**
     * @param encoded the raw 12-bit heading field
     * @return a lower bound for the heading in degrees [0,360), or {@code null} if unavailable
     */
    public static Double heading(int encoded) {
        if (encoded == 0) return null;
        return (encoded - 1) * 360. / 4095;
    }
}
