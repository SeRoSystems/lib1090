package de.serosystems.lib1090.cpr;

import java.util.Arrays;

/**
 * Latitude represented on Lattice L0.
 */
class L0Latitude {
    /**
     * Transition latitudes, scaled into L0.
     */
    private static final int[] T_LAT = {
            0x0337ad54,
            0x048e7ba9,
            0x0596a719,
            0x06764ff9,
            0x073c35cf,
            0x07efe698,
            0x0895ddf2,
            0x093106d8,
            0x09c367ac,
            0x0a4e798e,
            0x0ad35901,
            0x0b52e2ec,
            0x0bcdc6e6,
            0x0c449344,
            0x0cb7bd4c,
            0x0d27a700,
            0x0d94a34d,
            0x0dfef917,
            0x0e66e596,
            0x0ecc9e11,
            0x0f305145,
            0x0f92287b,
            0x0ff24861,
            0x1050d1c2,
            0x10ade211,
            0x110993e4,
            0x1163ff57,
            0x11bd3a58,
            0x121558f6,
            0x126c6d8f,
            0x12c2890a,
            0x1317bafe,
            0x136c11d2,
            0x13bf9ae1,
            0x1412628d,
            0x1464745b,
            0x14b5daff,
            0x1506a06c,
            0x1556cde0,
            0x15a66be8,
            0x15f58265,
            0x16441889,
            0x169234cd,
            0x16dfdce1,
            0x172d1591,
            0x1779e292,
            0x17c6463d,
            0x18124118,
            0x185dd11e,
            0x18a8f089,
            0x18f393ba,
            0x193da56a,
            0x1986ff34,
            0x19cf5991,
            0x1a1624e1,
            0x1a5a17bd,
            0x1a9772f8,
            0x1abc0000,
    };

    /**
     * Helper factor for lattice.
     */
    private static final int SCALE = 14160;

    /**
     * Denominator of lattice L0.
     */
    private static final int L0 = SCALE << 17;

    /**
     * Latitude on L0.
     */
    private final int lat;

    /**
     * Whether latitude is valid.
     */
    private final boolean valid;

    /**
     * New value on lattice.
     *
     * @param lat latitude on lattice.
     */
    private L0Latitude(int lat) {
        this.lat = lat;
        valid = Math.abs(lat) <= L0 / 4;
    }

    /**
     * Get latitude in degrees.
     *
     * @return latitude [°].
     * @see #isValid() if not valid, returned latitude is outside valid range
     */
    public double toDegrees() {
        return 360. * lat / L0;
    }

    /**
     * Whether latitude is valid.
     * Note: it is considered invalid if and only if it is outside range {@code [-90°,90°]}.
     *
     * @return true if latitude is valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Compute NL(toDegrees()), i.e. the number of longitude zones for this latitude.
     * See DO-260B §A.1.7.2 for reference.
     *
     * @return number of longitude zones for this latitude.
     */
    public int NL() {
        int idx = Arrays.binarySearch(T_LAT, Math.abs(lat));
        if (idx < 0) idx = -idx - 1;
        return T_LAT.length - idx + 1;
    }

    /**
     * Constructs a new L0Latitude object based on a provided latitude in degrees.
     *
     * @param degrees latitude in degrees.
     * @return latitude in L0
     */
    public static L0Latitude ofDegrees(double degrees) {
        return new L0Latitude((int) (degrees * L0 / 360.));
    }

    /**
     * Create new latitude for a given position message, using global decoding.
     *
     * @param cpr       CPR encoded position
     * @param zoneIndex zone index
     * @param refLat    reference latitude in degrees, needed if this is for a surface position
     * @return latitude in L0 for given parameters
     */
    public static L0Latitude ofGlobal(CPREncodedPosition cpr, int zoneIndex, double refLat) {
        int nBits = cpr.getNBits();

        int f = cpr.isSurface() ? 4 : 1;
        int zones = cpr.isOddFormat() ? 59 : 60;
        int effectiveScale = SCALE / f / zones;
        int nz = Util.mod(zoneIndex, zones) << nBits;
        int lat0 = nz + cpr.yz();
        int r = (lat0 << (17 - nBits)) * effectiveScale;

        if (!cpr.isSurface()) {
            if (r > L0 / 2) // Southern Hemisphere
                r -= L0;
        } else {
            int l = ofDegrees(refLat).lat;
            if (r == 0 && l > L0 / 8) // North Pole
                r = L0 / 4;
            else if (r - l > L0 / 8) // Southern Hemisphere
                r -= L0 / 4;
        }

        return new L0Latitude(r);
    }

    /**
     * Create new latitude for a given position message, using local decoding.
     *
     * @param cpr               CPR encoded position
     * @param referenceLatitude reference latitude
     * @return latitude in L0 for given parameters
     */
    public static L0Latitude ofLocal(CPREncodedPosition cpr, double referenceLatitude) {
        int nBits = cpr.getNBits();
        int f = cpr.isSurface() ? 4 : 1;
        int zones = cpr.isOddFormat() ? 59 : 60;
        double D = 360. / f / zones;
        double cprScale = 1 << nBits;
        int zone = (int) Math.floor(.5 + referenceLatitude / D - cpr.yz() / cprScale);

        int effectiveScale = SCALE / f / zones;
        int nz = zone << nBits;
        int lat0 = nz + cpr.yz();
        int r = (lat0 << (17 - nBits)) * effectiveScale;

        return new L0Latitude(r);
    }
}
