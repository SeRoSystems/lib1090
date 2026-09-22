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
 * What an ADS-B version 3 position message says about the integrity of its own position, as one row
 * per format type code and NIC supplement combination, ED-102B §2.2.3.2.3.1 TABLE 2-11.
 * <p>
 * Version 3 keeps supplements A, B and C exactly as version 2 uses them, and adds <b>supplement D</b>,
 * two bits carried in the airborne velocity message. D applies to type codes 20, 21 and 22 alone, and
 * where it applies it replaces A and B rather than joining them — which is what separates this table
 * from version 2's. Those same three type codes are the only rows where the two versions differ at
 * all: version 2 reads them from ED-102B TABLE N-24 instead.
 * <p>
 * For type codes 0 to 18 everything said of {@link NavigationCharacteristicsV2} holds here unchanged,
 * supplement B outranking supplement A included. The rows are restated rather than shared because the
 * two versions are separate documents that happen to agree today; a test asserts that they still do.
 * <p>
 * Supplement D is monotone where the single-bit supplements are not: each higher value reports a
 * smaller radius, so an unknown D yields the {@code D = 0} row, which is both the poorest row and the
 * one the standard assigns to that value. As everywhere else, the answer for partial knowledge is the
 * poorest answer any completion of that knowledge could give.
 *
 * @see NavigationCharacteristicsV2 for version 2, whose type code 20 to 22 rows differ
 */
public enum NavigationCharacteristicsV3 implements NavigationCharacteristics {

    /** No position information; nothing is claimed about a position that is not there. */
    TYPE_CODE_0(0, 0, ContainmentRadius.UNKNOWN),

    TYPE_CODE_5(5, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_6(6, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_7_A_SET(7, 9, ContainmentRadius.BELOW_75),
    TYPE_CODE_7_A_CLEAR(7, 8, ContainmentRadius.BELOW_185_2),

    TYPE_CODE_8_A_SET_C_SET(8, 7, ContainmentRadius.BELOW_370_4),
    TYPE_CODE_8_A_SET_C_CLEAR(8, 6, ContainmentRadius.BELOW_555_6),
    TYPE_CODE_8_A_CLEAR_C_SET(8, 6, ContainmentRadius.BELOW_1111_2),

    /** The only surface row that reports no integrity at all, bounding the radius from below. */
    TYPE_CODE_8_A_CLEAR_C_CLEAR(8, 0, ContainmentRadius.AT_LEAST_1111_2),

    TYPE_CODE_9(9, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_10(10, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_11_A_SET_B_SET(11, 9, ContainmentRadius.BELOW_75),
    TYPE_CODE_11_A_CLEAR_B_CLEAR(11, 8, ContainmentRadius.BELOW_185_2),
    TYPE_CODE_12(12, 7, ContainmentRadius.BELOW_370_4),

    /**
     * The three rows of the one type code where the supplements select rather than refine: all report
     * NIC 6, so only the radius separates them, and it does not grow with the supplements.
     */
    TYPE_CODE_13_A_CLEAR_B_SET(13, 6, ContainmentRadius.BELOW_555_6),
    TYPE_CODE_13_A_CLEAR_B_CLEAR(13, 6, ContainmentRadius.BELOW_926),
    TYPE_CODE_13_A_SET_B_SET(13, 6, ContainmentRadius.BELOW_1111_2),

    TYPE_CODE_14(14, 5, ContainmentRadius.BELOW_1852),
    TYPE_CODE_15(15, 4, ContainmentRadius.BELOW_3704),
    TYPE_CODE_16_A_SET_B_SET(16, 3, ContainmentRadius.BELOW_7408),
    TYPE_CODE_16_A_CLEAR_B_CLEAR(16, 2, ContainmentRadius.BELOW_14816),
    TYPE_CODE_17(17, 1, ContainmentRadius.BELOW_37040),

    /** The airborne counterpart of {@link #TYPE_CODE_8_A_CLEAR_C_CLEAR}. */
    TYPE_CODE_18(18, 0, ContainmentRadius.AT_LEAST_37040),

    /**
     * Type codes 20 and 22 are graded by supplement D alone, and unlike the single-bit supplements it
     * grades monotonically: every step up reports a smaller radius. Type code 21 defines only
     * {@code D = 0}, so nothing there depends on D at all.
     */
    TYPE_CODE_20_D_3(20, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_20_D_2(20, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_20_D_1(20, 9, ContainmentRadius.BELOW_75),
    TYPE_CODE_20_D_0(20, 8, ContainmentRadius.BELOW_185_2),

    TYPE_CODE_21(21, 7, ContainmentRadius.BELOW_370_4),

    TYPE_CODE_22_D_3(22, 6, ContainmentRadius.BELOW_1111_2),
    TYPE_CODE_22_D_2(22, 5, ContainmentRadius.BELOW_1852),
    TYPE_CODE_22_D_1(22, 4, ContainmentRadius.BELOW_3704),

    /** Version 3's third row reporting no integrity, alongside type codes 8 and 18. */
    TYPE_CODE_22_D_0(22, 0, ContainmentRadius.AT_LEAST_3704);

    private final byte formatTypeCode;
    private final byte nic;
    private final ContainmentRadius containmentRadius;

    NavigationCharacteristicsV3(int formatTypeCode, int nic, ContainmentRadius containmentRadius) {
        this.formatTypeCode = (byte) formatTypeCode;
        this.nic = (byte) nic;
        this.containmentRadius = containmentRadius;
    }

    /**
     * The row an airborne position message's format type code and NIC supplements select.
     * <p>
     * Type codes up to 18 read supplements A and B as version 2 does, B taking priority since only a
     * subset of their combinations is tabulated and B is the one that arrives with this message.
     * Type codes 20 to 22 read supplement D instead and ignore A and B entirely.
     *
     * @param formatTypeCode the message's format type code, 0, 9 to 18 or 20 to 22
     * @param nicSupplements what is known of the target's NIC supplements
     * @return the characteristics version 3 assigns to that combination
     * @throws IllegalArgumentException if the type code is not an airborne position type code
     */
    public static NavigationCharacteristicsV3 forAirborneFormatTypeCode(byte formatTypeCode,
                                                                        NICSupplements nicSupplements) {
        NICSupplement nicSupplementA = nicSupplements.getA();
        NICSupplement nicSupplementB = nicSupplements.getB();
        NICSupplementD nicSupplementD = nicSupplements.getD();

        switch (formatTypeCode) {
            case 0:
                return TYPE_CODE_0;
            case 9:
                return TYPE_CODE_9;
            case 10:
                return TYPE_CODE_10;
            case 11:
                return nicSupplementB == NICSupplement.SET
                        ? TYPE_CODE_11_A_SET_B_SET
                        : TYPE_CODE_11_A_CLEAR_B_CLEAR;
            case 12:
                return TYPE_CODE_12;
            case 13:
                // the only type code where B does not settle the row on its own: a set B leaves two,
                // of which a clear A picks the better. A clear B settles it whatever A says, B
                // outranking A there, and an unknown A falls to the poorest row of the three.
                if (nicSupplementB == NICSupplement.SET)
                    return nicSupplementA == NICSupplement.CLEAR
                            ? TYPE_CODE_13_A_CLEAR_B_SET
                            : TYPE_CODE_13_A_SET_B_SET;
                if (nicSupplementB == NICSupplement.CLEAR || nicSupplementA == NICSupplement.CLEAR)
                    return TYPE_CODE_13_A_CLEAR_B_CLEAR;
                return TYPE_CODE_13_A_SET_B_SET;
            case 14:
                return TYPE_CODE_14;
            case 15:
                return TYPE_CODE_15;
            case 16:
                return nicSupplementB == NICSupplement.SET
                        ? TYPE_CODE_16_A_SET_B_SET
                        : TYPE_CODE_16_A_CLEAR_B_CLEAR;
            case 17:
                return TYPE_CODE_17;
            case 18:
                return TYPE_CODE_18;
            case 20:
                switch (nicSupplementD) {
                    case THREE:
                        return TYPE_CODE_20_D_3;
                    case TWO:
                        return TYPE_CODE_20_D_2;
                    case ONE:
                        return TYPE_CODE_20_D_1;
                    default:
                        // D = 0, or not known: either way the poorest row this type code has
                        return TYPE_CODE_20_D_0;
                }
            case 21:
                // only D = 0 is defined here, so nothing depends on the supplement
                return TYPE_CODE_21;
            case 22:
                switch (nicSupplementD) {
                    case THREE:
                        return TYPE_CODE_22_D_3;
                    case TWO:
                        return TYPE_CODE_22_D_2;
                    case ONE:
                        return TYPE_CODE_22_D_1;
                    default:
                        // D = 0, or not known: either way the poorest row this type code has
                        return TYPE_CODE_22_D_0;
                }
            default:
                throw new IllegalArgumentException(
                        "Format type code " + formatTypeCode + " is not an airborne position type code");
        }
    }

    /**
     * The row a surface position message's format type code and NIC supplements select. Supplement D
     * reaches no surface type code, so version 3 reads these exactly as version 2 does.
     *
     * @param formatTypeCode the message's format type code, 0 or 5 to 8
     * @param nicSupplements what is known of the target's NIC supplements
     * @return the characteristics version 3 assigns to that combination
     * @throws IllegalArgumentException if the type code is not a surface position type code
     */
    public static NavigationCharacteristicsV3 forSurfaceFormatTypeCode(byte formatTypeCode,
                                                                       NICSupplements nicSupplements) {
        NICSupplement nicSupplementA = nicSupplements.getA();
        NICSupplement nicSupplementC = nicSupplements.getC();

        switch (formatTypeCode) {
            case 0:
                return TYPE_CODE_0;
            case 5:
                return TYPE_CODE_5;
            case 6:
                return TYPE_CODE_6;
            case 7:
                // supplement C does not reach this type code
                return nicSupplementA == NICSupplement.SET ? TYPE_CODE_7_A_SET : TYPE_CODE_7_A_CLEAR;
            case 8:
                if (nicSupplementA == NICSupplement.SET)
                    return nicSupplementC == NICSupplement.SET
                            ? TYPE_CODE_8_A_SET_C_SET
                            : TYPE_CODE_8_A_SET_C_CLEAR;
                return nicSupplementC == NICSupplement.SET
                        ? TYPE_CODE_8_A_CLEAR_C_SET
                        : TYPE_CODE_8_A_CLEAR_C_CLEAR;
            default:
                throw new IllegalArgumentException(
                        "Format type code " + formatTypeCode + " is not a surface position type code");
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
     * @return the encoded navigation integrity category
     */
    @Override
    public byte getNICEncoded() {
        return nic;
    }

    /**
     * The horizontal containment radius limit, with the side of the value the true radius lies on.
     * Travels with {@link #getNICEncoded()} and must not be derived from it — type code 13 reports NIC 6 at
     * three different radii, and surface type code 8 reports NIC 6 at two.
     *
     * @return the containment radius
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return containmentRadius;
    }
}
