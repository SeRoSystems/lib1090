package de.serosystems.lib1090.exceptions;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Exception which is thrown when a raw message is passed to the wrong
 * decoder. E.g. when the format type code in the raw message does not
 * correspond to the message type.
 * 
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class UnspecifiedFormatError extends Exception {
	private static final long serialVersionUID = 6482688479919911669L;

	public UnspecifiedFormatError(String reason) {
		super(reason);
	}
}
