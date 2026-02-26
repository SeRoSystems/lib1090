package de.serosystems.lib1090.decoding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitReaderTest {

	@Test
	@DisplayName("Read simple bits (1-based Big Endian)")
	void testSimpleBigEndian() {
		// 0b1101_0000 (0xD0)
		byte[] data = {(byte) 0xD0};
		BitReader reader = BitReader.forBigEndian(data);

		assertEquals(0b1, reader.readInt(1, 1));   // Bit 1 (MSB)
		assertEquals(0b101, reader.readInt(2, 4)); // Bits 2, 3, 4
		assertEquals(0, reader.readInt(5, 8));     // Remaining bits
	}

	@Test
	@DisplayName("Read across byte boundaries (1-based)")
	void testBoundaryCrossing() {
		// 0b11111111 0b00000000 (0xFF 0x00)
		byte[] data = {(byte) 0xFF, (byte) 0x00};
		BitReader reader = BitReader.forBigEndian(data);

		// Accessing Bits 7 through 10:
		// Byte 1: Bits 7,8 are [..11]
		// Byte 2: Bits 9,10 are [00..]
		// Expected: 0b1100 -> 12
		assertEquals(12, reader.readInt(7, 10));
	}

	@Test
	@DisplayName("Validate 1-based Little Endian")
	void testLittleEndian() {
		// 0b0000_0001 (0x01)
		byte[] data = {0x01};
		BitReader reader = BitReader.forLittleEndian(data);

		// Bit 1 is the LSB in Little Endian
		assertEquals(1, reader.readInt(1, 1));
		assertEquals(0, reader.readInt(2, 8));
	}

	@Test
	@DisplayName("Verify ICAO Address (Bits 9 to 32)")
	void testIcaoRange() {
		// Mode S messages often have ICAO starting after 8 bits of DF/CA
		byte[] data = {0x00, 0x4B, 0x12, 0x34};
		BitReader reader = BitReader.forBigEndian(data);

		assertEquals(0x4B1234, reader.readInt(9, 32));
	}

	@Test
	@DisplayName("Enforce Type Safety and Boundaries")
	void testSafety() {
		byte[] data = {0x00};
		BitReader reader = BitReader.forBigEndian(data);

		// Attempting to read 0 (since it's 1-based)
		assertThrows(IllegalArgumentException.class, () -> reader.readInt(0, 5));

		// Attempting to read past byte 1
		assertThrows(IndexOutOfBoundsException.class, () -> reader.readInt(1, 9));
	}

	@Test
	@DisplayName("Read typed values using readShort")
	void testReadShort() {
		// 0b10110011 0b11001101 (0xB3CD)
		byte[] data = {(byte) 0xB3, (byte) 0xCD};
		BitReader reader = BitReader.forBigEndian(data);

		// Bits 1 to 16 should be exactly 0xB3CD (signed: -19507, unsigned: 46029)
		assertEquals((short) 0xB3CD, reader.readShort(1, 16));

		// Bits 3 to 12: 110011 1100 -> 0b1100111100 (828)
		assertEquals((short) 828, reader.readShort(3, 12));
	}

	@Test
	@DisplayName("Read typed values using readLong")
	void testReadLong() {
		// 0x01 0x23 0x45 0x67 0x89 0xAB 0xCD 0xEF
		byte[] data = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF};
		BitReader reader = BitReader.forBigEndian(data);

		// Read all 64 bits
		assertEquals(0x0123456789ABCDEFL, reader.readLong(1, 64));

		// Read a 40-bit timestamp/value across middle bytes
		// Bits 9 to 48: 0x23456789AB
		assertEquals(0x23456789ABL, reader.readLong(9, 48));
	}
}
