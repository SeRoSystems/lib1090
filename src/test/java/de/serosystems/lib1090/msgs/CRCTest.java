package de.serosystems.lib1090.msgs;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CRCTest {

    @Test
    void testCalcParityInt() {
        byte[] msg = new byte[]{(byte) 0xa8, 0x20, 0x0a, (byte) 0x80, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] crc = calcParity(msg);
        int crc2 = ModeSDownlinkMsg.calcParityInt(msg);
        assertEquals(crc.length, 3);
        assertEquals(crc[0] & 0xff, (crc2 >> 16) & 0xff);
        assertEquals(crc[1] & 0xff, (crc2 >> 8) & 0xff);
        assertEquals(crc[2] & 0xff, crc2 & 0xff);
    }

    @Test
    void testCalcParity() {
        byte[] msg = new byte[]{(byte) 0xa8, 0x20, 0x0a, (byte) 0x80, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] crc = calcParity(msg);
        byte[] crc2 = ModeSDownlinkMsg.calcParity(msg);
        assertEquals(crc.length, 3);
        assertEquals(crc2.length, 3);
        assertArrayEquals(crc, crc2);
    }

    public static void main(String[] args) {
        /* create CRC table */

        final int divider = 0x1fff409; /* generator polynomial, including leading coefficient */
        final int dividerDegree = 24;
        /* Trade-off between (static) memory and computational resources. Anything other than 4 and 8 makes no sense. Must match the algorithm! */
        final int lookahead = 8;

        /* sanity checks */
        assert lookahead > 0 && lookahead <= dividerDegree;
        assert dividerDegree < 32;

        List<Integer> remainders = IntStream.range(0, 1 << lookahead)
                .map(divisor -> {
                    /* let remainder initially be the remainder of divisor * X^(dividerDegree - lookahead) divided by the generator */
                    int remainder = divisor << (dividerDegree - lookahead);
                    /* multiply the remainder by X^lookahead */
                    for (int i = 0; i < lookahead; ++i) {
                        /* multiply by X */
                        remainder <<= 1;
                        /* get remainder again */
                        if ((remainder >>> dividerDegree) != 0)
                            remainder ^= divider;
                    }
                    /* have remainder = divisor * X^(dividerDegree - lookahead) * X^lookahead % divider = divisor * X^dividerDegree % divider. */
                    return remainder;
                })
                .boxed()
                .collect(Collectors.toList());

        // print list in chunks of 8
        String format = String.format("0x%%0%dx", dividerDegree / 8 * 2);
        for (int i = 0; i < remainders.size(); i += 8) {
            String k8 = remainders.stream()
                    .skip(i)
                    .limit(8)
                    .map(x -> String.format(format, x))
                    .collect(Collectors.joining(",", "", ","));
            System.out.println(k8);
        }
    }

    /**
     * polynomial for the cyclic redundancy check<br>
     * Note: we assume that the degree of the polynomial
     * is divisible by 8 (holds for Mode S) and the msb is left out
     */
    public static final byte[] CRC_polynomial = {
            (byte) 0xFF,
            (byte) 0xF4,
            (byte) 0x09 // according to Annex 10 V4
    };

    /**
     * Note: this is a non-optimized (and, in fact, the old) version of {@link ModeSDownlinkMsg#calcParityInt(byte[])}.
     * We use it as ground truth for testing the optimized version.
     *
     * @param msg raw message as byte array
     * @return calculated parity field as 3-byte array. We used the implementation from<br>
     * http://www.eurocontrol.int/eec/gallery/content/public/document/eec/report/1994/022_CRC_calculations_for_Mode_S.pdf
     */
    public static byte[] calcParity(byte[] msg) {
        byte[] pi = Arrays.copyOf(msg, CRC_polynomial.length);

        boolean invert;
        int byteidx, bitshift;
        for (int i = 0; i < msg.length * 8; ++i) { // bit by bit
            invert = ((pi[0] & 0x80) != 0);

            // shift left
            pi[0] <<= 1;
            for (int b = 1; b < CRC_polynomial.length; ++b) {
                pi[b - 1] |= (pi[b] >>> 7) & 0x1;
                pi[b] <<= 1;
            }

            // get next bit from message
            byteidx = ((CRC_polynomial.length * 8) + i) / 8;
            bitshift = 7 - (i % 8);
            if (byteidx < msg.length)
                pi[pi.length - 1] |= (msg[byteidx] >>> bitshift) & 0x1;

            // xor
            if (invert)
                for (int b = 0; b < CRC_polynomial.length; ++b)
                    pi[b] ^= CRC_polynomial[b];
        }

        return Arrays.copyOf(pi, CRC_polynomial.length);
    }
}
