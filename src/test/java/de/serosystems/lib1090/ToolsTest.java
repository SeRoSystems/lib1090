package de.serosystems.lib1090;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

	@Property
	void toHexString_shouldMatchStringFormat(@ForAll @Positive int input, @ForAll @IntRange(min = 1, max = 256) int minDigits) {
		assertEquals(String.format("%0" + minDigits + "x", input), Tools.toHexString(input, minDigits));
	}

	@Test
	public void testHexStringToByteArray() {
		byte[] result = Tools.hexStringToByteArray("8D406B902015A678D4D220AA4BDA");
		assertEquals(14, result.length);
		assertEquals((byte) 0x8D, result[0]);
		assertEquals((byte) 0x40, result[1]);
		assertEquals((byte) 0x6B, result[2]);
		assertEquals((byte) 0x90, result[3]);
		assertEquals((byte) 0xDA, result[13]);
	}

	@Test
	public void testHexStringToByteArrayLowerCase() {
		byte[] upper = Tools.hexStringToByteArray("8D406B90");
		byte[] lower = Tools.hexStringToByteArray("8d406b90");
		assertTrue(Tools.areEqual(upper, lower));
	}

	@Test
	public void testToHexString() {
		byte[] bytes = {(byte) 0x40, (byte) 0x6B, (byte) 0x90};
		assertEquals("406b90", Tools.toHexString(bytes));
	}

	@Test
	public void testToHexStringSingleByte() {
		assertEquals("0a", Tools.toHexString((byte) 0x0A));
		assertEquals("ff", Tools.toHexString((byte) 0xFF));
		assertEquals("00", Tools.toHexString((byte) 0x00));
	}

	@Test
	public void testIsZero() {
		assertTrue(Tools.isZero(new byte[]{0, 0, 0}));
		assertFalse(Tools.isZero(new byte[]{0, 0, 1}));
		assertFalse(Tools.isZero(new byte[]{(byte) 0xFF, 0, 0}));
	}

	@Test
	public void testAreEqual() {
		byte[] a = {1, 2, 3};
		byte[] b = {1, 2, 3};
		byte[] c = {1, 2, 4};
		assertTrue(Tools.areEqual(a, b));
		assertFalse(Tools.areEqual(a, c));
	}

	@Test
	public void testFeet2Meters() {
		assertEquals(0.0, Tools.feet2Meters(0), 0.001);
		assertEquals(304.8, Tools.feet2Meters(1000), 0.1);
		assertNull(Tools.feet2Meters((Integer) null));
	}

	@Test
	public void testKnots2MetersPerSecond() {
		// 1 knot = 0.514444 m/s
		assertEquals(0.514444, Tools.knots2MetersPerSecond(1), 0.001);
		assertNull(Tools.knots2MetersPerSecond((Integer) null));
	}
}