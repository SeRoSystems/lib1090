package de.serosystems.lib1090.bds;

import java.io.Serializable;

public class ThreatIdentityData implements Serializable {

    private Long icao24;
    private Short altitudeCode;
    private Short threatIdentityDataRange;
    private Short threatIdentityDataBearing;

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected ThreatIdentityData(){}

    public ThreatIdentityData(Long icao24, Short altitudeCode, Short threatIdentityDataRange, Short threatIdentityDataBearing) {
        this.icao24 = icao24;
        this.altitudeCode = altitudeCode;
        this.threatIdentityDataRange = threatIdentityDataRange;
        this.threatIdentityDataBearing = threatIdentityDataBearing;
    }

    public Long getIcao24() {
        return icao24;
    }

    public Short getAltitudeCode() {
        return altitudeCode;
    }

    public Short getThreatIdentityDataRange() {
        return threatIdentityDataRange;
    }

    public Short getThreatIdentityDataBearing() {
        return threatIdentityDataBearing;
    }

}
