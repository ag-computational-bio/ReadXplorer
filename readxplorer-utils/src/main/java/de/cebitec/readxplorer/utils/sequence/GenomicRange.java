/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.utils.sequence;


/**
 * Defines objects that cover a certain genomic range, thus have a start and a
 * stop.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface GenomicRange extends Comparable<GenomicRange> {

    /**
     * @return The start of the genomic range. Always smaller than stop, also
     *         when on the reverse strand.
     */
    public int getStart();

    /**
     * @return The stop of the genomic range. Always larger than start, also
     *         when on the reverse strand.
     */
    public int getStop();

    /**
     * Returns if the genomic region is located on the fwd or rev strand.
     * <p>
     * @return <code>true</code> for featues on forward and <code>false</code>
     *         on reverse strand
     */
    public boolean isFwdStrand();
}
