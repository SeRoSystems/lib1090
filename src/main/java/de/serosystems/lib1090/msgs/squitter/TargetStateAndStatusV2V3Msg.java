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

package de.serosystems.lib1090.msgs.squitter;

/**
 * Common API for target state and status version 2 and 3 messages, ADS-B and ADS-R: the fields they add to version 1,
 * i.e. the source of the selected altitude, the barometric pressure setting and the autopilot and navigation modes.
 */
public interface TargetStateAndStatusV2V3Msg extends TargetStateAndStatusMsg {

    /**
     * Source for selected altitude according to ED-102B §2.2.3.2.7.1.3.2
     *
     * @return true if the value of {@link #getSelectedAltitude()} is derived from the Flight Management System (FMS),
     * false if it is derived from the Control Panel/Flight Control Unit (MCP/FCU)
     */
    boolean isFMSSelectedAltitude();

    /**
     * ED-102B §2.2.3.2.7.1.3.4
     *
     * @return whether the Barometric Pressure Setting is available, i.e. {@link #getBarometricPressureSetting()}
     * returns a non-null value
     */
    boolean hasBarometricPressureSetting();

    /**
     * The barometric pressure setting as transmitted, which is the setting minus 800 mb, ED-102B
     * §2.2.3.2.7.1.3.4 TABLE 2-35: 0 means no or invalid data, code n stands for (n - 1) * 0.8 mb.
     *
     * @return the encoded barometric pressure setting
     */
    short getBarometricPressureSettingEncoded();

    /**
     * The barometric pressure setting in millibars, according to ED-102B §2.2.3.2.7.1.3.4, in the range
     * [800, 1208] mb.
     *
     * @return the barometric pressure setting in millibars, or null if not available
     */
    Float getBarometricPressureSetting();

    /**
     * MCP/FCU mode status bit according to ED-102B §2.2.3.2.7.1.3.11
     * <p>
     * A value of false indicates that information of {@link #hasAutopilotEngaged()}, {@link #hasVNAVModeEngaged()},
     * {@link #hasActiveAltitudeHoldMode()}, {@link #hasActiveApproachMode()} and {@link #hasLNAVModeEngaged()} is not
     * provided by the aircraft.
     *
     * @return true if Mode is deliberately being provided, false otherwise
     */
    boolean hasMode();

    /**
     * Auto pilot engaged flag according to ED-102B §2.2.3.2.7.1.3.12
     *
     * @return true if the autopilot system is engaged, false if not engaged or status unknown, null if not available
     * ({@link #hasMode()} is false)
     */
    Boolean hasAutopilotEngaged();

    /**
     * VNAV Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.13
     *
     * @return true if vertical navigation mode is active, false otherwise or if status unknown, null if not available
     * ({@link #hasMode()} is false)
     */
    Boolean hasVNAVModeEngaged();

    /**
     * Altitude Hold Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.14
     *
     * @return true if altitude hold mode is active, false if inactive or status unknown, null if not available
     * ({@link #hasMode()} is false)
     */
    Boolean hasActiveAltitudeHoldMode();

    /**
     * Approach Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.16
     *
     * @return true if approach mode is active, false if inactive or status unknown, null if not available
     * ({@link #hasMode()} is false)
     */
    Boolean hasActiveApproachMode();

    /**
     * LNAV Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.18
     *
     * @return true if the lateral navigation mode is active, false otherwise or if status unknown, null if not
     * available ({@link #hasMode()} is false)
     */
    Boolean hasLNAVModeEngaged();

}
