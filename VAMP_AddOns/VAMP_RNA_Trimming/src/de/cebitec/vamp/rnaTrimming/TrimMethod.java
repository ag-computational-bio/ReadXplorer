/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

/**
 * A trim method describes the way a Read-Sequence can be trimmed
 * with the given maximum length
 * @author jeff
 */
public abstract class TrimMethod {
    private int maximumTrimLength;

    /**
     * @return the maximumTrimLength
     */
    public int getMaximumTrimLength() {
        return maximumTrimLength;
    }

    /**
     * @param maximumTrimLength the maximumTrimLength to set
     */
    public void setMaximumTrimLength(int maximumTrimLength) {
        this.maximumTrimLength = maximumTrimLength;
    }
    
    public abstract TrimResult trim(String sequence);
}
