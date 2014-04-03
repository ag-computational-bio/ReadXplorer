package de.cebitec.readXplorer.differentialExpression;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisData extends DeAnalysisData{

    private final Map<String, String[]> design;
    private final Iterator<String> designIterator;
    private final List<String> fittingGroupOne;
    private final List<String> fittingGroupTwo;
    private Set<String> levels;
    private final boolean moreThanTwoConditions;
    /**
     * Is the analysis performed with or without Replicates. If there are not at
     * least two tracks belonging to the same conditions this variable is true.
     * DeSeq is then called with a special option allowing it to work without
     * replicates. Be careful: The results may be unreliable.
     */
    private boolean workingWithoutReplicates;

    public DeSeqAnalysisData(int capacity, Map<String, String[]> design, 
            boolean moreThanTwoConditions, List<String> fittingGroupOne, 
            List<String> fittingGroupTwo, boolean workingWithoutReplicates) {
        super(capacity);
        this.design = design;
        this.fittingGroupOne=fittingGroupOne;
        this.fittingGroupTwo=fittingGroupTwo;
        this.workingWithoutReplicates = workingWithoutReplicates;
        this.moreThanTwoConditions=moreThanTwoConditions;
        designIterator = design.keySet().iterator();
    }

    public ReturnTupel getNextSubDesign() {
        String key = designIterator.next();
        return new ReturnTupel(key, design.get(key));
    }

    public boolean hasNextSubDesign() {
        return designIterator.hasNext();
    }

    public boolean isWorkingWithoutReplicates() {
        return workingWithoutReplicates;
    }

    public List<String> getFittingGroupOne() {
        return fittingGroupOne;
    }

    public List<String> getFittingGroupTwo() {
        return fittingGroupTwo;
    }
  
    public boolean moreThanTwoConditions(){
        return moreThanTwoConditions;
    }
    
    public String[] getLevels(){
        if(levels==null){
            levels=new HashSet<>();
            for(Iterator<String> it = design.keySet().iterator();it.hasNext();){
                String[] current = design.get(it.next());
                levels.addAll(Arrays.asList(current));
            }

        }
        return levels.toArray(new String[levels.size()]);
    }

    public static class ReturnTupel {

        private final String key;
        private final String[] value;

        public ReturnTupel(String key, String[] value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String[] getValue() {
            return value;
        }
    }
}
