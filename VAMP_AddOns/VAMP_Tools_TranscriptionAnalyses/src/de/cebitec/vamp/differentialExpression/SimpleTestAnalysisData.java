package de.cebitec.vamp.differentialExpression;

/**
 *
 * @author kstaderm
 */
public class SimpleTestAnalysisData extends DeAnalysisData {

    private int[] groupA;
    private int[] groupB;
    private boolean workingWithoutReplicates;

    public SimpleTestAnalysisData(int capacity, int[] groupA, int[] groupB, boolean workingWithoutReplicates) {
        super(capacity);
        this.groupA = groupA;
        this.groupB = groupB;
        this.workingWithoutReplicates = workingWithoutReplicates;
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
}
