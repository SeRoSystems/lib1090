package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

import java.util.Objects;

/**
 * CPR encoded position with decoding functions.
 */
public final class CPREncodedPosition {
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
	 * Whether the corresponding surface position message indicated a high or unknown speed.
	 * False (and not applicable) if {@link #isSurface} is false.
	 */
	private final boolean isHighSurfaceSpeed;

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
	private final long timestamp;

	/**
	 * Scaling factor for encoded values
	 */
	private final double scale;

	/**
	 * New CPR Encoded Position.
	 *
	 * @param nBits              number of bits for encoded latitude and longitude. Must be 12, 14, or 17
	 * @param isOdd              whether this encoded position originates from an odd position message
	 * @param isSurface          whether this encoded position originates from a surface position message
	 * @param isHighSurfaceSpeed whether the corresponding surface position message indicated a high or unknown speed. Can be arbitrary if isSurface is false.
	 * @param yz                 Y coordinate within CPR zone, i.e. encoded latitude as in position message
	 * @param xz                 X coordinate within CPR zone, i.e. encoded longitude as in position message
	 * @param timestamp          timestamp of position message
	 */
	private CPREncodedPosition(int nBits,
	                          boolean isOdd,
	                          boolean isSurface,
	                          boolean isHighSurfaceSpeed,
	                          int yz,
	                          int xz,
	                          long timestamp) {
		if (nBits != 12 && nBits != 14 && nBits != 17)
			throw new IllegalArgumentException("Unexpected number of bits");
		this.nBits = nBits;
		this.isOdd = isOdd;
		this.isSurface = isSurface;
		// note: setting to this to false if !isSurface makes equals/hashCode easier
		this.isHighSurfaceSpeed = isSurface && isHighSurfaceSpeed;
		this.yz = yz;
		this.xz = xz;
		this.timestamp = timestamp;

		scale = 1L << nBits;
	}

	/**
	 * New CPR Encoded Position for an airborne position message.
	 *
	 * @param nBits number of bits for encoded latitude and longitude. Must be 12, 14, or 17
	 * @param isOdd whether this encoded position originates from an odd position message
	 * @param yz Y coordinate within CPR zone, i.e. encoded latitude as in position message
	 * @param xz X coordinate within CPR zone, i.e. encoded longitude as in position message
	 * @param timestamp timestamp of position message
	 * @return CPR encoded position
	 */
	public static CPREncodedPosition ofAirborne(int nBits,
	                                            boolean isOdd,
	                                            int yz,
	                                            int xz,
	                                            long timestamp) {
		return new CPREncodedPosition(nBits, isOdd, false, false, yz, xz, timestamp);
	}

	/**
	 * New CPR Encoded Position for a surface position message.
	 *
	 * @param nBits number of bits for encoded latitude and longitude. Must be 12, 14, or 17
	 * @param isOdd whether this encoded position originates from an odd position message
	 * @param isHighSurfaceSpeed whether the corresponding surface position message indicated a high or unknown speed. Can be arbitrary if isSurface is false.
	 * @param yz Y coordinate within CPR zone, i.e. encoded latitude as in position message
	 * @param xz X coordinate within CPR zone, i.e. encoded longitude as in position message
	 * @param timestamp timestamp of position message
	 * @return CPR encoded position
	 */
	public static CPREncodedPosition ofSurface(int nBits,
	                                           boolean isOdd,
	                                           boolean isHighSurfaceSpeed,
	                                           int yz,
	                                           int xz,
	                                           long timestamp) {
		return new CPREncodedPosition(nBits, isOdd, true, isHighSurfaceSpeed, yz, xz, timestamp);
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

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Get maximum time gap between messages, based on their type.
	 * This is only applicable if messages are of different CPR format (even/odd).
	 *
	 * @param other other message, see constraints above
	 * @return maximum duration [ms] between messages
	 */
	public long maxGap(CPREncodedPosition other) {
		if (isSurface && other.isSurface) {
			if (isHighSurfaceSpeed || other.isHighSurfaceSpeed)
				return 25_000L;
			else
				return 50_000L;
		} else {
			return 10_000L;
		}
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
	 * Compact Position Reporting: Global decoding.
	 * Can only be used if another position report with a different format (even/odd) is available.
	 *
	 * @param other     position message of the other format (even/odd). Note that the time between those message must not exceed {@link #maxGap(CPREncodedPosition)}
	 * @param reference reference (e.g. receiver's) position to determine the correct surface position; use arbitrary (or null) for airborne (will be ignored)
	 * @return globally unambiguously decoded position or empty if the two encoded positions cannot be combined or if the position is otherwise unavailable or invalid
	 */
	public Position decodeGlobal(CPREncodedPosition other, Position reference) {
		/* early sanity checks */
		if (other.nBits != nBits) return null;
		if (isOdd == other.isOdd) return null;
		if (isSurface != other.isSurface) return null;
		if (isSurface && reference == null) return null;
		long gap = Math.abs(timestamp - other.timestamp);
		if (gap > maxGap(other)) return null;

		final CPREncodedPosition even = isOdd ? other : this;
		final CPREncodedPosition odd = isOdd ? this : other;

		final double angle = isSurface ? 90. : 360.;

		// latitude index
		int j = zoneIndex(60, even.yz, odd.yz);

		// global latitudes
		final double refLat = reference == null ? 0. : reference.getLatitude();
		final L0Latitude Rlat0L = L0Latitude.ofGlobal(even, j, refLat);
		final L0Latitude Rlat1L = L0Latitude.ofGlobal(odd, j, refLat);

		// additional check against invalid latitudes
		if (!Rlat0L.isValid() || !Rlat1L.isValid())
			return null;

		// require that the number of longitude zones are equal
		final int nLon = Rlat0L.NL();
		if (nLon != Rlat1L.NL()) return null; // straddling position

		// reconstruct latitude
		final double Rlat = isOdd ? Rlat1L.toDegrees() : Rlat0L.toDegrees();

		// reconstruct longitude
		double Rlon;
		if (nLon != 1) {
			// longitude index
			int m = zoneIndex(nLon, even.xz, odd.xz);
			// global longitude
			int n_helper = nLon - (isOdd ? 1 : 0);
			Rlon = reconstructGlobal(angle, n_helper, m, xz);
		} else {
			Rlon = angle * (xz / scale);
		}

		if (isSurface) {
			double delta = normalize(reference.getLongitude() - Rlon);
			int k = (int) Math.round(delta / 90.);
			Rlon = normalize(Rlon + k * 90);
		} else {
			Rlon = normalize(Rlon);
		}

		return new Position(Rlon, Rlat, 0.);
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
	Position decodeLocal(Position reference) {
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
		double Rlon = reconstructLocal(angle, nLon, reference.getLongitude(), xz);

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
	 *
	 * @param other     the other CPR encoded position in complementary format (even/odd). Also surface positions can
	 *                  only be combined with other surface positions. Use null for local decoding only.
	 * @param reference reference point for plausibility, surface and local decoding. Must be within 175 NM of the
	 *                  true airborne position or within 42 NM for surface. Use null for global decoding only.
	 * @return the decoded position or null if could not be decoded
	 */
	public Position decodePosition(CPREncodedPosition other, Position reference) {
		// apply global decoding
		Position globalPos = other == null ? null : decodeGlobal(other, reference);

		// apply local decoding
		Position localPos = reference != null ? decodeLocal(reference) : null;

		//////// Reasonableness Test //////////
		// see A.1.7.10.2 of DO-260B

		boolean reasonable = true; // be positive :-)
		double mu = 5.0; // 5 meters is a random small distance

		// check distance between global and local position if possible -> should be almost equal
		if (globalPos != null && localPos != null && globalPos.haversine(localPos) > mu)
			reasonable = false;

		// use local CPR to verify even and odd position
		if (globalPos != null) {
			Position localThis = decodeLocal(globalPos);

			// check local/global dist of new message
			if (globalPos.haversine(localThis) > mu)
				reasonable = false;

			// check if distance to other is within limits
			Position globalOther = other.decodeGlobal(this, reference);
			Position localOther = other.decodeLocal(globalPos);

			// should be within 3 NM (= 555.6 m/s * 10 seconds)
			if (globalOther != null && !isSurface && globalOther.haversine(globalPos) > 5556)
				reasonable = false;

			if (localOther != null && !isSurface && localOther.haversine(globalPos) > 5556)
				reasonable = false;
		}

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
				this.isHighSurfaceSpeed == that.isHighSurfaceSpeed &&
				this.yz == that.yz &&
				this.xz == that.xz &&
				this.timestamp == that.timestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nBits, isOdd, isSurface, isHighSurfaceSpeed, yz, xz, timestamp);
	}

	@Override
	public String toString() {
		return "CPREncodedPosition[" +
				"nBits=" + nBits + ", " +
				"isOdd=" + isOdd + ", " +
				"isSurface=" + isSurface + ", " +
				"isHighSurfaceSpeed=" + isHighSurfaceSpeed + ", " +
				"yz=" + yz + ", " +
				"xz=" + xz + ", " +
				"timestamp=" + timestamp + ']';
	}
}
