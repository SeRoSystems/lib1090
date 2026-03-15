package de.serosystems.lib1090.cpr;

final class Util {
    private Util() {}

    /**
     * Euclidean modulo operator.
     * <br>
     * Let {@code a, b} be given integers, then {@code a = b * k + r} for some integers {@code k, r} such that {@code 0 <= r < |b|}. Then this function returns {@code r}.
     * An alternative definition is {@code r = a - floor(a/b)*b}.
     *
     * @param a some a
     * @param b some b > 0
     * @return a mod b following Euclidean definition.
     */
    public static int mod(int a, int b) {
        int m = a % b;
        if (m < 0)
            m += b;
        return m;
    }
}
