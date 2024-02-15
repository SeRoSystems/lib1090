package de.serosystems.lib1090;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

	@Property
	void toHexString_shouldMatchStringFormat(@ForAll @Positive int input, @ForAll @IntRange(min = 1, max = 256) int minDigits) {
		assertEquals(String.format("%0" + minDigits + "x", input), Tools.toHexString(input, minDigits));
	}

}