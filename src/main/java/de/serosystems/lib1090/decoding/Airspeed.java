package de.serosystems.lib1090.decoding;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class Airspeed {

	private Airspeed() {}

	/**
	 * The 95% accuracy for horizontal velocity. We interpret the coding according to
	 * DO-260B Table 2-22 for all ADS-B versions.
	 * @return Navigation Accuracy Category for velocity according to RTCA DO-260B 2.2.3.2.6.1.5 in m/s, -1 means
	 * "unknown" or &gt;10m
	 */
	public static double decodeNACv(byte navigationAccuracyCategory) {
		switch(navigationAccuracyCategory) {
			case 1: return 10;
			case 2: return 3;
			case 3: return 1;
			case 4: return 0.3F;
			default: return -1;
		}
	}
}
