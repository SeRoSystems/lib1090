package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

import static java.lang.Math.abs;

/**
 * Stateful decoder for positions. Use this one to decode positions.
 */
public class StatefulPositionDecoder implements PositionDecoder {
	private CPREncodedPosition last_even_airborne;
	private CPREncodedPosition last_odd_airborne;
	private Position last_pos; // lat lon
	private Long last_time; // in ms
	private int num_reasonable; // number of successive reasonable msgs
	private boolean disableSpeedTest = false;

	/**
	 * Default constructor that uses speed test
	 */
	public StatefulPositionDecoder () {}

	/**
	 * Constructor that allows disabling of speed-based reasonableness test. Use this
	 * if you have a network of heterogeneous receivers with variable data delay and
	 * no deduplication.
	 * @param disableSpeedTest true if speed test should not be applied
	 */
	public StatefulPositionDecoder (boolean disableSpeedTest) {
		this.disableSpeedTest = disableSpeedTest;
	}

	// distance to receiver threshold
	private static final int MAX_DIST_TO_SENDER = 700000; // 700km

	/**
	 * @param cpr              CPR encoded position
	 * @param receiver         position of the receiver for surface decoding and to check if received position was more than 700km away;
	 *                         null disables checks and surface decoding
	 * @param disableSpeedTest do not perform speed estimation for reasonableness testing (use this, e.g., when you are merging
	 *                         data from different streams with different delays)
	 * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in feet. altitude might be null
	 * if unavailable. On error, the returned position is null. Check the .isReasonable() flag before using
	 * the position.
	 */
	public Position decodePosition(CPREncodedPosition cpr, Position receiver, boolean disableSpeedTest) {
		if (cpr == null) return null;

		// get last position in complementary format for global decoding
		CPREncodedPosition lastOther =
				cpr.isOddFormat() ? last_even_airborne : last_odd_airborne;

		// store position message for global decoding
		if (cpr.isOddFormat()) last_odd_airborne = cpr;
		else last_even_airborne = cpr;

		// only use receiver as reference for surface positions (might be too far away for airborne)
		Position refPos = last_pos != null ? last_pos : (cpr.isSurface() ? receiver : null);

		Position newPos = cpr.decodePosition(lastOther, refPos);

		if (newPos == null) return null;

		//////// apply some additional (stateful) reasonableness tests //////////

		// check if it's realistic that the target covered this distance (faster than 1000 knots?)
		if (!disableSpeedTest && last_pos != null && last_time != null && cpr.getTimestamp() != null) {
			double td = abs((cpr.getTimestamp() - last_time) / 1_000.);
			double groundSpeed = newPos.haversine(last_pos) / td; // in meters per second

			if (groundSpeed > 514.4) {
				newPos.setReasonable(false);
			}
		}

		last_pos = newPos;
		last_time = cpr.getTimestamp();

		if (!newPos.isReasonable()) num_reasonable = 0; // reset
			// at least n good msgs before we declare reasonable
		else if (num_reasonable++ < 2) newPos.setReasonable(false);

		// apply additional reasonableness test
		if (receiver != null && receiver.haversine(newPos) > MAX_DIST_TO_SENDER) {
			newPos.setReasonable(false);
			num_reasonable = 0;
		}

		return newPos;
	}

	/**
	 * Decodes position with speed estimation-based reasonableness test.
	 *
	 * @param cpr      CPR encoded position
	 * @param receiver position of the receiver for surface decoding and to check if received position was more than 700km away;
	 *                 null disables checks and surface decoding
	 * @return WGS84 coordinates with latitude and longitude in dec degrees, and altitude in feet. altitude might be null
	 * if unavailable. On error, the returned position is null. Check the .isReasonable() flag before using
	 * the position.
	 */
	@Override
	public Position decodePosition(CPREncodedPosition cpr, Position receiver) {
		return decodePosition(cpr, receiver, disableSpeedTest);
	}

}
