package de.serosystems.lib1090.decoding;

/**
 * @author Markus Fuchs (fuchs@sero-systems.de)
 */
public final class Altitude {

	private Altitude() {}

	/**
	 * This method converts a gray code encoded int to a standard decimal int
	 * @param gray gray code encoded int of length bitlength
	 *        bitlength bitlength of gray code
	 * @return radix 2 encoded integer
	 */
	public static int grayToBin(int gray, int bitlength) {
		int result = 0;
		for (int i = bitlength-1; i >= 0; --i)
			result = result|((((0x1<<(i+1))&result)>>>1)^((1<<i)&gray));
		return result;
	}

	/**
	 * Decode altitude code according to Annex 10 V4 3.1.2.6.5.4
	 * @param altitude_code as provided in most Mode S replies (13 bits)
	 * @return altitude in feet
	 */
	public static Integer decode13BitAltitude(short altitude_code) {
		// altitude unavailable
		if (altitude_code == 0) return null;

		boolean Mbit = (altitude_code&0x40)!=0;
		if (!Mbit) {
			boolean Qbit = (altitude_code&0x10)!=0;
			if (Qbit) { // altitude reported in 25ft increments
				int N = (altitude_code&0x0F) | ((altitude_code&0x20)>>>1) | ((altitude_code&0x1F80)>>>2);
				return 25*N-1000;
			}
			else { // altitude is above 50175ft, so we use 100ft increments

				// it's decoded using the Gillham code
				int C1 = (0x1000&altitude_code)>>>12;
				int A1 = (0x0800&altitude_code)>>>11;
				int C2 = (0x0400&altitude_code)>>>10;
				int A2 = (0x0200&altitude_code)>>>9;
				int C4 = (0x0100&altitude_code)>>>8;
				int A4 = (0x0080&altitude_code)>>>7;
				int B1 = (0x0020&altitude_code)>>>5;
				int B2 = (0x0008&altitude_code)>>>3;
				int D2 = (0x0004&altitude_code)>>>2;
				int B4 = (0x0002&altitude_code)>>>1;
				int D4 = (0x0001&altitude_code);

				// this is standard gray code
				int N500 = grayToBin(D2<<7|D4<<6|A1<<5|A2<<4|A4<<3|B1<<2|B2<<1|B4, 8);

				// 100-ft steps must be converted
				int N100 = grayToBin(C1<<2|C2<<1|C4, 3)-1;
				if (N100 == 6) N100=4;
				if (N500%2 != 0) N100=4-N100; // invert it

				return -1200+N500*500+N100*100;
			}
		}
		else return null; // unspecified metric encoding
	}

	/**
	 * Decode altitude according to DO-260B 2.2.3.2.3.4.3 <br>
	 * @param altitude_encoded 12 bit encoded altitude
	 * @return altitude in feet
	 */
	public static Integer decode12BitAltitude(short altitude_encoded) {
		// In contrast to the decodeAltitude method in {@link de.serosystems.lib1090.msgs.modes.AltitudeReply}, input
		// does not contain the MBit
		boolean Qbit = (altitude_encoded&0x10)!=0;
		int N;
		if (Qbit) { // altitude reported in 25ft increments
			N = (altitude_encoded&0xF) | ((altitude_encoded&0xFE0)>>>1);
			return 25*N-1000;
		}
		else { // altitude is above 50175ft, so we use 100ft increments

			// it's decoded using the Gillham code
			int C1 = (0x800&altitude_encoded)>>>11;
			int A1 = (0x400&altitude_encoded)>>>10;
			int C2 = (0x200&altitude_encoded)>>>9;
			int A2 = (0x100&altitude_encoded)>>>8;
			int C4 = (0x080&altitude_encoded)>>>7;
			int A4 = (0x040&altitude_encoded)>>>6;
			int B1 = (0x020&altitude_encoded)>>>5;
			int B2 = (0x008&altitude_encoded)>>>3;
			int D2 = (0x004&altitude_encoded)>>>2;
			int B4 = (0x002&altitude_encoded)>>>1;
			int D4 = (0x001&altitude_encoded);

			// this is standard gray code
			int N500 = grayToBin(D2<<7|D4<<6|A1<<5|A2<<4|A4<<3|B1<<2|B2<<1|B4, 8);

			// 100-ft steps must be converted
			int N100 = grayToBin(C1<<2|C2<<1|C4, 3)-1;
			if (N100 == 6) N100=4;
			if (N500%2 != 0) N100=4-N100; // invert it

			return -1200+N500*500+N100*100;
		}
	}
}
