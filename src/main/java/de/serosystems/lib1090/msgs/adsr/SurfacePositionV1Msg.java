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

package de.serosystems.lib1090.msgs.adsr;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.adsb.AirborneOperationalStatusV1Msg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;
import java.time.Instant;

import static de.serosystems.lib1090.decoding.SurfacePosition.decodeHCR;
import static de.serosystems.lib1090.decoding.SurfacePosition.decodeNIC;

public class SurfacePositionV1Msg extends SurfacePositionV0Msg implements Serializable {

    private static final long serialVersionUID = 5508826457167641894L;

    private boolean nic_suppl_a;

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected SurfacePositionV1Msg() {
    }

    /**
     * @param rawMessage raw ADS-R surface position message as hex string
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public SurfacePositionV1Msg(String rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param rawMessage raw ADS-R surface position message as byte array
     * @param timestamp   timestamp for this position message
     * @throws BadFormatException     if message has wrong format
     * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
     */
    public SurfacePositionV1Msg(byte[] rawMessage, Instant timestamp) throws BadFormatException, UnspecifiedFormatError {
        this(new ExtendedSquitter(rawMessage), timestamp);
    }

    /**
     * @param squitter  extended squitter which contains this surface position msg
     * @param timestamp timestamp for this position message
     * @throws BadFormatException if message has wrong format
     */
    public SurfacePositionV1Msg(ExtendedSquitter squitter, Instant timestamp) throws BadFormatException {
        super(squitter, timestamp);
    }

    /**
     * @return NIC supplement that was set before
     */
    public boolean hasNICSupplementA() {
        return nic_suppl_a;
    }

    /**
     * @param nic_suppl Navigation Integrity Category (NIC) supplement from operational status message.
     *                  Otherwise worst case is assumed for containment radius limit and NIC.
     */
    public void setNICSupplementA(boolean nic_suppl) {
        this.nic_suppl_a = nic_suppl;
    }

    /**
     * The position error, i.e., 95% accuracy for the horizontal position. For the navigation accuracy category
     * (NACp) see {@link AirborneOperationalStatusV1Msg}. Values according to DO-260B Table N-11.
     * <p>
     * The horizontal containment radius is also known as "horizontal protection level".
     *
     * @return horizontal containment radius limit in meters. A return value of -1 means "unknown".
     * If aircraft uses ADS-R version 1+, set NIC supplement A from Operational Status Message
     * for better precision.
     */
    @Override
    public double getHorizontalContainmentRadiusLimit() {
        return decodeHCR(getFormatTypeCode(), hasNICSupplementA());
    }

    /**
     * Values according to DO-260B Table N-11
     *
     * @return Navigation integrity category. A NIC of 0 means "unknown". If aircraft uses ADS-R version 1+,
     * set NIC supplement A from Operational Status Message for better precision.
     */
    @Override
    public byte getNIC() {
        return decodeNIC(getFormatTypeCode(), hasNICSupplementA());
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tSurfacePositionV1Msg{" +
                "nic_suppl_a=" + nic_suppl_a +
                '}';
    }

    @Override
    public subtype getType() {
        return subtype.ADSR_SURFACE_POSITION_V1;
    }
}
