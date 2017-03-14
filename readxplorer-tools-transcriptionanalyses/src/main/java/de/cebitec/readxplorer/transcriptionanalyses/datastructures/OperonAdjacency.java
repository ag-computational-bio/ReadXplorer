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

package de.cebitec.readxplorer.transcriptionanalyses.datastructures;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;


/**
 * A putative operon is a data structure for storing two neighboring features,
 * which might form an operon. It also contains the read counts only associated
 * to feature1/2,
 * the spanning and the internal read counts.
 *
 * @author MKD, rhilker
 */
public class OperonAdjacency {

    private final PersistentFeature feature1;
    private final PersistentFeature feature2;
    private int readsFeature1;
    private int spanningReads;
    private int readsFeature2;
    private int internalReads;


    /**
     * A putative operon is a data structure for storing two neighboring
     * features, which might form an operon. It also contains the read counts
     * only associated to feature1/2, the spanning and the internal read
     * counts.
     * <p>
     * @param feature1 First genomic feature of the operon adjacency.
     * @param feature2 Second genomic feature of the operon adjacency.
     */
    public OperonAdjacency( PersistentFeature feature1, PersistentFeature feature2, int chromId ) {
        this.feature1 = feature1;
        this.feature2 = feature2;
    }


    /**
     * @return the first feature of the operon
     */
    public PersistentFeature getFeature1() {
        return feature1;
    }


    /**
     * @return the second feature of the operon
     */
    public PersistentFeature getFeature2() {
        return feature2;
    }


    /**
     * @return the reads associated with Feature1
     */
    public int getReadsFeature1() {
        return readsFeature1;
    }


    /**
     * @param readsFeature1 the reads associated with Feature1
     */
    public void setReadsFeature1( int readsFeature1 ) {
        this.readsFeature1 = readsFeature1;
    }


    /**
     * @return the number of reads spanning from feature 1 into 2
     */
    public int getSpanningReads() {
        return spanningReads;
    }


    /**
     * @param spanningReads set the number of reads spanning from feature 1 into
     *                      2
     */
    public void setSpanningReads( int spanningReads ) {
        this.spanningReads = spanningReads;
    }


    /**
     * @return the readsFeature2
     */
    public int getReadsFeature2() {
        return readsFeature2;
    }


    /**
     * @param readsFeature2 the readsFeature2 to set
     */
    public void setReadsFeature2( int readsFeature2 ) {
        this.readsFeature2 = readsFeature2;
    }


    /**
     * @return the internalReads
     */
    public int getInternalReads() {
        return internalReads;
    }


    /**
     * @param internalReads the internalReads to set
     */
    public void setInternalReads( int internalReads ) {
        this.internalReads = internalReads;
    }


}
