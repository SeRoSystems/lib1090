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

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.BitReader;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decoder for the common usage GICB capability report (BDS 1,7), as defined in ICAO Doc 9871
 * (First Edition, AN/464) §A.2 TABLE A-2-23.
 */
@SuppressWarnings("unused")
public class CommonUsageGICBCapabilityReport extends BDSRegister implements Serializable {
    private static final long serialVersionUID = 2537138385702205527L;

    private static final BDSCode BDS_CODE = new BDSCode(1, 7);

    // Common Usage GICB Capability Report
    private Map<BDSCode, Boolean> commonUsageGICBCapabilityReport;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected CommonUsageGICBCapabilityReport() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public CommonUsageGICBCapabilityReport(byte[] message) {
        super(message);

        BitReader b = BitReader.forBigEndian(message);

        LinkedHashMap<BDSCode, Boolean> map = new LinkedHashMap<>();

        // BDS 0,5 Extended Squitter Airborne Position
        map.put(new BDSCode(0, 5), b.readBoolean(1));
        // BDS 0,6 Extended Squitter Surface Position
        map.put(new BDSCode(0, 6), b.readBoolean(2));
        // BDS 0,7 Extended Squitter Status
        map.put(new BDSCode(0, 7), b.readBoolean(3));
        // BDS 0,8 Extended Squitter Identification and Category
        map.put(new BDSCode(0, 8), b.readBoolean(4));
        // BDS 0,9 Extended Squitter Airborne Velocity Information
        map.put(new BDSCode(0, 9), b.readBoolean(5));
        // BDS 0,A Extended Squitter Event-Driven Information
        map.put(new BDSCode(0, 0xA), b.readBoolean(6));
        // BDS 2,0 Aircraft identification
        map.put(new BDSCode(2, 0), b.readBoolean(7));
        // BDS 2,1 Aircraft registration number
        map.put(new BDSCode(2, 1), b.readBoolean(8));
        // BDS 4,0 Selected vertical intention
        map.put(new BDSCode(4, 0), b.readBoolean(9));
        // BDS 4,1 Next waypoint identifier
        map.put(new BDSCode(4, 1), b.readBoolean(10));
        // BDS 4,2 Next waypoint position
        map.put(new BDSCode(4, 2), b.readBoolean(11));
        // BDS 4,3 Next waypoint information
        map.put(new BDSCode(4, 3), b.readBoolean(12));
        // BDS 4,4 Meteorological routine report
        map.put(new BDSCode(4, 4), b.readBoolean(13));
        // BDS 4,5 Meteorological hazard report
        map.put(new BDSCode(4, 5), b.readBoolean(14));
        // BDS 4,8 VHF channel report
        map.put(new BDSCode(4, 8), b.readBoolean(15));
        // BDS 5,0 Track and turn report
        map.put(new BDSCode(5, 0), b.readBoolean(16));
        // BDS 5,1 Position coarse
        map.put(new BDSCode(5, 1), b.readBoolean(17));
        // BDS 5,2 Position fine
        map.put(new BDSCode(5, 2), b.readBoolean(18));
        // BDS 5,3 Air-referenced state vector
        map.put(new BDSCode(5, 3), b.readBoolean(19));
        // BDS 5,4 Waypoint 1
        map.put(new BDSCode(5, 4), b.readBoolean(20));
        // BDS 5,5 Waypoint 2
        map.put(new BDSCode(5, 5), b.readBoolean(21));
        // BDS 5,6 Waypoint 3
        map.put(new BDSCode(5, 6), b.readBoolean(22));
        // BDS 5,F Quasi-static parameter monitoring
        map.put(new BDSCode(5, 0xF), b.readBoolean(23));
        // BDS 6,0 Heading and speed report
        map.put(new BDSCode(6, 0), b.readBoolean(24));
        // BDS E,1 Reserved for Mode S BITE (Built In Test Equipment)
        map.put(new BDSCode(0xE, 1), b.readBoolean(27));
        // BDS E,2 Reserved for Mode S BITE (Built In Test Equipment)
        map.put(new BDSCode(0xE, 2), b.readBoolean(28));
        // BDS F,1 Military applications
        map.put(new BDSCode(0xF, 1), b.readBoolean(29));

        commonUsageGICBCapabilityReport = Collections.unmodifiableMap(map);
    }

    /**
     * The registers this report covers, in the order of Table A-2-23, each mapped to whether it is
     * available in the aircraft installation. A register the report does not cover is not a key.
     *
     * @return an unmodifiable map from BDS code to whether that register is supported
     */
    public Map<BDSCode, Boolean> getCommonUsageGICBCapabilityReport() {
        return commonUsageGICBCapabilityReport;
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "CommonUsageGICBCapabilityReport{" + super.toString() +
                ", commonUsageGICBCapabilityReport=" + commonUsageGICBCapabilityReport +
                '}';
    }

}
