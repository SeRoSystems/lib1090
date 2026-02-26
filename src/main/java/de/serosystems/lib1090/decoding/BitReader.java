package de.serosystems.lib1090.decoding;

import java.util.Objects;

/**
 * A stateless utility for extracting bits from byte arrays using absolute bit ranges.
 * <p>
 * <b>Indexing Convention:</b> This class uses <b>1-based indexing</b> to match
 * technical specifications like ICAO Annex 10.
 * <ul>
 * <li>Bit 1: The Most Significant Bit (MSB) of the first byte.</li>
 * <li>Bit 8: The Least Significant Bit (LSB) of the first byte.</li>
 * <li>Bit 9: The MSB of the second byte.</li>
 * </ul>
 * <p>
 * Example: {@code readInt(1, 5)} extracts the first 5 bits of the message.
 */
public class BitReader {
	private final byte[] data;
	private final boolean bigEndian;

	private BitReader(byte[] data, boolean bigEndian) {
		this.data = Objects.requireNonNull(data);
		this.bigEndian = bigEndian;
	}

	/**
	 * Factory for Big-Endian bit ordering (Network Byte Order).
	 * Bit 1 is the MSB of data[0].
	 */
	public static BitReader forBigEndian(byte[] data) {
		return new BitReader(data, true);
	}

	/**
	 * Factory for Little-Endian bit ordering.
	 * Bit 1 is the LSB of data[0].
	 */
	public static BitReader forLittleEndian(byte[] data) {
		return new BitReader(data, false);
	}

	public byte readByte(int from, int to) {
		return (byte) readRange(from, to, 8);
	}

	public short readShort(int from, int to) {
		return (short) readRange(from, to, 16);
	}

	public int readInt(int from, int to) {
		return (int) readRange(from, to, 32);
	}

	public long readLong(int from, int to) {
		return readRange(from, to, 64);
	}

	/**
	 * Internal extraction logic.
	 * * @param from    The starting bit position (inclusive, starts at 1).
	 *
	 * @param to      The ending bit position (inclusive, starts at 1).
	 * @param maxBits Maximum allowed bit span for the return type.
	 * @return The extracted value as a long.
	 */
	private long readRange(int from, int to, int maxBits) {
		if (from < 1) {
			throw new IllegalArgumentException("Start bit must be >= 1.");
		}

		if (to < from) {
			throw new IllegalArgumentException("End bit < start bit.");
		}

		int numBits = (to - from) + 1;
		if (numBits > maxBits) {
			throw new IllegalArgumentException("Range exceeds type capacity.");
		}
		if (to > data.length * 8) {
			throw new IndexOutOfBoundsException("End of buffer.");
		}

		if (bigEndian) {
			return readBigEndian(from, to, numBits);
		} else {
			return readLittleEndian(from, to);
		}
	}

	/**
	 * Optimized Byte-Block extraction for Big-Endian.
	 */
	private long readBigEndian(int from, int to, int numBits) {
		int startBit0 = from - 1;
		int endBit0 = to - 1;
		int startByte = startBit0 / 8;
		int endByte = endBit0 / 8;

		long value = 0;
		for (int i = startByte; i <= endByte; i++) {
			value = (value << 8) | (data[i] & 0xFFL);
		}

		int bitsInLastByte = 7 - (endBit0 % 8);
		value >>>= bitsInLastByte;
		long mask = (numBits == 64) ? -1L : (1L << numBits) - 1;
		return value & mask;
	}

	/**
	 * Bit-by-bit extraction for Little-Endian to ensure LSB-first accuracy.
	 */
	private long readLittleEndian(int from, int to) {
		long value = 0;
		for (int i = to - 1; i >= from - 1; i--) {
			int byteIdx = i / 8;
			int bitIdxInByte = i % 8; // LSB is bit 0
			long bit = (data[byteIdx] >> bitIdxInByte) & 1;
			value = (value << 1) | bit;
		}
		return value;
	}
}
