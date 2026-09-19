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
 * Everything an ADS-B version 0 position message says about the quality of its own position, as one
 * row per format type code.
 * <p>
 * Version 0 transmits no accuracy or integrity field at all. It transmits a format type code, and
 * the type code alone determines NUCp — and, through the tables later standards added for reading
 * version 0 traffic, NACp, NIC, SIL and the containment radius as well. ED-102B §2.4.8.1.16
 * TABLE 2-304 gives the first four; §N.2.2.2 TABLE N-4 ("Version Zero (0) Format Type Code Mapping to
 * Navigation Source Characteristics") gives the containment radius, and §N.2.3.7 TABLE N-9 repeats
 * the NACp column. The rows here are those tables merged.
 * <p>
 * <b>The format type code is the only usable key.</b> NUCp is not: type code 8 and type code 12 both
 * report NUCp 6, yet the first maps to NACp 0 and the second to NACp 7, so no NUCp-to-NACp function
 * exists. Anything derived from a version 0 position message is therefore derived from its type code,
 * never from one derived value via another.
 * <p>
 * NIC and NACp happen to be equal in every row. That is a coincidence of the two tables and not an
 * identity — the two describe different things, integrity against accuracy — so neither may be
 * computed from the other, however tempting the shortcut looks.
 * <p>
 * Surface type codes (5 to 8) and airborne ones (9 to 18, 20 to 22) share this table because their
 * ranges are disjoint; type code 0, which reports no position, is common to both.
 */
public enum NavigationCharacteristicsV0 implements NavigationCharacteristics {

    /** No position information; nothing is claimed about a position that is not there. */
    TYPE_CODE_0(0, 0, 0, 0, 0, ContainmentRadius.UNKNOWN),

    TYPE_CODE_5(5, 9, 11, 11, 2, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_6(6, 8, 10, 10, 2, ContainmentRadius.BELOW_25),
    TYPE_CODE_7(7, 7, 8, 8, 2, ContainmentRadius.BELOW_185_2),

    /**
     * The one surface type code that guarantees nothing: it still reports a NUCp, but bounds the
     * containment radius only from below, and so reports neither accuracy nor integrity.
     */
    TYPE_CODE_8(8, 6, 0, 0, 0, ContainmentRadius.AT_LEAST_185_2),

    TYPE_CODE_9(9, 9, 11, 11, 2, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_10(10, 8, 10, 10, 2, ContainmentRadius.BELOW_25),
    TYPE_CODE_11(11, 7, 8, 8, 2, ContainmentRadius.BELOW_185_2),
    TYPE_CODE_12(12, 6, 7, 7, 2, ContainmentRadius.BELOW_370_4),
    TYPE_CODE_13(13, 5, 6, 6, 2, ContainmentRadius.BELOW_926),
    TYPE_CODE_14(14, 4, 5, 5, 2, ContainmentRadius.BELOW_1852),
    TYPE_CODE_15(15, 3, 4, 4, 2, ContainmentRadius.BELOW_3704),
    TYPE_CODE_16(16, 2, 1, 1, 2, ContainmentRadius.BELOW_18520),
    TYPE_CODE_17(17, 1, 1, 1, 2, ContainmentRadius.BELOW_37040),

    /** The airborne counterpart of {@link #TYPE_CODE_8}: a containment radius bounded from below. */
    TYPE_CODE_18(18, 0, 0, 0, 0, ContainmentRadius.AT_LEAST_37040),

    TYPE_CODE_20(20, 9, 11, 11, 2, ContainmentRadius.BELOW_7_5),
    TYPE_CODE_21(21, 8, 10, 10, 2, ContainmentRadius.BELOW_25),

    /**
     * No NUCp: this type code was introduced after version 0, so version 0 never assigned it one.
     * The remaining columns exist because a later receiver still has to read such a message.
     */
    TYPE_CODE_22(22, null, 0, 0, 0, ContainmentRadius.AT_LEAST_25);

    /** Indexed by format type code; entries for codes that carry no position stay null. */
    private static final NavigationCharacteristicsV0[] BY_TYPE_CODE = new NavigationCharacteristicsV0[32];

    static {
        for (NavigationCharacteristicsV0 characteristics : values())
            BY_TYPE_CODE[characteristics.formatTypeCode] = characteristics;
    }

    private final byte formatTypeCode;
    private final Byte nucP;
    private final byte nacP;
    private final byte nic;
    private final byte sil;
    private final ContainmentRadius containmentRadius;

    NavigationCharacteristicsV0(int formatTypeCode, Integer nucP, int nacP, int nic, int sil,
                                ContainmentRadius containmentRadius) {
        this.formatTypeCode = (byte) formatTypeCode;
        this.nucP = nucP == null ? null : (byte) nucP.intValue();
        this.nacP = (byte) nacP;
        this.nic = (byte) nic;
        this.sil = (byte) sil;
        this.containmentRadius = containmentRadius;
    }

    /**
     * The row a position message's format type code selects.
     *
     * @param formatTypeCode the message's format type code
     * @return the characteristics version 0 assigns to that type code
     * @throws IllegalArgumentException if the type code belongs to no position message, since no
     *                                  row exists for it and reporting "unknown" would hide the
     *                                  caller's mistake
     */
    public static NavigationCharacteristicsV0 forFormatTypeCode(byte formatTypeCode) {
        NavigationCharacteristicsV0 characteristics =
                formatTypeCode >= 0 && formatTypeCode < BY_TYPE_CODE.length
                        ? BY_TYPE_CODE[formatTypeCode]
                        : null;

        if (characteristics == null)
            throw new IllegalArgumentException(
                    "Format type code " + formatTypeCode + " is not a position message type code");

        return characteristics;
    }

    /**
     * @return the format type code this row belongs to
     */
    @Override
    public byte getFormatTypeCode() {
        return formatTypeCode;
    }

    /**
     * Navigation uncertainty category for position, the only one of these version 0 defined itself,
     * ED-102 §2.2.8.1.5.
     *
     * @return the NUCp, or {@code null} for a type code version 0 never assigned one
     */
    public Byte getNUCp() {
        return nucP;
    }

    /**
     * Navigation accuracy category for position. Version 0 transmits no such field; this is what
     * ED-102B says the type code amounts to.
     *
     * @return the NACp
     */
    public byte getNACp() {
        return nacP;
    }

    /**
     * Navigation integrity category. Version 0 transmits no such field, and no NIC supplements
     * either, so unlike later versions the type code determines it outright.
     *
     * @return the NIC
     */
    @Override
    public byte getNIC() {
        return nic;
    }

    /**
     * Source integrity level. Version 0 transmits no such field; the type codes that bound their
     * containment radius only from below report 0 here, the rest report 2.
     *
     * @return the SIL
     */
    public byte getSIL() {
        return sil;
    }

    /**
     * The horizontal containment radius limit, with the side of the value the true radius lies on.
     * Travels with {@link #getNIC()} and must not be derived from it: the two are separate columns
     * of the same row.
     *
     * @return the containment radius
     */
    @Override
    public ContainmentRadius getContainmentRadius() {
        return containmentRadius;
    }
}
