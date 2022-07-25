package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;
import java.util.Arrays;

/**
 * BDS 2,0
 */
public class AircraftIdentification extends ModeSDownlinkMsg implements Serializable {


    // Fields
    // ------

    // Flight Status
    private byte flightStatus;
    // Downlink Request
    private byte downlinkRequest;
    // Utility Message
    private byte utilityMsg;
    // Altitude Code (if DF20 otherwise null)
    private Short altitudeCode;
    // Identity (if DF21 otherwise null)
    private Short identity;
    // BDS Code
    private short bdsCode;
    // aircraft Identification
    private byte[] aircraftIdentification;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected AircraftIdentification() {
    }

    public AircraftIdentification(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public AircraftIdentification(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public AircraftIdentification(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.AIRCRAFT_IDENTIFICATION);

        byte[] payload = getPayload();
        byte[] message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        if (reply.getDownlinkFormat() == 20) {
            this.altitudeCode = extractAltitudeCode(payload);
            this.identity = null;
        } else if (reply.getDownlinkFormat() == 21) {
            this.altitudeCode = null;
            this.identity = extractIdentity(payload);
        } else {
            throw new BadFormatException("Message is not an altitude reply or an identity reply !");
        }

        this.flightStatus = getFirstField();
        this.downlinkRequest = extractDownlinkRequest(payload);
        this.utilityMsg = extractUtilityMessage(payload);

        this.bdsCode = extractBdsCode(message);
        this.aircraftIdentification = extractAircraftIdentification(message);

    }

    // Getters
    // -------

    public byte getFlightStatus() {
        return flightStatus;
    }

    public byte getDownlinkRequest() {
        return downlinkRequest;
    }

    public byte getUtilityMsg() {
        return utilityMsg;
    }

    public Short getAltitudeCode() {
        return altitudeCode;
    }

    public Short getIdentity() {
        return identity;
    }

    public short getBdsCode() {
        return bdsCode;
    }

    public char[] getAircraftIdentification() {
        return mapChar(aircraftIdentification);
    }

    // public static methods
    // ---------------------

    public static short extractBdsCode(byte[] msg) {
        return (short) ((msg[0] >>> 4) * 10 + (msg[0] & 0x0F));
    }

    public static byte[] extractAircraftIdentification(byte[] msg) {
        // extract identity
        byte [] identity = new byte[8];
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

    // Public static methods
    // ---------------------

    /**
     * Maps ADS-B encoded to readable characters
     * @param digits array of encoded digits
     * @return array of decoded characters
     */
    public static char[] mapChar (byte[] digits) {
        char[] result = new char[digits.length];

        for (int i=0; i<digits.length; i++)
            result[i] = mapChar(digits[i]);

        return result;
    }

    // Private static methods
    // ----------------------

    private static short extractIdentity(byte[] payload) {
        return (short) ((payload[1] << 8 | (payload[2] & 0xFF)) & 0x1FFF);
    }

    private static short extractAltitudeCode(byte[] payload) {
        return (short) ((payload[1] << 8 | payload[2] & 0xFF) & 0x1FFF);
    }

    private static byte extractUtilityMessage(byte[] payload) {
        return (byte) ((payload[0]&0x7)<<3 | (payload[1]>>>5)&0x7);
    }

    private static byte extractDownlinkRequest(byte[] payload) {
        return (byte) ((payload[0]>>>3) & 0x1F);
    }

    /**
     * Maps ADS-B encoded to readable characters
     * @param digit encoded digit
     * @return readable character
     */
    private static char mapChar (byte digit) {
        if (digit>0 && digit<27) return (char) ('A'+digit-1);
        else if (digit>47 && digit<58) return (char) ('0'+digit-48);
        else return ' ';
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "AircraftIdentification{" +
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", bdsCode=" + bdsCode +
                ", aircraftIdentification=" + Arrays.toString(aircraftIdentification) +
                '}';
    }

}
