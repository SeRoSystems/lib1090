package de.serosystems.lib1090.cpr;

import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.util.function.Function;

public interface PositionDecoderSupplier extends Function<ModeSDownlinkMsg.QualifiedAddress, PositionDecoder> {

	static PositionDecoderSupplier statefulPositionDecoder() {
		return address -> new StatefulPositionDecoder();
	}

	static PositionDecoderSupplier statefulPositionDecoder(final boolean disableSpeedTest) {
		return address -> new StatefulPositionDecoder(disableSpeedTest);
	}
}
