/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.rnaTrimming;

import java.util.Map;
import java.util.Observable;

/**
 * TrimProcessResult contains important data of a trimming process.
 * The data of this object will be changed while processing the data.
 * Interested objects can subscribe to those changes by using addObserver()-Method.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
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
