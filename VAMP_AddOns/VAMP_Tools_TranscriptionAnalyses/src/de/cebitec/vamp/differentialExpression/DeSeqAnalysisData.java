package de.cebitec.vamp.differentialExpression;

import java.util.List;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisData extends AnalysisData {

    private List<String[]> design;
    /**
     * Is the analysis performed with or without Replicates. If there are not at
     * least two tracks belonging to the same conditions this variable is true.
     * DeSeq is then called with a special option allowing it to work without
     * replicates. Be careful: The results may be unreliable.
     */
    private boolean workingWithoutReplicates;

    public DeSeqAnalysisData(int capacity, List<String[]> design, boolean workingWithoutReplicates) {
        super(capacity);
        this.design = design;
        this.workingWithoutReplicates = workingWithoutReplicates;
    }

    public String[] getNextSubDesign() {
        String[] ret = design.get(0);
        design.remove(0);
        return ret;
    }

    public boolean hasNextSubDesign() {
        return !design.isEmpty();
    }

    public boolean isWorkingWithoutReplicates() {
        return workingWithoutReplicates;
    }
}
