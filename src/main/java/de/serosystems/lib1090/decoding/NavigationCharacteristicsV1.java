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

/**
 * What an ADS-B version 1 position message says about the quality of its own position, as one row per
 * format type code and NIC supplement, ED-102B §N.3.2.2 TABLE N-16.
 * <p>
 * Version 1 introduced NIC, and with it a single NIC supplement bit — later renamed NIC supplement A —
 * which four type codes use to refine what the type code alone says. The supplement is transmitted in
 * the operational status message, not in the position message, so a receiver that has not yet seen an
 * operational status message for a target does not know it.
 * <p>
 * <b>That unknown state cannot be substituted by a constant.</b> At type codes 7, 11 and 16 a clear
 * supplement is the poorer report; at type code 13 it is the set one, where clear gives Rc &lt; 926 m
 * and set gives Rc &lt; 1111.2 m. Either assumption therefore overstates the position quality for some
 * type code. {@link #forFormatTypeCode(byte, NICSupplements)} instead answers
 * {@link NICSupplement#UNKNOWN} with the poorer of the two rows, which is the only claim a receiver
 * can defend when it has not seen the bit.
 * <p>
 * NIC and the containment radius are separate columns and neither may be computed from the other. Type
 * code 13 reports NIC 6 at two different radii, and NIC 0 covers "nothing reported" as well as three
 * different lower bounds.
 *
 * @see NavigationCharacteristicsV0 for version 0, where the type code alone settles everything
 */
public enum NavigationCharacteristicsV1 implements NavigationCharacteristics {

    /**
     * No position information; nothing is claimed about a position that is not there.
     */
    TYPE_CODE_0(0, 0, ContainmentRadius.UNKNOWN),

    TYPE_CODE_5(5, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_6(6, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_7_SUPPLEMENT_SET(7, 9, ContainmentRadius.BELOW_75),
    TYPE_CODE_7_SUPPLEMENT_CLEAR(7, 8, ContainmentRadius.BELOW_185_2),

    /**
     * The one surface type code that bounds its containment radius only from below.
     */
    TYPE_CODE_8(8, 0, ContainmentRadius.AT_LEAST_185_2),

    TYPE_CODE_9(9, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_10(10, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_11_SUPPLEMENT_SET(11, 9, ContainmentRadius.BELOW_75),
    TYPE_CODE_11_SUPPLEMENT_CLEAR(11, 8, ContainmentRadius.BELOW_185_2),
    TYPE_CODE_12(12, 7, ContainmentRadius.BELOW_370_4),

    /**
     * The type code where the supplement runs the other way: setting it reports the <i>larger</i>
     * radius, so a clear supplement is the better case here and the worse case everywhere else. Both
     * rows report NIC 6, so the radius alone separates them.
     */
    TYPE_CODE_13_SUPPLEMENT_SET(13, 6, ContainmentRadius.BELOW_1111_2),
    TYPE_CODE_13_SUPPLEMENT_CLEAR(13, 6, ContainmentRadius.BELOW_926),

    TYPE_CODE_14(14, 5, ContainmentRadius.BELOW_1852),
    TYPE_CODE_15(15, 4, ContainmentRadius.BELOW_3704),
    TYPE_CODE_16_SUPPLEMENT_SET(16, 3, ContainmentRadius.BELOW_7408),
    TYPE_CODE_16_SUPPLEMENT_CLEAR(16, 2, ContainmentRadius.BELOW_14816),
    TYPE_CODE_17(17, 1, ContainmentRadius.BELOW_37040),

    /**
     * The airborne counterpart of {@link #TYPE_CODE_8}: a containment radius bounded from below.
     */
    TYPE_CODE_18(18, 0, ContainmentRadius.AT_LEAST_37040),

    TYPE_CODE_20(20, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_21(21, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_22(22, 0, ContainmentRadius.AT_LEAST_25);

    private final byte formatTypeCode;
    private final byte nic;
    private final ContainmentRadius containmentRadius;

    NavigationCharacteristicsV1(int formatTypeCode, int nic, ContainmentRadius containmentRadius) {
        this.formatTypeCode = (byte) formatTypeCode;
        this.nic = (byte) nic;
        this.containmentRadius = containmentRadius;
    }

    /**
     * The row a position message's format type code and NIC supplement select.
     * <p>
     * Only four type codes look at the supplement at all; the rest report the same row whatever it
     * says, which is what a receiver needs, since the supplement describes the aircraft and an
     * aircraft transmitting a set supplement still sends those type codes.
     * <p>
     * Where the supplement does matter, the condition is written so that
     * {@link NICSupplement#UNKNOWN} falls to the poorer row — the clear one at type codes 7, 11 and
     * 16, and the set one at type code 13, where the supplement runs the other way.
     *
     * @param formatTypeCode the message's format type code
     * @param nicSupplements what is known of the aircraft's NIC supplements; version 1 reads
     *                       only supplement A, the single bit it defines
     * @return the characteristics version 1 assigns to that combination
     * @throws IllegalArgumentException if the type code belongs to no position message, since no row
     *                                  exists for it and reporting "unknown" would hide the caller's
     *                                  mistake
     */
    public static NavigationCharacteristicsV1 forFormatTypeCode(byte formatTypeCode,
                                                                NICSupplements nicSupplements) {
        NICSupplement nicSupplement = nicSupplements.getA();

        switch (formatTypeCode) {
            case 0:
                return TYPE_CODE_0;
            case 5:
                return TYPE_CODE_5;
            case 6:
                return TYPE_CODE_6;
            case 7:
                return nicSupplement == NICSupplement.SET
                        ? TYPE_CODE_7_SUPPLEMENT_SET
                        : TYPE_CODE_7_SUPPLEMENT_CLEAR;
            case 8:
                return TYPE_CODE_8;
            case 9:
                return TYPE_CODE_9;
            case 10:
                return TYPE_CODE_10;
            case 11:
                return nicSupplement == NICSupplement.SET
                        ? TYPE_CODE_11_SUPPLEMENT_SET
                        : TYPE_CODE_11_SUPPLEMENT_CLEAR;
            case 12:
                return TYPE_CODE_12;
            case 13:
                // the one type code where a set supplement is the poorer report, so an unknown
                // supplement falls to the set row rather than the clear one
                return nicSupplement == NICSupplement.CLEAR
                        ? TYPE_CODE_13_SUPPLEMENT_CLEAR
                        : TYPE_CODE_13_SUPPLEMENT_SET;
            case 14:
                return TYPE_CODE_14;
            case 15:
                return TYPE_CODE_15;
            case 16:
                return nicSupplement == NICSupplement.SET
                        ? TYPE_CODE_16_SUPPLEMENT_SET
                        : TYPE_CODE_16_SUPPLEMENT_CLEAR;
            case 17:
                return TYPE_CODE_17;
            case 18:
                return TYPE_CODE_18;
            case 20:
                return TYPE_CODE_20;
            case 21:
                return TYPE_CODE_21;
            case 22:
                return TYPE_CODE_22;
            default:
                throw new IllegalArgumentException(
                        "Format type code " + formatTypeCode + " is not a position message type code");
        }
    }

    /**
     * @return the format type code this row belongs to
     */
    @Override
    public byte getFormatTypeCode() {
        return formatTypeCode;
    }

    /**
     * @return the navigation integrity category
     */
    @Override
    public byte getNIC() {
        return nic;
    }

    /**
     * The horizontal containment radius limit, with the side of the value the true radius lies on.
     * Travels with {@link #getNIC()} and must not be derived from it — type code 13 reports NIC 6 at
     * two different radii.
     *
     * @return the containment radius
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return containmentRadius;
    }
}
