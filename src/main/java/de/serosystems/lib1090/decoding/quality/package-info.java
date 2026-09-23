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
 * The quality a message claims for what it reports: how accurate a position or velocity is, how much
 * of a region it is contained within, and how far any of that can be trusted.
 * <p>
 * Every type here is a table from one of the standards, and each follows the same shape: a message
 * transmits a small integer, that integer selects a constant, and the constant says what the integer
 * guarantees. The number is reached as {@code getXEncoded()} on the message and the constant as the
 * quantity it stands for, so a caller can stay with the standard's own categories or ask for meters
 * and be given one number.
 * <p>
 * Two rules hold across all of them, and both exist because a receiver must never report better
 * quality than the data supports.
 * <p>
 * <b>A category that means two things is named for both.</b> Several tables give their lowest value
 * as "unknown or worse than X", which a receiver cannot separate into the two facts it combines.
 * Those constants therefore guarantee nothing at all rather than the bound they half-state.
 * <p>
 * <b>What is not guaranteed reads as {@link java.lang.Double#NaN}.</b> Every quantity is reported
 * through {@code getGuaranteedUpperBound()}, which answers a single question — what does this bound
 * the value to? — and returns {@code NaN} where the answer is nothing. That makes
 * {@code getGuaranteedUpperBound() < limit} false whenever the quality is unknown, so the
 * conservative answer needs no special case, and the reverse comparison is false as well, since a
 * value that may be unknown cannot be shown to be poor either.
 * <p>
 * The navigation integrity category is the one that needs more than its own field: it is derived
 * from the format type code together with the supplements other messages carry, which is what
 * {@code NavigationCharacteristics} and {@code NICSupplements} are for.
 */
package de.serosystems.lib1090.decoding.quality;
