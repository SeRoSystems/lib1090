package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.decoding.Identity;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/**
 * Decoder for ADS-B version 1 Mode A code messages.
 */
public class ModeACodeV1Msg extends ExtendedSquitter implements Serializable {

	private static final long serialVersionUID = -6444923523067947936L;

	private byte msgsubtype;
	private short mode_a_code;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected ModeACodeV1Msg() { }

	/**
	 * @param raw_message raw ADS-B Mode A code message as hex string
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeACodeV1Msg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param raw_message raw ADS-B Mode A code message as byte array
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public ModeACodeV1Msg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message));
	}

	/**
	 * @param squitter extended squitter which contains this Mode A code message
	 * @throws BadFormatException if message has wrong format
	 */
	public ModeACodeV1Msg(ExtendedSquitter squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.ADSB_MODE_A_CODE_V1);

		if (this.getFormatTypeCode() != 23) {
			throw new BadFormatException("Mode A code messages must have typecode 23.");
		}

		BitReader reader = BitReader.forBigEndian(this.getMessage());
		msgsubtype = reader.readByte(6, 8);
		if (msgsubtype != 7) {
			throw new BadFormatException("Mode A code messages must have subtype 7.");
		}

		mode_a_code = reader.readShort(9, 21);
	}

	/**
	 * @return the subtype code of the message (should always be 7)
	 */
	public byte getSubtype() {
		return msgsubtype;
	}

	/**
	 * @return the four-digit Mode A (4096) code
	 */
	public short getModeACode() {
		return mode_a_code;
	}

	/**
	 * @return decoded Mode A code as four digits
	 */
	public String getIdentity() {
		return Identity.decodeIdentity(mode_a_code);
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tModeACodeV1Msg{" +
				"msgsubtype=" + msgsubtype +
				", mode_a_code=" + mode_a_code +
				'}';
	}
}
