package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.Position;

public interface PositionDecoder {
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
	Position decodePosition(CPREncodedPosition cpr, Position receiver);
}
