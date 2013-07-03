/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

/**
 * TrimResult is produced by the execution of a TrimMethod
 * @author Evgeny Anisiforov
 */
public class TrimMethodResult {
    private String sequence;
    private int trimmedCharsFromLeft;
    private int trimmedCharsFromRight;
    
    public TrimMethodResult(String sequence, int trimmedCharsFromLeft, int trimmedCharsFromRight) {
        setSequence(sequence);
        setTrimmedCharsFromLeft(trimmedCharsFromLeft);
        setTrimmedCharsFromRight(trimmedCharsFromRight);
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
}
