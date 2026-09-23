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

package de.serosystems.lib1090.decoding.quality;

import java.io.Serializable;
import java.util.Objects;

/**
 * Everything a receiver knows about one target's NIC supplements, collected in one place.
 * <p>
 * The supplements arrive from different messages and at different times — A and C from the
 * operational status message, D from the airborne velocity message, B from the airborne position
 * message under ADS-B and from the operational status message under ADS-R — so a receiver accumulates
 * them rather than reading them all at once. Anything not yet received stays
 * {@link NICSupplement#UNKNOWN}, which the tables answer with the poorest row that knowledge allows.
 * <p>
 * <b>A lookup reads the supplements its row is keyed on and ignores the rest.</b> That is not a
 * concession to callers passing too much: every table already works that way per format type code —
 * version 2 surface ignores C below type code 8, version 3 airborne ignores A and B at type codes 20
 * to 22 and ignores D everywhere else, and version 0 ignores all four, having none. A caller therefore
 * passes what it knows and need not know which version will read what.
 * <p>
 * <b>Each supplement can be set two ways, and which one to use says something.</b> The overload taking
 * a {@code boolean} or {@code byte} means "I received this and it is X" — the right one where the value
 * comes from a field that cannot be absent, which is every decoder path in this library. The overload
 * taking {@link NICSupplement} or {@link NICSupplementD} means "here is my state of knowledge", which
 * may be {@code UNKNOWN}: the right one where the value may not have been received, or where a
 * receiver has decided a supplement is too old to trust and wants to forget it again.
 * <p>
 * <b>Instances are immutable.</b> Each {@code with} method returns a new one, which is what makes the
 * object safe to share: a decoder holds the current instance per target and hands the same reference
 * to every position message decoded under it, so a message keeps reporting what was known when it
 * arrived even after the target's supplements move on. A caller that wants the newer reading asks for
 * it explicitly, by passing the newer instance back to the message.
 */
public final class NICSupplements implements Serializable {

    private static final long serialVersionUID = 1802934475518263471L;

    private static final NICSupplements NONE = new NICSupplements(
            NICSupplement.UNKNOWN, NICSupplement.UNKNOWN, NICSupplement.UNKNOWN, NICSupplementD.UNKNOWN);

    private NICSupplement nicSupplementA;
    private NICSupplement nicSupplementB;
    private NICSupplement nicSupplementC;
    private NICSupplementD nicSupplementD;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected NICSupplements() {
    }

    private NICSupplements(NICSupplement nicSupplementA, NICSupplement nicSupplementB,
                           NICSupplement nicSupplementC, NICSupplementD nicSupplementD) {
        this.nicSupplementA = nicSupplementA;
        this.nicSupplementB = nicSupplementB;
        this.nicSupplementC = nicSupplementC;
        this.nicSupplementD = nicSupplementD;
    }

    /**
     * @return the supplements of a target nothing has yet been received about, every one of them
     * {@link NICSupplement#UNKNOWN}
     */
    public static NICSupplements none() {
        return NONE;
    }

    /**
     * @param nicSupplementA NIC supplement A as received, from the operational status message
     * @return these supplements with A known
     */
    public NICSupplements withA(boolean nicSupplementA) {
        return withA(NICSupplement.of(nicSupplementA));
    }

    /**
     * @param nicSupplementA what is known of NIC supplement A; {@link NICSupplement#UNKNOWN} or
     *                       {@code null} forgets what was known of it
     * @return these supplements with A as given
     */
    public NICSupplements withA(NICSupplement nicSupplementA) {
        return new NICSupplements(orUnknown(nicSupplementA), nicSupplementB, nicSupplementC,
                nicSupplementD);
    }

    /**
     * @param nicSupplementB NIC supplement B as received — from the position message itself under
     *                       ADS-B, from the operational status message under ADS-R
     * @return these supplements with B known
     */
    public NICSupplements withB(boolean nicSupplementB) {
        return withB(NICSupplement.of(nicSupplementB));
    }

    /**
     * @param nicSupplementB what is known of NIC supplement B; {@link NICSupplement#UNKNOWN} or
     *                       {@code null} forgets what was known of it
     * @return these supplements with B as given
     */
    public NICSupplements withB(NICSupplement nicSupplementB) {
        return new NICSupplements(nicSupplementA, orUnknown(nicSupplementB), nicSupplementC,
                nicSupplementD);
    }

    /**
     * @param nicSupplementC NIC supplement C as received, from the operational status message
     * @return these supplements with C known
     */
    public NICSupplements withC(boolean nicSupplementC) {
        return withC(NICSupplement.of(nicSupplementC));
    }

    /**
     * @param nicSupplementC what is known of NIC supplement C; {@link NICSupplement#UNKNOWN} or
     *                       {@code null} forgets what was known of it
     * @return these supplements with C as given
     */
    public NICSupplements withC(NICSupplement nicSupplementC) {
        return new NICSupplements(nicSupplementA, nicSupplementB, orUnknown(nicSupplementC),
                nicSupplementD);
    }

    /**
     * @param nicSupplementD NIC supplement D as received, 0 to 3, from the airborne velocity message
     * @return these supplements with D known
     * @throws IllegalArgumentException if the value is outside the two bits the field occupies
     */
    public NICSupplements withD(byte nicSupplementD) {
        return withD(NICSupplementD.of(nicSupplementD));
    }

    /**
     * @param nicSupplementD what is known of NIC supplement D; {@link NICSupplementD#UNKNOWN} or
     *                       {@code null} forgets what was known of it
     * @return these supplements with D as given
     */
    public NICSupplements withD(NICSupplementD nicSupplementD) {
        return new NICSupplements(nicSupplementA, nicSupplementB, nicSupplementC,
                nicSupplementD == null ? NICSupplementD.UNKNOWN : nicSupplementD);
    }

    /**
     * @return what is known of NIC supplement A
     */
    public NICSupplement getA() {
        return nicSupplementA;
    }

    /**
     * @return what is known of NIC supplement B
     */
    public NICSupplement getB() {
        return nicSupplementB;
    }

    /**
     * @return what is known of NIC supplement C
     */
    public NICSupplement getC() {
        return nicSupplementC;
    }

    /**
     * @return what is known of NIC supplement D
     */
    public NICSupplementD getD() {
        return nicSupplementD;
    }

    private static NICSupplement orUnknown(NICSupplement nicSupplement) {
        return nicSupplement == null ? NICSupplement.UNKNOWN : nicSupplement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NICSupplements that = (NICSupplements) o;
        return nicSupplementA == that.nicSupplementA && nicSupplementB == that.nicSupplementB
                && nicSupplementC == that.nicSupplementC && nicSupplementD == that.nicSupplementD;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nicSupplementA, nicSupplementB, nicSupplementC, nicSupplementD);
    }

    @Override
    public String toString() {
        return "NICSupplements{A=" + nicSupplementA + ", B=" + nicSupplementB
                + ", C=" + nicSupplementC + ", D=" + nicSupplementD + '}';
    }
}
