package de.serosystems.lib1090.bds;

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

public class ThreatIdentityData implements Serializable {

    private Long icao24;
    private Short altitudeCode;
    private Short threatIdentityDataRange;
    private Short threatIdentityDataBearing;

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected ThreatIdentityData(){}

    /**
     *
     * @param icao24                        the ICAO 24-bit aircraft address
     * @param altitudeCode                  the barometric altitude
     * @param threatIdentityDataRange       the threat identity data range, i.e. the most recent threat range estimated
     *                                      by TCAS [NM]
     * @param threatIdentityDataBearing     the threat identity data bearing, i.e. the most recent estimated bearing of
     *                                      the threat aircraft, relative to the TCAS aircraft heading [deg].
     */
    public ThreatIdentityData(Long icao24, Short altitudeCode, Short threatIdentityDataRange, Short threatIdentityDataBearing) {
        this.icao24 = icao24;
        this.altitudeCode = altitudeCode;
        this.threatIdentityDataRange = threatIdentityDataRange;
        this.threatIdentityDataBearing = threatIdentityDataBearing;
    }

    /**
     * @return the ICAO 24-bit aircraft address
     */
    public Long getIcao24() {
        return icao24;
    }

    /**
     * @return the barometric altitude
     */
    public Short getAltitudeCode() {
        return altitudeCode;
    }

    /**
     * @return the threat identity data range
     */
    public Short getThreatIdentityDataRange() {
        return threatIdentityDataRange;
    }

    /**
     * @return the threat identity data bearing
     */
    public Short getThreatIdentityDataBearing() {
        return threatIdentityDataBearing;
    }

}
