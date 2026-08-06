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
        return reader.readBoolean(28);
    }

    public static boolean decodeRaTerminated(BitReader reader) {
        return reader.readBoolean(27);
    }

    public static byte decodeRacRecord(BitReader reader) {
        return reader.readByte(23, 26);
    }

    public static short decodeActiveRa(BitReader reader) {
        return reader.readShort(9, 22);
    }

    public static boolean[] extractActiveResolutionAdvisories(BitReader reader) {
        return new boolean[]{
                reader.readBoolean(9),
                reader.readBoolean(10),
                reader.readBoolean(11),
                reader.readBoolean(12),
                reader.readBoolean(13),
                reader.readBoolean(14),
                reader.readBoolean(15),
                reader.readBoolean(16),
                reader.readBoolean(17),
                reader.readBoolean(18),
                reader.readBoolean(19),
                reader.readBoolean(20),
                reader.readBoolean(21),
                reader.readBoolean(22)
        };
    }

    public static boolean[] extractResolutionAdvisoriesComplementsRecord(BitReader reader) {
        boolean doNotPassBelow = reader.readBoolean(23);
        boolean doNotPassAbove = reader.readBoolean(24);
        boolean doNotTurnLef = reader.readBoolean(25);
        boolean doNotTurnRight = reader.readBoolean(26);

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
