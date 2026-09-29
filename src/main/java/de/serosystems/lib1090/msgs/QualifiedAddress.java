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

package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.Tools;

import java.io.Serializable;

/**
 * Represents a qualified 24-bit address: an address value together with its {@link Type}, as
 * defined in ED-102B §2.2.3.2.1.5 TABLE 2-8, and the {@link Source} it was received from.
 * <p>
 * The source is part of the identity: the same address received directly from a transponder, from a
 * non-transponder device, via TIS-B or via ADS-R denotes separately tracked targets, see ED-102B
 * §2.2.10.1.2 (ADS-B reports organized by address, address source and address qualifier),
 * §2.2.17.4 (TIS-B processed independently of ADS-B) and §2.2.18.4 (ADS-R reports distinct from
 * ADS-B reports).
 */
public class QualifiedAddress implements Serializable {

    private static final long serialVersionUID = 1352093244882739883L;

    /**
     * Different types of addresses in the AA field, see ED-102B §2.2.3.2.1.5 TABLE 2-8,
     * "Determining the Type of Address in the AA Field" (the CF field itself, which this
     * table keys on for DF=18, is coded per §2.2.3.2.1.3 TABLE 2-7). Reserved values (DF=19
     * with AF&gt;0) are military-use codings not specified by ED-102B; see ICAO Annex 10
     * Volume IV §3.1.2.8.8.2.
     */
    public enum Type {
        // ICAO 24-bit address
        ICAO24,
        // NON-ICAO 24-bit address
        NON_ICAO,
        // Anonymous address or ground vehicle address or fixed obstacle address of transmitting ADS-B Participant
        ANONYMOUS, // DF=18 with CF=1 or CF=6 and IMF=1
        // 12-bit Mode A code and track file number
        MODEA_TRACK, // DF=18 with CF=2/3 and IMF=1
        // TIS-B/ADS-R management information
        TISB_MANAGEMENT_INFO, // DF=18 with CF=4
        // Reserved (e.g. for military use)
        RESERVED, // DF=19 with AF>0 or DF=18 with CF=5 and IMF=1 or DF=18 and CF=7
        // Not (yet) determined
        UNKNOWN
    }

    /**
     * Where an address was received from. The first two values are the Address Source of ED-102B
     * §2.2.8.1.3.1 TABLE 2-146; the others extend it to the messages that table leaves out, since
     * TIS-B and ADS-R are reported apart from ADS-B (NOTES 1 and 2 of that table).
     */
    public enum Source {
        // Mode S transponder: DF=0, 4, 5, 11, 16, 17, 20, 21 and 24, as well as DF=19 (military
        // extended squitter), which ED-102B does not specify
        TRANSPONDER,
        // Non-transponder (stand-alone) ADS-B device: DF=18 with CF=0 or CF=1
        NON_TRANSPONDER,
        // TIS-B: DF=18 with CF=2, 3 or 5, as well as the Traffic Uplink Management Message (CF=4),
        // grouped with TIS-B by ED-102B §2.2.3.2.2 TABLE 2-10
        TIS_B,
        // ADS-R: DF=18 with CF=6
        ADS_R
    }

    private int address;
    private Type type;
    private Source source;

    /**
     * protected no-arg constructor e.g. internal usage or for serialization with Kryo
     **/
    protected QualifiedAddress() {
    }

    /**
     * @param address the 24-bit address, 0 to 0xFFFFFF, as every address in the AA field is (ED-102B §2.2.3.2.1.5
     *                TABLE 2-8)
     * @param type    the type of address
     * @param source  where the address was received from
     * @throws IllegalArgumentException if the address does not fit in 24 bits
     */
    public QualifiedAddress(int address, Type type, Source source) {
        if ((address & ~0xFFFFFF) != 0)
            throw new IllegalArgumentException("Address " + Integer.toHexString(address) + " does not fit in 24 bits");
        this.address = address;
        this.type = type;
        this.source = source;
    }

    public QualifiedAddress(QualifiedAddress other) {
        this(other.address, other.type, other.source);
    }

    /**
     * @param address the 24-bit address as a hex string, 0 to FFFFFF
     * @param type    the type of address
     * @param source  where the address was received from
     * @throws IllegalArgumentException if the address is no hex number or does not fit in 24 bits
     */
    public QualifiedAddress(String address, Type type, Source source) {
        this(Integer.parseInt(address, 16), type, source);
    }

    /**
     * @return type of address (e.g. ICAO 24-bit)
     */
    public Type getType() {
        return type;
    }

    /**
     * @return where the address was received from (e.g. transponder or ADS-R)
     */
    public Source getSource() {
        return source;
    }

    /**
     * @return the address in integer representation
     */
    public int getAddress() {
        return address;
    }

    /**
     * @return address as 6 digit hex string
     */
    public String getHexAddress() {
        return Tools.toHexString(address, 6);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QualifiedAddress that = (QualifiedAddress) o;

        if (address != that.address) return false;
        if (type != that.type) return false;
        return source == that.source;
    }

    @Override
    public int hashCode() {
        int result = address;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QualifiedAddress{" +
                "address=" + address +
                ", type=" + type +
                ", source=" + source +
                '}';
    }
}
