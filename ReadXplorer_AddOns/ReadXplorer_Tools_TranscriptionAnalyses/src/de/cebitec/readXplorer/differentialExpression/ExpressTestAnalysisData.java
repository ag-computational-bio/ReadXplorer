package de.cebitec.readXplorer.differentialExpression;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import java.util.List;

/**
 *
 * @author kstaderm
 */
public class ExpressTestAnalysisData extends DeAnalysisData {

    private int[] groupA;
    private int[] groupB;
    private boolean workingWithoutReplicates;
    private List<Integer> normalizationFeatures;

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
