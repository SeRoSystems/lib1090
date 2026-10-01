/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.lib1090.msgs.acas;

import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.decoding.Bound;
import de.serosystems.lib1090.decoding.Interval;

import java.io.Serializable;

/**
 * Represents the decoded threat identity data, as defined in ICAO Annex 10 Volume IV
 * §4.3.8.4.2.2.1.6; the THREAT IDENTITY DATA subfield's register layout is also given in ICAO
 * Doc 9871 (First Edition, AN/464) §A.2 TABLE A-2-48 — BDS code 3,0, ACAS
 * active resolution advisory.
 * <p>
 * The ACAS X layout, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.2.9, codes the threat altitude in binary
 * rather than as a Mode C code, see {@link #withBinaryAltitude(Short, Short, Short)}; range and bearing are coded the
 * same way in both layouts.
 */
@SuppressWarnings("unused")
public class ThreatIdentityData implements Serializable {
    private static final long serialVersionUID = -2962790317508395221L;

    private Integer icao24;
    private Short altitudeCode;
    private Short range;
    private Short bearing;
    private boolean hasTransponderAddress;
    private boolean binaryAltitude;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ThreatIdentityData() {
    }

    /**
     * Create a new instance where the threat is identified by a 24 bit transponder address.
     *
     * @param icao24 the ICAO 24-bit aircraft address
     */
    public ThreatIdentityData(Integer icao24) {
        this.icao24 = icao24;
        hasTransponderAddress = true;
    }

    /**
     * Create a new instance where the target is identified by altitude, range and bearing.
     *
     * @param altitudeCode the barometric altitude
     * @param range        the threat identity data range, i.e. the most recent threat range estimated
     *                     by TCAS
     * @param bearing      the threat identity data bearing, i.e. the most recent estimated bearing of
     *                     the threat aircraft, relative to the TCAS aircraft heading; the codes 61 to 63 are not
     *                     assigned and decode as no bearing estimate, see {@link #getBearing()}
     * @throws IllegalArgumentException if range is outside [0, 127] or bearing outside [0, 63], the values of
     *                                  their 7-bit and 6-bit fields
     */
    public ThreatIdentityData(Short altitudeCode, Short range, Short bearing) {
        if (range < 0 || range > 127)
            throw new IllegalArgumentException("Threat identity data range must be between 0 and 127: " + range);
        if (bearing < 0 || bearing > 63)
            throw new IllegalArgumentException("Threat identity data bearing must be between 0 and 63: " + bearing);

        this.altitudeCode = altitudeCode;
        this.range = range;
        this.bearing = bearing;

        hasTransponderAddress = false;
    }

    /**
     * Create a new instance of the ACAS X layout, where the target is identified by altitude, range and bearing and
     * the altitude is coded in binary, ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.2.9.1.
     *
     * @param altitudeCode the 11-bit TIDA: 0 no data, 1 below -950 ft, n from 2 on at least 100 n - 1150 ft and
     *                     below 100 n - 1050 ft
     * @param range        the threat identity data range, coded as in {@link #ThreatIdentityData(Short, Short, Short)}
     * @param bearing      the threat identity data bearing, coded as in
     *                     {@link #ThreatIdentityData(Short, Short, Short)}
     * @return the threat identity data
     * @throws IllegalArgumentException if altitudeCode is outside [0, 2047], range outside [0, 127] or bearing
     *                                  outside [0, 63], the values of their 11-bit, 7-bit and 6-bit fields
     */
    public static ThreatIdentityData withBinaryAltitude(Short altitudeCode, Short range, Short bearing) {
        if (altitudeCode < 0 || altitudeCode > 2047)
            throw new IllegalArgumentException("Binary threat altitude must be between 0 and 2047: " + altitudeCode);

        ThreatIdentityData tid = new ThreatIdentityData(altitudeCode, range, bearing);
        tid.binaryAltitude = true;
        return tid;
    }

    /**
     * Check whether the underlying target is identified by its ICAO 24 bit address, or a triple of
     * (range, altitude, bearing).
     *
     * @return true if target is identified by its 24 bit address, false otherwise
     */
    public boolean hasTransponderAddress() {
        return hasTransponderAddress;
    }

    /**
     * When the target is identified by its 24 bit address, return the address, otherwise "null".
     * Check {@link #hasTransponderAddress()} first.
     *
     * @return the ICAO 24-bit aircraft address if applicable
     */
    public Integer getIcao24() {
        return icao24;
    }

    /**
     * Get the altitude code: a Mode C code in the TCAS layout, the binary TIDA in the ACAS X layout, see
     * {@link #isAltitudeBinary()}. See {@link #getAltitude()} for the value in feet.
     * <p>
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * @return the altitude code for the barometric altitude if applicable
     */
    public Short getAltitudeCode() {
        return altitudeCode;
    }

    /**
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * @return the encoded threat identity data range if applicable
     */
    public Short getEncodedRange() {
        return range;
    }

    /**
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * @return the encoded threat identity data bearing if applicable
     */
    public Short getEncodedBearing() {
        return bearing;
    }

    /**
     * @return true if the altitude is coded in binary, as in the ACAS X layout, ICAO Annex 10 Volume IV (6th edition)
     * §4.3.8.4.2.2.2.9.1; false if it is a Mode C code, as in the TCAS layout, §4.3.8.4.2.2.1.6.1, or the threat is
     * identified by its address
     */
    public boolean isAltitudeBinary() {
        return binaryAltitude;
    }

    /**
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     * <p>
     * A Mode C code decodes as the altitude it reports. A binary altitude (ACAS X) is a 100 ft range, and this
     * returns its center, 100 n - 1100 ft; code 1, below -950 ft, has none. {@link #getAltitudeInterval()} gives the
     * range itself.
     *
     * @return the decoded barometric altitude in feet if applicable, or null if the altitude is not available or its
     * code is invalid
     */
    public Integer getAltitude() {
        if (hasTransponderAddress) return null;
        if (binaryAltitude) return altitudeCode < 2 ? null : 100 * altitudeCode - 1100;
        return Altitude.decode13BitAltitude(altitudeCode);
    }

    /**
     * The altitude range in feet a binary altitude (ACAS X) stands for, ICAO Annex 10 Volume IV (6th edition)
     * §4.3.8.4.2.2.2.9.1: code 1 is below -950 ft, and code n from 2 on is at least 100 n - 1150 ft and below
     * 100 n - 1050 ft.
     *
     * @return the altitude range in feet, or null if the threat is identified by its address, no altitude is
     * available (code 0), or the altitude is a Mode C code, which {@link #getAltitude()} decodes
     */
    public Interval getAltitudeInterval() {
        if (hasTransponderAddress || !binaryAltitude || altitudeCode == 0) return null;
        if (altitudeCode == 1) return Interval.of(Bound.NONE, 0, Bound.BELOW, -950);
        return Interval.of(Bound.AT_LEAST, 100 * altitudeCode - 1150, Bound.BELOW, 100 * altitudeCode - 1050);
    }

    /**
     * The estimated range of the threat in NM, ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.2 (and §4.3.8.4.2.2.2.9.2 for
     * ACAS X): code n from 2 to 126 stands for (n - 1)/10 NM ±0.05, and this returns (n - 1)/10. Codes 1 (less than
     * 0.05 NM) and 127 (greater than 12.55 NM) state a bound only, which {@link #getRangeInterval()} gives.
     *
     * @return the estimated range in NM, or null if the threat is identified by its address, no range estimate is
     * available (code 0), or the code states a bound only (codes 1 and 127)
     */
    public Float getRange() {
        if (hasTransponderAddress || range < 2 || range > 126) return null;
        return (range - 1) / 10F;
    }

    /**
     * The range of the threat's estimated range in NM, ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.2: code 1 is less than
     * 0.05 NM, code n from 2 to 126 is (n - 1)/10 NM ±0.05, both ends included as the Annex states them, and code 127
     * is greater than 12.55 NM.
     *
     * @return the range in NM, or null if the threat is identified by its address or no range estimate is available
     * (code 0)
     */
    public Interval getRangeInterval() {
        if (hasTransponderAddress || range == 0) return null;
        if (range == 1) return Interval.of(Bound.AT_LEAST, 0, Bound.BELOW, 0.05);
        if (range == 127) return Interval.of(Bound.MORE_THAN, 12.55, Bound.NONE, 0);
        return Interval.of(Bound.AT_LEAST, (range - 1.5) / 10, Bound.AT_MOST, (range - 0.5) / 10);
    }

    /**
     * The estimated bearing of the threat in degrees relative to the ACAS aircraft heading, ICAO Annex 10 Volume IV
     * §4.3.8.4.2.2.1.6.3 (and §4.3.8.4.2.2.2.9.3 for ACAS X): code n from 1 to 60 stands for a bearing between 6(n - 1)
     * and 6n degrees, and this returns the middle, 6n - 3; {@link #getBearingInterval()} gives the range. The codes 61
     * to 63 are not assigned, and an unassigned code requires no action (§3.1.2.3.2.3), so they decode like code 0, no
     * bearing estimate available. The code itself is available as {@link #getEncodedBearing()}.
     *
     * @return the estimated bearing in degrees, or null if the threat is identified by its address, no bearing
     * estimate is available, or its code is not assigned
     */
    public Float getBearing() {
        if (hasTransponderAddress || bearing == 0 || bearing > 60) return null;
        return 6F * bearing - 3;
    }

    /**
     * The range of the threat's estimated bearing in degrees relative to the ACAS aircraft heading, ICAO Annex 10
     * Volume IV §4.3.8.4.2.2.1.6.3: code n from 1 to 60 is between 6(n - 1) and 6n degrees, both ends included as the
     * Annex states them.
     *
     * @return the range in degrees, or null if the threat is identified by its address, no bearing estimate is
     * available, or its code is not assigned (61 to 63)
     */
    public Interval getBearingInterval() {
        if (hasTransponderAddress || bearing == 0 || bearing > 60) return null;
        return Interval.of(Bound.AT_LEAST, 6 * (bearing - 1), Bound.AT_MOST, 6 * bearing);
    }

}
