/* 
 * Copyright (C) 2014 Kai Bernd Stadermann
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
package de.cebitec.readXplorer.differentialExpression;

import java.util.List;

/**
 *
 * @author kstaderm
 */
public class ExpressTestAnalysisData extends DeAnalysisData {

    private final int[] groupA;
    private final int[] groupB;
    private final boolean workingWithoutReplicates;
    private final List<Integer> normalizationFeatures;

    public ExpressTestAnalysisData(int capacity, int[] groupA, int[] groupB, 
            boolean workingWithoutReplicates, List<Integer> normalizationFeatures) {
        super(capacity);
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

    public List<Integer> getNormalizationFeatures() {
        return normalizationFeatures;
    }
}
