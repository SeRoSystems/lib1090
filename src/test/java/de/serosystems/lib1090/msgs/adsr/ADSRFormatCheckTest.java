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
import de.serosystems.lib1090.msgs.modes.TypeCodedExtendedSquitter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ED-102B §2.2.18.3: only DF=18 with CF=6 is ADS-R, so every ADS-R message class rejects other messages on
 * construction, whatever their TYPE Code.
 */
class ADSRFormatCheckTest {

    private static final Class<?>[] CLASSES = {
            AirborneOperationalStatusV1Msg.class, AirborneOperationalStatusV2Msg.class,
            AirborneOperationalStatusV3Msg.class, AirbornePositionV1Msg.class, AirbornePositionV2Msg.class,
            AirbornePositionV3Msg.class, AirborneVelocityV3Msg.class, AirspeedHeadingV1Msg.class,
            AirspeedHeadingV2Msg.class, EmergencyOrPriorityStatusV1Msg.class, EmergencyOrPriorityStatusV2Msg.class,
            EmergencyOrPriorityStatusV3Msg.class, IdentificationV1Msg.class, IdentificationV2Msg.class,
            IdentificationV3Msg.class, SurfaceOperationalStatusV1Msg.class, SurfaceOperationalStatusV2Msg.class,
            SurfaceOperationalStatusV3Msg.class, SurfacePositionV1Msg.class, SurfacePositionV2Msg.class,
            SurfacePositionV3Msg.class, TargetStateAndStatusV2Msg.class, TargetStateAndStatusV3Msg.class,
            VelocityOverGroundV1Msg.class, VelocityOverGroundV2Msg.class, WxAIREPAircraftStateMsg.class,
            WxAIREPAlternateWeatherStateMsg.class, WxAIREPWeatherStateMsg.class,
    };

    @Test
    void everyClassRejectsOtherThanDF18CF6() throws Exception {
        String[] notADSR = {
                "8D4840D6202CC371C32CE0576098", // DF=17
                "9240621D58C386435CC412692AD6", // DF=18, CF=2 (TIS-B)
        };
        for (Class<?> c : CLASSES)
            for (String raw : notADSR) {
                BadFormatException e = assertThrows(BadFormatException.class,
                        () -> construct(c, new TypeCodedExtendedSquitter(raw)), c.getSimpleName() + " " + raw);
                assertTrue(e.getMessage().contains("downlink format 18 and CF value 6"),
                        c.getSimpleName() + " " + raw + ": " + e.getMessage());
            }
    }

    private static void construct(Class<?> c, TypeCodedExtendedSquitter squitter) throws Exception {
        for (Constructor<?> constructor : c.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 0 || parameters[0] != TypeCodedExtendedSquitter.class) continue;
            Object[] arguments = parameters.length == 1
                    ? new Object[]{squitter} : new Object[]{squitter, Instant.EPOCH};
            try {
                constructor.newInstance(arguments);
            } catch (InvocationTargetException e) {
                throw (Exception) e.getCause();
            }
            return;
        }
        fail(c.getSimpleName() + " has no constructor taking a TypeCodedExtendedSquitter");
    }

}
