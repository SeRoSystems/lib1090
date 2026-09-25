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

package de.serosystems.lib1090.msgs.bds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SelectedVerticalIntentionTest {

    private static final byte[] MSG = {
            (byte) 0b10000101, (byte) 0b11100100, (byte) 0b00101111, (byte) 0b00110001, (byte) 0b00110000,
            (byte) 0b00000000, (byte) 0b00000000
    };

    /** A message with {@code value} written into ME bits {@code from} to {@code to}, all other bits clear. */
    private static SelectedVerticalIntention with(int from, int to, int value) {
        byte[] message = new byte[7];
        for (int bit = from; bit <= to; bit++) {
            if (((value >>> (to - bit)) & 1) != 0)
                message[(bit - 1) / 8] |= (byte) (0x80 >>> ((bit - 1) % 8));
        }
        return new SelectedVerticalIntention(message);
    }

    @Test
    public void decodesEveryField() {
        SelectedVerticalIntention svi = new SelectedVerticalIntention(MSG);

        assertTrue(svi.hasMcpFcuSelectedAltitude());
        assertEquals(188, svi.getMcpFcuSelectedAltitudeEncoded());
        assertEquals(3008, svi.getMcpFcuSelectedAltitude());

        assertTrue(svi.hasFmsSelectedAltitude());
        assertEquals(188, svi.getFmsSelectedAltitudeEncoded());
        assertEquals(3008, svi.getFmsSelectedAltitude());

        assertTrue(svi.hasBarometricPressureSetting());
        assertEquals(2200, svi.getBarometricPressureSettingEncoded());
        assertEquals(1020f, svi.getBarometricPressureSetting());

        assertFalse(svi.hasModeInfo());
        assertNull(svi.hasVNAVModeEngaged());
        assertNull(svi.hasActiveAltitudeHoldMode());
        assertNull(svi.hasActiveApproachMode());

        assertFalse(svi.hasTargetAltSource());
        assertEquals(0, svi.getTargetAltSourceEncoded());
        assertNull(svi.getTargetAltSource());
    }

    @Test
    public void ranges() {
        assertEquals(65520, with(1, 13, 0x1FFF).getMcpFcuSelectedAltitude());
        assertEquals(65520, with(14, 26, 0x1FFF).getFmsSelectedAltitude());
        assertEquals(800f, with(27, 39, 1 << 12).getBarometricPressureSetting());
        assertEquals(1209.5f, with(27, 39, 0x1FFF).getBarometricPressureSetting());
        // a resolution of 0.1 mb, e.g. the standard atmosphere's 1013.2 mb
        assertEquals(1013.2f, with(27, 39, 1 << 12 | 2132).getBarometricPressureSetting());
    }

    /** Each mode bit is reported only while the status of the mode bits says they are populated. */
    @Test
    public void modeBits() {
        SelectedVerticalIntention vnav = with(48, 51, 0b1100);
        assertTrue(vnav.hasModeInfo());
        assertTrue(vnav.hasVNAVModeEngaged());
        assertFalse(vnav.hasActiveAltitudeHoldMode());
        assertFalse(vnav.hasActiveApproachMode());

        SelectedVerticalIntention altHoldAndApproach = with(48, 51, 0b1011);
        assertFalse(altHoldAndApproach.hasVNAVModeEngaged());
        assertTrue(altHoldAndApproach.hasActiveAltitudeHoldMode());
        assertTrue(altHoldAndApproach.hasActiveApproachMode());

        SelectedVerticalIntention notProvided = with(48, 51, 0b0111);
        assertFalse(notProvided.hasModeInfo());
        assertNull(notProvided.hasVNAVModeEngaged());
        assertNull(notProvided.hasActiveAltitudeHoldMode());
        assertNull(notProvided.hasActiveApproachMode());
    }

    @Test
    public void targetAltSource() {
        for (short source = 0; source <= 3; source++) {
            SelectedVerticalIntention svi = with(54, 56, 0b100 | source);
            assertTrue(svi.hasTargetAltSource());
            assertEquals(source, svi.getTargetAltSourceEncoded());
            assertEquals(source, svi.getTargetAltSource());
        }

        SelectedVerticalIntention notProvided = with(54, 56, 0b011);
        assertFalse(notProvided.hasTargetAltSource());
        assertEquals(3, notProvided.getTargetAltSourceEncoded());
        assertNull(notProvided.getTargetAltSource());
    }

    /** A clear status bit makes the interpreting accessor answer null; the encoded value is still readable. */
    @Test
    public void unavailable() {
        SelectedVerticalIntention mcpFcu = with(2, 13, 188);
        assertFalse(mcpFcu.hasMcpFcuSelectedAltitude());
        assertEquals(188, mcpFcu.getMcpFcuSelectedAltitudeEncoded());
        assertNull(mcpFcu.getMcpFcuSelectedAltitude());

        SelectedVerticalIntention fms = with(15, 26, 188);
        assertFalse(fms.hasFmsSelectedAltitude());
        assertEquals(188, fms.getFmsSelectedAltitudeEncoded());
        assertNull(fms.getFmsSelectedAltitude());

        SelectedVerticalIntention baro = with(28, 39, 2200);
        assertFalse(baro.hasBarometricPressureSetting());
        assertEquals(2200, baro.getBarometricPressureSettingEncoded());
        assertNull(baro.getBarometricPressureSetting());
    }
}
