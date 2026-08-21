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

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;
import de.serosystems.lib1090.msgs.squitter.TargetStateAndStatusMsg;

import java.io.Serializable;

/**
 * Decoder for ADS-B target state and status message as specified in ED-102B §2.2.3.2.7.1
 */
public class TargetStateAndStatusV2Msg extends ExtendedSquitter implements Serializable, TargetStateAndStatusMsg, ADSBMsg {

    private static final long serialVersionUID = 8402350306532746670L;

    private boolean silSupplement;
    private boolean selectedAltitudeType;
    private int selectedAltitude;
    private int barometricPressureSetting;
    private boolean selectedHeadingStatus;
    private boolean selectedHeadingSign;
    private int selectedHeading;
    private byte nacP;
    private boolean nicBaro;
    private byte sil;
    private boolean mcpFcuStatus;
    private boolean autopilotEngaged;
    private boolean vnavModeEngaged;
    private boolean altitudeHoldMode;
    private boolean approachMode;
    private boolean operationalTcas;
    private boolean lnavModeEngaged;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected TargetStateAndStatusV2Msg() {
    }

    /**
     * @param rawMessage The full Mode S message in hex representation
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.1.2 TABLE 2-31
     */
    public TargetStateAndStatusV2Msg(String rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param rawMessage The full Mode S message as byte array
     * @throws BadFormatException     if message has the wrong typecode or ADS-B version
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.1.2 TABLE 2-31
     */
    public TargetStateAndStatusV2Msg(byte[] rawMessage) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage));
    }

    /**
     * @param squitter extended squitter which contains this message
     * @throws BadFormatException     if message has the wrong typecode or if reserved bits are set
     * @throws UnspecifiedFormatError if message has the wrong subtype, ED-102B §2.2.3.2.7.1.2 TABLE 2-31
     */
    public TargetStateAndStatusV2Msg(ExtendedSquitter squitter) throws BadFormatException, UnspecifiedFormatError {
        super(squitter);

        if (getFormatTypeCode() != 29)
            throw new BadFormatException("Target state and status messages must have typecode 29.");

        BitReader b = BitReader.forBigEndian(getMessage());

        byte subtypeCode = b.readByte(6, 7);
        if (subtypeCode != 1) // all others are reserved
            throw new UnspecifiedFormatError("Target state and status message subtype " + subtypeCode + " reserved.");

        silSupplement = b.readBoolean(8);
        selectedAltitudeType = b.readBoolean(9);

        selectedAltitude = b.readShort(10, 20);

        barometricPressureSetting = b.readShort(21, 29);

        selectedHeadingStatus = b.readBoolean(30);
        selectedHeadingSign = b.readBoolean(31);
        selectedHeading = b.readShort(32, 39);
        nacP = b.readByte(40, 43);

        nicBaro = b.readBoolean(44);
        sil = b.readByte(45, 46);

        mcpFcuStatus = b.readBoolean(47);

        // the following are only valid if mcpFcuStatus is true
        autopilotEngaged = b.readBoolean(48);
        vnavModeEngaged = b.readBoolean(49);
        altitudeHoldMode = b.readBoolean(50);
        approachMode = b.readBoolean(52);
        lnavModeEngaged = b.readBoolean(54);

        // this is always set and valid
        operationalTcas = b.readBoolean(53);

        // ED-102B §2.2.3.2.7.1.3.19
        if (b.readByte(55, 56) != 0)
            throw new BadFormatException("Target state and status message reserved bits must be 0.");
    }

    /**
     * ED-102B §2.2.3.2.7.1.3.1
     *
     * @return true if SIL (Source Integrity Level) is based on "per sample" probability, otherwise
     * it's based on "per hour".
     */
    public boolean hasSILSupplement() {
        return silSupplement;
    }

    @Override
    public boolean hasSelectedAltitude() {
        return selectedAltitude > 0;
    }

    @Override
    public Integer getSelectedAltitude() {
        return selectedAltitude != 0 ? (selectedAltitude - 1) * 32 : null;
    }

    @Override
    public int getSelectedAltitudeEncoded() {
        return selectedAltitude;
    }

    /**
     * Source for selected altitude according to ED-102B §2.2.3.2.7.1.3.2
     *
     * @return true is the value of {@link #getSelectedAltitude()} is derived from the Flight Management System (FMS),
     * false if it is derived from the Control Panel/Flight Control Unit (MCP/FCU)
     */
    public boolean isFMSSelectedAltitude() {
        return selectedAltitudeType;
    }

    /**
     * ED-102B §2.2.3.2.7.1.3.4
     *
     * @return whether the Barometric Pressure Setting is available, i.e. {@link #getBarometricPressureSetting()}
     * returns a non-null value
     */
    public boolean hasBarometricPressureSetting() {
        return barometricPressureSetting != 0;
    }

    /**
     * The barometric pressure setting (minus 800 millibars) according to ED-102B §2.2.3.2.7.1.3.4
     * <p>
     * Availability of this information can also be checked with {@link #hasBarometricPressureSetting()}.
     *
     * @return the barometric pressure settings that has been adjusted by subtracting 800 millibars from the pressure
     * source (in millibars), or null if not available.
     */
    public Float getBarometricPressureSetting() {
        return barometricPressureSetting != 0 ? (barometricPressureSetting - 1) * 0.8F : null;
    }

    @Override
    public boolean hasSelectedHeading() {
        return selectedHeadingStatus;
    }

    @Override
    public Float getSelectedHeading() {
        if (!hasSelectedHeading()) return null;

        return selectedHeading * (180.f / 256) + (selectedHeadingSign ? 180F : 0F);
    }

    @Override
    public int getSelectedHeadingEncoded() {
        return ((selectedHeadingSign ? 1 : 0) << 8) | selectedHeading;
    }

    @Override
    public byte getNACp() {
        return nacP;
    }

    @Override
    public boolean getBarometricAltitudeIntegrityCode() {
        return nicBaro;
    }

    @Override
    public byte getSIL() {
        return sil;
    }

    /**
     * MCP/FCU mode status bit according to ED-102B §2.2.3.2.7.1.3.11
     * <p>
     * A value of false indicates that information of {@link #hasAutopilotEngaged()}, {@link #hasVNAVModeEngaged()},
     * {@link #hasActiveAltitudeHoldMode()}, and {@link #hasActiveApproachMode()} is not provided by the aircraft.
     *
     * @return true if Mode is deliberately being provided, false otherwise
     */
    public boolean hasMode() {
        return mcpFcuStatus;
    }

    /**
     * Auto pilot engaged flag according to ED-102B §2.2.3.2.7.1.3.12
     * <p>
     * Information is only available if {@link #hasMode()} is true.
     *
     * @return true if the autopilot system is engaged, false if not engaged or status unknown, null if not available.
     */
    public Boolean hasAutopilotEngaged() {
        if (!mcpFcuStatus) return null;
        return autopilotEngaged;
    }

    /**
     * VNAV Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.13
     * <p>
     * Information is only available if {@link #hasMode()} is true.
     *
     * @return true if vertical navigation mode is active, false otherwise or if status unknown, null if not available
     */
    public Boolean hasVNAVModeEngaged() {
        if (!mcpFcuStatus) return null;
        return vnavModeEngaged;
    }

    /**
     * Altitude Hold Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.14
     * <p>
     * Information is only available if {@link #hasMode()} is true.
     *
     * @return true if altitude hold mode is active, false if inactive or status unknown, null if not available
     */
    public Boolean hasActiveAltitudeHoldMode() {
        if (!mcpFcuStatus) return null;
        return altitudeHoldMode;
    }

    /**
     * Approach Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.16
     * <p>
     * Information is only available if {@link #hasMode()} is true.
     *
     * @return true if approach mode is active, false if inactive or status unknown, null if not available
     */
    public Boolean hasActiveApproachMode() {
        if (!mcpFcuStatus) return null;
        return approachMode;
    }

    @Override
    public boolean hasOperationalTCAS() {
        return operationalTcas;
    }

    /**
     * LNAV Mode Engaged flag according to ED-102B §2.2.3.2.7.1.3.18
     * <p>
     * Information is only available if {@link #hasMode()} is true.
     *
     * @return true if the lateral navigation mode is active, false otherwise or if status unknown, null if not available
     */
    public Boolean hasLNAVModeEngaged() {
        if (!mcpFcuStatus) return null;
        return lnavModeEngaged;
    }

    @Override
    public String toString() {
        return "TargetStateAndStatusV2Msg{" + super.toString() +
                ", silSupplement=" + silSupplement +
                ", selectedAltitudeType=" + selectedAltitudeType +
                ", selectedAltitude=" + selectedAltitude +
                ", barometricPressureSetting=" + barometricPressureSetting +
                ", selectedHeadingStatus=" + selectedHeadingStatus +
                ", selectedHeadingSign=" + selectedHeadingSign +
                ", selectedHeading=" + selectedHeading +
                ", nacP=" + nacP +
                ", nicBaro=" + nicBaro +
                ", sil=" + sil +
                ", mcpFcuStatus=" + mcpFcuStatus +
                ", autopilotEngaged=" + autopilotEngaged +
                ", vnavModeEngaged=" + vnavModeEngaged +
                ", altitudeHoldMode=" + altitudeHoldMode +
                ", approachMode=" + approachMode +
                ", operationalTcas=" + operationalTcas +
                ", lnavModeEngaged=" + lnavModeEngaged +
                '}';
    }

}
