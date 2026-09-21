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
 * What an ADS-B version 2 position message says about the integrity of its own position, as one row
 * per format type code and NIC supplement combination.
 * <p>
 * The table originates in ED-102A TABLE 2-14. ED-102B carries it as TABLE 2-11 for type codes up to
 * 18, and since that table adds the version 3 NIC supplement D for type codes 20 to 22, ED-102B
 * TABLE N-24 supplies those three rows as version 2 defines them. The two together are ED-102A
 * TABLE 2-14 again.
 * <p>
 * Version 2 uses three supplements, and which two apply depends on the subtype: <b>A and B</b>
 * airborne, <b>A and C</b> surface. Hence the two entry points: each reads the two its rows are keyed
 * on and ignores the third, as every table ignores the supplements its rows do not turn on.
 * <p>
 * <b>Where the supplements come from decides how much is ever unknown.</b> Supplement B travels in
 * ME bit 8 of the airborne position message itself, so an ADS-B receiver always has it; A and C
 * travel in the operational status message, so a receiver holding a position for a target it has not
 * heard an operational status from has neither.
 * <p>
 * <b>Airborne, supplement B outranks supplement A.</b> The source tables define only a subset of the
 * A/B combinations, leaving a receiver without a row whenever it observes one of the rest. Supplement
 * B is the sounder of the two to resolve that by: it arrives with the very message it qualifies,
 * whereas A comes from a separate operational status message that may have never been received, or
 * may have been received long enough ago to no longer describe the aircraft. So B decides wherever
 * the two disagree, and at type codes 11 and 16 — where the tables pair a set B only with a set A —
 * it decides outright and A is not consulted at all.
 * <p>
 * Type code 13 is the one place a set B still leaves two rows. There an unknown A takes the worst NIC
 * and radius the type code offers, Rc &lt; 1111.2 m, rather than the &lt; 555.6 m a clear A would
 * give. Surface likewise: a supplement not known to be set is read as clear, which is that
 * knowledge's poorest row. Throughout, the answer for partial knowledge is exactly the poorest answer
 * any completion of that knowledge could give — a worst case, never a guess.
 * <p>
 * Taken together these rules are what ED-129C TABLE 15 prescribes.
 *
 * @see NavigationCharacteristicsV1 for version 1, which has a single supplement
 */
public enum NavigationCharacteristicsV2 implements NavigationCharacteristics {

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

    TYPE_CODE_20(20, 11, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_21(21, 10, ContainmentRadius.BELOW_25),
    TYPE_CODE_22(22, 0, ContainmentRadius.AT_LEAST_25);

    private final byte formatTypeCode;
    private final byte nic;
    private final ContainmentRadius containmentRadius;

    NavigationCharacteristicsV2(int formatTypeCode, int nic, ContainmentRadius containmentRadius) {
        this.formatTypeCode = (byte) formatTypeCode;
        this.nic = (byte) nic;
        this.containmentRadius = containmentRadius;
    }

    /**
     * The row an airborne position message's format type code and NIC supplements select.
     * <p>
     * Supplement B takes priority over supplement A, since only a subset of their combinations is
     * tabulated and B is the one that arrives with this message while A may be missing or stale. At
     * type codes 11 and 16 B therefore decides alone and A is not consulted, so a receiver that has
     * heard no operational status message still decodes them exactly. Type code 13 is the one place
     * A still narrows what B leaves, and an unknown A there yields the worst NIC and radius the type
     * code offers.
     *
     * @param formatTypeCode the message's format type code, 0, 9 to 18 or 20 to 22
     * @param nicSupplements what is known of the target's NIC supplements
     * @return the characteristics version 2 assigns to that combination
     * @throws IllegalArgumentException if the type code is not an airborne position type code
     */
    public static NavigationCharacteristicsV2 forAirborneFormatTypeCode(byte formatTypeCode,
                                                                        NICSupplements nicSupplements) {
        NICSupplement nicSupplementA = nicSupplements.getA();
        NICSupplement nicSupplementB = nicSupplements.getB();

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
                return TYPE_CODE_20;
            case 21:
                return TYPE_CODE_21;
            case 22:
                return TYPE_CODE_22;
            default:
                throw new IllegalArgumentException(
                        "Format type code " + formatTypeCode + " is not an airborne position type code");
        }
    }

    /**
     * The row a surface position message's format type code and NIC supplements select.
     * <p>
     * Both supplements travel in the operational status message, so both are routinely unknown. Each
     * improves the report where it applies, so one not known to be set is read as clear, which is the
     * poorest row its knowledge allows.
     *
     * @param formatTypeCode the message's format type code, 0 or 5 to 8
     * @param nicSupplements what is known of the target's NIC supplements
     * @return the characteristics version 2 assigns to that combination
     * @throws IllegalArgumentException if the type code is not a surface position type code
     */
    public static NavigationCharacteristicsV2 forSurfaceFormatTypeCode(byte formatTypeCode,
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
     * @return the navigation integrity category
     */
    @Override
    public byte getNIC() {
        return nic;
    }

    /**
     * The horizontal containment radius limit, with the side of the value the true radius lies on.
     * Travels with {@link #getNIC()} and must not be derived from it — type code 13 reports NIC 6 at
     * three different radii, and surface type code 8 reports NIC 6 at two.
     *
     * @return the containment radius
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return containmentRadius;
    }
}
