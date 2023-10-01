package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.Altitude;
import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoded threat identity info according to Annex 10 V4 4.3.8.4.2.2.1.6
 */
@SuppressWarnings("unused")
public class ThreatIdentityData implements Serializable {

    private Integer icao24;
    private Short altitudeCode;
    private Short range;
    private Short bearing;
    private boolean hasTransponderAddress;

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected ThreatIdentityData() { }

    /**
     * Create a new instance where the threat is identified by a 24 bit transponder address.
     *
     * @param icao24                        the ICAO 24-bit aircraft address
     */
    public ThreatIdentityData(Integer icao24) {
        this.icao24 = icao24;
        hasTransponderAddress = true;
    }

    /**
     * Create a new instance where the target is identified by altitude, range and bearing.
     *
     * @param altitudeCode    the barometric altitude
     * @param range           the threat identity data range, i.e. the most recent threat range estimated
     *                        by TCAS
     * @param bearing         the threat identity data bearing, i.e. the most recent estimated bearing of
     *                        the threat aircraft, relative to the TCAS aircraft heading.
     * @throws BadFormatException if range outside the interval [0, 127] or bearing &gt; 60
     */
    public ThreatIdentityData(Short altitudeCode, Short range, Short bearing) throws BadFormatException {
        this.altitudeCode = altitudeCode;
        this.range = range;
        this.bearing = bearing;

        hasTransponderAddress = false;

        if (range > 127 || bearing > 60) {
            throw new BadFormatException("Threat identity data range must be between 0 and 127");
        }
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
     * Get the altitude code. See {@link #getAltitude()} for the value in feet.
     *
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
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * @return the decoded barometric altitude in feet if applicable
     */
    public Integer getAltitude() {
        return Altitude.decode13BitAltitude(altitudeCode);
    }

    /**
     * Get the decoded lower bound for the range in NM
     *
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * See Annex 10 V4 4.3.8.4.2.2.1.6.2
     *
     * @return null if the target is identified by its 24 bit address or the range estimate is not available;
     * 0.05 if the estimate is less than 0.05NM, or the actual estimated range (shortest estimate) in NM
     */
    public Float getRange() {
        if (range == 0) {
            return null;
        }

        if (range == 1) {
            return 0.05F;
        }

        return (range - 1) / 10 - 0.05F;
    }

    /**
     * Get the decoded lower bound for the bearing in degrees
     *
     * The method returns "null" when the target is identified by its ICAO 24 bit address, i.e., when
     * {@link #hasTransponderAddress()} is true
     *
     * See Annex 10 V4 4.3.8.4.2.2.1.6.3
     *
     * @return null if the target is identified by its 24 bit address or the bearing estimate is not available;
     *         an array with two elements defining the estimated lower and upper bound for the bearing estimate in
     *         degrees
     */
    public Float[] getBearing() {
        if (bearing == 0) {
            return null;
        }

        return new Float[] { 6F * (bearing - 1), 6F * bearing };
    }

}
