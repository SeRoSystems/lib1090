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
import de.serosystems.lib1090.decoding.Identification;
import de.serosystems.lib1090.decoding.InternationalAlphabet5;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Decoder for aircraft identification (BDS 2,0), ICAO Doc 9871 (First Edition, AN/464)
 * §A.2 TABLE A-2-32, p. 79. Individual callsign characters are decoded by
 * {@link Identification#identificationDigits(long)}, whose 6-bit IA-5 alphabet is cited in
 * ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8.
 */
@SuppressWarnings("unused")
public class AircraftIdentification extends BDSRegister implements Serializable {
    private static final long serialVersionUID = -8005492828828163576L;

    // BDS Code
    private short bdsCode;
    // aircraft Identification
    private byte[] aircraftIdentification;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AircraftIdentification() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public AircraftIdentification(byte[] message) {
        super(message);
        setBds(BDSRegister.bdsCode.AIRCRAFT_IDENTIFICATION);

        this.bdsCode = extractBdsCode(message);
        this.aircraftIdentification = Identification.identificationDigits(BitReader.forBigEndian(message).readLong(9, 56));
    }

    /**
     * @return The call sign as 8 characters array
     */
    public char[] getAircraftIdentification() {
        return InternationalAlphabet5.mapChar(aircraftIdentification);
    }

    @Override
    public String toString() {
        return "AircraftIdentification{" +
                "bdsCode=" + bdsCode +
                ", aircraftIdentification=" + Arrays.toString(aircraftIdentification) +
                '}';
    }

}
