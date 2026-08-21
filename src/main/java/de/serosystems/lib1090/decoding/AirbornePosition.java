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

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;

import java.time.Instant;
import java.util.Objects;

public final class AirbornePosition {

    private AirbornePosition() {
    }

    /**
     * Validate whether the given format type code denotes an airborne position message, per the
     * ADS-B message type determination in ED-102B §2.2.3.2.2 TABLE 2-9: type code 0, 9-18 or
     * 20-22 identifies an Airborne Position Message, defined in ED-102B §2.2.3.2.3.
     *
     * @throws BadFormatException if the format type code is not an airborne position type
     */
    public static void validateAirbornePositionFormat(byte formatTypeCode) throws BadFormatException {
        if (!(formatTypeCode == 0 ||
                (formatTypeCode >= 9 && formatTypeCode <= 18) ||
                (formatTypeCode >= 20 && formatTypeCode <= 22))) {
            throw new BadFormatException("This is not a position message! Wrong format type code (" + formatTypeCode + ").");
        }
    }

    /**
     * Extract the CPR-encoded airborne position from the message payload.
     *
     * @param br        bit reader positioned over the 7-byte extended squitter payload
     * @param timestamp timestamp for the position message
     * @return the encoded airborne position
     */
    public static CPREncodedPosition extractCPREncodedPosition(BitReader br, Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp");
        boolean cprFormat = br.readBoolean(22);
        int cprEncodedLat = br.readInt(23, 39);
        int cprEncodedLon = br.readInt(40, 56);
        return CPREncodedPosition.ofAirborne(17, cprFormat, cprEncodedLat, cprEncodedLon, timestamp);
    }

    /**
     * Values according to ED-102B §2.2.3.2.7.2.6 TABLE 2-67 (PIC to NIC, PPM Type Code and NIC
     * Supplement Mapping), using only the NIC Supplement-A column.
     *
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    public static byte decodeNIC(byte formatTypeCode, boolean nicSupplA) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 11;
            case 10:
            case 21:
                return 10;
            case 11:
                return (byte) (nicSupplA ? 9 : 8);
            case 12:
                return 7;
            case 13:
                return 6;
            case 14:
                return 5;
            case 15:
                return 4;
            case 16:
                return (byte) (nicSupplA ? 3 : 2);
            case 17:
                return 1;
            // case 0: case 18: case 22: return 0;
            default:
                return 0;
        }
    }

    /**
     * According to ED-102B §2.2.3.2.7.2.6 TABLE 2-67 (PIC to NIC, PPM Type Code and NIC Supplement
     * Mapping).
     *
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    public static byte decodeNIC(byte formatTypeCode, boolean nicSupplA, boolean nicSupplB) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 11;
            case 10:
            case 21:
                return 10;
            case 11:
                return (byte) (nicSupplB && nicSupplA ? 9 : 8);
            case 12:
                return 7;
            case 13:
                return 6;
            case 14:
                return 5;
            case 15:
                return 4;
            case 16:
                return (byte) (nicSupplB && nicSupplA ? 3 : 2);
            case 17:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * NIC for ADS-B version 3 format type codes 20-22, based on NIC supplement D. For FTC=21, only
     * D=0 is currently defined by the spec; since NIC=7 is already the worst (lowest) integrity
     * guaranteed for this format type code, any D value is decoded as NIC=7.
     *
     * @return Navigation integrity category. A NIC of 0 means "unknown".
     */
    public static byte decodeNIC(byte formatTypeCode, byte nicSupplementD) {
        switch (formatTypeCode) {
            case 20:
                switch (nicSupplementD) {
                    case 3:
                        return 11;
                    case 2:
                        return 10;
                    case 1:
                        return 9;
                    default:
                        return 8;
                }
            case 21:
                // only D=0 is defined for FTC=21, but NIC=7 is already the worst value for this FTC
                return 7;
            case 22:
                switch (nicSupplementD) {
                    case 3:
                        return 6;
                    case 2:
                        return 5;
                    case 1:
                        return 4;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. For the navigation accuracy category
     * (NACp) see {@link AirborneOperationalStatusV1Msg}. Values according to ED-102B §2.2.3.2.7.2.6 TABLE 2-67
     * (PIC to NIC, PPM Type Code and NIC Supplement Mapping), Radius of Containment column.
     * <p>
     * The horizontal containment radius is also known as "horizontal protection level".
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     * If aircraft uses ADS-B version 1+, set NIC supplement A from Operational Status Message
     * for better precision.
     */
    public static double decodeHCR(byte formatTypeCode, boolean nicSupplA) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 7.5;
            case 10:
            case 21:
                return 25;
            case 11:
                return nicSupplA ? 75.0 : 185.2;
            case 12:
                return 370.4;
            case 13:
                return nicSupplA ? 1111.2 : 926;
            case 14:
                return 1852;
            case 15:
                return 3704;
            case 16:
                return nicSupplA ? 7408 : 14816;
            case 17:
                return 37040;
            // case 0: case 18: case 22: return -1;
            default:
                return -1;
        }
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position.
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     */
    public static double decodeHCR(byte formatTypeCode, boolean nicSupplA, boolean nicSupplB) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 7.5;
            case 10:
            case 21:
                return 25;
            case 11:
                return nicSupplB && nicSupplA ? 75 : 185.2;
            case 12:
                return 370.4;
            case 13:
                if (!nicSupplB) return 926;
                else return nicSupplA ? 1111.2 : 555.6;
            case 14:
                return 1852;
            case 15:
                return 3704;
            case 16:
                return nicSupplB && nicSupplA ? 7408 : 14816;
            case 17:
                return 37040;
            default:
                return -1;
        }
    }

    /**
     * HCR for ADS-B version 3 format type codes 20-22, based on NIC supplement D. For FTC=21, only
     * D=0 is currently defined by the spec; since 370.4m is already the worst (highest) containment
     * radius guaranteed for this format type code, any D value is decoded as 370.4m.
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown" or &gt;=3704m.
     */
    public static double decodeHCR(byte formatTypeCode, byte nicSupplementD) {
        switch (formatTypeCode) {
            case 20:
                switch (nicSupplementD) {
                    case 3:
                        return 7.5;
                    case 2:
                        return 25;
                    case 1:
                        return 75;
                    default:
                        return 185.2;
                }
            case 21:
                return 370.4;
            case 22:
                switch (nicSupplementD) {
                    case 3:
                        return 1111.2;
                    case 2:
                        return 1852;
                    case 1:
                        return 3704;
                    default:
                        return -1;
                }
            default:
                return -1;
        }
    }

    /**
     * According to ED-102B §N.2.2.2 TABLE N-4 (Version Zero (0) Format Type Code Mapping to
     * Navigation Source Characteristics).
     *
     * @param formatTypeCode the messages' format type code
     * @return the derived HCR
     */
    public static double typeCodeToHCR(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 7.5;
            case 10:
            case 21:
                return 25;
            case 11:
                return 185.2;
            case 12:
                return 370.4;
            case 13:
                return 926;
            case 14:
                return 1852;
            case 15:
                return 3704;
            case 16:
                return 18520;
            case 17:
                return 37040;
            // case 0: case 18: case 22: return -1;
            default:
                return -1;
        }
    }

    /**
     * According to ED-102B §N.2.3.7 TABLE N-9 (Type Code to NACP Mapping).
     *
     * @param formatTypeCode the messages' format type code
     * @return the derived NACp
     */
    public static byte typeCodeToNACp(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 11;
            case 10:
            case 21:
                return 10;
            case 11:
                return 8;
            case 12:
                return 7;
            case 13:
                return 6;
            case 14:
                return 5;
            case 15:
                return 4;
            case 16:
            case 17:
                return 1;
            // case 0: case 18: case 22: return 0;
            default:
                return 0;
        }
    }

    /**
     * According to ED-102B §N.2.3.7 TABLE N-9 (Type Code to NACP Mapping), "Position Error
     * (95%)" column.
     *
     * @param formatTypeCode the messages' format type code
     * @return the derived position uncertainty in meters
     */
    public static double typeCodeToPositionUncertainty(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 9:
                return 3;
            case 10:
                return 10;
            case 11:
                return 92.6;
            case 12:
                return 185.2;
            case 13:
                return 463;
            case 14:
                return 926;
            case 15:
                return 1852;
            case 16:
                return 9260;
            case 17:
                return 18520;
            // case 0: case 18: case 22: return -1;
            default:
                return -1;
        }
    }

    /**
     * According to ED-102B §N.2.2.2 TABLE N-4 (Version Zero (0) Format Type Code Mapping to
     * Navigation Source Characteristics), "Reported NIC" column.
     *
     * @param formatTypeCode the messages' format type code
     * @return the derived NIC
     */
    public static byte typeCodeToNIC(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 9:
            case 20:
                return 11;
            case 6:
            case 10:
            case 21:
                return 10;
            case 7:
            case 11:
                return 8;
            case 12:
                return 7;
            case 13:
                return 6;
            case 14:
                return 5;
            case 15:
                return 4;
            case 16:
            case 17:
                return 1;
            // case 0: case 18: case 22: return 0;
            default:
                return 0;
        }
    }

    /**
     * According to ED-102B §N.2.3.9 TABLE N-10 (SIL Reporting).
     *
     * @param formatTypeCode the messages' format type code
     * @return the derived SIL
     */
    public static byte typeCodeToSIL(byte formatTypeCode) {
        switch (formatTypeCode) {
            case 0:
            case 18:
            case 22:
                return 0;
            default:
                return 2;
        }
    }
}
