/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.utils.classification.Classification;


/**
 * Container for the coverage of a certain classification type of a given
 * interval.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class Coverage {

    private int leftBound;
    private int rightBound;

    private int[] fwdCoverage;
    private int[] revCoverage;
    private final Classification classification;


    /**
     * Container for the coverage of a certain classification type of a given
     * interval. If you want to set each coverage position separately you have
     * to call <code>incArraysToIntervalSize()</code>. The arrays are
     * initialized with a default length of 0.
     * <p>
     * @param leftBound      left bound of the interval in reference
     *                       coordinates. The borders are inclusive in the data
     *                       structures.
     * @param rightBound     right bound of the interval in reference
     *                       coordinates. The borders are inclusive in the data
     *                       structures.
     * @param classification The <code>Classification</code>
     *                       classification type of this coverage object.
     */
    public Coverage( int leftBound, int rightBound, Classification classification ) {
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.fwdCoverage = new int[0];
        this.revCoverage = new int[0];
        this.classification = classification;
    }


    /**
     * @return The left bound of the stored coverage interval in reference
     *         coordinates. The borders are inclusive in the data structures.
     */
    public int getLeftBound() {
        return leftBound;
    }


    /**
     * @return The right bound of the stored coverage interval in reference
     *         coordinates. The borders are inclusive in the data structures.
     */
    public int getRightBound() {
        return rightBound;
    }


    /**
     * @param leftBound The left bound of the stored coverage interval in
     *                  reference coordinates. The borders are inclusive in the
     *                  data structures.
     */
    public void setLeftBound( int leftBound ) {
        this.leftBound = leftBound;
    }


    /**
     * @param rightBound The right bound of the stored coverage interval in
     *                   reference coordinates. The borders are inclusive in the
     *                   data structures.
     */
    public void setRightBound( int rightBound ) {
        this.rightBound = rightBound;
    }


    /**
     * @return The <code>Classification</code> type of this coverage object.
     */
    public Classification getClassification() {
        return classification;
    }


    /**
     * Set the forward coverage with duplicates for the given genomic position.
     * <p>
     * @param logPos   position genomic position on the current reference
     *                 chromosome
     * @param coverage new coverage value to set
     * <p>
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setFwdCoverage( int logPos, int coverage ) throws ArrayIndexOutOfBoundsException {
        this.setCoverage( logPos, coverage, fwdCoverage );
    }


    /**
     * Set the reverse coverage with duplicates for the given genomic position.
     * <p>
     * @param logPos   position genomic position on the current reference
     *                 chromosome
     * @param coverage new coverage value to set
     * <p>
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setRevCoverage( int logPos, int coverage ) throws ArrayIndexOutOfBoundsException {
        this.setCoverage( logPos, coverage, revCoverage );
    }


    /**
     * Increases the forward coverage with duplicates by one.
     * <p>
     * @param logPos position to increase
     * <p>
     * @throws ArrayIndexOutOfBoundsException
     */
    public void increaseFwdCoverage( int logPos ) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage( logPos, 1, fwdCoverage );
    }


    /**
     * Increases the reverse coverage with duplicates by one.
     * <p>
     * @param logPos position to increase
     * <p>
     * @throws ArrayIndexOutOfBoundsException
     */
    public void increaseRevCoverage( int logPos ) throws ArrayIndexOutOfBoundsException {
        this.increaseCoverage( logPos, 1, revCoverage );
    }


    /**
     * @param logPos absolute position on the chromosome, whose coverage is
     *               needed
     * <p>
     * @return the forward coverage with duplicates. If the position is not
     *         covered 0 is returned.
     */
    public int getFwdCov( int logPos ) {
        return this.getCoverage( logPos, fwdCoverage );
    }


    /**
     * @param logPos absolute position on the chromosome, whose coverage is
     *               needed
     * <p>
     * @return the reverse coverage. If the position is not
     *         covered 0 is returned.
     */
    public int getRevCov( int logPos ) {
        return this.getCoverage( logPos, revCoverage );
    }


    /**
     * @param logPos      absolute position on the chromosome, whose coverage is
     *                    needed
     * @param isFwdStrand
     *                    <p>
     * @return the coverage for the given strand at the given position. If the
     *         position is not covered 0 is returned.
     */
    public int getCoverage( int logPos, boolean isFwdStrand ) {
        if( isFwdStrand ) {
            return this.getFwdCov( logPos );
        } else {
            return this.getRevCov( logPos );
        }
    }


    /**
     * @return Get whole fwd coverage array for the given interval.
     */
    public int[] getFwdCov() {
        return fwdCoverage;
    }


    /**
     * @return Get whole rev coverage array for the given interval.
     */
    public int[] getRevCov() {
        return revCoverage;
    }


    /**
     * @param isFwdStrand
     *                    <p>
     * @return Get whole coverage array for the given strand and the interval
     *         stored in this coverage object.
     */
    public int[] getCoverage( boolean isFwdStrand ) {
        if( isFwdStrand ) {
            return this.getFwdCov();
        }
        else {
            return this.getRevCov();
        }
    }


    /**
     * Replaces the fwd coverage array in this coverage object by the given one.
     * <p>
     * @param perfectFwdCov The complete fwd coverage array to set
     */
    public void setFwdCoverage( int[] perfectFwdCov ) {
        this.fwdCoverage = perfectFwdCov;
    }


    /**
     * Replaces the rev coverage array in this coverage object by the given one.
     * <p>
     * @param perfectRevCov The complete rev coverage array to set
     */
    public void setRevCoverage( int[] perfectRevCov ) {
        this.revCoverage = perfectRevCov;
    }


    /**
     * @param logPos reference position to translate
     * <p>
     * @return The internal index position at which the data for the given
     *         reference position can be found
     */
    public int getInternalPos( int logPos ) {
        return logPos - this.leftBound;
    }


    /**
     * Sets the coverage for a given reference position to a given value in the
     * given array.
     * <p>
     * @param logPos   the reference position whose coverage shall be updated
     * @param coverage the coverage value to store
     * @param covArray the coverage array in which the value shall be stored
     */
    private void setCoverage( int logPos, int coverage, int[] covArray ) {
        covArray[this.getInternalPos( logPos )] = coverage;
    }


    /**
     * Increases the coverage for a given reference position to a given value in
     * the given array.
     *
     * @param logPos   the reference position whose coverage shall be updated
     * @param coverage the coverage value to store
     * @param covArray the coverage array in which the value shall be stored
     */
    private void increaseCoverage( int logPos, int valueToAdd, int[] covArray ) {
        covArray[this.getInternalPos( logPos )] += valueToAdd;
    }


    /**
     * @param logPos absolute position on the chromosome, whose coverage is
     *               needed
     * <p>
     * @return the best match forward coverage with duplicates.
     */
    private int getCoverage( int logPos, int[] coverageArray ) {
        int internalPos = this.getInternalPos( logPos );
        if( internalPos < coverageArray.length && internalPos >= 0 ) {
            return coverageArray[internalPos];
        }
        else {
            return 0;
        }
    }


    /**
     * Increase the size of all arrays whose size is currently 0 to the interval
     * size covered by this Coverage object. This behaviour prevents
     * overwriting coverage data already stored in this coverage object.
     */
    public void incArraysToIntervalSize() {
        int size = this.rightBound - this.leftBound + 1;
        if( this.fwdCoverage.length == 0 ) {
            fwdCoverage = new int[size];
        }
        if( this.revCoverage.length == 0 ) {
            revCoverage = new int[size];
        }
    }


}
