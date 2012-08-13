package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class DeSeqAnalysisHandler extends AnalysisHandler {

    private List<String[]> design;
    private DeSeq deSeq = new DeSeq();
    private boolean workingWithoutReplicates;
    
    public DeSeqAnalysisHandler(List<PersistantTrack> selectedTraks, List<String[]> design, Integer refGenomeID, boolean workingWithoutReplicates, File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        this.design = design;
        this.workingWithoutReplicates = workingWithoutReplicates;
    }

    @Override
    public void performAnalysis() {
        List<RVector> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            DeSeqAnalysisData deSeqAnalysisData = new DeSeqAnalysisData(getSelectedTraks().size(), this.design, this.workingWithoutReplicates);
            prepareAnnotations(deSeqAnalysisData);
            prepareCountData(deSeqAnalysisData, allCountData);
            results = deSeq.process(deSeqAnalysisData, getPersAnno().size(), getSelectedTraks().size(), getSaveFile());
        } else {
            results = deSeq.process(null, 3232, getSelectedTraks().size(), getSaveFile());
        }

//        setResults(convertRresults(results));
        notifyObservers(this);
    }

    @Override
    public void endAnalysis() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
