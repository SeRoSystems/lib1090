package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.msgs.adsb.IdentificationMsg;

import java.io.Serializable;
import java.util.Arrays;

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
 * Decoder for aircraft identification (BDS 2,0)
 */
@SuppressWarnings("unused")
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

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public AircraftIdentification(byte[] message) {

        super(message);
        setBds(BDSRegister.bdsCode.AIRCRAFT_IDENTIFICATION);

        this.bdsCode = extractBdsCode(message);
        this.aircraftIdentification = IdentificationMsg.decodeAircraftIdentification(message);

    }

    // Getters
    // -------

    /**
     * @return The call sign as 8 characters array
     */
    public char[] getAircraftIdentification() {
        return IdentificationMsg.mapChar(aircraftIdentification);
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
