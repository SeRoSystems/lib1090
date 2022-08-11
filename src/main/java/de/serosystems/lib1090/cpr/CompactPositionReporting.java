package de.serosystems.lib1090.cpr;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.serosystems.lib1090.Position;

import static java.lang.Math.abs;

/**
 * Decoder for CPR encoded positions
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class CompactPositionReporting {

    /**
     * This method can only be used if another position report with a different format (even/odd) is available
     * and set with msg.setOtherFormatMsg(other).
     * @param pos CPR encoded position
     * @param old airborne position message of the other format (even/odd). Note that the time between
     *        both messages should be not longer than 10 seconds!
     * @param reference position to determine right surface position; use null for airborne (will be ignored)
     * @return globally unambiguously decoded position for cpr1 or null if two encoded positions cannot be combined or
     *         if position is otherwise unavailable. Altitude of resulting position is null.
     */
    public static Position decodeGlobalPosition(CPREncodedPosition pos, CPREncodedPosition old, Position reference) {

        if (pos.isOddFormat() == old.isOddFormat()) return null;
        if (pos.isSurface() != old.isSurface()) return null;
        if (pos.isSurface() && reference == null) return null;

        CPREncodedPosition even = pos.isOddFormat() ? old : pos;
        CPREncodedPosition odd = pos.isOddFormat() ? pos : old;

        double angle = even.isSurface() ? 90 : 360.0;

        // Helper for latitude (Number of zones NZ is set to 15)
        double Dlat0 = angle / 60.0;
        double Dlat1 = angle / 59.0;

        // latitude index
        double j = Math.floor((
                (59.0 * ((double) even.getEncodedLat())) / ((double) (1 << even.getNumBits())) -
                        (60.0 * ((double) odd.getEncodedLat())) / ((double) (1 << odd.getNumBits()))) + 0.5);

        // global latitudes
        double Rlat0 = Dlat0 * (mod(j, 60.0) + ((double) even.getEncodedLat()) / ((double) (1 << even.getNumBits())));
        double Rlat1 = Dlat1 * (mod(j, 59.0) + ((double) odd.getEncodedLat()) / ((double) (1 << odd.getNumBits())));

        // Southern hemisphere?
        if (Rlat0 >= 270.0 && Rlat0 <= 360.0) Rlat0 -= 360.0;
        if (Rlat1 >= 270.0 && Rlat1 <= 360.0) Rlat1 -= 360.0;

        // ensure that the number of even longitude zones are equal
        if (NL(Rlat0) != NL(Rlat1)) return null; // position straddle

        double Rlat = pos.isOddFormat() ? Rlat1 : Rlat0;

        // Helper for longitude
        double NL_helper = NL(Rlat0);// NL(Rlat0) and NL(Rlat1) are equal

        // longitude index
        double m = Math.floor(
                ((double) even.getEncodedLon() * (NL_helper - 1.0)) / ((double) (1 << even.getNumBits())) -
                        ((double) odd.getEncodedLon() * NL_helper) / ((double) (1 << odd.getNumBits())) + 0.5);

        // global longitude
        double n_helper = Math.max(1.0, NL_helper - (pos.isOddFormat() ? 1.0 : 0.0));
        double Rlon = (angle / n_helper) * (mod(m, n_helper) + ((double) pos.getEncodedLon()) / ((double) (1 << pos.getNumBits())));

        // correct longitude
        if (Rlon < -180.0 && Rlon > -360.0) Rlon += 360.0;
        if (Rlon > 180.0 && Rlon < 360.0) Rlon -= 360.0;

        if (pos.isSurface()) {
            // check the 4 possible solutions of the surface decoding
            Position candidate = new Position(Rlon, Rlat, 0.0);
            for (int o : new int[] {90, 180, 270}) {
                Position alternative = new Position(Rlon + o, Rlat, 0.0);
                if (reference.haversine(alternative) < reference.haversine(candidate))
                    candidate = alternative;
            }
            return candidate;
        } else return new Position(Rlon, Rlat, null);
    }

    /**
     * This method uses a locally unambiguous decoding for airborne position messages. It
     * uses a reference position known to be within 180NM (= 333.36km) of the true target
     * airborne position and within 45NM for surface positions. The reference point may be
     * a previously decoded position that has been confirmed by global decoding (see
     * {@link #decodeGlobalPosition(CPREncodedPosition, CPREncodedPosition, Position)}) or
     * the receiver position.
     * @param pos CPR encoded position
     * @param ref reference position
     * @return decoded position (without altitude)
     */
    public static Position decodeLocalPosition(CPREncodedPosition pos, Position ref) {
        if (ref == null) return null;

        // latitude zone size
        double angle = pos.isSurface() ? 90.0 : 360.0;
        double Dlat = pos.isOddFormat() ? angle / 59.0 : angle / 60.0;

        // latitude zone index
        double j = Math.floor(ref.getLatitude() / Dlat) + Math.floor(
                0.5 + mod(ref.getLatitude(), Dlat) / Dlat - ((double) pos.getEncodedLat()) / ((double) (1 << pos.getNumBits())));

        // decoded position latitude
        double Rlat = Dlat * (j + ((double) pos.getEncodedLat()) / ((double) (1 << pos.getNumBits())));

        // longitude zone size
        double Dlon = angle / Math.max(1.0, NL(Rlat) - (pos.isOddFormat() ? 1.0 : 0.0));

        // longitude zone coordinate
        double m = Math.floor(ref.getLongitude() / Dlon) + Math.floor(0.5 + mod(ref.getLongitude(), Dlon) / Dlon
                - ((double) pos.getEncodedLon()) / ((double) (1 << pos.getNumBits())));

        // and finally the longitude
        double Rlon = Dlon * (m + ((double) pos.getEncodedLon()) / ((double) (1 << pos.getNumBits())));

        return new Position(Rlon, Rlat, null);
    }

    /**
     * @param Rlat Even or odd Rlat value (CPR internal)
     * @return the number of even longitude zones at a latitude
     */
    private static double NL(double Rlat) {
        if (Rlat == 0) return 59;
        else if (Math.abs(Rlat) == 87) return 2;
        else if (Math.abs(Rlat) > 87) return 1;

        double tmp = 1-(1-Math.cos(Math.PI/(2.0*15.0)))/Math.pow(Math.cos(Math.PI/180.0*Math.abs(Rlat)), 2);
        return Math.floor(2*Math.PI/Math.acos(tmp));
    }

    /**
     * Modulo operator in java has stupid behavior
     */
    private static double mod(double a, double b) {
        return ((a%b)+b)%b;
    }

}
