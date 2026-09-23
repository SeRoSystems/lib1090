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

/**
 * TIS-B messages: traffic reports a ground station synthesises from other surveillance, such as
 * radar, for aircraft that do not broadcast ADS-B themselves.
 * <p>
 * Because the source is not the target, these formats say less and say it differently: positions come
 * as coarse or fine variants, and a field the aircraft would have transmitted may simply be absent,
 * which these classes report as {@code null} rather than as a category meaning "poor". That
 * distinction matters when reading the quality a message claims — an absent field is not a reported
 * one, and {@link de.serosystems.lib1090.decoding.quality} describes only the latter.
 */
package de.serosystems.lib1090.msgs.tisb;
