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

/**
 * The reply information (RI) of the air-air surveillance replies, the short one (DF=0, {@link ShortACAS}) and the long
 * one (DF=16, {@link LongACAS}): a 4-bit field (bits 14-17) whose codes tell the kind of reply and, depending on it,
 * either the ACAS capability or the maximum cruising true airspeed.
 * <p>
 * ICAO Annex 10 Volume IV (6th edition), §4.3.8.4.1.2 (codes 0-7) and §3.1.2.8.2.2 (codes 8-15), with code 1 as the
 * Mode S MOPS define it:
 * <ul>
 *     <li>0: no operating ACAS (a reply to an interrogation with AQ = 0, as are codes 0-7)</li>
 *     <li>1: not assigned in the Annex; EUROCAE ED-73F (RTCA DO-181F) §3.27.1.5 defines it as "Active CAS of junior
 *     status with resolution capability or Passive CAS with resolution capability and a Mode S transponder", and
 *     ED-102B (RTCA DO-260C) TABLE 2-44 counts it, as code 3, as a CAS that can issue an RA. A junior CAS adapts its RA
 *     sense to the senior one, a passive CAS coordinates via ADS-B only. Neither says in which dimension it
 *     resolves.</li>
 *     <li>2: ACAS with resolution capability inhibited, reported also at sensitivity level 2 or with the TA only mode
 *     selected</li>
 *     <li>3: ACAS with vertical-only resolution capability and capability to utilize 1 030/1 090 MHz discrete Mode S
 *     interrogations/replies for coordination</li>
 *     <li>4: ACAS with vertical and horizontal resolution capability and capability to utilize 1 030/1 090 MHz
 *     discrete Mode S interrogations/replies for coordination</li>
 *     <li>5-6: reserved for passive ACAS; 7: not assigned</li>
 *     <li>8-15: an acquisition reply, to an interrogation with AQ = 1, which gives the maximum airspeed instead of the
 *     ACAS capability, see {@link #getMaximumAirspeed()}</li>
 * </ul>
 * Codes 0-7 are a tracking reply, 8-15 an acquisition reply: bit 14 replicates the AQ bit of the interrogation. The
 * ACAS accessors therefore know nothing about an acquisition reply and return null for it, as for the codes that are
 * not assigned or reserved.
 * <p>
 * ED-73F describes codes 1 and 3 alike as "with resolution capability" and assigns no code 4: the MOPS tell whether a
 * CAS can resolve, the Annex in which dimension. {@link #hasResolutionCapability()} answers the first, independent of
 * the dimension; {@link #hasVerticalResolutionCapability()} and {@link #hasHorizontalResolutionCapability()} answer
 * the second where the Annex does, for codes 3 and 4.
 */
public interface AirAirSurveillanceReply {

    /**
     * @return the reply information (RI) as transmitted, see the interface documentation for its codes
     */
    byte getReplyInformationEncoded();

    /**
     * @return whether this is an acquisition reply (RI 8-15), which reports the maximum airspeed and no ACAS
     * capability, rather than a tracking reply (RI 0-7)
     */
    default boolean isAcquisitionReply() {
        return getReplyInformationEncoded() >= 8;
    }

    /**
     * @return true if the reply reports an operating ACAS (RI 1-4), false if it reports none (RI 0), or null if it does
     * not tell: an acquisition reply, or a code that is not assigned or reserved (RI 5-7)
     */
    default Boolean hasOperatingACAS() {
        switch (getReplyInformationEncoded()) {
            case 0:
                return false;
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return null;
        }
    }

    /**
     * @return true if the reply reports resolution capability in some dimension, i.e. that ACAS can issue RAs (RI 1, 3
     * and 4), false if it reports none (RI 0, no operating ACAS, or 2, resolution capability inhibited), or null if it
     * does not tell: an acquisition reply, or a code that is not assigned or reserved (RI 5-7)
     */
    default Boolean hasResolutionCapability() {
        switch (getReplyInformationEncoded()) {
            case 0:
            case 2:
                return false;
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return null;
        }
    }

    /**
     * @return true if the reply reports vertical resolution capability (RI 3-4), false if it reports none (RI 0, no
     * operating ACAS, or 2, resolution capability inhibited), or null if it does not tell: an acquisition reply, RI 1,
     * which reports resolution capability without a dimension (see {@link #hasResolutionCapability()}), or a code that
     * is not assigned or reserved (RI 5-7)
     */
    default Boolean hasVerticalResolutionCapability() {
        switch (getReplyInformationEncoded()) {
            case 0:
            case 2:
                return false;
            case 3:
            case 4:
                return true;
            default:
                return null;
        }
    }

    /**
     * @return true if the reply reports horizontal resolution capability (RI 4), false if it reports none (RI 0, 2 or
     * 3), or null if it does not tell: an acquisition reply, RI 1, which reports resolution capability without a
     * dimension (see {@link #hasResolutionCapability()}), or a code that is not assigned or reserved (RI 5-7)
     */
    default Boolean hasHorizontalResolutionCapability() {
        switch (getReplyInformationEncoded()) {
            case 0:
            case 2:
            case 3:
                return false;
            case 4:
                return true;
            default:
                return null;
        }
    }

    /**
     * The maximum cruising true airspeed of an acquisition reply, ICAO Annex 10 Volume IV §3.1.2.8.2.2: RI 9 at most
     * 75 kt, 10 more than 75 and at most 150 kt, 11 up to 300 kt, 12 up to 600 kt, 13 up to 1200 kt, 14 more than
     * 1200 kt.
     *
     * @return the upper bound of the maximum airspeed in kt, {@code Integer.MAX_VALUE} for RI 14, which has none, or
     * null for a tracking reply, RI 8 (no maximum airspeed data available) or RI 15 (not assigned)
     */
    default Integer getMaximumAirspeed() {
        switch (getReplyInformationEncoded()) {
            case 9:
                return 75;
            case 10:
                return 150;
            case 11:
                return 300;
            case 12:
                return 600;
            case 13:
                return 1200;
            case 14:
                return Integer.MAX_VALUE;
            default:
                return null;
        }
    }
}
