package de.serosystems.lib1090.bds;

import de.serosystems.lib1090.exceptions.BadFormatException;

import java.io.Serializable;
import java.util.Arrays;

/**
 * BDS 3,0
 */
public class ACASActiveResolutionAdvisoryReport extends BDSRegister implements Serializable {

    // Fields
    // ------

    // BDS Number
    private short bdsCode;
    // Active Resolution Advisories
    private boolean[] activeResolutionAdvisories;
    // Resolution Advisories Components Record
    private boolean[] resolutionAdvisoriesComponentsRecord;
    // Resolution Advisories Terminated Indicator
    private boolean resolutionAdvisoriesTerminatedIndicator;
    // Multiple Threat Encounter
    private boolean multipleThreatEncounter;
    // Threat Type Indicator
    private short threatTypeIndicator;
    // Threat Identity Data
    private ThreatIdentityData threatIdentityData;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected ACASActiveResolutionAdvisoryReport() {}

    public ACASActiveResolutionAdvisoryReport(byte[] message) throws BadFormatException {

        super(message);
        setBds(BDSRegister.bdsCode.ACAS_ACTIVE_RESOLUTION_ADVISORY);

        this.bdsCode = extractBdsCode(message);
        this.activeResolutionAdvisories = extractActiveResolutionAdvisories(message);
        this.resolutionAdvisoriesComponentsRecord = extractResolutionAdvisoriesComponentsRecord(message);
        this.resolutionAdvisoriesTerminatedIndicator = extractResolutionAdvisoryTerminated(message);
        this.multipleThreatEncounter = extractMultipleThreatEncounter(message);
        this.threatTypeIndicator = extractThreatTypeIndicator(message);
        this.threatIdentityData = extractThreatIdentityData(this.threatTypeIndicator, message);

    }

    // Getters
    // -------

    public short getBdsCode() {
        return bdsCode;
    }

    public boolean[] getActiveResolutionAdvisories() {
        return computeActiveResolutionAdvisories(activeResolutionAdvisories, multipleThreatEncounter);
    }

    public boolean[] getResolutionAdvisoriesComponentsRecord() {
        return resolutionAdvisoriesComponentsRecord;
    }

    public boolean isResolutionAdvisoriesTerminatedIndicator() {
        return resolutionAdvisoriesTerminatedIndicator;
    }

    public boolean isMultipleThreatEncounter() {
        return multipleThreatEncounter;
    }

    public short getThreatTypeIndicator() {
        return threatTypeIndicator;
    }

    public ThreatIdentityData getThreatIdentityData() {
        return threatIdentityData;
    }

    // Private static methods
    // ----------------------

    public static boolean[] extractActiveResolutionAdvisories(byte[] message) {

        return new boolean[]{
                ((message[1] >>> 7) & 0x01) == 1,
                ((message[1] >>> 6) & 0x01) == 1,
                ((message[1] >>> 5) & 0x01) == 1,
                ((message[1] >>> 4) & 0x01) == 1,
                ((message[1] >>> 3) & 0x01) == 1,
                ((message[1] >>> 2) & 0x01) == 1,
                ((message[1] >>> 1) & 0x01) == 1,
                (message[1] & 0x01) == 1,
                ((message[2] >>> 7) & 0x01) == 1,
                ((message[2] >>> 6) & 0x01) == 1,
                ((message[2] >>> 5) & 0x01) == 1,
                ((message[2] >>> 4) & 0x01) == 1,
                ((message[2] >>> 3) & 0x01) == 1,
                ((message[2] >>> 2) & 0x01) == 1
        };
    }

    public static boolean[] extractResolutionAdvisoriesComponentsRecord(byte[] message) {

        boolean doNotPassBelow = ((message[2] >>> 1) & 0x01) == 1;
        boolean doNotPassAbove = (message[2] & 0x01) == 1;
        boolean doNotTurnLef = ((message[3] >>> 7) & 0x01) == 1;
        boolean doNotTurnRight = ((message[3] >>> 6) & 0x01) == 1;

        return new boolean[]{doNotPassBelow, doNotPassAbove, doNotTurnLef, doNotTurnRight};

    }

    public static boolean extractResolutionAdvisoryTerminated(byte[] message) {
        return ((message[3] >>> 5) & 0x01) == 1;
    }

    public static boolean extractMultipleThreatEncounter(byte[] message) {
        return ((message[3] >>> 4) & 0x01) == 1;
    }

    public static short extractThreatTypeIndicator(byte[] message) {
        return (short) ((message[3] >>> 2) & 0x03);
    }

    public static ThreatIdentityData extractThreatIdentityData(short threatTypeIndicator, byte[] message) {

        ThreatIdentityData threatIdentityData = null;

        switch (threatTypeIndicator) {

            case 1 :
                long icao = (((message[3] & 0x03) << 22) | ((message[4] & 0xFF) << 14) | ((message[5] & 0xFF) << 6) | ((message[6] >>> 2) & 0x3F)) & 0xFFFFFF;
                threatIdentityData = new ThreatIdentityData(icao, null, null, null);
                break;

            case 2 :
                short altitudeCode = (short) ((((message[3] & 0x03) << 11) | ((message[4] & 0xFF) << 3) | ((message[5] >>> 5) & 0x03)) & 0x1FFF);
                short threatIdentityDataRange = (short) ((((message[5] & 0x1F) << 2) | ((message[6] >>> 6) & 0x03)) & 0x7F);
                short threatIdentityDataBearing = (short) (message[6] & 0x3F);
                threatIdentityData = new ThreatIdentityData(null, altitudeCode, threatIdentityDataRange, threatIdentityDataBearing);
                break;

        }

        return threatIdentityData;

    }

    public static boolean[] computeActiveResolutionAdvisories(boolean[] ara, boolean mte) {
        return (ara[0] || mte) ? Arrays.copyOfRange(ara, 1, 7) : null;
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "ACASActiveResolutionAdvisoryReport{" +
                "bdsCode=" + bdsCode +
                ", activeResolutionAdvisories=" + Arrays.toString(activeResolutionAdvisories) +
                ", resolutionAdvisoriesComponentsRecord=" + Arrays.toString(resolutionAdvisoriesComponentsRecord) +
                ", resolutionAdvisoriesTerminatedIndicator=" + resolutionAdvisoriesTerminatedIndicator +
                ", multipleThreatEncounter=" + multipleThreatEncounter +
                ", threatTypeIndicator=" + threatTypeIndicator +
                ", threatIdentityData=" + threatIdentityData +
                '}';
    }
}
