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


import de.cebitec.readxplorer.databackend.dataobjects.TrackResultEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Data structure for storing operons. Operons consist of a list of
 * OperonAdjacencies, since each operon can contain more than two genes.
 * <p>
 * @author MKD, rhilker
 * <p>
 */
public class Operon extends TrackResultEntry {

    private final List<OperonAdjacency> operonAdjacencies;


    /**
     * Data structure for storing operons. Operons consist of a list of
     * OperonAdjacencies, since each operon can contain more than two genes.
     * <p>
     * @param trackId The id of the track whose reads are analyzed here
     */
    public Operon( int trackId ) {
        super( trackId );
        this.operonAdjacencies = new ArrayList<>();
    }


    /**
     * @return the operon adjacencies of this operon
     */
    public List<OperonAdjacency> getOperonAdjacencies() {
        return Collections.unmodifiableList( operonAdjacencies );
    }


    /**
     * @param newOperonAdjacencys the operon adjacencies to associate with this
     *                            operon object.
     */
    public void setOperonAdjacencies( List<OperonAdjacency> newOperonAdjacencys ) {
        operonAdjacencies.clear();
        operonAdjacencies.addAll( newOperonAdjacencys );
    }


    /**
     * Remove all operon adjacencies associated with this operon object.
     */
    public void clearOperonAdjacencyList() {
        operonAdjacencies.clear();
    }


    /**
     * Adds the operon adjacency to the list of OperonAdjacencies.
     * <p>
     * @param operonAdjacency An operon adjacency to associate with this operon
     *                              object.
     */
    public void addOperonAdjacency( OperonAdjacency operonAdjacency ) {
        operonAdjacencies.add( operonAdjacency );
    }


    /**
     * Adds the operon adjacencies to the end of the list of OperonAdjacencies.
     * <p>
     * @param operonAdjacencies the new operon adjacencies to associate with
     *                          this operon object.
     */
    public void addAllOperonAdjacencies( List<OperonAdjacency> operonAdjacencies ) {
        this.operonAdjacencies.addAll( operonAdjacencies );
    }


}
