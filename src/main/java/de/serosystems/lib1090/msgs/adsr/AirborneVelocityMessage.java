package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.decoding.AirborneVelocity;

/**
 * Common API for ADS-R airborne velocity messages across message subtypes.
 */
public interface AirborneVelocityMessage {

	/**
	 * @return the ICAO Mode A Flag used for address type determination
	 */
	boolean getIMF();

	/**
	 * Note: only defined for ADS-R version 0 and 1.
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
