/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


import java.util.List;


/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisData extends DeAnalysisData {

    /**
     * The groups which should be taken into account by the analysis step.
     */
    private final List<Group> groups;
    /**
     * The replicate structure of the selected tracks.
     */
    private final int[] replicateStructure;


    public BaySeqAnalysisData( int capacity, List<Group> groups, int[] replicateStructure, ProcessingLog processingLog ) {
        super( capacity, processingLog );
        this.groups = groups;
        this.replicateStructure = replicateStructure;
    }


    private int nextGroup = 0;


    /**
     * Returns the next group that has not been returned yet.
     * <p>
     * @return the next unreturned group.
     */
    public int[] getNextGroup() {
        int[] ret = new int[0];
        if( !(nextGroup >= groups.size()) ) {
            int[] current = groups.get( nextGroup++ ).getIntegerRepresentation();
            ret = new int[current.length];
            System.arraycopy( current, 0, ret, 0, current.length );
        }
        return ret;
    }


    /**
     * Checks if there is still an unreturned group.
     * <p>
     * @return true if there is still at least one unreturned group otherwise
     *         false
     */
    public boolean hasGroups() {
        return !(nextGroup >= groups.size());
    }


    /**
     * Return the replicate structure.
     * <p>
     * @return int array representing the replicate structure of the data.
     */
    public int[] getReplicateStructure() {
        return replicateStructure;
    }


}
