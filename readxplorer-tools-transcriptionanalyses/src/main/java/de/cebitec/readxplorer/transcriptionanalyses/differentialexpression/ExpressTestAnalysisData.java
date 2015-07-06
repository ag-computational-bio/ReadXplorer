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


/**
 *
 * @author kstaderm
 */
public class ExpressTestAnalysisData extends DeAnalysisData {

    private final int[] groupA;
    private final int[] groupB;
    private final boolean workingWithoutReplicates;
    private final int[] normalizationFeatures;


    public ExpressTestAnalysisData( int capacity, int[] groupA, int[] groupB,
                                    boolean workingWithoutReplicates, int[] normalizationFeatures,
                                    ProcessingLog processingLog ) {
        super( capacity, processingLog );
        this.groupA = groupA;
        this.groupB = groupB;
        this.workingWithoutReplicates = workingWithoutReplicates;
        this.normalizationFeatures = normalizationFeatures;
    }


    public int[] getGroupA() {
        return groupA;
    }


    public int[] getGroupB() {
        return groupB;
    }


    public boolean isWorkingWithoutReplicates() {
        return workingWithoutReplicates;
    }


    public int[] getNormalizationFeatures() {
        return normalizationFeatures;
    }


}
