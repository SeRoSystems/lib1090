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
 * Everything a System Design Assurance (SDA) says about the equipment transmitting it, as one row per
 * encoded value, ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58.
 * <p>
 * The field is two bits in the operational mode code, and the standard gives each value three
 * columns: the failure condition the design supports, how often an undetected fault may cause false
 * or misleading information to be transmitted, and the software and hardware assurance level the
 * equipment was developed to. They are one row read three ways, not three independent facts, but a
 * caller normally wants one of them, so each is reachable on its own.
 * <p>
 * Version 1 transmits no such field and version 0 defines no operational mode code at all, so this
 * is reached only from the version 2 and 3 layouts.
 */
public enum SystemDesignAssurance {

    /**
     * SDA 0, the row that claims nothing: the failure condition and the probability each combine
     * "unknown" with their poorest value, and no assurance level is named.
     */
    UNKNOWN_OR_NO_SAFETY_EFFECT(SupportedFailureCondition.UNKNOWN_OR_NO_SAFETY_EFFECT,
            UndetectedFaultProbability.UNKNOWN_OR_ABOVE_1E_MINUS_3,
            DesignAssuranceLevel.NOT_APPLICABLE),

    /** SDA 1: minor failure condition, 10&#94;-3 per flight hour or less, assurance level D. */
    MINOR(SupportedFailureCondition.MINOR,
            UndetectedFaultProbability.AT_MOST_1E_MINUS_3,
            DesignAssuranceLevel.LEVEL_D),

    /** SDA 2: major failure condition, 10&#94;-5 per flight hour or less, assurance level C. */
    MAJOR(SupportedFailureCondition.MAJOR,
            UndetectedFaultProbability.AT_MOST_1E_MINUS_5,
            DesignAssuranceLevel.LEVEL_C),

    /** SDA 3: hazardous failure condition, 10&#94;-7 per flight hour or less, assurance level B. */
    HAZARDOUS(SupportedFailureCondition.HAZARDOUS,
            UndetectedFaultProbability.AT_MOST_1E_MINUS_7,
            DesignAssuranceLevel.LEVEL_B);

    /** The field is two bits wide, so this is every value a message can carry. */
    private static final int MAX_SDA = 3;

    private final SupportedFailureCondition supportedFailureCondition;
    private final UndetectedFaultProbability undetectedFaultProbability;
    private final DesignAssuranceLevel designAssuranceLevel;

    SystemDesignAssurance(SupportedFailureCondition supportedFailureCondition,
                          UndetectedFaultProbability undetectedFaultProbability,
                          DesignAssuranceLevel designAssuranceLevel) {
        this.supportedFailureCondition = supportedFailureCondition;
        this.undetectedFaultProbability = undetectedFaultProbability;
        this.designAssuranceLevel = designAssuranceLevel;
    }

    /**
     * The row an encoded System Design Assurance selects, ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58.
     *
     * @param sda the encoded system design assurance, 0 to 3
     * @return what that value assures
     * @throws IllegalArgumentException if the value is outside the two bits the field occupies
     */
    public static SystemDesignAssurance forSDA(byte sda) {
        if (sda < 0 || sda > MAX_SDA)
            throw new IllegalArgumentException(
                    "System design assurance " + sda + " does not fit the two bits it occupies");

        switch (sda) {
            case 1:
                return MINOR;
            case 2:
                return MAJOR;
            case 3:
                return HAZARDOUS;
            default:
                return UNKNOWN_OR_NO_SAFETY_EFFECT;
        }
    }

    /**
     * @return the failure condition the design supports, which for the lowest value combines
     * "unknown" with "no safety effect"
     */
    public SupportedFailureCondition getSupportedFailureCondition() {
        return supportedFailureCondition;
    }

    /**
     * @return how often an undetected fault may cause false or misleading information to be
     * transmitted, per flight hour
     */
    public UndetectedFaultProbability getUndetectedFaultProbability() {
        return undetectedFaultProbability;
    }

    /**
     * @return the software and hardware assurance level the equipment was developed to, which the
     * table names "N/A" for the lowest value
     */
    public DesignAssuranceLevel getDesignAssuranceLevel() {
        return designAssuranceLevel;
    }

    /**
     * The failure condition a System Design Assurance claims to support, ED-102B §2.2.3.2.7.2.4.6
     * TABLE 2-58.
     * <p>
     * The names come from the safety assessment vocabulary the certification standards share, where a
     * condition is classified by how bad its effects are. A higher System Design Assurance supports a
     * worse condition, because the design was assured against it.
     * <p>
     * <b>Category 0 is two answers in one, and the standard makes it so.</b> It reads "Unknown / No
     * safety effect", so a receiver cannot tell an installation assessed as harmless from one that
     * simply reports nothing. A constant naming only the assessment would credit it to both.
     */
    public enum SupportedFailureCondition {

        /**
         * SDA 0, which ED-102B defines as "Unknown / No safety effect" — one category for both, so
         * nothing is claimed.
         */
        UNKNOWN_OR_NO_SAFETY_EFFECT,

        /** SDA 1: a minor failure condition is supported. */
        MINOR,

        /** SDA 2: a major failure condition is supported. */
        MAJOR,

        /** SDA 3: a hazardous failure condition is supported. */
        HAZARDOUS
    }

    /**
     * The probability of an undetected fault causing the transmission of false or misleading information,
     * as a System Design Assurance reports it, ED-102B §2.2.3.2.7.2.4.6 TABLE 2-58.
     * <p>
     * The four values are the same figures {@link SourceIntegrityLevel} tabulates, and they are not the
     * same quantity: that one bounds how often a position falls outside its containment radius, this one
     * how often the equipment transmits something false without noticing. A system can be trustworthy by
     * one measure and not the other, so the two are separate types and neither may be read for the other.
     * <p>
     * <b>Unlike the source integrity level, this is always per flight hour.</b> That figure needs the SIL
     * supplement to say whether it is per hour or per sample; this one never does, the table stating the
     * basis itself.
     * <p>
     * <b>Category 0 is two answers in one, and the standard makes it so.</b> It reads "more than 10^-3
     * per flight hour or unknown", which a receiver cannot separate, so it guarantees nothing.
     */
    public enum UndetectedFaultProbability {

        /**
         * SDA 0, which ED-102B defines as "&gt; 10&#94;-3 per flight hour or unknown" — one category for
         * both, so no bound is guaranteed.
         */
        UNKNOWN_OR_ABOVE_1E_MINUS_3(Double.NaN),

        /** SDA 1: the probability is 10&#94;-3 per flight hour or less. */
        AT_MOST_1E_MINUS_3(1e-3),

        /** SDA 2: the probability is 10&#94;-5 per flight hour or less. */
        AT_MOST_1E_MINUS_5(1e-5),

        /** SDA 3: the probability is 10&#94;-7 per flight hour or less. */
        AT_MOST_1E_MINUS_7(1e-7);

        private final double probabilityPerFlightHour;

        UndetectedFaultProbability(double probabilityPerFlightHour) {
            this.probabilityPerFlightHour = probabilityPerFlightHour;
        }

        /**
         * The largest probability per flight hour this category guarantees the true one stays at or below,
         * or {@link Double#NaN} where it guarantees none.
         * <p>
         * As with {@link ContainmentRadius#getGuaranteedUpperBound()}, the encoding is chosen so that
         * {@code getGuaranteedUpperBound() < limit} is <b>false</b> wherever nothing is guaranteed, so a
         * caller asking "is this equipment known to fault less often than {@code limit}?" gets the
         * conservative answer without a special case. {@code NaN} rather than an infinity, because
         * {@link #UNKNOWN_OR_ABOVE_1E_MINUS_3} may also mean nothing was reported — the reverse question
         * cannot be answered either.
         * <p>
         * The bound is inclusive: {@link #AT_MOST_1E_MINUS_3} guarantees 10&#94;-3 or less.
         *
         * @return the guaranteed probability per flight hour, or {@code NaN}
         */
        public double getGuaranteedUpperBound() {
            return probabilityPerFlightHour;
        }

        /**
         * @return true where a probability is guaranteed, i.e. for every category but
         * {@link #UNKNOWN_OR_ABOVE_1E_MINUS_3}
         */
        public boolean isKnown() {
            return !Double.isNaN(probabilityPerFlightHour);
        }
    }

    /**
     * The software and hardware design assurance level a System Design Assurance reports, ED-102B
     * §2.2.3.2.7.2.4.6 TABLE 2-58.
     * <p>
     * The levels are those of the software and airborne electronic hardware development standards, where
     * A is the most rigorous. System Design Assurance reaches B at its highest, so the levels above it
     * have no encoding here and are deliberately absent rather than declared and unreachable.
     * <p>
     * The lowest System Design Assurance claims no level at all, which the table states as "N/A" — not a
     * letter, which is why this is an enumeration and not a character.
     */
    public enum DesignAssuranceLevel {

        /** SDA 0: no assurance level is claimed, the table reading "N/A". */
        NOT_APPLICABLE,

        /** SDA 1: design assurance level D. */
        LEVEL_D,

        /** SDA 2: design assurance level C. */
        LEVEL_C,

        /** SDA 3: design assurance level B. */
        LEVEL_B
    }
}
