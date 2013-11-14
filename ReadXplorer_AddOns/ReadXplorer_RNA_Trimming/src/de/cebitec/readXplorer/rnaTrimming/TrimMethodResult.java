/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.rnaTrimming;

/**
 * TrimResult is produced by the execution of a TrimMethod
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class TrimMethodResult {
    private String sequence;
    private String originalSequence;
    private int trimmedCharsFromLeft;
    private int trimmedCharsFromRight;
    
    public TrimMethodResult(String sequence, String originalSequence, int trimmedCharsFromLeft, int trimmedCharsFromRight) {
        setSequence(sequence);
        setTrimmedCharsFromLeft(trimmedCharsFromLeft);
        setTrimmedCharsFromRight(trimmedCharsFromRight);
        setOriginalSequence(originalSequence);
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
    public void setSequence(String sequence) {
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
    public void setTrimmedCharsFromLeft(int trimmedCharsFromLeft) {
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
    public void setTrimmedCharsFromRight(int trimmedCharsFromRight) {
        this.trimmedCharsFromRight = trimmedCharsFromRight;
    }
    
    /** 
     * the os field will contain the trimmed chars of the original
     * sequence with the new sequence marked as @
     * Example: AACGCCCA shortened by 2 nucleotides from left and right side will give
     * os: AA@CA
     */
    public String getOsField() {
        return originalSequence.substring(0, this.getTrimmedCharsFromLeft())
           +"@"+originalSequence.substring(originalSequence.length()-this.getTrimmedCharsFromRight(), 
                originalSequence.length());      
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
    public void setOriginalSequence(String originalSequence) {
        this.originalSequence = originalSequence;
    }
}
