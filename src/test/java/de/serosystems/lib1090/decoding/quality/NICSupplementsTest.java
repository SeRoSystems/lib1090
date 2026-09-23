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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NICSupplementsTest {

    @Test
    void testNothingIsKnownToBeginWith() {
        NICSupplements supplements = NICSupplements.none();

        assertSame(NICSupplement.UNKNOWN, supplements.getA());
        assertSame(NICSupplement.UNKNOWN, supplements.getB());
        assertSame(NICSupplement.UNKNOWN, supplements.getC());
        assertSame(NICSupplementD.UNKNOWN, supplements.getD());
    }

    /** Each supplement has its own slot, so setting one leaves the others where they were. */
    @Test
    void testEachSupplementIsIndependent() {
        NICSupplements supplements = NICSupplements.none()
                .withA(true).withB(false).withC(true).withD((byte) 2);

        assertSame(NICSupplement.SET, supplements.getA());
        assertSame(NICSupplement.CLEAR, supplements.getB());
        assertSame(NICSupplement.SET, supplements.getC());
        assertSame(NICSupplementD.TWO, supplements.getD());
    }

    /** The primitive overload is shorthand for lifting a received value into the known state. */
    @Test
    void testThePrimitiveOverloadMatchesTheEnumOne() {
        assertEquals(NICSupplements.none().withA(NICSupplement.SET), NICSupplements.none().withA(true));
        assertEquals(NICSupplements.none().withB(NICSupplement.CLEAR), NICSupplements.none().withB(false));
        assertEquals(NICSupplements.none().withC(NICSupplement.SET), NICSupplements.none().withC(true));
        assertEquals(NICSupplements.none().withD(NICSupplementD.TWO), NICSupplements.none().withD((byte) 2));
    }

    /**
     * Setting a supplement back to unknown forgets it, which is how a receiver expires one it no
     * longer trusts — a supplement from an operational status message heard long ago, say. The
     * primitive overloads cannot express this, which is the reason the enum ones exist.
     */
    @Test
    void testUnknownForgetsWhatWasKnown() {
        NICSupplements known = NICSupplements.none().withA(true).withB(true).withC(true).withD((byte) 3);

        assertSame(NICSupplement.UNKNOWN, known.withA(NICSupplement.UNKNOWN).getA());
        assertSame(NICSupplement.UNKNOWN, known.withB(NICSupplement.UNKNOWN).getB());
        assertSame(NICSupplement.UNKNOWN, known.withC(NICSupplement.UNKNOWN).getC());
        assertSame(NICSupplementD.UNKNOWN, known.withD(NICSupplementD.UNKNOWN).getD());

        // and forgetting one leaves the rest alone
        assertSame(NICSupplement.SET, known.withA(NICSupplement.UNKNOWN).getB());
    }

    /** A null reads as "not received" rather than throwing, so the with-ers stay total. */
    @Test
    void testNullIsUnknown() {
        NICSupplements known = NICSupplements.none().withA(true).withD((byte) 1);

        assertSame(NICSupplement.UNKNOWN, known.withA((NICSupplement) null).getA());
        assertSame(NICSupplementD.UNKNOWN, known.withD((NICSupplementD) null).getD());
    }

    /** The two bits the field occupies are all it has; anything else is a caller's mistake. */
    @Test
    void testSupplementDRejectsValuesOutsideItsField() {
        assertThrows(IllegalArgumentException.class, () -> NICSupplements.none().withD((byte) 4));
        assertThrows(IllegalArgumentException.class, () -> NICSupplements.none().withD((byte) -1));
    }

    @Test
    void testInstancesAreImmutable() {
        NICSupplements original = NICSupplements.none().withA(true);
        NICSupplements derived = original.withB(true);

        assertSame(NICSupplement.UNKNOWN, original.getB());
        assertSame(NICSupplement.SET, derived.getB());
        assertNotEquals(original, derived);
    }

    @Test
    void testEqualityIsByValue() {
        assertEquals(NICSupplements.none().withA(true).withC(false),
                NICSupplements.none().withC(false).withA(true));
        assertEquals(NICSupplements.none().withA(true).hashCode(),
                NICSupplements.none().withA(true).hashCode());
        assertNotEquals(NICSupplements.none().withA(true), NICSupplements.none().withA(false));
    }
}
