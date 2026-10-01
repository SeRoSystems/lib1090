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
 * The content of ACAS resolution advisory messages, message bits 41-88, as ICAO Annex 10 Volume IV (6th edition)
 * §4.3.8.4.2 defines it. Three containers carry it: the RA report in the MB field of a Comm-B reply (BDS 3,0,
 * {@link de.serosystems.lib1090.msgs.bds.ACASActiveResolutionAdvisoryReport}), the 1090ES RA broadcast, which carries
 * the same bits in ME bits 9-56 ({@link de.serosystems.lib1090.msgs.adsb.ACASResolutionAdvisoryMsg}), and the
 * coordination reply in the MV field of a long air-air surveillance reply, DF=16
 * ({@link de.serosystems.lib1090.msgs.modes.LongACAS}), which has the first part only.
 * <p>
 * The content has a layout per collision avoidance system, which the RA message format (RMF, bits 53-54) tells apart,
 * see {@link de.serosystems.lib1090.msgs.acas.RAMessageFormat}: the TCAS layout, §4.3.8.4.2.2.1, and the ACAS X
 * layout, §4.3.8.4.2.2.2. TCAS II version 6.04 systems (FAA TSO-C119A) transmit a third, older one, RTCA DO-185B
 * §2.2.3.9.3.2.3.1.2. The interfaces state what the layouts have in common, the classes what each one has:
 * <ul>
 *     <li>{@link de.serosystems.lib1090.msgs.acas.ResolutionAdvisory}: ARA and RAC, in every layout;
 *     {@link de.serosystems.lib1090.msgs.acas.TCAS6ResolutionAdvisory}</li>
 *     <li>{@link de.serosystems.lib1090.msgs.acas.ResolutionAdvisoryState}: RAT and MTE as well, the content of a
 *     coordination reply; {@link de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisory},
 *     {@link de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisory}</li>
 *     <li>{@link de.serosystems.lib1090.msgs.acas.ResolutionAdvisoryReport}: the threat identity as well, the content
 *     of an RA report; {@link de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisoryReport},
 *     {@link de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisoryReport}</li>
 * </ul>
 * {@link de.serosystems.lib1090.msgs.acas.ResolutionAdvisories} picks the layout. The package holds no messages:
 * the air-air replies themselves, {@link de.serosystems.lib1090.msgs.modes.ShortACAS} and
 * {@link de.serosystems.lib1090.msgs.modes.LongACAS}, are Mode S downlink formats and stay in {@code msgs.modes}.
 */
package de.serosystems.lib1090.msgs.acas;
