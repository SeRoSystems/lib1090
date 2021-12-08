package org.opensky.libadsb;

/*
 *  This file is part of org.opensky.libadsb.
 *
 *  org.opensky.libadsb is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  org.opensky.libadsb is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with org.opensky.libadsb.  If not, see <http://www.gnu.org/licenses/>.
 */

import static java.lang.Math.abs;

/**
 * Decoder for CPR encoded positions
 * @author Matthias Schaefer (schaefer@sero-systems.de)
 */
public class CompactPositionReporting {

    public static class CPREncodedPosition {

        private final boolean is_odd;
        private final int encoded_lat;
        private final int encoded_lon;
        private final int nbits;
        private final boolean surface;
        private final long timestamp;

        /**
         * @param is_odd true if it is a odd format, false if it is even (format field in most position messags)
         * @param encoded_lat CPR encoded latitude
         * @param encoded_lon CPR encoded longitude
         * @param nbits number of bits used to encode latitude and longitude; 17 for airborne position, 14 for intent,
         *              and 12 for TIS-B
         * @param surface true if encoded position is surface position
         * @param timestamp timestamp when this position was received in milliseconds
         */
        public CPREncodedPosition(boolean is_odd, int encoded_lat, int encoded_lon, int nbits, boolean surface, long timestamp) {
            this.is_odd = is_odd;
            this.encoded_lat = encoded_lat;
            this.encoded_lon = encoded_lon;
            this.nbits = nbits;
            this.surface = surface;
            this.timestamp = timestamp;
        }

        /**
         * @return timestamp of this position message in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * @return true if message was odd format
         */
        public boolean isOddFormat() {
            return is_odd;
        }

        /**
         * This method decodes this position using the global or local CPR decoding. If possible, runs a couple of
         * reasonableness tests.
         *
         * @param other the other CPR encoded position in complementary format (even/odd). Also surface positions can
         *              only be combined with other surface positions. Use null for local decoding only.Long
         * @param reference reference point for plausibility, surface and local decoding. Must be within 175 NM of the
         *                  true airborne position or within 42 NM for surface. Use null for global decoding only.
         * @return the decoded position or null if could not be decoded
         */
        public Position decodePosition(CPREncodedPosition other, Position reference) {
            // can we apply global decoding?
            boolean global = other != null && // need other pos for global decoding
                    this.is_odd != other.is_odd && // other pos must be complementary format
                    this.surface == other.surface && // cannot combine surface and airborne
                    (!this.surface || reference != null) && // we need reference position for surface positions
                    (this.surface ||  Math.abs(this.timestamp - other.timestamp) < 10_000L) && // airborne should not be more than 10 seconds apart
                    (!this.surface ||  Math.abs(this.timestamp - other.timestamp) < 25_000L); // surface should not be more than 25 seconds apart

            // can we apply local decoding?
            boolean local = reference != null; // need reference position for local decoding

            Position globalPos = null;
            // apply global decoding
            if (global) globalPos = decodeGlobalPosition(this, other, reference);

            Position localPos = null;
            // apply local decoding
            if (local) localPos = decodeLocalPosition(this, reference);

            //////// Reasonableness Test //////////
            // see A.1.7.10.2 of DO-260B

            boolean reasonable = true; // be positive :-)
            double mu = 5.0; // 5 meters is a random small distance

            // check distance between global and local position if possible -> should be almost equal
            if (globalPos != null && localPos != null && globalPos.haversine(localPos) > mu)
                reasonable = false;

            // use local CPR to verify even and odd position
            if (globalPos != null) {
                Position localThis = decodeLocalPosition(this, globalPos);

                // check local/global dist of new message
                if (globalPos.haversine(localThis) > mu)
                    reasonable = false;

                // check if distance to other is within limits
                Position globalOther = decodeGlobalPosition(other, this, reference);
                Position localOther = decodeLocalPosition(other, globalPos);

                // should be within 3 NM (see comment in method's java doc)
                if (globalOther != null && !surface && globalOther.haversine(globalPos) > 5556)
                    reasonable = false;

                if (localOther != null && !surface && localOther.haversine(globalPos) > 5556)
                    reasonable = false;
            }

            // prefer global over local position
            Position ret = global ? globalPos : localPos;

            if (ret != null) {
                // is it a valid coordinate?
                if (Math.abs(ret.getLongitude()) > 180.0 || Math.abs(ret.getLatitude()) > 90.0)
                    reasonable = false;

                ret.setReasonable(reasonable);
            }

            return ret;
        }

        @Override
        public String toString() {
            return "CPREncodedPosition{" +
                    "is_odd=" + is_odd +
                    ", encoded_lat=" + encoded_lat +
                    ", encoded_lon=" + encoded_lon +
                    ", nbits=" + nbits +
                    ", surface=" + surface +
                    ", timestamp=" + timestamp +
                    '}';
        }
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

        if (pos.is_odd == old.is_odd) return null;
        if (pos.surface != old.surface) return null;
        if (pos.surface && reference == null) return null;

        CPREncodedPosition even = pos.is_odd ? old : pos;
        CPREncodedPosition odd = pos.is_odd ? pos : old;

        // Helper for latitude (Number of zones NZ is set to 15)
        double Dlat0 = (even.surface ? 90 : 360.0) / 60.0;
        double Dlat1 = (odd.surface ? 90 : 360.0) / 59.0;

        // latitude index
        double j = Math.floor((
                (59.0 * ((double) even.encoded_lat)) / ((double) (1 << even.nbits)) -
                        (60.0 * ((double) odd.encoded_lat)) / ((double) (1 << odd.nbits))) + 0.5);

        // global latitudes
        double Rlat0 = Dlat0 * (mod(j, 60.0) + ((double) even.encoded_lat) / ((double) (1 << even.nbits)));
        double Rlat1 = Dlat1 * (mod(j, 59.0) + ((double) odd.encoded_lat) / ((double) (1 << odd.nbits)));

        // Southern hemisphere?
        if (Rlat0 >= 270.0 && Rlat0 <= 360.0) Rlat0 -= 360.0;
        if (Rlat1 >= 270.0 && Rlat1 <= 360.0) Rlat1 -= 360.0;

        // ensure that the number of even longitude zones are equal
        if (NL(Rlat0) != NL(Rlat1)) return null; // position straddle

        double Rlat = pos.is_odd ? Rlat1 : Rlat0;

        // Helper for longitude
        double NL_helper = NL(Rlat0);// NL(Rlat0) and NL(Rlat1) are equal

        // longitude index
        double m = Math.floor(
                ((double) even.encoded_lon * (NL_helper - 1.0)) / ((double) (1 << even.nbits)) -
                        ((double) odd.encoded_lon * NL_helper) / ((double) (1 << odd.nbits)) + 0.5);

        // global longitude
        double n_helper = Math.max(1.0, NL_helper - (pos.is_odd ? 1.0 : 0.0));
        double Rlon = (360.0 / n_helper) * (mod(m, n_helper) + ((double) pos.encoded_lon) / ((double) (1 << pos.nbits)));

        // correct longitude
        if (Rlon < -180.0 && Rlon > -360.0) Rlon += 360.0;
        if (Rlon > 180.0 && Rlon < 360.0) Rlon -= 360.0;

        if (pos.surface) {
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
        double angle = pos.surface ? 90.0 : 360.0;
        double Dlat = pos.is_odd ? angle / 59.0 : angle / 60.0;

        // latitude zone index
        double j = Math.floor(ref.getLatitude() / Dlat) + Math.floor(
                0.5 + mod(ref.getLatitude(), Dlat) / Dlat - ((double) pos.encoded_lat) / ((double) (1 << pos.nbits)));

        // decoded position latitude
        double Rlat = Dlat * (j + ((double) pos.encoded_lat) / ((double) (1 << pos.nbits)));

        // longitude zone size
        double Dlon = angle / Math.max(1.0, NL(Rlat) - (pos.is_odd ? 1.0 : 0.0));

        // longitude zone coordinate
        double m = Math.floor(ref.getLongitude() / Dlon) + Math.floor(0.5 + mod(ref.getLongitude(), Dlon) / Dlon
                - ((double) pos.encoded_lon) / ((double) (1 << pos.nbits)));

        // and finally the longitude
        double Rlon = Dlon * (m + ((double) pos.encoded_lon) / ((double) (1 << pos.nbits)));

        return new Position(Rlon, Rlat, null);
    }

    /**
     * Stateful decoder for positions. Use this one to decode positions.
     */
    public static class StatefulPositionDecoder {
        private CompactPositionReporting.CPREncodedPosition last_even_airborne;
        private CompactPositionReporting.CPREncodedPosition last_odd_airborne;
        private Position last_pos; // lat lon
        private long last_time; // in ms
        private int num_reasonable; // number of successive reasonable msgs

        // distance to receiver threshold
        private static final int MAX_DIST_TO_SENDER = 700000; // 700km

        /**
         * @param cpr CPR encoded position
         * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in feet. altitude might be null
         *         if unavailable. On error, the returned position is null. Check the .isReasonable() flag before using
         *         the position.
         */
        public Position decodePosition(CompactPositionReporting.CPREncodedPosition cpr) {
            if (cpr == null) return null;

            // get last position in complementary format for global decoding
            CompactPositionReporting.CPREncodedPosition last_other =
                    cpr.isOddFormat() ? last_even_airborne : last_odd_airborne;

            // store position message for global decoding
            if (cpr.isOddFormat()) last_odd_airborne = cpr;
            else last_even_airborne = cpr;

            Position new_pos = cpr.decodePosition(last_other, last_pos);

            if (new_pos == null) return null;

            //////// apply some additional (stateful) reasonableness tests //////////

            // check if it's realistic that the target covered this distance (faster than 1000 knots?)
            if (last_pos != null) {
                double td = abs((cpr.getTimestamp() - last_time) / 1_000.);
                double groundSpeed = new_pos.haversine(last_pos) / td; // in meters per second

                if (groundSpeed > 514.4) new_pos.setReasonable(false);
            }

            last_pos = new_pos;
            last_time = cpr.getTimestamp();

            if (!new_pos.isReasonable()) num_reasonable = 0; // reset
                // at least n good msgs before we declare reasonable
            else if (num_reasonable++<2) new_pos.setReasonable(false);

            return new_pos;
        }

        /**
         * @param cpr CPR encoded position
         * @param receiver position of the receiver to check if received position was more than 700km away
         * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in meters. altitude might be null if unavailable
         *         On error, the returned position is null. Check the .isReasonable() flag before using the position.
         */
        public Position decodePosition(CompactPositionReporting.CPREncodedPosition cpr, Position receiver) {
            Position ret = decodePosition(cpr);

            // apply additional reasonableness test
            if (ret != null && receiver != null && receiver.haversine(ret) > MAX_DIST_TO_SENDER) {
                ret.setReasonable(false);
                num_reasonable = 0;
            }

            return ret;
        }

    }

}
