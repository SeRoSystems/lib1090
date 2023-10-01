package de.serosystems.lib1090.decoding;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class OperationalStatus {

	private OperationalStatus() {}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value, see table A-13 in RCTA DO-260B
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public static double nacPtoEPU(byte nacPos) {
		switch (nacPos) {
			case 1: return 18520;
			case 2: return 7408;
			case 3: return 3704;
			case 4: return 1852.0;
			case 5: return 926.0;
			case 6: return 555.6;
			case 7: return 185.2;
			case 8: return 92.6;
			case 9: return 30.0;
			case 10: return 10.0;
			case 11: return 3.0;
			default: return -1;
		}
	}
}
