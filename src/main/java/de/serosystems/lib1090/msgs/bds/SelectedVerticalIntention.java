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

import de.serosystems.lib1090.decoding.BitReader;

import java.io.Serializable;

/**
 * Decoder for the selected vertical intention report (BDS 4,0), as defined in ICAO Doc 9871
 * (First Edition, AN/464) §A.2 TABLE A-2-64.
 */
@SuppressWarnings("unused")
public class SelectedVerticalIntention extends BDSRegister implements Serializable {
    private static final long serialVersionUID = 4359765861426574750L;

    private static final BDSCode BDS_CODE = new BDSCode(4, 0);

    // MCP/FCU Selected Altitude
    private boolean mcpFcuSelectedAltitudeStatus;
    private short mcpFcuSelectedAltitudeEncoded;
    // FMS Selected Altitude
    private boolean fmsSelectedAltitudeStatus;
    private short fmsSelectedAltitudeEncoded;
    // Barometric Pressure Setting
    private boolean barometricPressureSettingStatus;
    private short barometricPressureSettingEncoded;
    // MCP/FCU Mode Bits
    private boolean mcpFcuModeStatus;
    private boolean vnavMode;
    private boolean altitudeHoldMode;
    private boolean approachMode;
    // Target Altitude Source
    private boolean targetAltSourceStatus;
    private short targetAltSourceEncoded;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SelectedVerticalIntention() {
    }

    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public SelectedVerticalIntention(byte[] message) {
        super(message);

        BitReader b = BitReader.forBigEndian(message);

        mcpFcuSelectedAltitudeStatus = b.readBoolean(1);
        mcpFcuSelectedAltitudeEncoded = b.readShort(2, 13);
        fmsSelectedAltitudeStatus = b.readBoolean(14);
        fmsSelectedAltitudeEncoded = b.readShort(15, 26);
        barometricPressureSettingStatus = b.readBoolean(27);
        barometricPressureSettingEncoded = b.readShort(28, 39);
        mcpFcuModeStatus = b.readBoolean(48);
        vnavMode = b.readBoolean(49);
        altitudeHoldMode = b.readBoolean(50);
        approachMode = b.readBoolean(51);
        targetAltSourceStatus = b.readBoolean(54);
        targetAltSourceEncoded = b.readShort(55, 56);
    }

    /**
     * @return whether the MCP/FCU selected altitude is available
     */
    public boolean hasMcpFcuSelectedAltitude() {
        return mcpFcuSelectedAltitudeStatus;
    }

    /**
     * @return the encoded MCP/FCU selected altitude
     */
    public short getMcpFcuSelectedAltitudeEncoded() {
        return mcpFcuSelectedAltitudeEncoded;
    }

    /**
     * The MCP/FCU selected altitude, derived from the mode control panel/flight control unit or
     * equivalent equipment. Alerting devices may be used to provide data if it is not available from
     * "control" equipment. The resolution is 16 feet and the range is [0, 65520] feet.
     *
     * @return the MCP/FCU selected altitude in feet, or null if not available
     */
    public Integer getMcpFcuSelectedAltitude() {
        if (!mcpFcuSelectedAltitudeStatus) return null;
        return mcpFcuSelectedAltitudeEncoded * 16;
    }

    /**
     * @return whether the FMS selected altitude is available
     */
    public boolean hasFmsSelectedAltitude() {
        return fmsSelectedAltitudeStatus;
    }

    /**
     * @return the encoded FMS selected altitude
     */
    public short getFmsSelectedAltitudeEncoded() {
        return fmsSelectedAltitudeEncoded;
    }

    /**
     * The FMS selected altitude, derived from the flight management system or equivalent equipment
     * managing the vertical profile of the aircraft. The resolution is 16 feet and the range is
     * [0, 65520] feet.
     *
     * @return the FMS selected altitude in feet, or null if not available
     */
    public Integer getFmsSelectedAltitude() {
        if (!fmsSelectedAltitudeStatus) return null;
        return fmsSelectedAltitudeEncoded * 16;
    }

    /**
     * @return whether the barometric pressure setting is available
     */
    public boolean hasBarometricPressureSetting() {
        return barometricPressureSettingStatus;
    }

    /**
     * The barometric pressure setting as transmitted, which is the setting minus 800 mb.
     *
     * @return the encoded barometric pressure setting
     */
    public short getBarometricPressureSettingEncoded() {
        return barometricPressureSettingEncoded;
    }

    /**
     * The barometric pressure setting in millibars, at a resolution of 0.1 mb and in the range
     * [800, 1209.5] mb. A setting outside that range is reported as not available.
     *
     * @return the barometric pressure setting in millibars, or null if not available
     */
    public Float getBarometricPressureSetting() {
        if (!barometricPressureSettingStatus) return null;
        return (float) (800 + barometricPressureSettingEncoded * 0.1);
    }

    /**
     * @return whether the MCP/FCU mode bits are populated, i.e. whether {@link #hasVNAVModeEngaged()},
     * {@link #hasActiveAltitudeHoldMode()} and {@link #hasActiveApproachMode()} carry information
     */
    public boolean hasModeInfo() {
        return mcpFcuModeStatus;
    }

    /**
     * @return whether the vertical navigation mode is active, or null if no mode information is provided
     */
    public Boolean hasVNAVModeEngaged() {
        if (!mcpFcuModeStatus) return null;
        return vnavMode;
    }

    /**
     * @return whether the altitude hold mode is active, or null if no mode information is provided
     */
    public Boolean hasActiveAltitudeHoldMode() {
        if (!mcpFcuModeStatus) return null;
        return altitudeHoldMode;
    }

    /**
     * @return whether the approach mode is active, or null if no mode information is provided
     */
    public Boolean hasActiveApproachMode() {
        if (!mcpFcuModeStatus) return null;
        return approachMode;
    }

    /**
     * @return whether the target altitude source is available
     */
    public boolean hasTargetAltSource() {
        return targetAltSourceStatus;
    }

    /**
     * @return the encoded target altitude source
     * @see #getTargetAltSource()
     */
    public short getTargetAltSourceEncoded() {
        return targetAltSourceEncoded;
    }

    /**
     * @return the target altitude source, or null if not available:
     * <ul>
     *     <li>0: unknown</li>
     *     <li>1: aircraft altitude</li>
     *     <li>2: FCU/MCP selected altitude</li>
     *     <li>3: FMS selected altitude</li>
     * </ul>
     */
    public Short getTargetAltSource() {
        if (!targetAltSourceStatus) return null;
        return targetAltSourceEncoded;
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "SelectedVerticalIntention{" + super.toString() +
                ", mcpFcuSelectedAltitudeStatus=" + mcpFcuSelectedAltitudeStatus +
                ", mcpFcuSelectedAltitudeEncoded=" + mcpFcuSelectedAltitudeEncoded +
                ", fmsSelectedAltitudeStatus=" + fmsSelectedAltitudeStatus +
                ", fmsSelectedAltitudeEncoded=" + fmsSelectedAltitudeEncoded +
                ", barometricPressureSettingStatus=" + barometricPressureSettingStatus +
                ", barometricPressureSettingEncoded=" + barometricPressureSettingEncoded +
                ", mcpFcuModeStatus=" + mcpFcuModeStatus +
                ", vnavMode=" + vnavMode +
                ", altitudeHoldMode=" + altitudeHoldMode +
                ", approachMode=" + approachMode +
                ", targetAltSourceStatus=" + targetAltSourceStatus +
                ", targetAltSourceEncoded=" + targetAltSourceEncoded +
                '}';
    }

}
