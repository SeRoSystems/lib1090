package de.serosystems.lib1090.msgs;

public interface PositionMsgWithTime extends PositionMsg {

    /**
     * @return flag which will indicate whether the Time of Applicability of the message
     *         is synchronized with UTC time. False will denote that the time is not synchronized
     *         to UTC. True will denote that Time of Applicability is synchronized to UTC time.
     */
    boolean hasTimeFlag();

}
