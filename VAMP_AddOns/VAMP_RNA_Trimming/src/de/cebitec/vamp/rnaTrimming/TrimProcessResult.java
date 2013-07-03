/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import java.util.Map;
import java.util.Observable;

/**
 *
 * @author Evgeny Anisiforov
 */
class TrimProcessResult extends Observable {
    private Integer allReads = 0;
    private Integer mappedReads = 0;
    private Integer trimmedReads = 0;
    private Integer trimmedMappedReads = 0;
    private Map<String, Object> params;
    private Boolean ready = false;
  
    
    public TrimProcessResult() {
    }
    
    /**
     * @return the allReads
     */
    public Integer getAllReads() {
        return allReads;
    }

    /**
     * @param allReads the allReads to set
     */
    public void setAllReads(Integer allReads) {
        this.allReads = allReads;
    }

    /**
     * @return the mappedReads
     */
    public Integer getMappedReads() {
        return mappedReads;
    }

    /**
     * @param mappedReads the mappedReads to set
     */
    public void setMappedReads(Integer mappedReads) {
        this.mappedReads = mappedReads;
    }

    /**
     * @return the trimmedReads
     */
    public Integer getTrimmedReads() {
        return trimmedReads;
    }

    /**
     * @param trimmedReads the trimmedReads to set
     */
    public void setTrimmedReads(Integer trimmedReads) {
        this.trimmedReads = trimmedReads;
    }

    /**
     * @return the trimmedMappedReads
     */
    public Integer getTrimmedMappedReads() {
        return trimmedMappedReads;
    }

    /**
     * @param trimmedMappedReads the trimmedMappedReads to set
     */
    public void setTrimmedMappedReads(Integer trimmedMappedReads) {
        this.trimmedMappedReads = trimmedMappedReads;
    }
    
    /**
     * Sets used analysis parameters to have them connected with the search
     * results.
     * @param params 
     */
    public void setAnalysisParameters(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * returns the used analysis parameters
     * @return 
     */
    public Map<String, Object> getAnalysisParameters() {
        return this.params;
    }
    

    public void ready() {
        this.ready = true;
        this.setChanged();
        this.notifyObservers();
    }

    public void incrementTrimmedReads() {
        this.trimmedReads++;
    }

    public void incrementMappedReads() {
        this.mappedReads++;
    }

    public void incrementTrimmedMappedReads() {
        this.trimmedMappedReads++;
    }

    public void notifyChanged() {
        this.setChanged();
        this.notifyObservers();
    }

    
}
