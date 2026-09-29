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

package de.serosystems.lib1090.decoding;

/**
 * The "Contingency Plan" of a UAS/RPAS Contingency Message, ED-102B §2.2.3.2.8.1.3.1 TABLE 2-104.
 */
public enum ContingencyPlan {

    /**
     * The whole UAS/RPAS Contingency Message is invalid or carries no data.
     */
    INVALID_OR_NO_DATA(0, "UAS/RPAS Contingency Message is Invalid OR No Data"),
    RETURN_TO_TAKEOFF_LOCATION(1, "Return to Takeoff Location"),
    HOLD_AT_FIX(2, "Hold at Fix"),
    CONTINUE_FLIGHT_PLAN_AS_FILED(3, "Continue Flight Plan as Filed"),
    DIVERT_TO_ALTERNATE_LANDING_LOCATION(4, "Divert to Alternate Landing Location"),
    DITCHING_AIRCRAFT_OR_FLIGHT_TERMINATION(5, "Ditching Aircraft / Flight Termination"),
    OPERATOR_DEFINED_PLAN_A(6, "Operator Defined Plan A"),
    OPERATOR_DEFINED_PLAN_B(7, "Operator Defined Plan B"),
    OPERATOR_DEFINED_PLAN_C(8, "Operator Defined Plan C"),
    OPERATOR_DEFINED_PLAN_D(9, "Operator Defined Plan D"),
    OPERATOR_DEFINED_PLAN_E(10, "Operator Defined Plan E"),
    OPERATOR_DEFINED_PLAN_F(11, "Operator Defined Plan F"),
    OPERATOR_DEFINED_PLAN_G(12, "Operator Defined Plan G"),
    OPERATOR_DEFINED_PLAN_H(13, "Operator Defined Plan H"),
    OPERATOR_DEFINED_PLAN_I(14, "Operator Defined Plan I"),
    LOST_LINK_RESTORED(15, "Lost Link Restored");

    /**
     * In declaration order, which is the order of the encoded values.
     */
    private static final ContingencyPlan[] BY_CODE = values();

    private final byte encoded;
    private final String text;

    ContingencyPlan(int encoded, String text) {
        this.encoded = (byte) encoded;
        this.text = text;
    }

    /**
     * @param encoded the 4-bit encoded contingency plan
     * @return the contingency plan it stands for
     * @throws IllegalArgumentException if the value is not 0 to 15
     */
    public static ContingencyPlan forEncoded(int encoded) {
        if (encoded < 0 || encoded >= BY_CODE.length)
            throw new IllegalArgumentException("Contingency plan is a 4-bit field, got " + encoded);
        return BY_CODE[encoded];
    }

    /**
     * @return the encoded value, TABLE 2-104
     */
    public byte getEncoded() {
        return encoded;
    }

    /**
     * @return the meaning as TABLE 2-104 states it
     */
    public String getText() {
        return text;
    }
}
