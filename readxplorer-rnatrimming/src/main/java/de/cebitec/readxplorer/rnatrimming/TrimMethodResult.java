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

package de.cebitec.readxplorer.rnatrimming;


/**
 * TrimResult is produced by the execution of a TrimMethod
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class TrimMethodResult {

    private String sequence;
    private String originalSequence;
    private int trimmedCharsFromLeft;
    private int trimmedCharsFromRight;


    public TrimMethodResult( String sequence, String originalSequence, int trimmedCharsFromLeft, int trimmedCharsFromRight ) {
        setSequence( sequence );
        setTrimmedCharsFromLeft( trimmedCharsFromLeft );
        setTrimmedCharsFromRight( trimmedCharsFromRight );
        setOriginalSequence( originalSequence );
    }


    /**
     * @return the sequence
     */
    public String getSequence() {
        return sequence;
    }


    /**
     * @param sequence the sequence to set
     */
    public void setSequence( String sequence ) {
        this.sequence = sequence;
    }


    /**
     * @return the trimmedCharsFromLeft
     */
    public int getTrimmedCharsFromLeft() {
        return trimmedCharsFromLeft;
    }


    /**
     * @param trimmedCharsFromLeft the trimmedCharsFromLeft to set
     */
    public void setTrimmedCharsFromLeft( int trimmedCharsFromLeft ) {
        this.trimmedCharsFromLeft = trimmedCharsFromLeft;
    }


    /**
     * @return the trimmedCharsFromRight
     */
    public int getTrimmedCharsFromRight() {
        return trimmedCharsFromRight;
    }


    /**
     * @param trimmedCharsFromRight the trimmedCharsFromRight to set
     */
    public void setTrimmedCharsFromRight( int trimmedCharsFromRight ) {
        this.trimmedCharsFromRight = trimmedCharsFromRight;
    }


    /**
     * the os field will contain the trimmed chars of the original
     * sequence with the new sequence marked as @
     * Example: AACGCCCA shortened by 2 nucleotides from left and right side
     * will give
     * os: AA@CA
     */
    public String getOsField() {
        return originalSequence.substring( 0, this.getTrimmedCharsFromLeft() )
               + "@" + originalSequence.substring( originalSequence.length() - this.getTrimmedCharsFromRight(),
                                                   originalSequence.length() );
    }


    /**
     * @return the originalSequence
     */
    public String getOriginalSequence() {
        return originalSequence;
    }


    /**
     * @param originalSequence the originalSequence to set
     */
    public void setOriginalSequence( String originalSequence ) {
        this.originalSequence = originalSequence;
    }


}
