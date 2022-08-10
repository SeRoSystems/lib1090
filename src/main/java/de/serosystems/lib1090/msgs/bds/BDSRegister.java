package de.serosystems.lib1090.msgs.bds;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoder for BDS register
 */
@SuppressWarnings("unused")
public class BDSRegister {

    // Enum
    // ----

    /**
     * Indicator set by all specializations of this class to tell
     * users which message format is encapsulated in this BDS register
     */
    public enum bdsCode {

        UNKNOWN,

        // ELEMENTARY SURVEILLANCE
        DATA_LINK_CAPABILITY_REPORT,
        COMMON_USAGE_GICB_CAPABILITY_REPORT,
        AIRCRAFT_IDENTIFICATION,
        ACAS_ACTIVE_RESOLUTION_ADVISORY,

        // ENHANCED SURVEILLANCE
        SELECTED_VERTICAL_INTENTION,
        TRACK_AND_TURN_REPORT,
        HEADING_AND_SPEED_REPORT

    }

    // Private fields
    // --------------

    private bdsCode bds;
    private byte[] message;

    // Constructors
    // ------------

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected BDSRegister() { }

    /**
     * Copy constructor for subclasses
     * @param bdsRegister instance of bdsRegister to copy from
     */
    public BDSRegister(BDSRegister bdsRegister) {
        this.bds = bdsRegister.getBds();
        this.message = bdsRegister.getMessage();
    }



    /**
     * @param message the 7-byte comm-b message (BDS register) as byte array
     */
    public BDSRegister(byte[] message) {
        this.bds = bdsCode.UNKNOWN;
        this.message = message;
    }

    // Getters
    // -------

    /**
     * @return the bds code
     */
    public bdsCode getBds() {
        return bds;
    }


    /**
     * @return the 7-byte comm-b message (BDS register)
     */
    public byte[] getMessage() {
        return message;
    }

    // Setters
    // -------

    /**
     * @param bds the bds code to set
     */
    public void setBds(bdsCode bds) {
        this.bds = bds;
    }

    // static methods
    // ---------------------

    static short extractBdsCode(byte[] message) {
        return (short) (((message[0] >>> 4) & 0x0F) * 10 + (message[0] & 0x0F));
    }

}
