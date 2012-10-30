package de.cebitec.vamp.differentialExpression;

/**
 *
 * @author kstaderm
 */
public class SimpleTestAnalysisData extends AnalysisData {

    private int[] groupA;
    private int[] groupB;

    public SimpleTestAnalysisData(int capacity, int[] groupA, int[] groupB) {
        super(capacity);
        this.groupA = groupA;
        this.groupB = groupB;
    }

    public int[] getGroupA() {
        return groupA;
    }

    public int[] getGroupB() {
        return groupB;
    }
}
