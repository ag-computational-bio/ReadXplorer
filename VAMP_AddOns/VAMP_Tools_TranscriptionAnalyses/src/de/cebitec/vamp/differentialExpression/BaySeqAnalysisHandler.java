package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.differentialExpression.BaySeq.SamplesNotValidException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RVector;

/**
 *
 * @author kstaderm
 */
public class BaySeqAnalysisHandler extends AnalysisHandler {

    private int[] replicateStructure;
    private List<Group> groups;
    private BaySeq baySeq = new BaySeq();

    public static enum Plot {

        Priors, MACD, Posteriors;
    }

    public BaySeqAnalysisHandler(List<PersistantTrack> selectedTraks, List<Group> groups, Integer refGenomeID, int[] replicateStructure, File saveFile) {
        super(selectedTraks, refGenomeID, saveFile);
        this.groups = groups;
        this.replicateStructure = replicateStructure;
    }

    @Override
    public void performAnalysis() {
        List<RVector> results;
        if (!AnalysisHandler.TESTING_MODE) {
            Map<Integer, Map<Integer, Integer>> allCountData = collectCountData();
            BaySeqAnalysisData baySeqAnalysisData = new BaySeqAnalysisData(getSelectedTraks().size(), this.groups, this.replicateStructure);
            prepareAnnotations(baySeqAnalysisData);
            prepareCountData(baySeqAnalysisData, allCountData);
            results = baySeq.process(baySeqAnalysisData, getPersAnno().size(), getSelectedTraks().size(), getSaveFile());
        } else {
            results = baySeq.process(null, 3232, getSelectedTraks().size(), getSaveFile());
        }

        setResults(convertRresults(results));
        notifyObservers(this);
    }

    @Override
    public void endAnalysis() {
        baySeq.shutdown();
        baySeq = null;
    }

    private List<Object[][]> convertRresults(List<RVector> results) {
        List<Object[][]> ret = new ArrayList<>();
        for (Iterator<RVector> it = results.iterator(); it.hasNext();) {
            RVector currentRVector = it.next();
            int i = 0;
            Object[][] current = new Object[currentRVector.at(1).asIntArray().length][currentRVector.size()];
            RFactor currentStringValues = currentRVector.at(i).asFactor();
            for (int j = 0; j < currentStringValues.size(); j++) {
                current[j][i] = currentStringValues.at(i);
            }
            i++;
            for (; i < 3; i++) {
                int[] currentIntValues = currentRVector.at(i).asIntArray();
                for (int j = 0; j < currentIntValues.length; j++) {
                    current[j][i] = currentIntValues[j];
                }
            }
            for (; i < currentRVector.size(); i++) {
                double[] currentDoubleValues = currentRVector.at(i).asDoubleArray();
                for (int j = 0; j < currentDoubleValues.length; j++) {
                    current[j][i] = currentDoubleValues[j];
                }
            }
            ret.add(current);
        }
        return ret;
    }

    public File plot(Plot plot, Group group, int[] samplesA, int[] samplesB) throws IOException, SamplesNotValidException {
        File file = File.createTempFile("VAMP_Plot_", ".svg");
        file.deleteOnExit();
        if (plot == Plot.MACD) {
            baySeq.plotMACD(file, samplesA, samplesB);
        }
        if (plot == Plot.Posteriors) {
            baySeq.plotPosteriors(file, group, samplesA, samplesB);
        }
        if (plot == Plot.Priors) {
            baySeq.plotPriors(file, group);
        }
        return file;
    }

    public void saveResultsAsCSV(Group group, String path, boolean normalized) {
        File saveFile = new File(path);
        baySeq.saveResultsAsCSV(group, saveFile, normalized);
    }

    public List<Group> getGroups() {
        return groups;
    }
}
