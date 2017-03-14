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


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;


/**
 * TrimProcessResult contains important data of a trimming process.
 * The data of this object will be changed while processing the data.
 * Interested objects can subscribe to those changes by using
 * addObserver()-Method.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
class TrimProcessResult extends Observable {

    private int allReads = 0;
    private int mappedReads = 0;
    private int trimmedReads = 0;
    private int trimmedMappedReads = 0;
    private final Map<String, Object> params;


    TrimProcessResult() {

        params = new HashMap<>();

    }


    /**
     * @return the allReads
     */
    public int getAllReads() {
        return allReads;
    }


    /**
     * @param allReads the allReads to set
     */
    public void setAllReads( Integer allReads ) {
        this.allReads = allReads;
    }


    /**
     * @return the mappedReads
     */
    public int getMappedReads() {
        return mappedReads;
    }


    /**
     * @param mappedReads the mappedReads to set
     */
    public void setMappedReads( Integer mappedReads ) {
        this.mappedReads = mappedReads;
    }


    /**
     * @return the trimmedReads
     */
    public int getTrimmedReads() {
        return trimmedReads;
    }


    /**
     * @param trimmedReads the trimmedReads to set
     */
    public void setTrimmedReads( Integer trimmedReads ) {
        this.trimmedReads = trimmedReads;
    }


    /**
     * @return the trimmedMappedReads
     */
    public int getTrimmedMappedReads() {
        return trimmedMappedReads;
    }


    /**
     * @param trimmedMappedReads the trimmedMappedReads to set
     */
    public void setTrimmedMappedReads( int trimmedMappedReads ) {
        this.trimmedMappedReads = trimmedMappedReads;
    }


    /**
     * Sets used analysis parameters to have them connected with the search
     * results.
     * <p>
     * @param params
     */
    public void setAnalysisParameters( Map<String, Object> params ) {
        this.params.clear();
        this.params.putAll( params );
    }


    /**
     * returns the used analysis parameters
     * <p>
     * @return
     */
    public Map<String, Object> getAnalysisParameters() {
        return Collections.unmodifiableMap( params );
    }


    public void ready() {
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
