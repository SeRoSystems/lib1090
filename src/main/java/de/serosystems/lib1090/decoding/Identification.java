package de.serosystems.lib1090.decoding;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class Identification {

	private Identification() {}

	/**
	 * Maps ADS-B encoded to readable characters
	 * @param digit encoded digit
	 * @return readable character
	 */
	public static char mapChar(byte digit) {
		if (digit>0 && digit<27) return (char) ('A'+digit-1);
		else if (digit>47 && digit<58) return (char) ('0'+digit-48);
		else return ' ';
	}

	/**
	 * Maps ADS-B encoded to readable characters
	 * @param digits array of encoded digits
	 * @return array of decoded characters
	 */
	public static char[] mapChar(byte[] digits) {
		char[] result = new char[digits.length];

		for (int i=0; i<digits.length; i++)
			result[i] = mapChar(digits[i]);

		return result;
	}

	public static byte[] decodeAircraftIdentification(byte[] msg) {
		byte[] identity = new byte[8];

		int byte_off, bit_off;
		for (int i=8; i>=1; i--) {
			// calculate offsets
			byte_off = (i*6)/8; bit_off = (i*6)%8;

			// char aligned with byte?
			if (bit_off == 0) identity[i-1] = (byte) (msg[byte_off]&0x3F);
			else {
				++byte_off;
				identity[i-1] = (byte) (msg[byte_off]>>>(8-bit_off)&(0x3F>>>(6-bit_off)));
				// should we add bits from the next byte?
				if (bit_off < 6) identity[i-1] |= msg[byte_off-1]<<bit_off&0x3F;
			}
		}

		return identity;
	}

	/**
	 * @param type_code format type code of identity message
	 * @param emitter_category reported emitter category
	 * @return a textual description of the emitter's category according to DO-260B
	 */
	public static String categoryDescription(byte type_code, byte emitter_category) {
		// category descriptions according
		// to the ADS-B specification
		String[][] categories = {{
				"No ADS-B Emitter Category Information",
				"Light (< 15500 lbs)",
				"Small (15500 to 75000 lbs)",
				"Large (75000 to 300000 lbs)",
				"High Vortex Large (aircraft such as B-757)",
				"Heavy (> 300000 lbs)",
				"High Performance (> 5g acceleration and 400 kts)",
				"Rotorcraft"
		},{
				"No ADS-B Emitter Category Information",
				"Glider / sailplane",
				"Lighter-than-air",
				"Parachutist / Skydiver",
				"Ultralight / hang-glider / paraglider",
				"Reserved",
				"Unmanned Aerial Vehicle",
				"Space / Trans-atmospheric vehicle",
		},{
				"No ADS-B Emitter Category Information",
				"Surface Vehicle – Emergency Vehicle",
				"Surface Vehicle – Service Vehicle",
				"Point Obstacle (includes tethered balloons)",
				"Cluster Obstacle",
				"Line Obstacle",
				"Reserved",
				"Reserved"
		},{
				"Reserved",
				"Reserved",
				"Reserved",
				"Reserved",
				"Reserved",
				"Reserved",
				"Reserved",
				"Reserved"
		}};

		return categories[4-type_code][emitter_category];
	}
}
