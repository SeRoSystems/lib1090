package de.serosystems.lib1090.msgs.tisb;

import de.serosystems.lib1090.decoding.AirborneVelocity;

/**
 * Common API for TIS-B airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMessage {

	/**
	 * @return the ICAO Mode A Flag used for address type determination
	 */
	boolean getIMF();

	/**
	 * @return whether the vertical rate field is available
	 */
	boolean hasVerticalRateInfo();

	/**
	 * @return the raw encoded Navigation Accuracy Category for velocity, or {@code null} if unavailable
	 */
	Byte getNACv();

	/**
	 * @return the interpreted 95% horizontal velocity accuracy in m/s, or {@code null} if unavailable
	 */
	default Float getAccuracyBound() {
		Byte nacv = getNACv();
		if (nacv == null) {
			return null;
		}
		return AirborneVelocity.decodeAccuracyBound(nacv);
	}

	/**
	 * @return the vertical rate in feet/min, or {@code null} if unavailable
	 */
	Integer getVerticalRate();

	/**
	 * @return whether the geometric minus barometric altitude difference is available
	 */
	boolean hasGeoMinusBaroInfo();

	/**
	 * @return the geometric minus barometric altitude difference in feet, or {@code null} if unavailable
	 */
	Integer getGeoMinusBaro();
}
