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

package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * CPR encoded position with decoding functions.
 */
public final class CPREncodedPosition implements Serializable {

    private static final long serialVersionUID = 5081730583999422576L;

    /**
     * Number of bits in {@link #yz} and {@link #xz}.
     */
    private final int nBits;

    /**
     * Whether this encoded position originates from an odd message.
     */
    private final boolean isOdd;

    /**
     * Whether this encoded position originates from a surface position message
     */
    private final boolean isSurface;

    /**
     * The longest time this position may be apart from one of the other format to be decoded globally
     * with it, see {@link #maxGap(CPREncodedPosition)}.
     */
    private final Duration pairingWindow;

    /**
     * Y coordinate within CPR Zone.
     */
    private final int yz;

    /**
     * X coordinate within CPR Zone.
     */
    private final int xz;

    /**
     * Timestamp of position message.
     */
    private final Instant timestamp;

    /**
     * Scaling factor for encoded values
     */
    private final double scale;

    /**
     * New CPR Encoded Position.
     *
     * @param nBits         number of bits for encoded latitude and longitude. Must be 12, 14, or 17
     * @param isOdd         whether this encoded position originates from an odd position message
     * @param isSurface     whether this encoded position originates from a surface position message
     * @param pairingWindow the longest time this position may be apart from one of the other format for global
     *                      decoding, must not be null
     * @param yz            Y coordinate within CPR zone, i.e. encoded latitude as in position message
     * @param xz            X coordinate within CPR zone, i.e. encoded longitude as in position message
     * @param timestamp     timestamp of position message, must not be null
     */
    private CPREncodedPosition(int nBits,
                               boolean isOdd,
                               boolean isSurface,
                               Duration pairingWindow,
                               int yz,
                               int xz,
                               Instant timestamp) {
        if (nBits != 12 && nBits != 14 && nBits != 17)
            throw new IllegalArgumentException("Unexpected number of bits");
        this.nBits = nBits;
        this.isOdd = isOdd;
        this.isSurface = isSurface;
        this.pairingWindow = Objects.requireNonNull(pairingWindow, "pairingWindow");
        this.yz = yz;
        this.xz = xz;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");

        scale = 1L << nBits;
    }

    /**
     * New CPR Encoded Position for an airborne position message. It is decoded globally only with one of the other
     * format received within 10 seconds, ED-102B §2.2.10.3.1.
     *
     * @param nBits     number of bits for encoded latitude and longitude. Must be 12, 14, or 17
     * @param isOdd     whether this encoded position originates from an odd position message
     * @param yz        Y coordinate within CPR zone, i.e. encoded latitude as in position message
     * @param xz        X coordinate within CPR zone, i.e. encoded longitude as in position message
     * @param timestamp timestamp of position message
     * @return CPR encoded position
     */
    public static CPREncodedPosition ofAirborne(int nBits,
                                                boolean isOdd,
                                                int yz,
                                                int xz,
                                                Instant timestamp) {
        return new CPREncodedPosition(nBits, isOdd, false, Duration.ofSeconds(10), yz, xz, timestamp);
    }

    /**
     * New CPR Encoded Position for an ADS-B surface position message. It is decoded globally only with one of the
     * other format received within 25 seconds, or within 50 seconds if the ground speed is at most 25 knots,
     * ED-102B §2.2.10.3.2.
     *
     * @param nBits              number of bits for encoded latitude and longitude. Must be 12, 14, or 17
     * @param isOdd              whether this encoded position originates from an odd position message
     * @param isHighSurfaceSpeed whether the corresponding surface position message indicated a high or unknown speed
     * @param yz                 Y coordinate within CPR zone, i.e. encoded latitude as in position message
     * @param xz                 X coordinate within CPR zone, i.e. encoded longitude as in position message
     * @param timestamp          timestamp of position message
     * @return CPR encoded position
     */
    public static CPREncodedPosition ofSurface(int nBits,
                                               boolean isOdd,
                                               boolean isHighSurfaceSpeed,
                                               int yz,
                                               int xz,
                                               Instant timestamp) {
        return ofSurface(nBits, isOdd, Duration.ofSeconds(isHighSurfaceSpeed ? 25 : 50), yz, xz, timestamp);
    }

    /**
     * New CPR Encoded Position for a surface position message whose source has its own pairing rule, e.g. TIS-B,
     * whose even and odd messages must be received within 10 seconds, ED-102B §2.2.17.4.2.
     *
     * @param nBits         number of bits for encoded latitude and longitude. Must be 12, 14, or 17
     * @param isOdd         whether this encoded position originates from an odd position message
     * @param pairingWindow the longest time this position may be apart from one of the other format for global
     *                      decoding, must not be null
     * @param yz            Y coordinate within CPR zone, i.e. encoded latitude as in position message
     * @param xz            X coordinate within CPR zone, i.e. encoded longitude as in position message
     * @param timestamp     timestamp of position message
     * @return CPR encoded position
     */
    public static CPREncodedPosition ofSurface(int nBits,
                                               boolean isOdd,
                                               Duration pairingWindow,
                                               int yz,
                                               int xz,
                                               Instant timestamp) {
        return new CPREncodedPosition(nBits, isOdd, true, pairingWindow, yz, xz, timestamp);
    }

    public int getNBits() {
        return nBits;
    }

    public boolean isOddFormat() {
        return isOdd;
    }

    public boolean isSurface() {
        return isSurface;
    }

    public int yz() {
        return yz;
    }

    public int xz() {
        return xz;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Test if both CPR encoded coordinates are all zeros.
     * This is meant to be used to implement ED-102B §2.2.10.3.1.
     *
     * @return true if XZ and YZ coordinates are both all zeros, false otherwise
     */
    public boolean allZero() {
        return yz == 0 && xz == 0;
    }

    /**
     * Test if two CPR positions are close by checking if the absolute difference in each coordinate is below 1000.
     * This only works under certain conditions and <b>shall only be used to implement ED-102B §2.2.10.3.1</b>.
     *
     * @param other other CPR encoded position (of same format), must not be null
     * @return true if the encoded difference in XZ and YZ is below 1000, false otherwise
     * @throws IllegalArgumentException if any sanity check fails
     */
    public boolean isClose(CPREncodedPosition other) {
        Objects.requireNonNull(other, "other must not be null");
        if (other.nBits != nBits) throw new IllegalArgumentException("Number of bits must match");
        if (isOdd != other.isOdd) throw new IllegalArgumentException("Messages must be of same CPR format");
        if (isSurface != other.isSurface) throw new IllegalArgumentException("Airborne/Surface format must match");
        return Math.abs(yz - other.yz) < 1000 && Math.abs(xz - other.xz) < 1000;
    }

    /**
     * Get maximum time gap between messages for global decoding: the shorter of the two pairing windows the
     * positions were created with. See ED-102B §2.2.10.3.1 and ED-102B §2.2.10.3.2 for ADS-B, and ED-102B
     * §2.2.17.4.2 for TIS-B.
     * For airborne position messages, it may be used, but it is stricter than the standard, see ED-102B §2.2.10.3.1.
     *
     * @param other other message, must not be null
     * @return maximum duration between messages
     */
    public Duration maxGap(CPREncodedPosition other) {
        Objects.requireNonNull(other, "other must not be null");
        return pairingWindow.compareTo(other.pairingWindow) <= 0 ? pairingWindow : other.pairingWindow;
    }

    /**
     * Reconstruct zone index.
     *
     * @param zones number of even zones
     * @param even  CPR coordinate (xz or yz) of even message
     * @param odd   CPR coordinate (xz or yz) of odd message
     * @return reconstructed zone index
     */
    private int zoneIndex(int zones, int even, int odd) {
        int halfScale = 1 << (nBits - 1);
        return (zones * (even - odd) - even + halfScale) >> nBits;
    }

    /**
     * Compact Position Reporting: Global decoding with check on timestamps.
     *
     * @param other     position message of the other format (even/odd). Note that the time between those message must not exceed {@link #maxGap(CPREncodedPosition)}
     * @param reference reference (e.g. receiver's) position to determine the correct surface position; use arbitrary (or null) for airborne (will be ignored)
     * @return globally unambiguously decoded position or null if the two encoded positions cannot be combined or if the position is otherwise unavailable or invalid
     * @see #decodeGlobal(CPREncodedPosition, Position, boolean) with {@code timeCheck=true}
     */
    public Position decodeGlobal(CPREncodedPosition other, Position reference) {
        return decodeGlobal(other, reference, true);
    }

    /**
     * Compact Position Reporting: Global decoding.
     * Can only be used if another position report with a different format (even/odd) is available.
     * This and the {@code other} position must fulfill the following constraints:
     * <ul>
     *     <li>Number of bits must match</li>
     *     <li>Exactly one must be even and exactly one must be odd</li>
     *     <li>Both must have the same format (airborne or surface)</li>
     *     <li>If the {@code timeCheck} is enabled, their absolute time difference must be within {@link #maxGap(CPREncodedPosition)}</li>
     * </ul>
     * Apart from those sanity checks, global CPR can fail if
     * <ul>
     *     <li>The two positions cannot be combined (because they are close to a latitude zone boundary)</li>
     *     <li>One of the messages encodes an invalid position, e.g. {@code |latitude| > 90}</li>
     * </ul>
     *
     * @param other     position message of the other format (even/odd)
     * @param reference reference (e.g. receiver's) position to determine the correct surface position; use arbitrary (or null) for airborne (will be ignored)
     * @param timeCheck if true, the timestamps between the two positions will be checked according to {@link #maxGap(CPREncodedPosition)}; see also that function for notes
     * @return globally unambiguously decoded position or null if the two encoded positions cannot be combined or if the position is otherwise unavailable or invalid
     * @throws IllegalArgumentException if any sanity check fails
     */
    public Position decodeGlobal(CPREncodedPosition other, Position reference, boolean timeCheck) {
        Position[] pair = decodeGlobalPair(other, reference, timeCheck);
        return pair == null ? null : pair[0];
    }

    /**
     * Global decoding as {@link #decodeGlobal(CPREncodedPosition, Position, boolean)}, of both messages of the pair:
     * the zone indices and latitudes it computes are those of both.
     *
     * @return the position of this message and of the other one, or null as for
     * {@link #decodeGlobal(CPREncodedPosition, Position, boolean)}
     * @throws IllegalArgumentException as {@link #decodeGlobal(CPREncodedPosition, Position, boolean)}
     */
    private Position[] decodeGlobalPair(CPREncodedPosition other, Position reference, boolean timeCheck) {
        /* early sanity checks */
        Objects.requireNonNull(other, "other must not be null");
        if (other.nBits != nBits) throw new IllegalArgumentException("Number of bits must match");
        if (isOdd == other.isOdd) throw new IllegalArgumentException("Even/Odd pair required");
        if (isSurface != other.isSurface) throw new IllegalArgumentException("Airborne/Surface format must match");
        if (isSurface && reference == null)
            throw new IllegalArgumentException("Reference must not be null for surface positions");
        if (timeCheck) {
            // between the earlier and the later timestamp, so that the gap is never negative: Duration.abs() of a
            // negative duration goes through BigDecimal on Java 8
            Duration gap = timestamp.isAfter(other.timestamp)
                    ? Duration.between(other.timestamp, timestamp)
                    : Duration.between(timestamp, other.timestamp);
            if (gap.compareTo(maxGap(other)) > 0) return null;
        }

        final CPREncodedPosition even = isOdd ? other : this;
        final CPREncodedPosition odd = isOdd ? this : other;

        final double angle = isSurface ? 90. : 360.;

        // latitude index
        int j = zoneIndex(60, even.yz, odd.yz);

        // global latitudes
        final double refLat = reference == null ? 0. : reference.getLatitude();
        final L0Latitude RlatEven = L0Latitude.ofGlobal(even, j, refLat);
        final L0Latitude RlatOdd = L0Latitude.ofGlobal(odd, j, refLat);

        // additional check against invalid latitudes
        if (!RlatEven.isValid() || !RlatOdd.isValid())
            return null;

        // require that the number of longitude zones are equal
        final int nLon = RlatEven.NL();
        if (nLon != RlatOdd.NL()) // straddling position, cannot decode this pair
            return null;

        // longitude index
        final int m = nLon != 1 ? zoneIndex(nLon, even.xz, odd.xz) : 0;

        // reconstruct latitude and longitude of both
        return new Position[]{
                new Position(reconstructGlobalLongitude(angle, nLon, m, reference),
                        (isOdd ? RlatOdd : RlatEven).toDegrees(), 0.),
                new Position(other.reconstructGlobalLongitude(angle, nLon, m, reference),
                        (isOdd ? RlatEven : RlatOdd).toDegrees(), 0.)
        };
    }

    /**
     * Reconstruct the longitude of this message in a global decode.
     *
     * @param angle     full range angle
     * @param nLon      number of longitude zones of the pair
     * @param m         longitude index, ignored if there is only one longitude zone
     * @param reference reference position, which determines the quadrant of a surface position
     * @return the longitude [°] in [-180, 180)
     */
    private double reconstructGlobalLongitude(double angle, int nLon, int m, Position reference) {
        double Rlon;
        if (nLon != 1) {
            // global longitude
            int ni = nLon - (isOdd ? 1 : 0);
            Rlon = reconstructGlobal(angle, ni, m, xz);
        } else {
            Rlon = angle * (xz / scale);
        }

        if (isSurface) {
            double delta = normalize(reference.getLongitude() - Rlon);
            int k = (int) Math.round(delta / 90.);
            return normalize(Rlon + k * 90);
        } else {
            return normalize(Rlon);
        }
    }

    /**
     * Normalize angle to [-180, 180).
     *
     * @param phi angle in degrees
     * @return normalized angle
     */
    private static double normalize(double phi) {
        return phi - 360.0 * Math.floor((phi + 180.0) / 360.0);
    }

    /**
     * Compact Position Reporting: Local decoding.
     * <br>
     * This function uses a locally unambiguous decoding for airborne position messages.
     * It uses a reference position known to be within 180NM (airborne) resp. within 45NM (surface) the target's true position.
     * This reference position may be a previously decoded position that has been confirmed by global decoding, see
     * {@link #decodeGlobal(CPREncodedPosition, Position)}.
     * <br>
     * Note that the returned position can still be invalid, e.g. it is possible to construct latitudes that are not within [-90,90]°.
     *
     * @param reference reference position
     * @return decoded position
     */
    public Position decodeLocal(Position reference) {
        if (reference == null)
            return null;

        // latitude/longitude zone size
        final double angle = isSurface ? 90. : 360.;

        // decode position latitude
        final L0Latitude RlatL = L0Latitude.ofLocal(this, reference.getLatitude());
        final double Rlat = RlatL.toDegrees();

        // number of longitude zones
        int nLon = Math.max(1, RlatL.NL() - (isOdd ? 1 : 0));

        // decode position longitude
        double Rlon = normalize(reconstructLocal(angle, nLon, reference.getLongitude(), xz));

        return new Position(Rlon, Rlat, 0.);
    }

    /**
     * Reconstruct latitude resp. longitude from an CPR encoded number and a reference position.
     *
     * @param angle      full range angle
     * @param zones      number of zones
     * @param ref        reference latitude resp. longitude
     * @param coordinate CPR coordinate (xz or yz)
     * @return reconstructed latitude resp. longitude
     */
    private double reconstructLocal(double angle, int zones, double ref, int coordinate) {
        final double D = angle / zones;
        final double scaled = coordinate / scale;
        final double zone = Math.floor(0.5 + ref / D - scaled);
        return D * (zone + scaled);
    }

    /**
     * Reconstruct latitude resp. longitude from an CPR encoded number its zone index.
     *
     * @param angle      full range angle
     * @param zones      number of zones
     * @param zone       zone index
     * @param coordinate CPR coordinate (xz or yz)
     * @return reconstructed latitude resp. longitude
     */
    private double reconstructGlobal(double angle, int zones, int zone, int coordinate) {
        return angle / zones * (Util.mod(zone, zones) + coordinate / scale);
    }

    /**
     * This method decodes this position using the global or local CPR decoding. If possible, runs a couple of
     * reasonableness tests.
     * Those tests are the library's own set of tests. They are inspired by ED-102B §A.1.7.10.2 but deviate from the
     * rules given there.
     * The function also doesn't help on target acquisition based on ED-102B §2.2.10.3.
     *
     * @param other     the other CPR encoded position in complementary format (even/odd). Also surface positions can
     *                  only be combined with other surface positions. Use null for local decoding only.
     * @param reference reference point for plausibility, surface and local decoding. Must be within 175 NM of the
     *                  true airborne position or within 42 NM for surface. Use null for global decoding only; a
     *                  surface position cannot be decoded without a reference, so null yields null for it.
     * @return the decoded position or null if could not be decoded
     */
    public Position decodePosition(CPREncodedPosition other, Position reference) {
        // apply global decoding; a surface position is ambiguous by a 90° quadrant without a reference
        Position[] pair = other == null || (isSurface && reference == null) ? null :
                decodeGlobalPair(other, reference, true);
        Position globalPos = pair == null ? null : pair[0];

        // apply local decoding
        Position localPos = reference != null ? decodeLocal(reference) : null;

        //////// Reasonableness Test //////////
        // see ED-102B §A.1.7.10.2

        boolean reasonable = true; // be positive :-)
        double mu = 5.0; // 5 meters is a random small distance

        // check distance between global and local position if possible -> should be almost equal
        if (globalPos != null && localPos != null && globalPos.haversine(localPos) > mu)
            reasonable = false;

        // check that the even and odd position belong together: within 3 NM (= 555.6 m/s * 10 seconds)
        if (pair != null && !isSurface && pair[1].haversine(globalPos) > 5556)
            reasonable = false;

        // prefer global over local position
        Position ret = globalPos != null ? globalPos : localPos;

        if (ret != null) {
            // is it a valid coordinate?
            if (Math.abs(ret.getLongitude()) > 180.0 || Math.abs(ret.getLatitude()) > 90.0)
                reasonable = false;

            ret.setReasonable(reasonable);
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final CPREncodedPosition that = (CPREncodedPosition) obj;
        return this.nBits == that.nBits &&
                this.isOdd == that.isOdd &&
                this.isSurface == that.isSurface &&
                this.pairingWindow.equals(that.pairingWindow) &&
                this.yz == that.yz &&
                this.xz == that.xz &&
                this.timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nBits, isOdd, isSurface, pairingWindow, yz, xz, timestamp);
    }

    @Override
    public String toString() {
        return "CPREncodedPosition[" +
                "nBits=" + nBits + ", " +
                "isOdd=" + isOdd + ", " +
                "isSurface=" + isSurface + ", " +
                "pairingWindow=" + pairingWindow + ", " +
                "yz=" + yz + ", " +
                "xz=" + xz + ", " +
                "timestamp=" + timestamp + ']';
    }
}
