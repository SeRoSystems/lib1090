package de.serosystems.lib1090.decoding;

import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.adsb.SurfaceOperationalStatusV2Msg;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class SurfacePosition {


	private SurfacePosition() {}

	/**
	 * @return speed resolution (accuracy) in knots or null if ground speed is not available.
	 */
	public static Double groundSpeedResolution(byte movement) {
		double resolution;

		if (movement >= 1 && movement <= 8)
			resolution = 0.125;
		else if (movement >= 9 && movement <= 12)
			resolution = 0.25;
		else if (movement >= 13 && movement <= 38)
			resolution = 0.5;
		else if (movement >= 39 && movement <= 93)
			resolution = 1;
		else if (movement >= 94 && movement <= 108)
			resolution = 2;
		else if (movement >= 109 && movement <= 123)
			resolution = 5;
		else if (movement == 124)
			resolution = 175;
		else
			return null;

		return resolution;
	}

	/**
	 * @return speed in knots or null if ground speed is not available.
	 */
	public static Double groundSpeed(byte movement) {
		double speed;

		if (movement == 1)
			speed = 0;
		else if (movement >= 2 && movement <= 8)
			speed = 0.125+(movement -2)*0.125;
		else if (movement >= 9 && movement <= 12)
			speed = 1+(movement -9)*0.25;
		else if (movement >= 13 && movement <= 38)
			speed = 2+(movement -13)*0.5;
		else if (movement >= 39 && movement <= 93)
			speed = 15+(movement -39);
		else if (movement >= 94 && movement <= 108)
			speed = 70+(movement -94)*2;
		else if (movement >= 109 && movement <= 123)
			speed = 100+(movement -109)*5;
		else if (movement == 124)
			speed = 175;
		else
			return null;

		return speed;
	}

	/**
	 * @return Navigation integrity category. A NIC of 0 means "unkown". Values according to DO-260B Table N-4.
	 */
	public static byte decodeNIC(byte formatTypeCode) {
		return decodeNIC(formatTypeCode, false);
	}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
	 *
	 * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link SurfaceOperationalStatusV1Msg}'s and
	 * {@link SurfaceOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public static double decodeEPU(byte formatTypeCode) {
		switch (formatTypeCode) {
			case 5: return 3;
			case 6: return 10;
			case 7: return 92.6;
			// case 0: case 8: return -1;
			default: return -1;
		}
	}

	/**
	 * The position error, i.e., 95% accuracy for the horizontal position. Values according to DO-260B Table N-4.
	 *
	 *  The horizontal containment radius is also known as "horizontal protection level".
	 *
	 * @return horizontal containment radius limit in meters. A return value of -1 means "unkown".
	 */
	public static double decodeHCR(byte formatTypeCode) {
		switch (formatTypeCode) {
			case 5: return 7.5;
			case 6: return 25;
			case 7: return 185.2;
			// case 0: case 8: return -1;
			default: return -1;
		}
	}

	/**
	 * Values according to DO-260B Table N-11
	 * @return Navigation integrity category. A NIC of 0 means "unkown". If aircraft uses ADS-B version 1+,
	 * set NIC supplement A from Operational Status Message for better precision.
	 */
	public static byte decodeNIC(byte formatTypeCode, boolean nicSupplA) {
		switch (formatTypeCode) {
			case 5: return 11;
			case 6: return 10;
			case 7: return (byte) (nicSupplA ? 9 : 8);
			// case 0: case 8: return 0;
			default: return 0;
		}
	}

	/**
	 * The position error, i.e., 95% accuracy for the horizontal position. For the navigation accuracy category
	 * (NACp) see {@link AirborneOperationalStatusV1Msg}. Values according to DO-260B Table N-11.
	 *
	 * The horizontal containment radius is also known as "horizontal protection level".
	 *
	 * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
	 *         If aircraft uses ADS-B version 1+, set NIC supplement A from Operational Status Message
	 *         for better precision.
	 */
	public static double decodeHCR(byte formatTypeCode, boolean nicSupplA) {
		switch (formatTypeCode) {
			case 5: return 7.5;
			case 6: return 25;
			case 7: return nicSupplA ? 75 : 185.2;
			case 8: return 185.2;
			// case 0: return -1;
			default: return -1;
		}
	}
}
