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

package de.serosystems.lib1090.decoding;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.bds.ThreatIdentityData;

/**
 * Decoding helpers for TCAS/ACAS resolution advisory payloads.
 */
public class TCASResolutionAdvisory {

    public static int decodeThreatIdentity(BitReader reader) {
        return reader.readInt(31, 56);
    }

    public static byte decodeThreatType(BitReader reader) {
        return reader.readByte(29, 30);
    }

    public static boolean decodeMultiThreatEncounter(BitReader reader) {
        return reader.readByte(28, 28) == 1;
    }

    public static boolean decodeRaTerminated(BitReader reader) {
        return reader.readByte(27, 27) == 1;
    }

    public static byte decodeRacRecord(BitReader reader) {
        return reader.readByte(23, 26);
    }

    public static short decodeActiveRa(BitReader reader) {
        return reader.readShort(9, 22);
    }

    public static boolean[] extractActiveResolutionAdvisories(BitReader reader) {
        return new boolean[]{
                reader.readByte(9, 9) == 1,
                reader.readByte(10, 10) == 1,
                reader.readByte(11, 11) == 1,
                reader.readByte(12, 12) == 1,
                reader.readByte(13, 13) == 1,
                reader.readByte(14, 14) == 1,
                reader.readByte(15, 15) == 1,
                reader.readByte(16, 16) == 1,
                reader.readByte(17, 17) == 1,
                reader.readByte(18, 18) == 1,
                reader.readByte(19, 19) == 1,
                reader.readByte(20, 20) == 1,
                reader.readByte(21, 21) == 1,
                reader.readByte(22, 22) == 1
        };
    }

    public static boolean[] extractResolutionAdvisoriesComplementsRecord(BitReader reader) {
        boolean doNotPassBelow = reader.readByte(23, 23) == 1;
        boolean doNotPassAbove = reader.readByte(24, 24) == 1;
        boolean doNotTurnLef = reader.readByte(25, 25) == 1;
        boolean doNotTurnRight = reader.readByte(26, 26) == 1;

        return new boolean[]{doNotPassBelow, doNotPassAbove, doNotTurnLef, doNotTurnRight};
    }

    public static ThreatIdentityData extractThreatIdentityData(short threatTypeIndicator, BitReader reader) throws BadFormatException {
        ThreatIdentityData threatIdentityData = null;

        switch (threatTypeIndicator) {
            case 1:
                int icao = reader.readInt(31, 54);
                threatIdentityData = new ThreatIdentityData(icao);
                break;
            case 2:
                short altitudeCode = reader.readShort(31, 43);
                short threatIdentityDataRange = reader.readShort(44, 50);
                short threatIdentityDataBearing = reader.readShort(51, 56);
                threatIdentityData = new ThreatIdentityData(altitudeCode, threatIdentityDataRange, threatIdentityDataBearing);
                break;
        }

        return threatIdentityData;
    }
}
