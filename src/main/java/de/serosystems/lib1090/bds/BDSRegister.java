package de.serosystems.lib1090.bds;

public class BDSRegister {

    // Enum
    // ----

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

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    public BDSRegister() {
    }

    public BDSRegister(byte[] message) {
        this.bds = bdsCode.UNKNOWN;
        this.message = message;
    }

    public BDSRegister(BDSRegister bdsRegister) {
        this.bds = bdsRegister.getBds();
        this.message = bdsRegister.getMessage();
    }

    // Getters
    // -------

    public bdsCode getBds() {
        return bds;
    }

    public byte[] getMessage() {
        return message;
    }

    // Setters
    // -------

    public void setBds(bdsCode bds) {
        this.bds = bds;
    }

    // Public static methods
    // ---------------------

    public static short extractBdsCode(byte[] message) {
        return (short) (((message[0] >>> 4) & 0x0F) * 10 + (message[0] & 0x0F));
    }

}
