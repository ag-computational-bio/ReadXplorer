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

package de.cebitec.readXplorer.view.dataVisualisation.abstractViewer;


/**
 * Contains useful info about a painting area, such as heights, widths,
 * left and right end positions.
 *
 * @author ddoppmeier, rhilker
 */
public class PaintingAreaInfo {

    private int phyLeft;  // left physical boundary (pixel) of the painting area
    private int phyRight; // right physical boundary (pixel) of the painting area
    private int phyWidth; // physical width (pixel) of the painting area

    private int forwardLow;
    private int forwardHigh;
    private int availableForwardHeight;

    private int reverseLow;
    private int reverseHigh;
    private int availableReverseHeight;

    private int completeHeight;


    /**
     * Contains useful info about a painting area, such as heights, widths, left
     * and right end positions.
     */
    public PaintingAreaInfo() {

    }


    /**
     * @return Highest pixel on the y-axis available for painting
     *         something belonging to the forward strand.
     */
    public int getForwardHigh() {
        return forwardHigh;
    }


    /**
     * @param forwardHigh Highest pixel on the y-axis available for painting
     *                    something belonging to the forward strand.
     */
    public void setForwardHigh( int forwardHigh ) {
        this.forwardHigh = forwardHigh;
        updateAvailableForwardHeight();
        updateCompleteHeight();
    }


    /**
     * @return Lowest pixel on the y-axis available for painting something
     *         belonging to the fwd strand.
     */
    public int getForwardLow() {
        return forwardLow;
    }


    /**
     * @param forwardLow The lowest pixel on the y-axis available for painting
     *                   something belonging to the fwd strand.
     */
    public void setForwardLow( int forwardLow ) {
        this.forwardLow = forwardLow;
        updateAvailableForwardHeight();
    }


    /**
     * @return Lowest pixel on the y-axis available for painting something
     *         belonging to the reverse strand.
     */
    public int getReverseLow() {
        return reverseLow;
    }


    /**
     * @param reverseLow The lowest pixel on the y-axis available for painting
     *                   something belonging to the reverse strand.
     */
    public void setReverseLow( int reverseLow ) {
        this.reverseLow = reverseLow;
        updateAvailableReverseHeight();
    }


    /**
     * @return Highest pixel on the y-axis available for painting something
     *         belonging to the reverse strand.
     */
    public int getReverseHigh() {
        return reverseHigh;
    }


    /**
     * @param reverseHigh Highest pixel on the y-axis available for painting
     *                    something belonging to the reverse strand.
     */
    public void setReverseHigh( int reverseHigh ) {
        this.reverseHigh = reverseHigh;
        updateAvailableReverseHeight();
        updateCompleteHeight();
    }


    private void updateCompleteHeight() {
        completeHeight = reverseHigh - forwardHigh + 1;
    }


    /**
     * @return left physical boundary (pixel) of the painting area
     */
    public int getPhyLeft() {
        return phyLeft;
    }


    /**
     * Sets the left physical boundary (pixel) in the painting area and
     * recalculates the width.
     * <p>
     * @param phyLeft left physical boundary (pixel) of the painting area
     */
    public void setPhyLeft( int phyLeft ) {
        this.phyLeft = phyLeft;
        recalcWidth();
    }


    /**
     * @return right physical boundary (pixel) of the painting area
     */
    public int getPhyRight() {
        return phyRight;
    }


    /**
     * Sets the right physical boundary (pixel) of the painting area and
     * recalculates the width.
     * <p>
     * @param phyRight right physical boundary (pixel) of the painting area
     */
    public void setPhyRight( int phyRight ) {
        this.phyRight = phyRight;
        recalcWidth();
    }


    /**
     * @return the physical width (pixel) of the painting area
     */
    public int getPhyWidth() {
        return phyWidth;
    }


    /**
     * Recalculates the physical width (pixel) of the painting area
     */
    private void recalcWidth() {
        this.phyWidth = phyRight - phyLeft + 1;
    }


    private void updateAvailableForwardHeight() {
        availableForwardHeight = forwardLow - forwardHigh + 1;
    }


    private void updateAvailableReverseHeight() {
        availableReverseHeight = reverseHigh - reverseLow + 1;
    }


    /**
     * @return The total height available for painting on the forward strand in
     *         pixels.
     */
    public int getAvailableForwardHeight() {
        return availableForwardHeight;
    }


    /**
     * @return The total height available for painting on the reverse strand in
     *         pixels.
     */
    public int getAvailableReverseHeight() {
        return availableReverseHeight;
    }


    /**
     * @param isFwdStrand true, if the value is needed for the fwd strand, false
     *                    if the reverse strand is needed
     * <p>
     * @return The total height available for painting on the given strand in
     *         pixels.
     */
    public double getAvailableHeight( boolean isFwdStrand ) {
        if( isFwdStrand ) {
            return this.getAvailableForwardHeight();
        }
        else {
            return this.getAvailableReverseHeight();
        }
    }


    /**
     * @return The total height available for painting in pixels.
     */
    public int getCompleteHeight() {
        return completeHeight;
    }


    /**
     * @param yValue      The y-value of a pixel to check
     * @param isFwdStrand true, if the value is needed for the fwd strand, false
     *                    if the reverse strand is needed
     * <p>
     * @return true, if the y-value of the pixel fits into the available
     *         painting area.
     */
    public boolean fitsIntoAvailableSpace( double yValue, boolean isFwdStrand ) {
        if( isFwdStrand ) {
            return yValue <= availableForwardHeight;
        }
        else {
            return yValue <= availableReverseHeight;
        }
    }


    /**
     * @param yValue The y-value of a pixel to check
     * <p>
     * @return true, if the y-value of the pixel fits into the available
     *         painting area.
     */
    public boolean fitsIntoAvailableForwardSpace( double yValue ) {
        return yValue <= availableForwardHeight;
    }


    /**
     * @param yValue The y-value of a pixel to check
     * <p>
     * @return true, if the y-value of the pixel fits into the available
     *         painting area.
     */
    public boolean fitsIntoAvailableReverseSpace( double yValue ) {
        return yValue <= availableReverseHeight;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( 20 );
        sb.append( "left: " ).append( phyLeft ).append( "\n" );
        sb.append( "right: " ).append( phyRight ).append( "\n" );
        sb.append( "width: " ).append( phyWidth ).append( "\n" );
        sb.append( "fwHigh: " ).append( forwardHigh ).append( "\n" );
        sb.append( "fwLow: " ).append( forwardLow ).append( "\n" );
        sb.append( "fwHght: " ).append( availableForwardHeight ).append( "\n" );
        sb.append( "rvLow: " ).append( reverseLow ).append( "\n" );
        sb.append( "rvHigh: " ).append( reverseHigh ).append( "\n" );
        sb.append( "rvHght: " ).append( availableReverseHeight ).append( "\n" );
        sb.append( "compl. Hght: " ).append( completeHeight );

        return sb.toString();
    }


}
