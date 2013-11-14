/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.rnaTrimming;

/**
 * A trim method describes the way a Read-Sequence can be trimmed
 * with the given maximum length
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public abstract class TrimMethod {
    private int maximumTrimLength;
    
    private String shortName;
    
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
    
    public abstract TrimMethodResult trim(String sequence);

    /**
     * the shortname is filename-safe (i.e. alphanumeric, no spaces)
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * the shortname should be filename-safe (i.e. alphanumeric, no spaces)
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    /**
     * @return the short description can be used as part of the filename 
     * to save the results of trimming
     */
    public String getShortDescription() {
        return this.getShortName()+"_"+Integer.toString(this.getMaximumTrimLength());
    }
}
