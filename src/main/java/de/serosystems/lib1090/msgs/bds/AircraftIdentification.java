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
import de.serosystems.lib1090.exceptions.BadFormatException;

/**
 * Decoder for aircraft identification (BDS 2,0), as defined in ICAO Doc 9871 (First Edition,
 * AN/464) §A.2 TABLE A-2-32. Individual callsign characters are decoded by
 * {@link Identification#identificationDigits(long)}, whose 6-bit IA-5 alphabet is cited in
 * ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8.
 */
@SuppressWarnings("unused")
public class AircraftIdentification extends BDSRegister {
    private static final long serialVersionUID = -8005492828828163576L;

    private static final BDSCode BDS_CODE = new BDSCode(2, 0);

    private long aircraftIdentificationEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected AircraftIdentification() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     * @throws BadFormatException if MB bits 1-8 are not 0010 0000, BDS1 = 2 and BDS2 = 0 (ICAO Doc 9871 §A.2 Table
     *                            A-2-32)
     */
    public AircraftIdentification(byte[] message) throws BadFormatException {
        super(message);
        requireBDSCode();

        aircraftIdentificationEncoded = BitReader.forBigEndian(message).readLong(9, 56);
    }

    /**
     * @return the eight 6-bit characters of the aircraft identification as transmitted
     */
    public long getAircraftIdentificationEncoded() {
        return aircraftIdentificationEncoded;
    }

    /**
     * @return the eight characters of the aircraft identification as 6-bit IA-5 codes
     */
    public byte[] getAircraftIdentificationDigits() {
        return Identification.identificationDigits(aircraftIdentificationEncoded);
    }

    /**
     * @return the call sign as an array of 8 characters; a character whose code is not defined is decoded as a
     * space, see {@link #hasValidAircraftIdentification()}
     */
    public char[] getAircraftIdentification() {
        return InternationalAlphabet5.mapChar(getAircraftIdentificationDigits());
    }

    /**
     * Whether every character has a code that ICAO Annex 10 Volume IV §3.1.2.9.1.2 TABLE 3-8 defines. As a
     * Comm-B reply does not say which register it carries, this is one indication of whether it is BDS 2,0.
     *
     * @return true if every character of the call sign can be decoded
     */
    public boolean hasValidAircraftIdentification() {
        return InternationalAlphabet5.isDefined(getAircraftIdentificationDigits());
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "AircraftIdentification{" + super.toString() +
                ", aircraftIdentificationEncoded=" + aircraftIdentificationEncoded +
                '}';
    }

}
