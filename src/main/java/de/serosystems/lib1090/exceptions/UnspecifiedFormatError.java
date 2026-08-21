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

package de.serosystems.lib1090.exceptions;

/**
 * Exception which is thrown when a raw message is passed to the wrong
 * decoder. E.g. when the format type code in the raw message does not
 * correspond to the message type.
 * <p>
 * The applicable table or paragraph depends on the throwing decoder; the most common are
 * ED-102B §2.2.3.2.2 TABLE 2-9 for ADS-B message type determination, ED-102B §2.2.3.2.2
 * TABLE 2-10 for TIS-B/Traffic Uplink Management message type determination, ED-102B
 * §2.2.17.2 TABLE 2-184 for DF=18 CF code definitions and, for Mode S downlink formats
 * outside ED-102B's scope, ICAO Annex 10 Volume IV §3.1.2.
 */
public class UnspecifiedFormatError extends Exception {
    private static final long serialVersionUID = 6482688479919911669L;

    public UnspecifiedFormatError(String reason) {
        super(reason);
    }
}
