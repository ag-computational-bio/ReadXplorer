/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface GenomicRange extends Comparable<GenomicRange> {

    /**
     * @return The start of the genomic range. Always smaller than stop, also
     *         when on the reverse strand.
     */
    int getStart();


    /**
     * @return The stop of the genomic range. Always larger than start, also
     *         when on the reverse strand.
     */
    int getStop();


    /**
     * Returns if the genomic region is located on the fwd or rev strand.
     * <p>
     * @return <code>true</code> for featues on forward and <code>false</code>
     *         on reverse strand
     */
    boolean isFwdStrand();


    /**
     * Contains utility methods related to genomic ranges.
     */
    public final class Utils {


        private Utils() {
        }


        /**
         * @param genomicRange The genomic range whose start is needed
         * <p>
         * @return The start position on the region strand = smaller position
         *         for features on the fwd and larger position for features on
         *         the rev strand.
         */
        public static int getStartOnStrand( GenomicRange genomicRange ) {
            return genomicRange.isFwdStrand() ? genomicRange.getStart() : genomicRange.getStop();
        }


        /**
         * @param genomicRange The genomic range whose stop is needed
         * <p>
         * @return The stop position on the region strand = smaller position for
         *         features on the rev and larger position for features on the
         *         fwd strand.
         */
        public static int getStopOnStrand( GenomicRange genomicRange ) {
            return genomicRange.isFwdStrand() ? genomicRange.getStop() : genomicRange.getStart();
        }


        /**
         * @param genomicRange The genomic range whose length is needed
         * <p>
         * @return The length of the region in base pairs.
         */
        public static int getLength( GenomicRange genomicRange ) {
            return genomicRange.getStop() - genomicRange.getStart() + 1;
        }


        /**
         * Compare to method for all standard genomic ranges. Compares two
         * genomic ranges (e.g. two Mappings) based on their start position. '0'
         * is returned for equal start positions, 1, if the start position of
         * the other is larger and -1, if the start position of this Mapping is
         * larger.
         * <p>
         * @param genomicRange1 First genomic range to compare, needs to be
         *                      <code>this</code> from the object's
         *                      <code>compareTo()</code> method.
         * @param genomicRange2 Second genomic range to compare, needs to be the
         *                      other object handed over to the object's
         *                      <code>compareTo()</code> method.
         * <p>
         * @return '0' for equal start positions, 1, if the start position of
         *         the other is larger and -1, if the start position of this
         *         Mapping is larger.
         */
        public static int compareTo( GenomicRange genomicRange1, GenomicRange genomicRange2 ) {
            int result = 0;
            if( genomicRange1.getStart() < genomicRange2.getStart() ) {
                result = -1;
            } else if( genomicRange1.getStart() > genomicRange2.getStart() ) {
                result = 1;
            }
            return result;
        }


    }

}
