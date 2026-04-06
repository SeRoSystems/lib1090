package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.AirborneVelocity;

/**
 * Common API for ADS-B airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMessage {

	/**
	 * @return true if the aircraft indicates an intent to change altitude or a similar flight status change
	 */
	boolean hasChangeIntent();

	/**
	 * Note: only defined for ADS-B version 0 and 1.
	 * @return true if the aircraft reports IFR capability
	 */
	boolean hasIFRCapability();

	/**
	 * @return the raw encoded Navigation Accuracy Category for velocity
	 */
	byte getNACv();

	/**
	 * @return the interpreted 95% horizontal velocity accuracy in m/s, or -1 if unknown or greater than 10m/s
	 */
	default float getAccuracyBound() {
		return AirborneVelocity.decodeAccuracyBound(getNACv());
	}

	/**
	 * @return whether the vertical rate field is available
	 */
	boolean hasVerticalRateInfo();

	/**
	 * @return whether the reported vertical speed is barometric
	 */
	boolean isBarometricVerticalSpeed();

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
