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

package de.serosystems.lib1090.msgs.squitter;

/**
 * Common API for the weather-related ADS-B/ADS-R Wx AIREP subtypes: the weather state subtype (1)
 * and the alternate weather state subtype (2). Further fields shared by these two subtypes will
 * be added here as they are decoded.
 */
public interface WxAIREPWeatherMsg extends WxAIREPMsg {

    /**
     * @return the raw encoded icing status field
     */
    byte getIcingStatusEncoded();

    /**
     * @return the raw air temperature type bit: {@code false} means "total air temperature",
     * {@code true} means "static air temperature"
     */
    boolean getAirTemperatureType();

    /**
     * @return the raw encoded air temperature field
     */
    short getAirTemperatureEncoded();

    /**
     * @return whether the air temperature field is available
     */
    default boolean hasAirTemperature() {
        return getAirTemperatureEncoded() != 0;
    }

    /**
     * Decode the air temperature field.
     *
     * @return a lower bound for the air temperature in degrees Celsius, {@code -77} meaning "below
     * -76.5°C", or {@code null} if unavailable
     * @see #getAirTemperatureType() to determine whether this is total or static air temperature
     */
    default Double getAirTemperature() {
        if (!hasAirTemperature()) return null;
        return getAirTemperatureEncoded() * 0.5 - 77.5;
    }

    /**
     * @return the raw airspeed type bit: {@code false} means Indicated Airspeed (IAS),
     * {@code true} means True Airspeed (TAS)
     */
    boolean getAirspeedType();

    /**
     * @return the raw encoded airspeed field
     */
    short getAirspeedEncoded();

    /**
     * @return whether the airspeed field is available
     */
    default boolean hasAirspeed() {
        return getAirspeedEncoded() != 0;
    }

    /**
     * Decode the airspeed field.
     *
     * @return a lower bound for the airspeed in knots, or {@code null} if unavailable
     * @see #getAirspeedType() to determine whether this is indicated or true airspeed
     */
    default Short getAirspeed() {
        if (!hasAirspeed()) return null;
        return (short) (getAirspeedEncoded() - 1);
    }
}
