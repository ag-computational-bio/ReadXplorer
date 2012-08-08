package de.cebitec.vamp.differentialExpression.wizard;

import de.cebitec.vamp.differentialExpression.AnalysisData;
import java.util.List;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisData extends AnalysisData{
    
    private List<String[]> design;

    public DeSeqAnalysisData(int capacity, List<String[]> design) {
        super(capacity);
        this.design = design;
    }
    
    public String[] getNextSubDesign(){
        String[] ret = design.get(0);
        design.remove(0);
        return ret;
    }
    
    public boolean hasNextSubDesign(){
        return !design.isEmpty();
    }
    
}
