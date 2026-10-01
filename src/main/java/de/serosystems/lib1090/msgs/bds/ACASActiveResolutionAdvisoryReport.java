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

package de.serosystems.lib1090.msgs.bds;

import de.serosystems.lib1090.decoding.BitReader;
import de.serosystems.lib1090.msgs.acas.RAMessageFormat;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisories;
import de.serosystems.lib1090.msgs.acas.ResolutionAdvisory;

/**
 * Decoder for the ACAS active resolution advisory report (BDS 3,0), as defined in ICAO Doc 9871
 * (First Edition, AN/464) §A.2 TABLE A-2-48, for the register layout; field semantics per
 * ICAO Annex 10 Volume IV (6th edition) §4.3.8.4.2.2.
 * <p>
 * MB bits 9-56, message bits 41-88, have a layout per collision avoidance system; {@link #getResolutionAdvisory()}
 * decodes them in the one their RA message format selects, see {@link de.serosystems.lib1090.msgs.acas}.
 */
@SuppressWarnings("unused")
public class ACASActiveResolutionAdvisoryReport extends BDSRegister {
    private static final long serialVersionUID = -5637418816699536015L;

    private static final BDSCode BDS_CODE = new BDSCode(3, 0);

    /**
     * protected no-arg constructor e.g. for serialization with Kryo
     **/
    protected ACASActiveResolutionAdvisoryReport() {
    }

    /**
     * @param msg the 7-byte comm-b message (BDS register) as byte array
     */
    public ACASActiveResolutionAdvisoryReport(byte[] msg) {
        super(msg);
    }

    /**
     * @return message bits 41-88 (MB bits 9-56) as transmitted, right-aligned
     */
    public long getResolutionAdvisoryEncoded() {
        return BitReader.forBigEndian(getMessage()).readLong(9, 56);
    }

    /**
     * @return the collision avoidance system that generated the report, and so its layout, bits 53-54
     */
    public RAMessageFormat getRAMessageFormat() {
        return ResolutionAdvisories.messageFormat(getResolutionAdvisoryEncoded());
    }

    /**
     * @return the report's content in the layout of its RA message format: a
     * {@link de.serosystems.lib1090.msgs.acas.TCASResolutionAdvisoryReport}, an
     * {@link de.serosystems.lib1090.msgs.acas.ACASXResolutionAdvisoryReport} or a
     * {@link de.serosystems.lib1090.msgs.acas.TCAS6ResolutionAdvisory}, or null for a format without a defined layout
     * @see ResolutionAdvisories#report(long)
     */
    public ResolutionAdvisory getResolutionAdvisory() {
        return ResolutionAdvisories.report(getResolutionAdvisoryEncoded());
    }

    @Override
    public BDSCode getBDSCode() {
        return BDS_CODE;
    }

    @Override
    public String toString() {
        return "ACASActiveResolutionAdvisoryReport{" + super.toString() +
                ", resolutionAdvisory=" + getResolutionAdvisory() +
                '}';
    }
}
