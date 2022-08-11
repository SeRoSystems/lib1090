package de.serosystems.lib1090.msgs;

import de.serosystems.lib1090.cpr.CPREncodedPosition;
import de.serosystems.lib1090.Position;

public interface PositionMsg {

    /**
     * @return true if this message has a valid CPR encoded position
     */
    boolean hasValidPosition();

    /**
     * @return the CPR encoded position that was announced in this message
     */
    CPREncodedPosition getCPREncodedPosition();

    /**
     * @return whether altitude information is available
     */
    boolean hasValidAltitude();

    /**
     * @return the decoded altitude in feet or null if altitude is not available. The latter can be checked with
     * {@link #hasValidAltitude()}. Also see {@link #getAltitudeType()}.
     */
    Integer getAltitude();

    /**
     * @return reference system used for altitude
     */
    Position.AltitudeType getAltitudeType();

}
