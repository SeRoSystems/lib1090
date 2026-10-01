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

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.msgs.QualifiedAddress;
import de.serosystems.lib1090.msgs.bds.*;

/**
 * A Comm-B reply, the altitude one (DF=20, {@link CommBAltitudeReply}) or the identity one (DF=21,
 * {@link CommBIdentifyReply}), whose 56-bit MB field carries a transponder register.
 * <p>
 * The reply does not tell which register it carries: that depends on the interrogation it answers, which the receiver
 * of a reply does not see. The {@code as…()} methods therefore decode MB as the register the caller asserts it is.
 * Three registers identify themselves in MB bits 1-8, BDS1 and BDS2: the data link capability report (BDS 1,0), the
 * aircraft identification (BDS 2,0) and the ACAS active resolution advisory report (BDS 3,0); their constructors throw
 * a {@link BadFormatException} if MB identifies itself otherwise. The other registers carry no code, so for them the
 * assertion stays the caller's. All methods share MB with this reply, as {@link #getMessage()} does.
 * <p>
 * <b>Data parity.</b> The last 24 bits of a Comm-B reply are normally the address/parity field (AP), parity overlaid on
 * the aircraft address, which {@link #getAddress()} reports. If the interrogation sets the overlay control bit (OVC)
 * and the transponder supports it, they are the data parity field (DP) instead: parity overlaid on a "Modified AA", the
 * aircraft address with its most significant 8 bits XORed with BDS1 and BDS2 of the requested register, e.g. F5AAAA
 * for address AAAAAA and register 5,F (ICAO Annex 10 Volume IV (6th edition) §3.1.2.3.2.1.5). Mode S level 2
 * transponders certified on or after 1 January 2020 shall have data parity with overlay control (§2.1.5.4.3). The reply
 * does not tell which of the two it carries, since OVC is in the interrogation; {@link #getAddress()} is then the
 * Modified AA, and {@link #getAddressAssumingDataParity(BDSCode)} gives the address under that assumption.
 */
public interface CommBReply {

    /**
     * The address of the reply, recovered from its AP field. If the reply carries data parity instead, this is the
     * "Modified AA" rather than the aircraft address; see the interface documentation and
     * {@link #getAddressAssumingDataParity(BDSCode)}.
     *
     * @return the address as recovered from the parity field
     */
    QualifiedAddress getAddress();

    /**
     * The aircraft address, assuming that the reply carries data parity (DP) for the given register: {@link
     * #getAddress()} with its most significant 8 bits XORed with BDS1 and BDS2 again (ICAO Annex 10 Volume IV
     * §3.1.2.3.2.1.5). If the reply carries address parity (AP), the result is wrong; whether it does, the reply does
     * not tell.
     *
     * @param code the register the interrogation requested
     * @return the address under that assumption
     */
    default QualifiedAddress getAddressAssumingDataParity(BDSCode code) {
        QualifiedAddress modified = getAddress();
        int bds = (code.getBDS1() << 4 | code.getBDS2()) << 16;
        return new QualifiedAddress(modified.getAddress() ^ bds, modified.getType(), modified.getSource());
    }

    /**
     * The aircraft address, assuming that the reply carries data parity (DP) for the given register, e.g. one decoded
     * from this reply with an {@code as…()} method: {@link #getAddressAssumingDataParity(BDSCode)} with its
     * {@link BDSRegister#getBDSCode()}.
     *
     * @param register the register the interrogation requested
     * @return the address under that assumption
     */
    default QualifiedAddress getAddressAssumingDataParity(BDSRegister register) {
        return getAddressAssumingDataParity(register.getBDSCode());
    }

    /**
     * The Comm-B message (BDS register; register numbering and content per ICAO Doc 9871 (First Edition, AN/464)
     * §A.2.1 Register Allocation — individual registers are decoded in package de.serosystems.lib1090.msgs.bds).
     * <p>
     * <b>The array is not a copy and must not be modified:</b> it is this message's own, shared with every message
     * decoded or copied from it, so a change would alter all of them. It is not copied for performance.
     *
     * @return the 7-byte Comm-B message
     */
    byte[] getMessage();

    /**
     * @return MB decoded as the data link capability report, BDS 1,0, in the layout its Mode S subnetwork version
     * number selects
     * @throws BadFormatException if MB is not BDS 1,0, see {@link DataLinkCapabilityReport#decode(byte[])}
     */
    default DataLinkCapabilityReport asDataLinkCapabilityReport() throws BadFormatException {
        return DataLinkCapabilityReport.decode(getMessage());
    }

    /**
     * @return MB decoded as the common usage GICB capability report, BDS 1,7
     */
    default CommonUsageGICBCapabilityReport asCommonUsageGICBCapabilityReport() {
        return new CommonUsageGICBCapabilityReport(getMessage());
    }

    /**
     * @return MB decoded as the aircraft identification, BDS 2,0
     * @throws BadFormatException if MB is not BDS 2,0, see
     *                            {@link AircraftIdentification#AircraftIdentification(byte[])}
     */
    default AircraftIdentification asAircraftIdentification() throws BadFormatException {
        return new AircraftIdentification(getMessage());
    }

    /**
     * @return MB decoded as the ACAS active resolution advisory report, BDS 3,0
     * @throws BadFormatException if MB is not BDS 3,0, see
     *                            {@link ACASActiveResolutionAdvisoryReport#ACASActiveResolutionAdvisoryReport(byte[])}
     */
    default ACASActiveResolutionAdvisoryReport asACASActiveResolutionAdvisoryReport() throws BadFormatException {
        return new ACASActiveResolutionAdvisoryReport(getMessage());
    }

    /**
     * @return MB decoded as the selected vertical intention, BDS 4,0
     */
    default SelectedVerticalIntention asSelectedVerticalIntention() {
        return new SelectedVerticalIntention(getMessage());
    }

    /**
     * @return MB decoded as the track and turn report, BDS 5,0
     */
    default TrackAndTurn asTrackAndTurn() {
        return new TrackAndTurn(getMessage());
    }

    /**
     * @return MB decoded as the heading and speed report, BDS 6,0
     */
    default HeadingAndSpeed asHeadingAndSpeed() {
        return new HeadingAndSpeed(getMessage());
    }
}
