package de.serosystems.lib1090.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;
import java.util.Arrays;

/**
 * BDS 2,0
 */
public class AircraftIdentification extends BDSRegister implements Serializable {


    // Fields
    // ------

    // BDS Code
    private short bdsCode;
    // aircraft Identification
    private byte[] aircraftIdentification;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected AircraftIdentification() {
    }

    public AircraftIdentification(byte[] message) throws BadFormatException {

        super(message);
        setBds(BDSRegister.bdsCode.DATA_LINK_CAPABILITY_REPORT);

        this.bdsCode = extractBdsCode(message);
        this.aircraftIdentification = extractAircraftIdentification(message);

    }

    // Getters
    // -------

    public short getBdsCode() {
        return bdsCode;
    }

    public char[] getAircraftIdentification() {
        return mapChar(aircraftIdentification);
    }

    // public static methods
    // ---------------------


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
                "bdsCode=" + bdsCode +
                ", aircraftIdentification=" + Arrays.toString(aircraftIdentification) +
                '}';
    }

}
