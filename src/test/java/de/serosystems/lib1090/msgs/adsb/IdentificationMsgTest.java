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

package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.msgs.squitter.IdentificationMsg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class IdentificationMsgTest {

    protected abstract IdentificationMsg create(String hex) throws Exception;

    @Test
    public void testCallsignEZY85MH() throws Exception {
        IdentificationMsg msg = create("8D406B902015A678D4D220AA4BDA");
        assertEquals("EZY85MH ", new String(msg.getIdentification()));
    }

    @Test
    public void testCallsignKLM1023() throws Exception {
        IdentificationMsg msg = create("8D4840D6202CC371C32CE0576098");
        assertEquals("KLM1023 ", new String(msg.getIdentification()));
    }

    @Test
    public void testCategoryEZY85MH() throws Exception {
        IdentificationMsg msg = create("8D406B902015A678D4D220AA4BDA");
        assertEquals(0, msg.getEmitterCategoryEncoded());
    }

    @Test
    public void testCategoryKLM1023() throws Exception {
        IdentificationMsg msg = create("8D4840D6202CC371C32CE0576098");
        assertEquals(0, msg.getEmitterCategoryEncoded());
    }

    @Test
    public void testTypeCodeIdentification() throws Exception {
        IdentificationMsg msg = create("8D406B902015A678D4D220AA4BDA");
        assertEquals(4, msg.getFormatTypeCode());
    }
}
