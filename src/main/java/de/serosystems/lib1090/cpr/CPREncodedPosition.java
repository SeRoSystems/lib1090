package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

@SuppressWarnings("unused")
public class CPREncodedPosition {

	private final boolean is_odd;
	private final int encoded_lat;
	private final int encoded_lon;
	private final int nbits;
	private final boolean surface;
	private final Long timestamp;

	/**
	 * @param is_odd      true if it is a odd format, false if it is even (format field in most position messags)
	 * @param encoded_lat CPR encoded latitude
	 * @param encoded_lon CPR encoded longitude
	 * @param nbits       number of bits used to encode latitude and longitude; 17 for airborne position, 14 for intent,
	 *                    and 12 for TIS-B
	 * @param surface     true if encoded position is surface position
	 * @param timestamp   timestamp when this position was received in milliseconds (null disables all tests based on time)
	 */
	public CPREncodedPosition(boolean is_odd, int encoded_lat, int encoded_lon, int nbits, boolean surface, Long timestamp) {
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
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return true if message was odd format
	 */
	public boolean isOddFormat() {
		return is_odd;
	}

	public int getEncodedLat() {
		return encoded_lat;
	}

	public int getEncodedLon() {
		return encoded_lon;
	}

	public boolean isSurface() {
		return surface;
	}

	public int getNumBits() {
		return nbits;
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
		// can we apply global decoding?
		boolean global = other != null && // need other pos for global decoding
				this.is_odd != other.is_odd && // other pos must be complementary format
				this.surface == other.surface && // cannot combine surface and airborne
				(!this.surface || reference != null); // we need reference position for surface positions

		// time-based tests
		global = global && this.timestamp != null && other.timestamp != null &&
				(this.surface || Math.abs(this.timestamp - other.timestamp) < 10_000L) && // airborne should not be more than 10 seconds apart
				(!this.surface || Math.abs(this.timestamp - other.timestamp) < 25_000L); // surface should not be more than 25 seconds apart

		// can we apply local decoding?
		boolean local = reference != null; // need reference position for local decoding

		Position globalPos = null;
		// apply global decoding
		if (global) globalPos = CompactPositionReporting.decodeGlobalPosition(this, other, reference);

		Position localPos = null;
		// apply local decoding
		if (local) localPos = CompactPositionReporting.decodeLocalPosition(this, reference);

		//////// Reasonableness Test //////////
		// see A.1.7.10.2 of DO-260B

		boolean reasonable = true; // be positive :-)
		double mu = 5.0; // 5 meters is a random small distance

		// check distance between global and local position if possible -> should be almost equal
		if (globalPos != null && localPos != null && globalPos.haversine(localPos) > mu)
			reasonable = false;

		// use local CPR to verify even and odd position
		if (globalPos != null) {
			Position localThis = CompactPositionReporting.decodeLocalPosition(this, globalPos);

			// check local/global dist of new message
			if (globalPos.haversine(localThis) > mu)
				reasonable = false;

			// check if distance to other is within limits
			Position globalOther = CompactPositionReporting.decodeGlobalPosition(other, this, reference);
			Position localOther = CompactPositionReporting.decodeLocalPosition(other, globalPos);

			// should be within 3 NM (= 555.6 m/s * 10 seconds)
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
